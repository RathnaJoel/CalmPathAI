package com.calmpath.ai

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
import com.calmpath.ai.data.model.AqiCategory
import com.calmpath.ai.data.model.CalmnessLevel
import com.calmpath.ai.data.model.Mood
import com.calmpath.ai.data.model.NoiseCategory
import com.calmpath.ai.data.remote.SampleDataSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests verifying domain logic, 8 Room entities, and converters (CO1, CO3).
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
            latitude = 37.7,
            longitude = -122.4,
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
}
