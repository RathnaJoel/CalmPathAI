package com.calmpath.ai.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.calmpath.ai.data.location.LocationHelper
import com.calmpath.ai.data.location.LocationResult
import com.calmpath.ai.data.model.HeatmapZone
import com.calmpath.ai.data.model.Place
import com.calmpath.ai.data.remote.NetworkStatus
import com.calmpath.ai.data.repository.CalmPathRepository
import com.calmpath.ai.util.NavigationUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class ExploreUiState(
    val searchQuery: String = "",
    val categories: List<String> = listOf("All", "Parks", "Lakes", "Libraries", "Cafes", "Meditation", "Fitness"),
    val selectedCategory: String = "All",
    val heatmapZones: List<HeatmapZone> = emptyList(),
    val places: List<Place> = emptyList(),
    val filteredPlaces: List<Place> = emptyList(),
    val selectedPlaceForPreview: Place? = null,
    val favoritePlaceIds: Set<String> = emptySet(),
    val isMapView: Boolean = true,
    val networkStatus: NetworkStatus = NetworkStatus.ONLINE,
    val currentLocality: String = LocationHelper.DEFAULT_LOCALITY,
    val currentLatitude: Double = LocationHelper.DEFAULT_LATITUDE,
    val currentLongitude: Double = LocationHelper.DEFAULT_LONGITUDE,
    val cameraTargetLat: Double = LocationHelper.DEFAULT_LATITUDE,
    val cameraTargetLon: Double = LocationHelper.DEFAULT_LONGITUDE,
    val cameraMoveTrigger: Long = 0L,
    val activeRouteDestination: Place? = null,
    val routeCoordinates: List<Pair<Double, Double>> = emptyList(),
    val hasLocationPermission: Boolean = false,
    val isOutsideIndia: Boolean = false,
    val isLoading: Boolean = false
)

