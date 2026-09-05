package com.calmpath.ai.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.calmpath.ai.data.location.LocationHelper
import com.calmpath.ai.data.location.LocationResult
import com.calmpath.ai.data.model.EnvironmentalSummary
import com.calmpath.ai.data.model.Mood
import com.calmpath.ai.data.model.Place
import com.calmpath.ai.data.remote.NetworkStatus
import com.calmpath.ai.data.repository.CalmPathRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

sealed interface DataLoadState {
    data object Loading : DataLoadState
    data class Success(val isLive: Boolean) : DataLoadState
    data object OfflineCached : DataLoadState
    data class Error(val message: String) : DataLoadState
    data class OutsideIndia(val country: String) : DataLoadState
}

data class HomeUiState(
    val selectedMood: Mood = Mood.RELAX,
    val environmentalSummary: EnvironmentalSummary = EnvironmentalSummary(),
    val recommendedPlaces: List<Place> = emptyList(),
    val favoritePlaceIds: Set<String> = emptySet(),
    val networkStatus: NetworkStatus = NetworkStatus.ONLINE,
    val dataLoadState: DataLoadState = DataLoadState.Loading,
    val currentLocality: String = LocationHelper.DEFAULT_LOCALITY,
    val currentLatitude: Double = LocationHelper.DEFAULT_LATITUDE,
    val currentLongitude: Double = LocationHelper.DEFAULT_LONGITUDE,
    val isLocationPermissionGranted: Boolean = false,
    val isLoading: Boolean = false
)

class HomeViewModel(
    private val repository: CalmPathRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeNetworkAndLocation()
        observeRoomDatabase()
        refreshEnvironmentalData()
    }

    private fun observeNetworkAndLocation() {
        viewModelScope.launch {
            repository.networkMonitor?.networkStatus?.collect { status ->
                _uiState.value = _uiState.value.copy(networkStatus = status)
                if (status == NetworkStatus.ONLINE && _uiState.value.dataLoadState is DataLoadState.OfflineCached) {
                    refreshEnvironmentalData()
                }
            }
        }
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

                _uiState.value.copy(
                    selectedMood = currentMood,
                    recommendedPlaces = recommended,
                    favoritePlaceIds = favIds,
                    isLoading = false
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    fun refreshEnvironmentalData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                dataLoadState = DataLoadState.Loading,
                isLoading = true
            )

            val locationResult = repository.locationHelper?.getCurrentLocation()
                ?: LocationHelper.defaultLocation

            when (locationResult) {
                is LocationResult.OutsideIndia -> {
                    _uiState.value = _uiState.value.copy(
                        dataLoadState = DataLoadState.OutsideIndia(locationResult.country),
                        currentLocality = locationResult.country,
                        isLoading = false
                    )
                    return@launch
                }
                is LocationResult.PermissionDenied -> {
                    _uiState.value = _uiState.value.copy(
                        isLocationPermissionGranted = false,
                        currentLocality = LocationHelper.DEFAULT_LOCALITY,
                        currentLatitude = LocationHelper.DEFAULT_LATITUDE,
                        currentLongitude = LocationHelper.DEFAULT_LONGITUDE
                    )
                }
                is LocationResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLocationPermissionGranted = true,
                        currentLocality = locationResult.locality,
                        currentLatitude = locationResult.latitude,
                        currentLongitude = locationResult.longitude
                    )
                }
                is LocationResult.Unavailable -> {
                    _uiState.value = _uiState.value.copy(
                        currentLocality = locationResult.fallbackLocation.locality,
                        currentLatitude = locationResult.fallbackLocation.latitude,
                        currentLongitude = locationResult.fallbackLocation.longitude
                    )
                }
            }

            val lat = _uiState.value.currentLatitude
            val lon = _uiState.value.currentLongitude
            val locality = _uiState.value.currentLocality

            try {
                val summary = repository.fetchLiveEnvironment(lat, lon, locality)
                val loadState = if (summary.isLive) {
                    DataLoadState.Success(isLive = true)
                } else {
                    DataLoadState.OfflineCached
                }

                _uiState.value = _uiState.value.copy(
                    environmentalSummary = summary,
                    dataLoadState = loadState,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    dataLoadState = DataLoadState.Error(e.message ?: "Unable to retrieve environmental data."),
                    isLoading = false
                )
            }
        }
    }

    fun onLocationPermissionResult(granted: Boolean) {
        _uiState.value = _uiState.value.copy(isLocationPermissionGranted = granted)
        refreshEnvironmentalData()
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
