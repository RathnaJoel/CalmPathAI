package com.calmpath.ai

import com.calmpath.ai.data.domain.PeaceScoreCalculator
import com.calmpath.ai.data.local.Converters
import com.calmpath.ai.data.local.DatabaseSeeder
import com.calmpath.ai.data.local.entities.AppSettingsEntity
import com.calmpath.ai.data.local.entities.ChatMessageEntity
import com.calmpath.ai.data.local.entities.EnvironmentalSnapshotEntity
import com.calmpath.ai.data.local.entities.FavoritePlaceEntity
import com.calmpath.ai.data.local.entities.MoodHistoryEntity
import com.calmpath.ai.data.local.entities.PlaceEntity
import com.calmpath.ai.data.local.entities.PlaceHistoryEntity
import com.calmpath.ai.data.local.entities.UserPreferencesEntity
import com.calmpath.ai.data.local.entities.UserProfileEntity
import com.calmpath.ai.data.location.LocationHelper
import com.calmpath.ai.data.model.AqiCategory
import com.calmpath.ai.data.model.CalmnessLevel
import com.calmpath.ai.data.model.Mood
import com.calmpath.ai.data.model.NoiseCategory
import com.calmpath.ai.data.remote.model.AirQualityInfo
import com.calmpath.ai.data.remote.model.CurrentAirQualityDto
import com.calmpath.ai.data.remote.model.CurrentWeatherDto
import com.calmpath.ai.data.remote.model.WeatherInfo
import com.calmpath.ai.data.local.entities.RecommendationInteractionEntity
import com.calmpath.ai.data.model.Place
import com.calmpath.ai.media.AudioPlaybackState
import com.calmpath.ai.media.AudioPlayerUiState
import com.calmpath.ai.media.AudioTrack
import com.calmpath.ai.ml.FeaturePreprocessor
import com.calmpath.ai.ml.PeaceRecommendationModel
import com.calmpath.ai.ml.PlaceFeatureBuilder
import com.calmpath.ai.ml.RawPlaceFeatures
import com.calmpath.ai.ml.RecommendationEngine
import com.calmpath.ai.ml.TreeNode
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests verifying domain logic, 8 Room entities, converters,
 * Peace Score calculation, and India location boundaries (CO1, CO3, CO5).
 */
class CalmPathLogicTest {

    @Test
    fun testAqiCategorization() {
        assertEquals(AqiCategory.GOOD, AqiCategory.fromAqi(25))
        assertEquals(AqiCategory.MODERATE, AqiCategory.fromAqi(75))
        assertEquals(AqiCategory.UNHEALTHY_SENSITIVE, AqiCategory.fromAqi(120))
        assertEquals(AqiCategory.POOR, AqiCategory.fromAqi(180))
    }

    @Test
    fun testNoiseCategorization() {
        assertEquals(NoiseCategory.VERY_QUIET, NoiseCategory.fromDecibels(25))
        assertEquals(NoiseCategory.QUIET, NoiseCategory.fromDecibels(42))
        assertEquals(NoiseCategory.MODERATE, NoiseCategory.fromDecibels(65))
        assertEquals(NoiseCategory.NOISY, NoiseCategory.fromDecibels(85))
    }

    @Test
    fun testCalmnessLevelCalculation() {
        assertEquals(CalmnessLevel.EXCELLENT, CalmnessLevel.fromScore(95))
        assertEquals(CalmnessLevel.GOOD, CalmnessLevel.fromScore(78))
        assertEquals(CalmnessLevel.MODERATE, CalmnessLevel.fromScore(60))
        assertEquals(CalmnessLevel.MODERATE_POOR, CalmnessLevel.fromScore(45))
        assertEquals(CalmnessLevel.POOR, CalmnessLevel.fromScore(25))
    }

    @Test
    fun testMoodLookup() {
        assertEquals(Mood.RELAX, Mood.fromId("relax"))
        assertEquals(Mood.MEDITATE, Mood.fromId("meditate"))
        assertEquals(Mood.STUDY, Mood.fromId("study"))
        assertEquals(Mood.EXERCISE, Mood.fromId("exercise"))
        assertEquals(Mood.FRESH_AIR, Mood.fromId("fresh_air"))
        assertEquals(Mood.QUIET_TIME, Mood.fromId("quiet_time"))
        assertEquals(Mood.RELAX, Mood.fromId("unknown_mood"))
    }

    @Test
    fun testTypeConverters() {
        val converters = Converters()
        val originalList = listOf("Low noise", "Clean air", "Great scenery")
        val serialized = converters.fromStringList(originalList)
        val deserialized = converters.toStringList(serialized)

        assertEquals(originalList, deserialized)
    }

