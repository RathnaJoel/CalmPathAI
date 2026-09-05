package com.calmpath.ai

import com.calmpath.ai.data.domain.PeaceScoreCalculator
import com.calmpath.ai.data.local.Converters
import com.calmpath.ai.data.local.DatabaseSeeder
import com.calmpath.ai.data.local.entities.AppSettingsEntity
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
        assertEquals(8, DatabaseSeeder.samplePlaces.size)

        val snapshots = DatabaseSeeder.createSampleSnapshots()
        assertTrue(snapshots.isNotEmpty())
        assertEquals(16, snapshots.size)

        DatabaseSeeder.samplePlaces.forEach { place ->
            assertTrue(place.peaceScore in 0..100)
            assertTrue(place.averageAQI > 0)
            assertTrue(place.averageNoiseLevel > 0)
            assertNotNull(place.imageUrl)
            val domain = place.toDomainModel()
            assertEquals(place.placeId, domain.id)
            assertEquals(place.name, domain.name)
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
        assertEquals(26, weather.temperatureC)
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
        assertEquals(42, aqi.aqi)
        assertEquals(AqiCategory.GOOD, aqi.category)
        assertEquals(11.5, aqi.pm25, 0.01)
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
}
