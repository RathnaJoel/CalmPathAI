package com.calmpath.ai

import com.calmpath.ai.data.local.Converters
import com.calmpath.ai.data.local.entities.FavoritePlaceEntity
import com.calmpath.ai.data.local.entities.HistoryEntity
import com.calmpath.ai.data.model.AqiCategory
import com.calmpath.ai.data.model.CalmnessLevel
import com.calmpath.ai.data.model.Mood
import com.calmpath.ai.data.model.NoiseCategory
import com.calmpath.ai.data.model.Place
import com.calmpath.ai.data.remote.SampleDataSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests verifying domain logic, environmental metrics, converters, and models (CO1, CO3).
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
    fun testFavoriteEntityMapping() {
        val place = SampleDataSource.places.first()
        val entity = FavoritePlaceEntity.fromPlace(place, isSynced = true)

        assertEquals(place.id, entity.id)
        assertEquals(place.name, entity.placeName)
        assertEquals(place.peaceScore, entity.peaceScore)
        assertTrue(entity.isSyncedWithCloud)

        val restoredPlace = entity.toPlace()
        assertEquals(place.id, restoredPlace.id)
        assertEquals(place.name, restoredPlace.name)
    }

    @Test
    fun testSampleDataIntegrity() {
        assertTrue(SampleDataSource.places.isNotEmpty())
        assertTrue(SampleDataSource.heatmapZones.isNotEmpty())
        assertTrue(SampleDataSource.categories.contains("Parks"))

        SampleDataSource.places.forEach { place ->
            assertTrue(place.peaceScore in 0..100)
            assertTrue(place.aqi > 0)
            assertTrue(place.noiseDb > 0)
            assertNotNull(place.imageUrl)
            assertTrue(place.recommendationReasons.isNotEmpty())
        }
    }
}