    @Test
    fun testAllEightEntitiesInstantiable() {
        val user = UserProfileEntity(userId = "u1", name = "Joel", email = "joel@calmpath.ai")
        val pref = UserPreferencesEntity(preferenceId = "p1", userId = "u1", preferredMood = "Relax")
        val place = PlaceEntity(
            placeId = "pl1",
            name = "Zenith Park",
            category = "Parks",
            address = "123 Way",
            latitude = 19.076,
            longitude = 72.877,
            description = "Quiet",
            imageUrl = "https://example.com/img.jpg",
            averageAQI = 25,
            averageNoiseLevel = 35,
            peaceScore = 92
        )
        val fav = FavoritePlaceEntity(favoriteId = "f1", userId = "u1", placeId = "pl1", userRating = 5)
        val hist = PlaceHistoryEntity(historyId = "h1", userId = "u1", placeId = "pl1", peaceScoreAtVisit = 92, aqiAtVisit = 25, noiseLevelAtVisit = 35)
        val mood = MoodHistoryEntity(moodHistoryId = "m1", userId = "u1", mood = "Meditate", recommendedPlaceId = "pl1")
        val snapshot = EnvironmentalSnapshotEntity(
            snapshotId = "s1",
            placeId = "pl1",
            aqi = 25,
            noiseLevelDb = 35,
            temperature = 22.0,
            humidity = 55,
            weatherCondition = "Clear",
            peaceScore = 92
        )
        val settings = AppSettingsEntity(settingsId = "st1", userId = "u1", theme = "DARK")

        assertEquals("u1", user.userId)
        assertEquals("p1", pref.preferenceId)
        assertEquals("pl1", place.placeId)
        assertEquals("f1", fav.favoriteId)
        assertEquals("h1", hist.historyId)
        assertEquals("m1", mood.moodHistoryId)
        assertEquals("s1", snapshot.snapshotId)
        assertEquals("st1", settings.settingsId)
    }

    @Test
    fun testDatabaseSeederSampleIntegrity() {
        assertTrue(DatabaseSeeder.samplePlaces.isNotEmpty())
        assertEquals(22, DatabaseSeeder.samplePlaces.size)

        val snapshots = DatabaseSeeder.createSampleSnapshots()
        assertTrue(snapshots.isNotEmpty())
        assertEquals(44, snapshots.size)

        DatabaseSeeder.samplePlaces.forEach { place ->
            assertTrue(place.peaceScore in 0..100)
            assertTrue(place.averageAQI > 0)
            assertTrue(place.averageNoiseLevel > 0)
            assertNotNull(place.imageUrl)
            // Assert all seeded places are strictly in India
            assertTrue(
                "Place ${place.name} (${place.latitude}, ${place.longitude}) must be within India boundaries",
                LocationHelper.isLocationInIndia(place.latitude, place.longitude)
            )
            val domain = place.toDomainModel()
            assertEquals(place.placeId, domain.id)
            assertEquals(place.name, domain.name)
        }

        // Verify SampleDataSource places are also strictly within India
        com.calmpath.ai.data.remote.SampleDataSource.places.forEach { place ->
            assertTrue(
                "Sample place ${place.name} must be within India boundaries",
                LocationHelper.isLocationInIndia(place.latitude, place.longitude)
            )
        }
    }

    // ==========================================
    // CO5: PEACE SCORE CALCULATOR TESTS
    // ==========================================

    @Test
    fun testPeaceScoreCalculationOptimal() {
        val calculator = PeaceScoreCalculator()
        // Low AQI (25), low noise (30 dB), pleasant temp (22°C), close distance (1.2 km), high rating (5.0)
        val score = calculator.calculatePeaceScore(
            aqi = 25,
            noiseDb = 30,
            temperatureC = 22,
            weatherCondition = "Clear Sky",
            distanceKm = 1.2,
            userRating = 5.0
        )
        assertTrue("Optimal score should be >= 85, got $score", score >= 85)
        assertTrue("Score cannot exceed 100", score <= 100)
    }

    @Test
    fun testPeaceScoreCalculationPolluted() {
        val calculator = PeaceScoreCalculator()
        // High AQI (250), loud noise (85 dB), hot weather (38°C), far distance (25 km), low rating (2.0)
        val score = calculator.calculatePeaceScore(
            aqi = 250,
            noiseDb = 85,
            temperatureC = 38,
            weatherCondition = "Thunderstorm",
            distanceKm = 25.0,
            userRating = 2.0
        )
        assertTrue("Polluted conditions score should be <= 45, got $score", score <= 45)
        assertTrue("Score cannot be negative", score >= 0)
    }

    @Test
    fun testPeaceScoreHandlesMissingParametersGracefully() {
        val calculator = PeaceScoreCalculator()
        // Only AQI provided
        val scoreAqiOnly = calculator.calculatePeaceScore(aqi = 40, noiseDb = null)
        assertTrue(scoreAqiOnly in 0..100)

        // All null
        val scoreAllNull = calculator.calculatePeaceScore(aqi = null, noiseDb = null)
        assertEquals(75, scoreAllNull) // Default baseline
    }

    // ==========================================
    // CO5: LOCATION & BOUNDARY TESTS (INDIA)
    // ==========================================

    @Test
    fun testIndiaBoundaryCoordinates() {
        // Mumbai, Maharashtra
        assertTrue(LocationHelper.isLocationInIndia(19.0760, 72.8777))
        // New Delhi
        assertTrue(LocationHelper.isLocationInIndia(28.6139, 77.2090))
        // Bangalore, Karnataka
        assertTrue(LocationHelper.isLocationInIndia(12.9716, 77.5946))
        // Chennai, Tamil Nadu
        assertTrue(LocationHelper.isLocationInIndia(13.0827, 80.2707))
        // Kolkata, West Bengal
        assertTrue(LocationHelper.isLocationInIndia(22.5726, 88.3639))

        // Outside India: London, UK (51.5074, -0.1278)
        assertFalse(LocationHelper.isLocationInIndia(51.5074, -0.1278))
        // Outside India: New York, USA (40.7128, -74.0060)
        assertFalse(LocationHelper.isLocationInIndia(40.7128, -74.0060))
        // Outside India: Tokyo, Japan (35.6762, 139.6503)
        assertFalse(LocationHelper.isLocationInIndia(35.6762, 139.6503))
    }