class ExploreViewModel(
    private val repository: CalmPathRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ExploreUiState(
            heatmapZones = repository.getHeatmapZones(),
            hasLocationPermission = repository.locationHelper?.hasLocationPermission() ?: false
        )
    )
    val uiState: StateFlow<ExploreUiState> = _uiState.asStateFlow()

    init {
        observeNetwork()
        observeRoomPlaces()
        observeNavigationRequests()
        refreshNearbyPlaces()
    }

    private fun observeNavigationRequests() {
        viewModelScope.launch {
            repository.navigationTargetPlaceId.collect { targetId ->
                if (targetId != null) {
                    val place = _uiState.value.places.find { it.id == targetId }
                    if (place != null) {
                        startInAppNavigation(place)
                        repository.clearNavigationRequest()
                    }
                }
            }
        }
    }

    private fun observeNetwork() {
        viewModelScope.launch {
            repository.networkMonitor?.networkStatus?.collect { status ->
                _uiState.value = _uiState.value.copy(networkStatus = status)
            }
        }
    }

    private fun observeRoomPlaces() {
        viewModelScope.launch {
            combine(
                repository.placesFlow,
                repository.favoritesWithPlacesFlow
            ) { placesEntities, favorites ->
                val lat = _uiState.value.currentLatitude
                val lon = _uiState.value.currentLongitude
                val domainPlaces = placesEntities.map { entity ->
                    val dist = LocationHelper.calculateDistanceKm(lat, lon, entity.latitude, entity.longitude)
                    entity.toDomainModel().copy(distanceKm = dist)
                }.sortedBy { it.distanceKm }

                val favIds = favorites.map { it.favorite.placeId }.toSet()

                val query = _uiState.value.searchQuery
                val category = _uiState.value.selectedCategory

                val filtered = domainPlaces.filter { place ->
                    val matchesCategory = category == "All" || place.category.equals(category, ignoreCase = true)
                    val matchesQuery = query.isBlank() ||
                            place.name.contains(query, ignoreCase = true) ||
                            place.description.contains(query, ignoreCase = true) ||
                            place.address.contains(query, ignoreCase = true)
                    matchesCategory && matchesQuery
                }

                _uiState.value.copy(
                    places = domainPlaces,
                    filteredPlaces = filtered,
                    selectedPlaceForPreview = _uiState.value.selectedPlaceForPreview ?: filtered.firstOrNull(),
                    favoritePlaceIds = favIds
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    fun refreshNearbyPlaces() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val locationResult = repository.locationHelper?.getCurrentLocation()
                ?: LocationHelper.defaultLocation

            when (locationResult) {
                is LocationResult.OutsideIndia -> {
                    _uiState.value = _uiState.value.copy(
                        isOutsideIndia = true,
                        hasLocationPermission = true,
                        currentLocality = locationResult.country,
                        isLoading = false
                    )
                    return@launch
                }
                is LocationResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isOutsideIndia = false,
                        hasLocationPermission = true,
                        currentLocality = locationResult.locality,
                        currentLatitude = locationResult.latitude,
                        currentLongitude = locationResult.longitude,
                        cameraTargetLat = locationResult.latitude,
                        cameraTargetLon = locationResult.longitude,
                        cameraMoveTrigger = System.currentTimeMillis()
                    )
                }
                is LocationResult.Unavailable -> {
                    _uiState.value = _uiState.value.copy(
                        isOutsideIndia = false,
                        currentLocality = locationResult.fallbackLocation.locality,
                        currentLatitude = locationResult.fallbackLocation.latitude,
                        currentLongitude = locationResult.fallbackLocation.longitude,
                        cameraTargetLat = locationResult.fallbackLocation.latitude,
                        cameraTargetLon = locationResult.fallbackLocation.longitude
                    )
                }
                is LocationResult.PermissionDenied -> {
                    _uiState.value = _uiState.value.copy(
                        isOutsideIndia = false,
                        hasLocationPermission = false,
                        currentLocality = LocationHelper.DEFAULT_LOCALITY,
                        currentLatitude = LocationHelper.DEFAULT_LATITUDE,
                        currentLongitude = LocationHelper.DEFAULT_LONGITUDE,
                        cameraTargetLat = LocationHelper.DEFAULT_LATITUDE,
                        cameraTargetLon = LocationHelper.DEFAULT_LONGITUDE
                    )
                }
            }

            val lat = _uiState.value.currentLatitude
            val lon = _uiState.value.currentLongitude

            val places = repository.fetchNearbyPeacefulPlaces(lat, lon)
            _uiState.value = _uiState.value.copy(
                places = places,
                isLoading = false
            )
            filterPlaces()
        }
    }

    fun onLocationPermissionResult(granted: Boolean) {
        _uiState.value = _uiState.value.copy(hasLocationPermission = granted)
        refreshNearbyPlaces()
    }

    fun onMyLocationClicked() {
        val lat = _uiState.value.currentLatitude
        val lon = _uiState.value.currentLongitude
        _uiState.value = _uiState.value.copy(
            cameraTargetLat = lat,
            cameraTargetLon = lon,
            cameraMoveTrigger = System.currentTimeMillis()
        )
        refreshNearbyPlaces()
    }

    fun onMarkerClicked(place: Place) {
        _uiState.value = _uiState.value.copy(
            selectedPlaceForPreview = place,
            cameraTargetLat = place.latitude,
            cameraTargetLon = place.longitude,
            cameraMoveTrigger = System.currentTimeMillis()
        )
    }

    fun onDismissPreview() {
        _uiState.value = _uiState.value.copy(selectedPlaceForPreview = null)
    }

    /**
     * Activates In-App Navigation mode:
     * 1. Draws a serene walking route polyline from current GPS position to destination.
     * 2. Centers camera on the midpoint between origin and destination.
     * 3. Displays the In-App Navigation HUD on the Google Map.
     */
    fun startInAppNavigation(place: Place) {
        val userLat = _uiState.value.currentLatitude
        val userLon = _uiState.value.currentLongitude
        val route = NavigationUtils.generateTranquilRouteCoordinates(
            startLat = userLat,
            startLon = userLon,
            endLat = place.latitude,
            endLon = place.longitude
        )
        val midLat = (userLat + place.latitude) / 2.0
        val midLon = (userLon + place.longitude) / 2.0

        _uiState.value = _uiState.value.copy(
            isMapView = true,
            activeRouteDestination = place,
            selectedPlaceForPreview = place,
            routeCoordinates = route,
            cameraTargetLat = midLat,
            cameraTargetLon = midLon,
            cameraMoveTrigger = System.currentTimeMillis()
        )
    }

    /**
     * Exits in-app navigation mode and clears the route polyline.
     */
    fun stopInAppNavigation() {
        _uiState.value = _uiState.value.copy(
            activeRouteDestination = null,
            routeCoordinates = emptyList()
        )
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        filterPlaces()
    }

    fun onCategorySelected(category: String) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
        filterPlaces()
    }

    fun onSelectPlace(place: Place) {
        _uiState.value = _uiState.value.copy(
            selectedPlaceForPreview = place,
            cameraTargetLat = place.latitude,
            cameraTargetLon = place.longitude,
            cameraMoveTrigger = System.currentTimeMillis()
        )
    }

    fun toggleViewMode() {
        _uiState.value = _uiState.value.copy(isMapView = !_uiState.value.isMapView)
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

    private fun filterPlaces() {
        val query = _uiState.value.searchQuery
        val category = _uiState.value.selectedCategory
        val all = _uiState.value.places

        val filtered = all.filter { place ->
            val matchesCategory = category == "All" || place.category.equals(category, ignoreCase = true)
            val matchesQuery = query.isBlank() ||
                    place.name.contains(query, ignoreCase = true) ||
                    place.description.contains(query, ignoreCase = true) ||
                    place.address.contains(query, ignoreCase = true)
            matchesCategory && matchesQuery
        }

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
