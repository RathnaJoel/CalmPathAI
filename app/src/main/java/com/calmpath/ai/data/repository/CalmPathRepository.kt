package com.calmpath.ai.data.repository

import android.util.Log
import com.calmpath.ai.data.auth.AuthManager
import com.calmpath.ai.data.domain.PeaceScoreCalculator
import com.calmpath.ai.data.local.CalmPathDatabase
import com.calmpath.ai.data.local.DatabaseSeeder
import com.calmpath.ai.data.local.entities.AppSettingsEntity
import com.calmpath.ai.data.local.entities.EnvironmentalSnapshotEntity
import com.calmpath.ai.data.local.entities.FavoritePlaceEntity
import com.calmpath.ai.data.local.entities.FavoriteWithPlace
import com.calmpath.ai.data.local.entities.MoodHistoryEntity
import com.calmpath.ai.data.local.entities.PlaceEntity
import com.calmpath.ai.data.local.entities.PlaceHistoryEntity
import com.calmpath.ai.data.local.entities.PlaceHistoryWithPlace
import com.calmpath.ai.data.local.entities.UserPreferencesEntity
import com.calmpath.ai.data.local.entities.UserProfileEntity
import com.calmpath.ai.data.location.LocationHelper
import com.calmpath.ai.data.model.AqiCategory
import com.calmpath.ai.data.model.EnvironmentalSummary
import com.calmpath.ai.data.model.HeatmapZone
import com.calmpath.ai.data.model.Mood
import com.calmpath.ai.data.model.NoiseCategory
import com.calmpath.ai.data.model.Place
import com.calmpath.ai.BuildConfig
import com.calmpath.ai.data.model.WalkingRouteData
import com.calmpath.ai.data.remote.FirestoreSyncManager
import com.calmpath.ai.data.remote.NetworkMonitor
import com.calmpath.ai.data.remote.RetrofitClient
import com.calmpath.ai.data.remote.SampleDataSource
import com.calmpath.ai.data.remote.api.AirQualityApiService
import com.calmpath.ai.data.remote.api.DirectionsApiService
import com.calmpath.ai.data.remote.api.OsrmApiService
import com.calmpath.ai.data.remote.api.PlacesApiService
import com.calmpath.ai.data.remote.api.WeatherApiService
import com.calmpath.ai.data.remote.model.AirQualityInfo
import com.calmpath.ai.data.remote.model.WeatherInfo
import com.calmpath.ai.util.NavigationUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * Master Repository coordinating all 8 Room entities (CO3), Cloud Firestore (CO4),
 * and live REST API network data exchange with Android Location Services (CO5).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CalmPathRepository(
    private val database: CalmPathDatabase,
    private val authManager: AuthManager,
    private val firestoreSync: FirestoreSyncManager = FirestoreSyncManager(),
    val networkMonitor: NetworkMonitor? = null,
    val locationHelper: LocationHelper? = null,
    private val weatherApi: WeatherApiService = RetrofitClient.weatherApi,
    private val airQualityApi: AirQualityApiService = RetrofitClient.airQualityApi,
    private val placesApi: PlacesApiService = RetrofitClient.placesApi,
    private val directionsApi: DirectionsApiService = RetrofitClient.directionsApi,
    private val osrmApi: OsrmApiService = RetrofitClient.osrmApi,
    val peaceScoreCalculator: PeaceScoreCalculator = PeaceScoreCalculator.default,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    private val tag = "CalmPathRepository"

    val placeRepository = PlaceRepository(database.placeDao(), database.environmentalSnapshotDao())
    val favoriteRepository = FavoriteRepository(database.favoritePlaceDao())
    val historyRepository = HistoryRepository(database.placeHistoryDao())
    val moodRepository = MoodRepository(database.moodHistoryDao())
    val userRepository = UserRepository(database.userProfileDao(), database.userPreferencesDao())
    val settingsRepository = SettingsRepository(database.appSettingsDao())

    // CO6: In-App Map Navigation Target Request
    val navigationTargetPlaceId = MutableStateFlow<String?>(null)

    fun requestInAppNavigation(placeId: String) {
        navigationTargetPlaceId.value = placeId
    }

    fun clearNavigationRequest() {
        navigationTargetPlaceId.value = null
    }

    /**
     * Fetches real turn-by-turn walking route coordinates and maneuver instructions (CO6).
     *
     * Priority:
     * 1. Google Directions API (via developer's active MAPS_API_KEY)
     * 2. OSRM Walking Engine (Free open-source fallback)
     * 3. Tranquil Corridor Interpolation (Offline fallback)
     */
    suspend fun fetchWalkingRoute(
        startLat: Double,
        startLon: Double,
        endLat: Double,
        endLon: Double
    ): WalkingRouteData = withContext(Dispatchers.IO) {
        val defaultFallback = {
            val coords = NavigationUtils.generateTranquilRouteCoordinates(startLat, startLon, endLat, endLon)
            val distKm = LocationHelper.calculateDistanceKm(startLat, startLon, endLat, endLon)
            val estMins = (distKm * 12).toInt().coerceAtLeast(1)
            WalkingRouteData(
                coordinates = coords,
                distanceText = String.format(java.util.Locale.US, "%.1f km", distKm),
                durationText = "$estMins min walk",
                distanceMeters = (distKm * 1000).toLong(),
                durationSeconds = (estMins * 60).toLong(),
                stepInstructions = listOf("Follow tranquil green corridor toward destination"),
                summary = "Tranquil Walking Corridor"
            )
        }

        // 1. Try Google Directions API
        val apiKey = BuildConfig.MAPS_API_KEY
        if (apiKey.isNotBlank() && apiKey != "YOUR_API_KEY_HERE") {
            try {
                val origin = "$startLat,$startLon"
                val dest = "$endLat,$endLon"
                val response = directionsApi.getWalkingDirections(
                    origin = origin,
                    destination = dest,
                    mode = "walking",
                    apiKey = apiKey
                )
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.status == "OK" && body.routes.isNotEmpty()) {
                        val route = body.routes.first()
                        val polylineStr = route.overviewPolyline?.points.orEmpty()
                        val decodedPoints = if (polylineStr.isNotEmpty()) {
                            NavigationUtils.decodePolyline(polylineStr)
                        } else emptyList()

                        if (decodedPoints.isNotEmpty()) {
                            val leg = route.legs.firstOrNull()
                            val distText = leg?.distance?.text ?: ""
                            val durText = leg?.duration?.text ?: ""
                            val distVal = leg?.distance?.value ?: 0L
                            val durVal = leg?.duration?.value ?: 0L

                            val steps = leg?.steps?.mapNotNull { step ->
                                val clean = NavigationUtils.cleanHtmlInstructions(step.htmlInstructions)
                                if (clean.isNotBlank()) clean else null
                            } ?: emptyList()

                            Log.d(tag, "Google Directions route fetched successfully: ${decodedPoints.size} points")
                            return@withContext WalkingRouteData(
                                coordinates = decodedPoints,
                                distanceText = distText,
                                durationText = durText,
                                distanceMeters = distVal,
                                durationSeconds = durVal,
                                stepInstructions = steps,
                                summary = route.summary.orEmpty()
                            )
                        }
                    } else {
                        Log.w(tag, "Google Directions returned status: ${body?.status} (${body?.errorMessage})")
                    }
                }
            } catch (e: Exception) {
                Log.w(tag, "Google Directions API request failed: ${e.message}, falling back to OSRM")
            }
        }

        // 2. Fallback to Open Source Routing Machine (OSRM)
        try {
            val coordsParam = "$startLon,$startLat;$endLon,$endLat"
            val osrmResp = osrmApi.getWalkingRoute(coordinates = coordsParam)
            if (osrmResp.isSuccessful) {
                val osrmBody = osrmResp.body()
                if (osrmBody != null && osrmBody.code == "Ok" && osrmBody.routes.isNotEmpty()) {
                    val osrmRoute = osrmBody.routes.first()
                    val rawCoords = osrmRoute.geometry?.coordinates.orEmpty()
                    val points = rawCoords.mapNotNull { pt ->
                        if (pt.size >= 2) Pair(pt[1], pt[0]) else null
                    }
                    if (points.isNotEmpty()) {
                        val distKm = osrmRoute.distance / 1000.0
                        val durMins = (osrmRoute.duration / 60.0).toInt().coerceAtLeast(1)
                        val leg = osrmRoute.legs.firstOrNull()
                        val steps = leg?.steps?.mapNotNull { step ->
                            val maneuver = step.maneuver
                            val action = "${maneuver?.type.orEmpty()} ${maneuver?.modifier.orEmpty()}".trim()
                            val street = step.name.trim()
                            if (street.isNotBlank()) {
                                if (action.isNotBlank()) "$action onto $street" else "Continue on $street"
                            } else if (action.isNotBlank()) action else null
                        } ?: emptyList()

                        Log.d(tag, "OSRM walking route fetched successfully: ${points.size} points")
                        return@withContext WalkingRouteData(
                            coordinates = points,
                            distanceText = String.format(java.util.Locale.US, "%.1f km", distKm),
                            durationText = "$durMins min walk",
                            distanceMeters = osrmRoute.distance.toLong(),
                            durationSeconds = osrmRoute.duration.toLong(),
                            stepInstructions = steps,
                            summary = "Pedestrian Corridor"
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(tag, "OSRM routing request failed: ${e.message}, using offline corridor")
        }

        // 3. Fallback: Tranquil Corridor Interpolation
        defaultFallback()
    }

    init {
        scope.launch {
            // Seed 8 Room entities with initial realistic dataset
            DatabaseSeeder.seedDatabase(database)

            // Sync remote favorites from cloud for the current user
            val currentId = getCurrentUserId()
            val remoteFavs = firestoreSync.fetchRemoteFavorites(currentId)
            remoteFavs.forEach { fav ->
                database.favoritePlaceDao().insertFavorite(fav)
            }
        }
    }

    fun getCurrentUserId(): String {
        return authManager.currentUser.value?.uid ?: DatabaseSeeder.DEFAULT_USER_ID
    }

    // ==========================================
    // CO5: LIVE REST API ENVIRONMENTAL DATA & ROOM CACHING
    // ==========================================

    /**
     * Fetches live weather and air quality from REST APIs (CO5).
     * If online: sends HTTP requests to Open-Meteo, calculates Peace Score, caches snapshot in Room.
     * If offline: retrieves latest cached snapshot from Room SQLite database.
     */
    suspend fun fetchLiveEnvironment(
        latitude: Double = LocationHelper.DEFAULT_LATITUDE,
        longitude: Double = LocationHelper.DEFAULT_LONGITUDE,
        localityName: String = LocationHelper.DEFAULT_LOCALITY
    ): EnvironmentalSummary = withContext(Dispatchers.IO) {
        val isOnline = networkMonitor?.isOnline() ?: true

        if (!isOnline) {
            Log.d(tag, "Device is offline. Loading cached environmental telemetry from Room.")
            return@withContext getCachedEnvironmentalSummary(localityName)
        }

        try {
            Log.d(tag, "Device is online. Fetching fresh REST data for ($latitude, $longitude)...")
            val weatherDeferred = async { weatherApi.getCurrentWeather(latitude, longitude) }
            val aqiDeferred = async { airQualityApi.getAirQuality(latitude, longitude) }

            val weatherRes = weatherDeferred.await()
            val aqiRes = aqiDeferred.await()

            val weatherDto = if (weatherRes.isSuccessful) weatherRes.body()?.current else null
            val aqiDto = if (aqiRes.isSuccessful) aqiRes.body()?.current else null

            val weatherInfo = WeatherInfo.fromDto(weatherDto)
            val airQualityInfo = AirQualityInfo.fromDto(aqiDto)

            // Calibrated acoustic environmental baseline taking into account locality, time of day, and wind
            val noiseDb = estimateAcousticBaseline(localityName, weatherInfo.windSpeedKmH)

            val peaceScore = peaceScoreCalculator.calculatePeaceScore(
                aqi = airQualityInfo.aqi,
                noiseDb = noiseDb,
                temperatureC = weatherInfo.temperatureC,
                weatherCondition = weatherInfo.weatherCondition
            )

            val summary = EnvironmentalSummary(
                aqi = airQualityInfo.aqi,
                aqiCategory = airQualityInfo.category,
                noiseDb = noiseDb,
                noiseCategory = NoiseCategory.fromDecibels(noiseDb),
                temperatureC = weatherInfo.temperatureC,
                weatherCondition = weatherInfo.weatherCondition,
                weatherIcon = weatherInfo.weatherIcon,
                humidityPercent = weatherInfo.humidityPercent,
                peaceScore = peaceScore,
                peaceDescription = when {
                    peaceScore >= 80 -> "Optimal Tranquility & Clean Air"
                    peaceScore >= 60 -> "Pleasant Atmosphere & Gentle Ambient"
                    else -> "Moderate Urban Conditions"
                },
                isLive = true,
                isCached = false,
                localityName = localityName,
                lastUpdatedTimestamp = System.currentTimeMillis(),
                pm25 = airQualityInfo.pm25,
                pm10 = airQualityInfo.pm10,
                feelsLikeC = weatherInfo.feelsLikeC,
                indianAqi = airQualityInfo.indianAqi,
                usAqi = airQualityInfo.usAqi,
                aqiStandard = "CPCB NAQI",
                windSpeedKmH = weatherInfo.windSpeedKmH,
                baseAqi = airQualityInfo.aqi,
                basePm25 = airQualityInfo.pm25,
                baseTemperatureC = weatherInfo.temperatureC,
                baseHumidityPercent = weatherInfo.humidityPercent,
                baseWindSpeedKmH = weatherInfo.windSpeedKmH,
                baseNoiseDb = noiseDb
            )

            // Cache into Room Database for offline resilience (CO3 + CO5)
            database.environmentalSnapshotDao().insertSnapshot(
                EnvironmentalSnapshotEntity(
                    snapshotId = UUID.randomUUID().toString(),
                    placeId = "sanctuary_mumbai_marine_drive",
                    aqi = summary.aqi,
                    noiseLevelDb = summary.noiseDb,
                    temperature = summary.temperatureC.toDouble(),
                    humidity = summary.humidityPercent,
                    weatherCondition = summary.weatherCondition,
                    peaceScore = summary.peaceScore,
                    recordedAt = summary.lastUpdatedTimestamp
                )
            )
            Log.d(tag, "Successfully cached fresh snapshot in Room: Peace Score ${summary.peaceScore}")

            summary
        } catch (e: Exception) {
            Log.e(tag, "REST API call failed: ${e.message}. Falling back to Room cache.", e)
            getCachedEnvironmentalSummary(localityName)
        }
    }

    /**
     * Calibrated acoustic estimator taking into account locality characteristics,
     * time of day (IST), and local wind conditions.
     */
    private fun estimateAcousticBaseline(locality: String, windSpeedKmH: Double): Int {
        val calendar = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("Asia/Kolkata"))
        val hour = calendar.get(java.util.Calendar.HOUR_OF_DAY)

        val isNight = hour in 22..23 || hour in 0..5
        val isRushHour = hour in 8..11 || hour in 17..20

        val locLower = locality.lowercase()
        val baseLocalityDb = when {
            // Quiet hill stations / nature reserves
            locLower.contains("shimla") || locLower.contains("manali") ||
            locLower.contains("rishikesh") || locLower.contains("darjeeling") ||
            locLower.contains("sanctuary") || locLower.contains("garden") ||
            locLower.contains("park") || locLower.contains("lake") -> 36

            // Suburban / quieter cities
            locLower.contains("mysuru") || locLower.contains("dehradun") ||
            locLower.contains("coimbatore") || locLower.contains("chandigarh") ||
            locLower.contains("udaipur") || locLower.contains("panaji") -> 44

            // High-density urban metros (Mumbai, Delhi, Bengaluru, etc.)
            locLower.contains("mumbai") || locLower.contains("delhi") ||
            locLower.contains("bengaluru") || locLower.contains("kolkata") ||
            locLower.contains("hyderabad") || locLower.contains("chennai") -> 54

            else -> 46
        }

        // Time of day modifier
        val timeMod = when {
            isNight -> -10
            isRushHour -> +6
            else -> 0
        }

        // Wind turbulence acoustic modifier (>15 km/h adds ambient sound)
        val windMod = if (windSpeedKmH > 15.0) ((windSpeedKmH - 15.0) * 0.3).toInt().coerceAtMost(5) else 0

        return (baseLocalityDb + timeMod + windMod).coerceIn(28, 78)
    }

    private suspend fun getCachedEnvironmentalSummary(localityName: String): EnvironmentalSummary {
        val cached = database.environmentalSnapshotDao().getLatestSnapshotForPlace("sanctuary_mumbai_marine_drive")
        return if (cached != null) {
            EnvironmentalSummary(
                aqi = cached.aqi,
                aqiCategory = AqiCategory.fromAqi(cached.aqi),
                noiseDb = cached.noiseLevelDb,
                noiseCategory = NoiseCategory.fromDecibels(cached.noiseLevelDb),
                temperatureC = cached.temperature.toInt(),
                weatherCondition = cached.weatherCondition,
                weatherIcon = "⛅",
                humidityPercent = cached.humidity,
                peaceScore = cached.peaceScore,
                peaceDescription = "Cached environmental data from Room storage.",
                isLive = false,
                isCached = true,
                localityName = localityName,
                lastUpdatedTimestamp = cached.recordedAt
            )
        } else {
            SampleDataSource.environmentalSummary.copy(
                isLive = false,
                isCached = true,
                localityName = localityName
            )
        }
    }

    /**
     * Discovers peaceful sanctuaries nearby in India using OpenStreetMap REST API (CO5).
     * Calculates distance dynamically based on user's GPS coordinates.
     */
    suspend fun fetchNearbyPeacefulPlaces(
        latitude: Double,
        longitude: Double
    ): List<Place> = withContext(Dispatchers.IO) {
        val isOnline = networkMonitor?.isOnline() ?: true

        if (isOnline && LocationHelper.isLocationInIndia(latitude, longitude)) {
            try {
                val query = """
                    [out:json][timeout:10];
                    (
                      node["leisure"="park"](around:8000,$latitude,$longitude);
                      node["leisure"="garden"](around:8000,$latitude,$longitude);
                      node["amenity"="library"](around:8000,$latitude,$longitude);
                      node["natural"="water"](around:8000,$latitude,$longitude);
                    );
                    out body 8;
                """.trimIndent()

                val response = placesApi.getNearbyPeacefulPlaces(query)
                if (response.isSuccessful) {
                    val elements = response.body()?.elements
                    if (!elements.isNullOrEmpty()) {
                        val discovered = elements.mapNotNull { elem ->
                            val name = elem.tags?.get("name") ?: return@mapNotNull null
                            val lat = elem.lat ?: elem.center?.lat ?: return@mapNotNull null
                            val lon = elem.lon ?: elem.center?.lon ?: return@mapNotNull null

                            val category = when {
                                elem.tags["leisure"] == "park" -> "Parks"
                                elem.tags["leisure"] == "garden" -> "Parks"
                                elem.tags["amenity"] == "library" -> "Libraries"
                                elem.tags["natural"] == "water" -> "Lakes"
                                else -> "Parks"
                            }

                            val distance = LocationHelper.calculateDistanceKm(latitude, longitude, lat, lon)
                            val peace = peaceScoreCalculator.calculatePeaceScore(
                                aqi = 42,
                                noiseDb = 36,
                                distanceKm = distance,
                                userRating = 4.8
                            )

                            PlaceEntity(
                                placeId = "osm_${elem.id}",
                                name = name,
                                category = category,
                                address = elem.tags["addr:street"] ?: "Peaceful Sanctuary, India",
                                latitude = lat,
                                longitude = lon,
                                description = "Live sanctuary discovered via OpenStreetMap REST telemetry.",
                                imageUrl = "https://images.unsplash.com/photo-1519331379826-f10be5486c6f?w=800",
                                averageAQI = 42,
                                averageNoiseLevel = 36,
                                peaceScore = peace
                            )
                        }
                        if (discovered.isNotEmpty()) {
                            database.placeDao().insertAllPlaces(discovered)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w(tag, "Overpass API place fetch notice: ${e.message}")
            }
        }

        // Return places from Room with dynamically computed distances from current GPS
        database.placeDao().getAllPlaces().map { entity ->
            val distance = LocationHelper.calculateDistanceKm(latitude, longitude, entity.latitude, entity.longitude)
            entity.toDomainModel().copy(distanceKm = distance)
        }.sortedBy { it.distanceKm }
    }

    suspend fun ensureUserExists(userId: String) {
        val existing = database.userProfileDao().getUserById(userId)
        if (existing == null) {
            val authUser = authManager.currentUser.value
            val name = if (authUser?.uid == userId) authUser.displayName else "Calm Traveler"
            val email = if (authUser?.uid == userId) authUser.email else "user@calmpath.ai"
            val newProfile = UserProfileEntity(
                userId = userId,
                name = name,
                email = email,
                profileImage = authUser?.photoUrl,
                createdAt = System.currentTimeMillis(),
                lastLogin = System.currentTimeMillis()
            )
            database.userProfileDao().insertUser(newProfile)

            val newPreferences = UserPreferencesEntity(
                preferenceId = "pref_$userId",
                userId = userId,
                preferredMood = "Relax",
                preferredCategory = "All",
                maxDistance = 10.0,
                maxAQI = 60,
                maxNoiseLevel = 45,
                preferredTemperature = 22.0
            )
            database.userPreferencesDao().insertOrUpdate(newPreferences)

            val newSettings = AppSettingsEntity(
                settingsId = "settings_$userId",
                userId = userId,
                theme = "SYSTEM",
                notificationsEnabled = true,
                locationEnabled = true,
                distanceUnit = "km",
                temperatureUnit = "°C",
                soundUnit = "dB"
            )
            database.appSettingsDao().insertOrUpdate(newSettings)

            // Cloud sync profile and preferences
            scope.launch {
                firestoreSync.syncUserProfileToCloud(userId, newProfile)
                firestoreSync.syncPreferencesToCloud(userId, newPreferences)
            }
        }
    }

    // ==========================================
    // PLACES & EXPLORE (CO3)
    // ==========================================

    val placesFlow: Flow<List<PlaceEntity>> = placeRepository.getAllPlacesFlow()

    suspend fun getAllPlaces(): List<PlaceEntity> = placeRepository.getAllPlaces()

    suspend fun getPlaceById(id: String): PlaceEntity? = placeRepository.getPlaceById(id)

    fun getPlaceByIdFlow(id: String): Flow<PlaceEntity?> = placeRepository.getPlaceByIdFlow(id)

    fun getPlacesByCategory(category: String): Flow<List<PlaceEntity>> =
        placeRepository.getPlacesByCategory(category)

    fun searchPlaces(
        query: String = "",
        category: String = "All",
        maxAqi: Int = 200,
        maxNoiseDb: Int = 120
    ): Flow<List<PlaceEntity>> = placeRepository.searchPlaces(query, category, maxAqi, maxNoiseDb)

    fun getLatestSnapshotFlow(placeId: String): Flow<EnvironmentalSnapshotEntity?> =
        placeRepository.getLatestSnapshotFlow(placeId)

    // ==========================================
    // FAVORITES (CO3: FavoritePlaceEntity & CO4: Firestore Sync)
    // ==========================================

    val favoritesWithPlacesFlow: Flow<List<FavoriteWithPlace>> =
        authManager.currentUser.flatMapLatest { user ->
            val uid = user?.uid ?: DatabaseSeeder.DEFAULT_USER_ID
            favoriteRepository.getFavoritesWithPlaces(uid)
        }

    fun getFavoritesFlowForUser(userId: String = getCurrentUserId()): Flow<List<FavoriteWithPlace>> =
        favoriteRepository.getFavoritesWithPlaces(userId)

    fun isFavoriteFlow(placeId: String, userId: String = getCurrentUserId()): Flow<Boolean> =
        favoriteRepository.isFavoriteFlow(userId, placeId)

    suspend fun isFavorite(placeId: String, userId: String = getCurrentUserId()): Boolean =
        favoriteRepository.isFavorite(userId, placeId)

    suspend fun toggleFavorite(
        placeId: String,
        userRating: Int = 5,
        personalNote: String = "",
        userId: String = getCurrentUserId()
    ): Boolean {
        ensureUserExists(userId)
        val nowFav = favoriteRepository.toggleFavorite(userId, placeId, userRating, personalNote)

        scope.launch {
            if (nowFav) {
                val savedEntity = database.favoritePlaceDao().getFavorite(userId, placeId)
                if (savedEntity != null) {
                    firestoreSync.syncFavoriteToCloud(userId, savedEntity)
                }
            } else {
                firestoreSync.deleteFavoriteFromCloud(userId, placeId)
            }
        }
        return nowFav
    }

    suspend fun removeFavorite(placeId: String, userId: String = getCurrentUserId()) {
        ensureUserExists(userId)
        favoriteRepository.removeFavorite(userId, placeId)
        scope.launch {
            firestoreSync.deleteFavoriteFromCloud(userId, placeId)
        }
    }

    suspend fun clearAllFavorites(userId: String = getCurrentUserId()) {
        ensureUserExists(userId)
        favoriteRepository.clearFavorites(userId)
    }

    // ==========================================
    // HISTORY (CO3: PlaceHistoryEntity & CO4: Firestore Sync)
    // ==========================================

    val historyWithPlacesFlow: Flow<List<PlaceHistoryWithPlace>> =
        authManager.currentUser.flatMapLatest { user ->
            val uid = user?.uid ?: DatabaseSeeder.DEFAULT_USER_ID
            historyRepository.getHistoryWithPlaces(uid)
        }

    fun getHistoryFlowForUser(userId: String = getCurrentUserId()): Flow<List<PlaceHistoryWithPlace>> =
        historyRepository.getHistoryWithPlaces(userId)

    suspend fun recordPlaceView(
        placeId: String,
        peaceScore: Int,
        aqi: Int,
        noiseLevel: Int,
        userId: String = getCurrentUserId()
    ) {
        ensureUserExists(userId)
        val histId = UUID.randomUUID().toString()
        val historyEntity = PlaceHistoryEntity(
            historyId = histId,
            userId = userId,
            placeId = placeId,
            viewedAt = System.currentTimeMillis(),
            peaceScoreAtVisit = peaceScore,
            aqiAtVisit = aqi,
            noiseLevelAtVisit = noiseLevel
        )
        database.placeHistoryDao().insertHistory(historyEntity)
        scope.launch {
            firestoreSync.syncHistoryToCloud(userId, historyEntity)
        }
    }

    suspend fun clearHistory(userId: String = getCurrentUserId()) {
        ensureUserExists(userId)
        historyRepository.clearHistory(userId)
    }

    // ==========================================
    // MOOD HISTORY (CO3: MoodHistoryEntity & CO4: Firestore Sync)
    // ==========================================

    val latestMoodFlow: Flow<MoodHistoryEntity?> =
        authManager.currentUser.flatMapLatest { user ->
            val uid = user?.uid ?: DatabaseSeeder.DEFAULT_USER_ID
            moodRepository.getLatestMoodFlow(uid)
        }

    fun getMoodHistoryFlow(userId: String = getCurrentUserId()): Flow<List<MoodHistoryEntity>> =
        moodRepository.getMoodHistoryFlow(userId)

    suspend fun recordMood(
        mood: String,
        recommendedPlaceId: String? = null,
        selectedPlaceId: String? = null,
        userId: String = getCurrentUserId()
    ) {
        ensureUserExists(userId)
        val validRecommended = if (recommendedPlaceId != null && database.placeDao().getPlaceById(recommendedPlaceId) != null) recommendedPlaceId else null
        val validSelected = if (selectedPlaceId != null && database.placeDao().getPlaceById(selectedPlaceId) != null) selectedPlaceId else null

        val moodId = UUID.randomUUID().toString()
        val moodEntity = MoodHistoryEntity(
            moodHistoryId = moodId,
            userId = userId,
            mood = mood,
            selectedAt = System.currentTimeMillis(),
            recommendedPlaceId = validRecommended,
            selectedPlaceId = validSelected
        )
        database.moodHistoryDao().insertMoodHistory(moodEntity)
        userRepository.updatePreferredMood(userId, mood)

        scope.launch {
            firestoreSync.syncMoodToCloud(userId, moodEntity)
        }
    }

    suspend fun saveMood(mood: Mood) {
        recordMood(mood.title)
    }

    // ==========================================
    // USER PROFILE & PREFERENCES (CO3 & CO4)
    // ==========================================

    val userProfileFlow: Flow<UserProfileEntity?> =
        authManager.currentUser.flatMapLatest { user ->
            val uid = user?.uid ?: DatabaseSeeder.DEFAULT_USER_ID
            userRepository.getUserFlow(uid)
        }

    val preferencesFlow: Flow<UserPreferencesEntity> =
        authManager.currentUser.flatMapLatest { user ->
            val uid = user?.uid ?: DatabaseSeeder.DEFAULT_USER_ID
            userRepository.getPreferencesFlow(uid).map { it ?: DatabaseSeeder.defaultPreferences }
        }

    suspend fun getUserProfile(userId: String = getCurrentUserId()): UserProfileEntity? =
        userRepository.getUser(userId)

    suspend fun getUserPreferences(userId: String = getCurrentUserId()): UserPreferencesEntity =
        userRepository.getPreferences(userId) ?: DatabaseSeeder.defaultPreferences

    suspend fun saveEnvironmentalPreferences(
        maxAqi: Int,
        noiseLevel: Int,
        distanceKm: Double,
        userId: String = getCurrentUserId()
    ) {
        ensureUserExists(userId)
        userRepository.updateEnvironmentalTolerances(userId, maxAqi, noiseLevel, distanceKm)

        scope.launch {
            val updatedPref = userRepository.getPreferences(userId)
            if (updatedPref != null) {
                firestoreSync.syncPreferencesToCloud(userId, updatedPref)
            }
        }
    }

    // ==========================================
    // APP SETTINGS (CO3: AppSettingsEntity)
    // ==========================================

    val settingsFlow: Flow<AppSettingsEntity> =
        authManager.currentUser.flatMapLatest { user ->
            val uid = user?.uid ?: DatabaseSeeder.DEFAULT_USER_ID
            settingsRepository.getSettingsFlow(uid).map { it ?: DatabaseSeeder.defaultAppSettings }
        }

    suspend fun getAppSettings(userId: String = getCurrentUserId()): AppSettingsEntity =
        settingsRepository.getSettings(userId) ?: DatabaseSeeder.defaultAppSettings

    suspend fun saveTheme(theme: String, userId: String = getCurrentUserId()) {
        ensureUserExists(userId)
        settingsRepository.updateTheme(userId, theme)
    }

    suspend fun saveNotifications(enabled: Boolean, userId: String = getCurrentUserId()) {
        ensureUserExists(userId)
        settingsRepository.updateNotifications(userId, enabled)
    }

    suspend fun saveUnits(distanceUnit: String, tempUnit: String, userId: String = getCurrentUserId()) {
        ensureUserExists(userId)
        settingsRepository.updateUnits(userId, distanceUnit, tempUnit)
    }

    // ==========================================
    // DASHBOARD & HEATMAP SUMMARY
    // ==========================================

    fun getEnvironmentalSummary(): EnvironmentalSummary = SampleDataSource.environmentalSummary

    fun getHeatmapZones(): List<HeatmapZone> = SampleDataSource.heatmapZones
}