    @Test
    fun testHaversineDistanceCalculation() {
        // Distance between Gateway of India (18.9220, 72.8347) and Marine Drive (18.9432, 72.8230) ~ 2.6 km
        val distance = LocationHelper.calculateDistanceKm(18.9220, 72.8347, 18.9432, 72.8230)
        assertTrue("Distance should be approx 2.6 km, got $distance", distance in 2.0..3.5)

        // Distance from a point to itself should be 0.0
        val zeroDist = LocationHelper.calculateDistanceKm(19.0760, 72.8777, 19.0760, 72.8777)
        assertEquals(0.0, zeroDist, 0.01)
    }

    // ==========================================
    // CO5: REST API DTO & WEATHER MAPPER TESTS
    // ==========================================

    @Test
    fun testWmoWeatherCodeMapping() {
        val (clearCond, clearIcon) = WeatherInfo.mapWmoWeatherCode(0)
        assertEquals("Clear Sky", clearCond)
        assertEquals("☀️", clearIcon)

        val (cloudCond, cloudIcon) = WeatherInfo.mapWmoWeatherCode(2)
        assertEquals("Partly Cloudy", cloudCond)
        assertEquals("⛅", cloudIcon)

        val (rainCond, rainIcon) = WeatherInfo.mapWmoWeatherCode(63)
        assertEquals("Rainy", rainCond)
        assertEquals("🌧️", rainIcon)
    }

    @Test
    fun testWeatherAndAqiDtoConversion() {
        val weatherDto = CurrentWeatherDto(
            time = "2026-09-05T10:00",
            temperature2m = 26.8,
            relativeHumidity2m = 65.0,
            apparentTemperature = 28.2,
            weatherCode = 1.0,
            windSpeed10m = 8.5
        )
        val weather = WeatherInfo.fromDto(weatherDto)
        assertEquals(27, weather.temperatureC)
        assertEquals(28, weather.feelsLikeC)
        assertEquals(65, weather.humidityPercent)
        assertEquals("Mainly Clear", weather.weatherCondition)

        val aqiDto = CurrentAirQualityDto(
            time = "2026-09-05T10:00",
            usAqi = 42.0,
            europeanAqi = 30.0,
            pm10 = 22.0,
            pm25 = 11.5,
            carbonMonoxide = 210.0,
            nitrogenDioxide = 14.0,
            sulphurDioxide = 3.5,
            ozone = 30.0
        )
        val aqi = AirQualityInfo.fromDto(aqiDto)
        assertEquals(22, aqi.indianAqi)
        assertEquals(42, aqi.usAqi)
        assertEquals(22, aqi.aqi)
        assertEquals(AqiCategory.GOOD, aqi.category)
        assertEquals(11.5, aqi.pm25, 0.01)
        assertEquals(22.0, aqi.pm10, 0.01)
    }

    @Test
    fun testIndianNaqiCalculation() {
        // Good category (0..30 PM2.5 -> 0..50 AQI)
        assertEquals(25, AirQualityInfo.calculatePm25SubIndex(15.0))
        // Satisfactory category (31..60 PM2.5 -> 51..100 AQI)
        assertEquals(76, AirQualityInfo.calculatePm25SubIndex(45.0))
        // Moderate category (61..90 PM2.5 -> 101..200 AQI)
        assertEquals(151, AirQualityInfo.calculatePm25SubIndex(75.0))
        // Poor category (91..120 PM2.5 -> 201..300 AQI)
        assertEquals(251, AirQualityInfo.calculatePm25SubIndex(105.0))
        // Very Poor category (121..250 PM2.5 -> 301..400 AQI)
        assertEquals(351, AirQualityInfo.calculatePm25SubIndex(185.0))

        // Max of PM2.5 and PM10 subindices
        val naqi = AirQualityInfo.calculateIndianNaqi(pm25 = 45.0, pm10 = 120.0)
        assertTrue("NAQI should be >= 100, got $naqi", naqi >= 100)
    }

    // ==========================================
    // CO5: INDIAN LOCATIONS & STATE SWITCHER TESTS
    // ==========================================

    @Test
    fun testAllIndianLocationsAreWithinTerritorialBoundaries() {
        val locations = com.calmpath.ai.data.location.IndianLocationsRegistry.allLocations
        assertTrue("Registry should have multiple Indian locations", locations.size >= 20)

        for (loc in locations) {
            assertTrue(
                "Location ${loc.cityName}, ${loc.stateName} (${loc.latitude}, ${loc.longitude}) must be within India boundaries",
                LocationHelper.isLocationInIndia(loc.latitude, loc.longitude)
            )
        }
    }

    @Test
    fun testIndianLocationsSearchAndLookup() {
        val registry = com.calmpath.ai.data.location.IndianLocationsRegistry

        val delhiSearch = registry.searchLocations("delhi")
        assertTrue(delhiSearch.any { it.cityName == "New Delhi" })

        val keralaSearch = registry.searchLocations("Kerala")
        assertTrue(keralaSearch.any { it.stateName == "Kerala" })

        val shimlaSearch = registry.searchLocations("shimla")
        assertTrue(shimlaSearch.any { it.cityName == "Shimla" })

        val found = registry.findById("bengaluru")
        assertEquals("Bengaluru", found.cityName)
        assertEquals("Karnataka", found.stateName)

        val fallback = registry.findById("non_existent_city")
        assertEquals("Mumbai", fallback.cityName)
    }

