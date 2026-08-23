package com.calmpath.ai.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.calmpath.ai.data.model.Place
import com.calmpath.ai.data.repository.CalmPathRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class PlaceDetailsUiState(
    val place: Place? = null,
    val isFavorite: Boolean = false,
    val isNavigating: Boolean = false,
    val navigationMessage: String? = null
)

class PlaceDetailsViewModel(
    private val placeId: String,
    private val repository: CalmPathRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlaceDetailsUiState())
    val uiState: StateFlow<PlaceDetailsUiState> = _uiState.asStateFlow()

    init {
        observePlaceAndEnvironmentalSnapshot()
        observeFavoriteState()
    }

    private fun observePlaceAndEnvironmentalSnapshot() {
        viewModelScope.launch {
            combine(
                repository.getPlaceByIdFlow(placeId),
                repository.getLatestSnapshotFlow(placeId)
            ) { placeEntity, snapshot ->
                placeEntity?.toDomainModel(snapshot)
            }.collect { domainPlace ->
                if (domainPlace != null) {
                    _uiState.value = _uiState.value.copy(place = domainPlace)
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
}

class PlaceDetailsViewModelFactory(
    private val placeId: String,
    private val repository: CalmPathRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PlaceDetailsViewModel(placeId, repository) as T
    }
}
