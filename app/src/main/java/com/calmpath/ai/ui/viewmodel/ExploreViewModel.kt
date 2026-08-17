package com.calmpath.ai.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.calmpath.ai.data.model.HeatmapZone
import com.calmpath.ai.data.model.Place
import com.calmpath.ai.data.remote.SampleDataSource
import com.calmpath.ai.data.repository.CalmPathRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ExploreUiState(
    val searchQuery: String = "",
    val categories: List<String> = SampleDataSource.categories,
    val selectedCategory: String = "All",
    val heatmapZones: List<HeatmapZone> = emptyList(),
    val places: List<Place> = emptyList(),
    val filteredPlaces: List<Place> = emptyList(),
    val selectedPlaceForPreview: Place? = null,
    val favoritePlaceIds: Set<String> = emptySet(),
    val isMapView: Boolean = true
)

class ExploreViewModel(
    private val repository: CalmPathRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExploreUiState())
    val uiState: StateFlow<ExploreUiState> = _uiState.asStateFlow()

    init {
        loadExploreData()
        observeFavorites()
    }

    private fun loadExploreData() {
        val allPlaces = repository.getAllPlaces()
        val zones = repository.getHeatmapZones()
        _uiState.value = _uiState.value.copy(
            places = allPlaces,
            filteredPlaces = allPlaces,
            heatmapZones = zones,
            selectedPlaceForPreview = allPlaces.firstOrNull()
        )
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

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        applyFilters()
    }

    fun onCategorySelected(category: String) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
        applyFilters()
    }

    fun onSelectPlace(place: Place) {
        _uiState.value = _uiState.value.copy(selectedPlaceForPreview = place)
    }

    fun toggleViewMode() {
        _uiState.value = _uiState.value.copy(isMapView = !_uiState.value.isMapView)
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

    private fun applyFilters() {
        val q = _uiState.value.searchQuery
        val cat = _uiState.value.selectedCategory
        val filtered = repository.searchPlaces(query = q, category = cat)
        _uiState.value = _uiState.value.copy(
            filteredPlaces = filtered,
            selectedPlaceForPreview = filtered.firstOrNull()
        )
    }
}

class ExploreViewModelFactory(
    private val repository: CalmPathRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ExploreViewModel(repository) as T
    }
}