    // ==========================================
    // CO6: GOOGLE MAPS NAVIGATION & MARKERS TESTS
    // ==========================================

    @Test
    fun testNavigationUriAndWebFallbackUrlGeneration() {
        val lat = 18.9220
        val lon = 72.8347
        val uriString = com.calmpath.ai.util.NavigationUtils.buildNavigationUriString(lat, lon, "w")
        assertEquals("google.navigation:q=18.922,72.8347&mode=w", uriString)

        val drivingUriString = com.calmpath.ai.util.NavigationUtils.buildNavigationUriString(lat, lon, "d")
        assertEquals("google.navigation:q=18.922,72.8347&mode=d", drivingUriString)

        val webUrl = com.calmpath.ai.util.NavigationUtils.buildWebDirectionsUrl(lat, lon, "w")
        assertEquals("https://www.google.com/maps/dir/?api=1&destination=18.922,72.8347&travelmode=walking", webUrl)

        val webDrivingUrl = com.calmpath.ai.util.NavigationUtils.buildWebDirectionsUrl(lat, lon, "d")
        assertEquals("https://www.google.com/maps/dir/?api=1&destination=18.922,72.8347&travelmode=driving", webDrivingUrl)
    }

    @Test
    fun testAllPlaceMarkersHaveValidCoordinatesAndPeaceScores() {
        val places = DatabaseSeeder.samplePlaces
        assertTrue("DatabaseSeeder should provide places for map markers", places.isNotEmpty())

        for (place in places) {
            // Latitude in India range (6.5 to 37.5)
            assertTrue(
                "Place ${place.name} latitude ${place.latitude} must be within India bounds",
                place.latitude in 6.5..37.5
            )
            // Longitude in India range (68.0 to 97.5)
            assertTrue(
                "Place ${place.name} longitude ${place.longitude} must be within India bounds",
                place.longitude in 68.0..97.5
            )
            // Peace score in valid 0..100 range
            assertTrue(
                "Place ${place.name} peace score ${place.peaceScore} must be in 0..100",
                place.peaceScore in 0..100
            )
            // Decibels and AQI valid
            assertTrue("Place ${place.name} averageAQI must be > 0", place.averageAQI > 0)
            assertTrue("Place ${place.name} averageNoiseLevel must be > 0", place.averageNoiseLevel > 0)
            assertTrue("Place ${place.name} must have non-empty address", place.address.isNotBlank())
        }
    }

    @Test
    fun testFilterPlacesBySearchAndCategory() {
        val places = DatabaseSeeder.samplePlaces.map { it.toDomainModel() }

        // Filter by category "Parks"
        val parkPlaces = places.filter { it.category.equals("Parks", ignoreCase = true) }
        assertTrue("Should have multiple Parks in India", parkPlaces.size >= 5)
        assertTrue(parkPlaces.all { it.category.equals("Parks", ignoreCase = true) })

        // Filter by search query "Garden"
        val gardenPlaces = places.filter {
            it.name.contains("Garden", ignoreCase = true) ||
            it.description.contains("Garden", ignoreCase = true) ||
            it.address.contains("Garden", ignoreCase = true)
        }
        assertTrue("Should find sanctuaries with 'Garden' in name/desc", gardenPlaces.isNotEmpty())

        // Filter by category "Libraries"
        val libraryPlaces = places.filter { it.category.equals("Libraries", ignoreCase = true) }
        assertTrue("Should have library sanctuaries", libraryPlaces.isNotEmpty())
    }

    @Test
    fun testIndiaBoundaryGeofenceForNavigationCoordinates() {
        // Valid Indian sanctuary coordinates
        assertTrue(LocationHelper.isLocationInIndia(19.0760, 72.8777)) // Mumbai
        assertTrue(LocationHelper.isLocationInIndia(28.6139, 77.2090)) // New Delhi
        assertTrue(LocationHelper.isLocationInIndia(12.9716, 77.5946)) // Bengaluru
        assertTrue(LocationHelper.isLocationInIndia(13.0827, 80.2707)) // Chennai
        assertTrue(LocationHelper.isLocationInIndia(30.0869, 78.2676)) // Rishikesh

        // Outside India coordinates must be rejected
        assertFalse(LocationHelper.isLocationInIndia(37.7749, -122.4194)) // San Francisco, USA
        assertFalse(LocationHelper.isLocationInIndia(51.5074, -0.1278))   // London, UK
        assertFalse(LocationHelper.isLocationInIndia(35.6762, 139.6503))  // Tokyo, Japan
        assertFalse(LocationHelper.isLocationInIndia(-33.8688, 151.2093)) // Sydney, Australia
    }

    @Test
    fun testTranquilRouteCoordinateGeneration() {
        val startLat = 19.0760
        val startLon = 72.8777
        val destLat = 18.9554
        val destLon = 72.8052

        val route = com.calmpath.ai.util.NavigationUtils.generateTranquilRouteCoordinates(
            startLat, startLon, destLat, destLon
        )

        assertTrue("Route should contain multiple waypoints", route.size >= 5)
        assertEquals(startLat, route.first().first, 0.0001)
        assertEquals(startLon, route.first().second, 0.0001)
        assertEquals(destLat, route.last().first, 0.0001)
        assertEquals(destLon, route.last().second, 0.0001)

        // Verify all intermediate route coordinates are within India
        for (pt in route) {
            assertTrue(
                "Route waypoint (${pt.first}, ${pt.second}) must be in India",
                LocationHelper.isLocationInIndia(pt.first, pt.second)
            )
        }
    }

