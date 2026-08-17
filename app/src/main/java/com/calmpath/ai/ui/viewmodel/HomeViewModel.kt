package com.calmpath.ai.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.calmpath.ai.data.model.EnvironmentalSummary
import com.calmpath.ai.data.model.Mood
import com.calmpath.ai.data.model.Place
import com.calmpath.ai.data.repository.CalmPathRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
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
        loadHomeData()
        observeFavorites()
        observePreferences()
    }

    private fun loadHomeData() {
        viewModelScope.launch {
            val pref = repository.getPreferences()
            val mood = Mood.fromId(pref.selectedMood)
            val summary = repository.getEnvironmentalSummary()
            val places = repository.getRecommendedPlaces(mood)

            _uiState.value = _uiState.value.copy(
                selectedMood = mood,
                environmentalSummary = summary,
                recommendedPlaces = places,
                isLoading = false
            )
        }
    }

    private fun observeFavorites() {
        viewModelScope.launch {
            repository.favoritesFlow.collect { favs ->
                _uiState.value = _uiState.value.copy(
                    favoritePlaceIds = favs.map { it.id }.toSet()
                )
            }
        }
    }

    private fun observePreferences() {
        viewModelScope.launch {
            repository.preferencesFlow.collect { pref ->
                val mood = Mood.fromId(pref.selectedMood)
                val places = repository.getRecommendedPlaces(mood)
                _uiState.value = _uiState.value.copy(
                    selectedMood = mood,
                    recommendedPlaces = places
                )
            }
        }
    }

    fun onMoodChanged(mood: Mood) {
        viewModelScope.launch {
            repository.saveMood(mood)
            val places = repository.getRecommendedPlaces(mood)
            _uiState.value = _uiState.value.copy(
                selectedMood = mood,
                recommendedPlaces = places
            )
        }
    }

    fun toggleFavorite(place: Place) {
        viewModelScope.launch {
            repository.toggleFavorite(place)
        }
    }

    fun onPlaceClicked(place: Place, onNavigateToDetails: (String) -> Unit) {
        viewModelScope.launch {
            repository.recordPlaceView(place)
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
