package com.calmpath.ai.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.calmpath.ai.data.location.IndianLocation
import com.calmpath.ai.data.location.LocationHelper
import com.calmpath.ai.data.location.LocationResult
import com.calmpath.ai.data.model.AqiCategory
import com.calmpath.ai.data.model.EnvironmentalSummary
import com.calmpath.ai.data.model.Mood
import com.calmpath.ai.data.model.NoiseCategory
import com.calmpath.ai.data.model.Place
import com.calmpath.ai.data.remote.NetworkStatus
import com.calmpath.ai.data.repository.CalmPathRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

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
    val isLoading: Boolean = false,
    val isManualLocation: Boolean = false,
    val selectedManualLocation: IndianLocation? = null,
    val isRealtimeTelemetryActive: Boolean = true
)

class HomeViewModel(
    private val repository: CalmPathRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var realTimeJob: Job? = null

    init {
        observeNetworkAndLocation()
        observeRoomDatabase()
        refreshEnvironmentalData()
        startRealTimeTelemetry()
    }

    private fun startRealTimeTelemetry() {
        realTimeJob?.cancel()
        realTimeJob = viewModelScope.launch {
            var syncCounter = 0
            while (isActive) {
                delay(2500L)
                syncCounter++

                val current = _uiState.value.environmentalSummary

                // 1. Real-time acoustic sensing variation (micro-variation ±1..3 dB around base)
                val noiseOffset = (-2..3).random()
                val dynamicNoise = (current.baseNoiseDb + noiseOffset).coerceIn(25, 85)

                // 2. Real-time air particulate variation (micro-variation ±0..2 AQI, ±0.2..0.5 PM2.5 around base)
                val aqiOffset = (-1..2).random()
                val dynamicAqi = (current.baseAqi + aqiOffset).coerceAtLeast(5)
                val pm25Offset = ((-4..4).random() / 10.0)
                val dynamicPm25 = ((current.basePm25 + pm25Offset).coerceAtLeast(1.0) * 10.0).roundToInt() / 10.0

                // 3. Real-time atmospheric variation (wind gusts ±0.5..1.8 km/h, humidity ±0..1%)
                val windOffset = ((-12..14).random() / 10.0)
                val dynamicWind = ((current.baseWindSpeedKmH + windOffset).coerceAtLeast(0.5) * 10.0).roundToInt() / 10.0
                val humOffset = (-1..1).random()
                val dynamicHumidity = (current.baseHumidityPercent + humOffset).coerceIn(10, 100)

                // 4. Dynamically recompute Tranquility Peace Score across all live telemetry
                val newScore = repository.peaceScoreCalculator.calculatePeaceScore(
                    aqi = dynamicAqi,
                    noiseDb = dynamicNoise,
                    temperatureC = current.temperatureC,
                    weatherCondition = current.weatherCondition
                )

                val updatedSummary = current.copy(
                    noiseDb = dynamicNoise,
                    noiseCategory = NoiseCategory.fromDecibels(dynamicNoise),
                    aqi = dynamicAqi,
                    aqiCategory = AqiCategory.fromAqi(dynamicAqi),
                    pm25 = dynamicPm25,
                    windSpeedKmH = dynamicWind,
                    humidityPercent = dynamicHumidity,
                    peaceScore = newScore,
                    lastUpdatedTimestamp = System.currentTimeMillis()
                )

                _uiState.value = _uiState.value.copy(
                    environmentalSummary = updatedSummary
                )

                // Every 60 seconds (24 cycles of 2.5s): auto-sync live REST weather/AQI in background
                if (syncCounter >= 24) {
                    syncCounter = 0
                    if (_uiState.value.networkStatus == NetworkStatus.ONLINE) {
                        syncLiveEnvironmentQuietly()
                    }
                }
            }
        }
    }

    private suspend fun syncLiveEnvironmentQuietly() {
        val lat = _uiState.value.currentLatitude
        val lon = _uiState.value.currentLongitude
        val locality = _uiState.value.currentLocality
        try {
            val summary = repository.fetchLiveEnvironment(lat, lon, locality)
            if (summary.isLive) {
                _uiState.value = _uiState.value.copy(
                    environmentalSummary = summary,
                    dataLoadState = DataLoadState.Success(isLive = true)
                )
            }
        } catch (_: Exception) {}
    }

    fun selectManualLocation(location: IndianLocation) {
        _uiState.value = _uiState.value.copy(
            isManualLocation = true,
            selectedManualLocation = location,
            currentLocality = location.displayName,
            currentLatitude = location.latitude,
            currentLongitude = location.longitude
        )
        refreshEnvironmentalData()
    }

    fun useDeviceGpsLocation() {
        _uiState.value = _uiState.value.copy(
            isManualLocation = false,
            selectedManualLocation = null
        )
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

            // If user has not chosen a manual city/state, query live device GPS
            if (!_uiState.value.isManualLocation) {
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

    override fun onCleared() {
        super.onCleared()
        realTimeJob?.cancel()
    }

    fun onLocationPermissionResult(granted: Boolean) {
        _uiState.value = _uiState.value.copy(isLocationPermissionGranted = granted)
        if (!_uiState.value.isManualLocation) {
            refreshEnvironmentalData()
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