    @Test
    fun testDecodeGooglePolyline() {
        // Standard official Google Maps polyline test vector
        val encoded = "_p~iF~ps|U_ulLnnqC_mqNvxq"
        val points = com.calmpath.ai.util.NavigationUtils.decodePolyline(encoded)

        assertEquals(3, points.size)
        assertEquals(38.5, points[0].first, 0.0001)
        assertEquals(-120.2, points[0].second, 0.0001)
        assertEquals(40.7, points[1].first, 0.0001)
        assertEquals(-120.95, points[1].second, 0.0001)
        assertEquals(43.252, points[2].first, 0.0001)
        assertEquals(-121.04628, points[2].second, 0.0001)
    }

    @Test
    fun testCleanHtmlInstructions() {
        val rawHtml = "Head <b>east</b> on <b>Old Custom House Rd</b> toward <div style=\"font-size:0.9em\">Mint Rd</div>"
        val cleaned = com.calmpath.ai.util.NavigationUtils.cleanHtmlInstructions(rawHtml)
        assertEquals("Head east on Old Custom House Rd toward Mint Rd", cleaned)
    }

    // ==========================================
    // CO7: MULTIMEDIA & AUDIO SERVICE TESTS
    // ==========================================

    @Test
    fun testAudioTrackCategoryRecommendations() {
        assertEquals(AudioTrack.NATURE, AudioTrack.recommendedForCategory("Parks"))
        assertEquals(AudioTrack.NATURE, AudioTrack.recommendedForCategory("Fitness"))
        assertEquals(AudioTrack.MEDITATION, AudioTrack.recommendedForCategory("Meditation"))
        assertEquals(AudioTrack.MEDITATION, AudioTrack.recommendedForCategory("Libraries"))
        assertEquals(AudioTrack.RAIN, AudioTrack.recommendedForCategory("Lakes"))
        assertEquals(AudioTrack.RAIN, AudioTrack.recommendedForCategory("Cafes"))
        assertEquals(AudioTrack.NATURE, AudioTrack.recommendedForCategory("unknown_category"))
    }

    @Test
    fun testAudioPlayerUiStateProgressCalculation() {
        val idleState = AudioPlayerUiState(state = AudioPlaybackState.IDLE, durationMs = 0L, currentPositionMs = 0L)
        assertEquals(0f, idleState.progressPercent, 0.001f)
        assertFalse(idleState.isPlaying)
        assertFalse(idleState.isPaused)

        val playingState = AudioPlayerUiState(
            state = AudioPlaybackState.PLAYING,
            currentTrack = AudioTrack.NATURE,
            currentPositionMs = 30000L,
            durationMs = 60000L
        )
        assertEquals(0.5f, playingState.progressPercent, 0.001f)
        assertTrue(playingState.isPlaying)
        assertFalse(playingState.isPaused)

        val pausedState = AudioPlayerUiState(
            state = AudioPlaybackState.PAUSED,
            currentTrack = AudioTrack.MEDITATION,
            currentPositionMs = 45000L,
            durationMs = 60000L
        )
        assertEquals(0.75f, pausedState.progressPercent, 0.001f)
        assertFalse(pausedState.isPlaying)
        assertTrue(pausedState.isPaused)

        // Clamped bounds: position > duration
        val clampedState = AudioPlayerUiState(
            state = AudioPlaybackState.PLAYING,
            currentPositionMs = 70000L,
            durationMs = 60000L
        )
        assertEquals(1.0f, clampedState.progressPercent, 0.001f)
    }

    @Test
    fun testAllAudioTracksHaveValidMetadata() {
        assertEquals(3, AudioTrack.ALL_TRACKS.size)
        for (track in AudioTrack.ALL_TRACKS) {
            assertTrue("Track ID must not be blank", track.id.isNotBlank())
            assertTrue("Track title must not be blank", track.title.isNotBlank())
            assertTrue("Track subtitle must not be blank", track.subtitle.isNotBlank())
            assertTrue("Track icon must not be blank", track.icon.isNotBlank())
            assertTrue("Track rawResId must be valid resource", track.rawResId != 0)
        }
    }

    @Test
    fun testCO7EndToEndInteractiveWorkflow() {
        // 1. GPS Location Determination
        val userLat = 18.9322
        val userLon = 72.8358
        assertTrue("User location must be within India geofence", LocationHelper.isLocationInIndia(userLat, userLon))

        // 2. Network Environmental Telemetry (Open-Meteo & CPCB NAQI simulated)
        val weatherDto = CurrentWeatherDto(
            time = "2026-09-12T10:00",
            temperature2m = 25.0,
            relativeHumidity2m = 60.0,
            apparentTemperature = 26.0,
            weatherCode = 0.0, // Clear Sky
            windSpeed10m = 5.0
        )
        val weather = WeatherInfo.fromDto(weatherDto)
        val aqiDto = CurrentAirQualityDto(
            time = "2026-09-12T10:00",
            usAqi = 35.0,
            europeanAqi = 25.0,
            pm10 = 18.0,
            pm25 = 8.0,
            carbonMonoxide = 150.0,
            nitrogenDioxide = 10.0,
            sulphurDioxide = 2.0,
            ozone = 25.0
        )
        val aqi = AirQualityInfo.fromDto(aqiDto)

        // 3. Peace Score Engine evaluates sanctuary
        val calculator = PeaceScoreCalculator()
        val peaceScore = calculator.calculatePeaceScore(
            aqi = aqi.indianAqi,
            noiseDb = 38,
            temperatureC = weather.temperatureC,
            weatherCondition = weather.weatherCondition,
            distanceKm = 1.8,
            userRating = 4.8
        )
        assertTrue("Peace Score must reflect high tranquility (>= 80)", peaceScore >= 80)

        // 4. Google Maps Route Rendering to Sanctuary
        val destLat = 18.9220
        val destLon = 72.8347
        val routeWaypoints = com.calmpath.ai.util.NavigationUtils.generateTranquilRouteCoordinates(
            userLat, userLon, destLat, destLon
        )
        assertTrue("Map route should have waypoints", routeWaypoints.size >= 5)

        // 5. Multimedia Service Experience (Image + Recommended Audio)
        val placeCategory = "Parks"
        val recommendedAudio = AudioTrack.recommendedForCategory(placeCategory)
        assertEquals(AudioTrack.NATURE, recommendedAudio)
        assertEquals("🌿", recommendedAudio.icon)
    }

