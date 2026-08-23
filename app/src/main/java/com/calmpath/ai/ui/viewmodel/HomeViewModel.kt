package com.calmpath.ai.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.calmpath.ai.data.model.EnvironmentalSummary
import com.calmpath.ai.data.model.Mood
import com.calmpath.ai.data.model.Place
import com.calmpath.ai.data.repository.CalmPathRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class HomeUiState(
    val selectedMood: Mood = Mood.RELAX,
    val environmentalSummary: EnvironmentalSummary = EnvironmentalSummary(),
    val recommendedPlaces: List<Place> = emptyList(),
    val favoritePlaceIds: Set<String> = emptySet(),
    val isLoading: Boolean = false
)

class HomeViewModel(
    private val repository: CalmPathRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeRoomDatabase()
    }

    private fun observeRoomDatabase() {
        viewModelScope.launch {
            combine(
                repository.placesFlow,
                repository.preferencesFlow,
                repository.favoritesWithPlacesFlow
            ) { placesEntities, preferences, favorites ->
                val currentMood = Mood.fromId(preferences.preferredMood)
                val placesDomain = placesEntities.map { it.toDomainModel() }
                val recommended = placesDomain.sortedWith(
                    compareByDescending<Place> { it.suitableMoods.contains(currentMood) }
                        .thenByDescending { it.peaceScore }
                )
                val favIds = favorites.map { it.favorite.placeId }.toSet()

                HomeUiState(
                    selectedMood = currentMood,
                    environmentalSummary = repository.getEnvironmentalSummary(),
                    recommendedPlaces = recommended,
                    favoritePlaceIds = favIds,
                    isLoading = false
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    fun onMoodChanged(mood: Mood) {
        viewModelScope.launch {
            val places = _uiState.value.recommendedPlaces
            val recommendedPlaceId = places.firstOrNull { it.suitableMoods.contains(mood) }?.id
            repository.recordMood(
                mood = mood.title,
                recommendedPlaceId = recommendedPlaceId,
                selectedPlaceId = recommendedPlaceId
            )
        }
    }

    fun toggleFavorite(place: Place) {
        viewModelScope.launch {
            repository.toggleFavorite(placeId = place.id)
        }
    }

    fun onPlaceClicked(place: Place, onNavigateToDetails: (String) -> Unit) {
        viewModelScope.launch {
            repository.recordPlaceView(
                placeId = place.id,
                peaceScore = place.peaceScore,
                aqi = place.aqi,
                noiseLevel = place.noiseDb
            )
            onNavigateToDetails(place.id)
        }
    }
}

class HomeViewModelFactory(
    private val repository: CalmPathRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HomeViewModel(repository) as T
    }
}
