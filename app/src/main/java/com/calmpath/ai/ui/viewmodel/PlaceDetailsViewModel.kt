package com.calmpath.ai.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.calmpath.ai.data.model.Place
import com.calmpath.ai.data.repository.CalmPathRepository
import com.calmpath.ai.media.AudioPlayerUiState
import com.calmpath.ai.media.AudioTrack
import com.calmpath.ai.media.CalmAudioManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class PlaceDetailsUiState(
    val place: Place? = null,
    val isFavorite: Boolean = false,
    val isNavigating: Boolean = false,
    val navigationMessage: String? = null,
    val mlSuitabilityScore: Int? = null,
    val mlMatchReason: String? = null
)

class PlaceDetailsViewModel(
    private val placeId: String,
    private val repository: CalmPathRepository,
    private val audioManager: CalmAudioManager? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlaceDetailsUiState())
    val uiState: StateFlow<PlaceDetailsUiState> = _uiState.asStateFlow()

    // CO7: Expose reactive audio player state flow to Jetpack Compose UI
    val audioPlayerState: StateFlow<AudioPlayerUiState> =
        audioManager?.playerState ?: MutableStateFlow(AudioPlayerUiState())

    init {
        observePlaceAndEnvironmentalSnapshot()
        observeFavoriteState()
    }

    private fun observePlaceAndEnvironmentalSnapshot() {
        viewModelScope.launch {
            combine(
                repository.getPlaceByIdFlow(placeId),
                repository.getLatestSnapshotFlow(placeId),
                repository.currentLocation
            ) { placeEntity, snapshot, currLoc ->
                placeEntity?.toDomainModel(
                    snapshot = snapshot,
                    userLat = currLoc.latitude,
                    userLon = currLoc.longitude
                )
            }.collect { domainPlace ->
                if (domainPlace != null) {
                    val preferences = repository.userRepository.getPreferences("guest")
                    val mood = com.calmpath.ai.data.model.Mood.fromId(preferences?.preferredMood ?: "Relax")
                    val recommendation = repository.activeRecommendationEngine.rankPlaces(
                        places = listOf(domainPlace),
                        userMood = mood,
                        preferences = preferences
                    ).firstOrNull()

                    _uiState.value = _uiState.value.copy(
                        place = domainPlace,
                        mlSuitabilityScore = recommendation?.mlSuitabilityScore,
                        mlMatchReason = recommendation?.matchReason
                    )
                    repository.recordPlaceView(
                        placeId = domainPlace.id,
                        peaceScore = domainPlace.peaceScore,
                        aqi = domainPlace.aqi,
                        noiseLevel = domainPlace.noiseDb
                    )
                }
            }
        }
    }

    private fun observeFavoriteState() {
        viewModelScope.launch {
            repository.isFavoriteFlow(placeId).collect { isFav ->
                _uiState.value = _uiState.value.copy(isFavorite = isFav)
            }
        }
    }

    fun toggleFavorite(userRating: Int = 5, note: String = "") {
        val currentPlace = _uiState.value.place ?: return
        viewModelScope.launch {
            val nowFav = repository.toggleFavorite(
                placeId = currentPlace.id,
                userRating = userRating,
                personalNote = note
            )
            _uiState.value = _uiState.value.copy(isFavorite = nowFav)
        }
    }

    fun requestInAppNavigation(placeId: String) {
        repository.requestInAppNavigation(placeId)
    }

    fun startNavigation() {
        val place = _uiState.value.place ?: return
        _uiState.value = _uiState.value.copy(
            isNavigating = true,
            navigationMessage = "🧭 Smart Peace Route active to ${place.name}. Estimated peaceful walking time: ${(place.distanceKm * 12).toInt()} min through low-noise green corridors."
        )
    }

    fun dismissNavigation() {
        _uiState.value = _uiState.value.copy(
            isNavigating = false,
            navigationMessage = null
        )
    }

    // ==========================================
    // CO7: MULTIMEDIA AUDIO PLAYBACK CONTROLS
    // ==========================================

    fun playAudio(track: AudioTrack) {
        audioManager?.playTrack(track)
    }

    fun pauseAudio() {
        audioManager?.pause()
    }

    fun resumeAudio() {
        audioManager?.resume()
    }

    fun stopAudio() {
        audioManager?.stop()
    }

    fun seekAudio(positionMs: Long) {
        audioManager?.seekTo(positionMs)
    }

    override fun onCleared() {
        super.onCleared()
        audioManager?.release()
    }
}

class PlaceDetailsViewModelFactory(
    private val placeId: String,
    private val repository: CalmPathRepository,
    private val context: Context? = null
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val audioManager = context?.let { CalmAudioManager(it.applicationContext) }
        return PlaceDetailsViewModel(placeId, repository, audioManager) as T
    }
}