    @Test
    fun testChatMessageEntityCreation() {
        val now = System.currentTimeMillis()
        val userMsg = ChatMessageEntity(
            messageId = "msg_1",
            userId = "guest",
            conversationId = "conv_default",
            role = ChatMessageEntity.ROLE_USER,
            message = "Find a quiet park near me with good AQI",
            timestamp = now
        )
        val modelMsg = ChatMessageEntity(
            messageId = "msg_2",
            userId = "guest",
            conversationId = "conv_default",
            role = ChatMessageEntity.ROLE_ASSISTANT,
            message = "I recommend Hanging Gardens in Malabar Hill with an AQI of 42 and Peace Score of 88.",
            recommendedPlaceId = "place_1",
            action = ChatMessageEntity.ACTION_SHOW_PLACE,
            timestamp = now + 1000
        )

        assertEquals("msg_1", userMsg.messageId)
        assertEquals(ChatMessageEntity.ROLE_USER, userMsg.role)
        assertEquals("Find a quiet park near me with good AQI", userMsg.message)
        assertEquals(null, userMsg.recommendedPlaceId)
        assertEquals("NONE", userMsg.action)
        assertTrue(userMsg.isUser)
        assertFalse(userMsg.isAssistant)

        assertEquals("msg_2", modelMsg.messageId)
        assertEquals(ChatMessageEntity.ROLE_ASSISTANT, modelMsg.role)
        assertEquals("place_1", modelMsg.recommendedPlaceId)
        assertEquals(ChatMessageEntity.ACTION_SHOW_PLACE, modelMsg.action)
        assertTrue(modelMsg.timestamp > userMsg.timestamp)
        assertTrue(modelMsg.isAssistant)
        assertFalse(modelMsg.isUser)
    }

    @Test
    fun testDynamicDistanceCalculation() {
        val place = PlaceEntity(
            placeId = "pl_dist_test",
            name = "Marine Drive Promenade",
            category = "Waterfronts",
            address = "Marine Drive, Mumbai",
            latitude = 18.9432,
            longitude = 72.8230,
            description = "Tranquil sea breeze and gentle wave sounds",
            imageUrl = "https://images.unsplash.com/photo-1570168007204-dfb528c6958f",
            averageAQI = 48,
            averageNoiseLevel = 52,
            peaceScore = 84
        )

        // Point A: Nariman Point (~1.5 km away)
        val userLatA = 18.9256
        val userLonA = 72.8242
        val domainModelA = place.toDomainModel(userLat = userLatA, userLon = userLonA)

        // Point B: Bandra Bandstand (~14 km away)
        val userLatB = 19.0475
        val userLonB = 72.8180
        val domainModelB = place.toDomainModel(userLat = userLatB, userLon = userLonB)

        // Verify distance is dynamically calculated and NOT hardcoded to 1.5 km
        assertTrue("Distance from Point A should be around 1.0 - 3.0 km (was ${domainModelA.distanceKm})", domainModelA.distanceKm in 1.0..3.0)
        assertTrue("Distance from Point B should be around 10.0 - 16.0 km (was ${domainModelB.distanceKm})", domainModelB.distanceKm in 10.0..16.0)
        assertFalse("Dynamic distance must change when user location changes", domainModelA.distanceKm == domainModelB.distanceKm)
    }

    @Test
    fun testCO8AIAssistantContextReasoning() {
        val contextDto = com.calmpath.ai.data.remote.api.CalmPathContextDto(
            mood = "meditate",
            latitude = 18.9322,
            longitude = 72.8358,
            locality = "Mumbai, Maharashtra",
            aqi = 45,
            noiseDb = 42,
            temperatureC = 26,
            weather = "26°C, Clear Sky",
            places = listOf(
                com.calmpath.ai.data.remote.api.CandidatePlaceDto(
                    id = "place_1",
                    name = "Hanging Gardens",
                    category = "Parks",
                    distanceKm = 2.1,
                    peaceScore = 91,
                    aqi = 40,
                    noiseDb = 36,
                    address = "Malabar Hill, Mumbai"
                )
            )
        )

        assertEquals("meditate", contextDto.mood)
        assertEquals(45, contextDto.aqi)
        assertEquals(1, contextDto.places.size)
        assertEquals("Hanging Gardens", contextDto.places[0].name)
        assertEquals("Parks", contextDto.places[0].category)
        assertEquals(91, contextDto.places[0].peaceScore)
    }

    // ==========================================
    // CO9: MACHINE LEARNING RECOMMENDATION TESTS
    // ==========================================

    @Test
    fun testFeaturePreprocessorEncoding() {
        assertEquals(0f, FeaturePreprocessor.encodeWeather("Clear"))
        assertEquals(1f, FeaturePreprocessor.encodeWeather("Partly Cloudy"))
        assertEquals(2f, FeaturePreprocessor.encodeWeather("Cloudy"))
        assertEquals(3f, FeaturePreprocessor.encodeWeather("Rainy"))
        assertEquals(4f, FeaturePreprocessor.encodeWeather("Thunderstorm"))
        assertEquals(5f, FeaturePreprocessor.encodeWeather("Mist/Fog"))

        assertEquals(0f, FeaturePreprocessor.encodeCategory("Parks"))
        assertEquals(1f, FeaturePreprocessor.encodeCategory("Lakes"))
        assertEquals(2f, FeaturePreprocessor.encodeCategory("Cafes"))
        assertEquals(3f, FeaturePreprocessor.encodeCategory("Libraries"))
        assertEquals(4f, FeaturePreprocessor.encodeCategory("Meditation"))
        assertEquals(5f, FeaturePreprocessor.encodeCategory("Fitness"))

        assertEquals(0f, FeaturePreprocessor.encodeMood("Relax"))
        assertEquals(1f, FeaturePreprocessor.encodeMood("Meditate"))
        assertEquals(2f, FeaturePreprocessor.encodeMood("Study"))
        assertEquals(3f, FeaturePreprocessor.encodeMood("Exercise"))
        assertEquals(4f, FeaturePreprocessor.encodeMood("Fresh Air"))
        assertEquals(5f, FeaturePreprocessor.encodeMood("Quiet Time"))

        val raw = RawPlaceFeatures(
            aqi = 35.0,
            pm25 = 18.0,
            pm10 = 32.0,
            noiseDb = 34.0,
            temperature = 24.0,
            humidity = 55.0,
            weatherCondition = "Clear",
            distanceKm = 1.8,
            placeCategory = "Parks",
            placeRating = 4.7,
            timeOfDay = 10.0,
            dayOfWeek = 4.0,
            userMood = "Relax",
            preferredCategory = "Parks",
            maxDistance = 10.0,
            userPreviousRating = 5.0
        )

        val array = FeaturePreprocessor.preprocess(raw)
        assertEquals(FeaturePreprocessor.FEATURE_COUNT, array.size)
        assertEquals(35f, array[0], 0.01f) // aqi
        assertEquals(18f, array[1], 0.01f) // pm25
        assertEquals(34f, array[3], 0.01f) // noiseDb
        assertEquals(0f, array[6], 0.01f)  // weather Clear -> 0
        assertEquals(0f, array[8], 0.01f)  // category Parks -> 0
        assertEquals(0f, array[12], 0.01f) // mood Relax -> 0
    }

    @Test
    fun testPeaceRecommendationModelPrediction() {
        val modelFile = listOf(
            File("src/main/assets/ml/peace_recommendation_rf_v1.json"),
            File("app/src/main/assets/ml/peace_recommendation_rf_v1.json"),
            File("D:/CalmPathAI/app/src/main/assets/ml/peace_recommendation_rf_v1.json")
        ).firstOrNull { it.exists() }

        val model = if (modelFile != null) {
            PeaceRecommendationModel.loadFromStream(modelFile.inputStream())
        } else {
            PeaceRecommendationModel.createBaselineModel()
        }

        assertNotNull("Model must be loaded", model)
        assertTrue("Model must have trees", model.trees.isNotEmpty())

        val testFeatures = FloatArray(FeaturePreprocessor.FEATURE_COUNT) { 0f }
        testFeatures[0] = 30f // Low AQI
        testFeatures[3] = 32f // Low noise dB
        testFeatures[4] = 23f // 23°C
        testFeatures[7] = 1.2f // 1.2 km away
        testFeatures[9] = 4.8f // 4.8 stars

        val prediction = model.predict(testFeatures)
        assertNotNull(prediction)
        assertTrue("Predicted suitability must be in 0..100 range, was ${prediction.suitabilityScore}", prediction.suitabilityScore in 0..100)
        assertTrue("Raw prediction must be in 0..100 range", prediction.rawPrediction in 0f..100f)
        assertEquals("v1.0", prediction.modelVersion)
    }

    @Test
    fun testDifferentMoodsProduceDifferentSuitabilityScores() {
        val modelFile = listOf(
            File("src/main/assets/ml/peace_recommendation_rf_v1.json"),
            File("app/src/main/assets/ml/peace_recommendation_rf_v1.json"),
            File("D:/CalmPathAI/app/src/main/assets/ml/peace_recommendation_rf_v1.json")
        ).firstOrNull { it.exists() }

        val model = if (modelFile != null) {
            PeaceRecommendationModel.loadFromStream(modelFile.inputStream())
        } else {
            PeaceRecommendationModel.createBaselineModel()
        }

        // Feature vector for an ultra-quiet indoor library: noise 30 dB, AQI 25, category Libraries
        val studyRaw = RawPlaceFeatures(
            aqi = 25.0,
            pm25 = 12.0,
            pm10 = 22.0,
            noiseDb = 30.0,
            temperature = 22.0,
            humidity = 50.0,
            weatherCondition = "Clear",
            distanceKm = 2.0,
            placeCategory = "Libraries",
            placeRating = 4.8,
            timeOfDay = 14.0,
            dayOfWeek = 3.0,
            userMood = "Study",
            preferredCategory = "Libraries",
            maxDistance = 10.0,
            userPreviousRating = 0.0
        )

        // Same physical place, but user is looking to "Exercise"
        val exerciseRaw = studyRaw.copy(
            userMood = "Exercise",
            preferredCategory = "Fitness"
        )

        val studyVec = FeaturePreprocessor.preprocess(studyRaw)
        val exerciseVec = FeaturePreprocessor.preprocess(exerciseRaw)

        val studyPred = model.predict(studyVec)
        val exercisePred = model.predict(exerciseVec)

        // Different moods must produce distinct personalized suitability scores
        assertTrue(
            "Suitability scores must differ across different moods (Study: ${studyPred.suitabilityScore}, Exercise: ${exercisePred.suitabilityScore})",
            studyPred.suitabilityScore != exercisePred.suitabilityScore
        )

        // For a noisy place (62 dB), Exercise suitability should be higher than Meditation
        val noisyPlace = studyRaw.copy(noiseDb = 62.0)
        val medNoisyVec = FeaturePreprocessor.preprocess(noisyPlace.copy(userMood = "Meditate", preferredCategory = "Meditation"))
        val exNoisyVec = FeaturePreprocessor.preprocess(noisyPlace.copy(userMood = "Exercise", preferredCategory = "Fitness"))
        val medPred = model.predict(medNoisyVec)
        val exPred = model.predict(exNoisyVec)
        assertTrue(
            "Exercise suitability (${exPred.suitabilityScore}) should exceed Meditate suitability (${medPred.suitabilityScore}) for 62 dB place",
            exPred.suitabilityScore > medPred.suitabilityScore
        )
    }

    @Test
    fun testRecommendationEngineRanking() {
        val modelFile = listOf(
            File("src/main/assets/ml/peace_recommendation_rf_v1.json"),
            File("app/src/main/assets/ml/peace_recommendation_rf_v1.json"),
            File("D:/CalmPathAI/app/src/main/assets/ml/peace_recommendation_rf_v1.json")
        ).firstOrNull { it.exists() }

        val model = if (modelFile != null) {
            PeaceRecommendationModel.loadFromStream(modelFile.inputStream())
        } else {
            PeaceRecommendationModel.createBaselineModel()
        }

        val engine = RecommendationEngine(model)

        val placeA = Place(
            id = "p_gardens",
            name = "Hanging Gardens",
            category = "Parks",
            categoryIcon = "🌿",
            latitude = 18.9554,
            longitude = 72.8052,
            distanceKm = 1.2,
            peaceScore = 92,
            aqi = 28,
            noiseDb = 32,
            temperatureC = 24,
            imageUrl = "",
            address = "Malabar Hill, Mumbai",
            description = "Quiet botanical garden",
            recommendationReasons = emptyList()
        )

        val placeB = Place(
            id = "p_busy_cafe",
            name = "Noisy Traffic Cafe",
            category = "Cafes",
            categoryIcon = "☕",
            latitude = 18.9322,
            longitude = 72.8358,
            distanceKm = 8.5,
            peaceScore = 55,
            aqi = 110,
            noiseDb = 72,
            temperatureC = 34,
            imageUrl = "",
            address = "Downtown Traffic Circle",
            description = "Loud cafe",
            recommendationReasons = emptyList()
        )

        val ranked = engine.rankPlaces(listOf(placeB, placeA), Mood.RELAX)
        assertEquals(2, ranked.size)

        // placeA should be ranked first due to high peace and clean air
        assertEquals("p_gardens", ranked.first().place.id)
        assertTrue("Top ranked place must be marked as best match", ranked.first().isBestMatch)
        assertFalse("Second place should not be best match", ranked[1].isBestMatch)
        assertTrue(
            "Top place ML suitability (${ranked.first().mlSuitabilityScore}) should be >= second place (${ranked[1].mlSuitabilityScore})",
            ranked.first().mlSuitabilityScore >= ranked[1].mlSuitabilityScore
        )
    }

    @Test
    fun testRecommendationInteractionEntityPersistence() {
        val interaction = RecommendationInteractionEntity(
            interactionId = "int_1",
            userId = "test_user",
            placeId = "place_hanging_gardens",
            mood = "Relax",
            mlScore = 94,
            action = RecommendationInteractionEntity.ACTION_NAVIGATED,
            userRating = 5,
            timestamp = System.currentTimeMillis()
        )

        assertEquals("int_1", interaction.interactionId)
        assertEquals("test_user", interaction.userId)
        assertEquals("place_hanging_gardens", interaction.placeId)
        assertEquals("Relax", interaction.mood)
        assertEquals(94, interaction.mlScore)
        assertEquals("NAVIGATED", interaction.action)
        assertEquals(5, interaction.userRating)
    }

    @Test
    fun testChatMessageEntityHoldsMlSuitabilityScore() {
        val assistantMsg = ChatMessageEntity.createAssistantMessage(
            userId = "guest",
            message = "Since you're feeling stressed, Hanging Gardens is your best match.",
            recommendedPlaceId = "place_1",
            action = ChatMessageEntity.ACTION_SHOW_PLACE,
            mlSuitabilityScore = 94
        )

        assertEquals("place_1", assistantMsg.recommendedPlaceId)
        assertEquals(94, assistantMsg.mlSuitabilityScore)
        assertEquals("SHOW_PLACE", assistantMsg.action)
        assertTrue(assistantMsg.isAssistant)
    }
}
