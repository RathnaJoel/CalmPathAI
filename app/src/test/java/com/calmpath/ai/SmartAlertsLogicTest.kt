package com.calmpath.ai

import com.calmpath.ai.alerts.AlertActionType
import com.calmpath.ai.alerts.AlertCooldownManager
import com.calmpath.ai.alerts.AlertDecision
import com.calmpath.ai.alerts.AlertDecisionEngine
import com.calmpath.ai.alerts.AlertRuleEvaluator
import com.calmpath.ai.alerts.ai.AlertMessageGenerator
import com.calmpath.ai.data.local.entities.AppSettingsEntity
import com.calmpath.ai.data.local.entities.SmartAlertEntity
import com.calmpath.ai.data.local.entities.UserPreferencesEntity
import com.calmpath.ai.data.model.Mood
import com.calmpath.ai.data.model.Place
import com.calmpath.ai.ml.PeaceRecommendationModel
import com.calmpath.ai.ml.RecommendationEngine
import com.calmpath.ai.ml.ScoredPlaceRecommendation
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit Tests for Course Outcome 10 (CO10): AI-Based Smart Alerts & Notifications.
 * Validates Rule Evaluator, Decision Engine, Cooldown/Anti-Spam logic,
 * AI message fallback templates, and entity integrity.
 */
class SmartAlertsLogicTest {

    private lateinit var defaultPrefs: UserPreferencesEntity
    private lateinit var defaultSettings: AppSettingsEntity
    private lateinit var samplePlaces: List<Place>
    private lateinit var recommendationEngine: RecommendationEngine

    @Before
    fun setup() {
        defaultPrefs = UserPreferencesEntity(
            userId = "test_user",
            preferredMood = "Relax",
            preferredCategory = "All",
            maxDistance = 10.0,
            maxAQI = 60,
            maxNoiseLevel = 45,
            preferredTemperature = 22.0
        )

        defaultSettings = AppSettingsEntity(
            userId = "test_user",
            notificationsEnabled = true,
            smartAlertsEnabled = true,
            environmentalAlertsEnabled = true,
            aqiAlertsEnabled = true,
            noiseAlertsEnabled = true,
            weatherAlertsEnabled = true,
            mlRecommendationsEnabled = true,
            maxAqiThreshold = 100,
            maxNoiseThreshold = 60,
            minMlScoreThreshold = 80,
            alertCooldownMinutes = 30
        )

        val place1 = Place(
            id = "p1",
            name = "Tranquil Garden",
            category = "Parks",
            categoryIcon = "🌿",
            latitude = 18.95,
            longitude = 72.80,
            distanceKm = 1.5,
            peaceScore = 92,
            aqi = 28,
            noiseDb = 35,
            imageUrl = "",
            address = "Malabar Hill, Mumbai",
            description = "Quiet park",
            recommendationReasons = listOf("Quiet park")
        )

        val place2 = Place(
            id = "p2",
            name = "Silent Library Sanctuary",
            category = "Libraries",
            categoryIcon = "📚",
            latitude = 18.94,
            longitude = 72.82,
            distanceKm = 2.1,
            peaceScore = 90,
            aqi = 32,
            noiseDb = 30,
            imageUrl = "",
            address = "Fort, Mumbai",
            description = "Quiet library",
            recommendationReasons = listOf("Quiet library")
        )

        samplePlaces = listOf(place1, place2)
        recommendationEngine = RecommendationEngine(PeaceRecommendationModel.createBaselineModel())
    }

    @Test
    fun testAqiThresholdExceededTriggersAqiAlert() {
        val ranked = samplePlaces.map { ScoredPlaceRecommendation(it, 88, it.peaceScore, "Good air") }

        // Current AQI is 158 which exceeds user threshold of 100
        val decision = AlertRuleEvaluator.evaluateAqiRule(
            currentAqi = 158,
            userPreferences = defaultPrefs,
            appSettings = defaultSettings,
            rankedPlaces = ranked
        )

        assertNotNull("AQI alert must be triggered", decision)
        assertTrue(decision is AlertDecision.TriggerAlert)
        val trigger = decision as AlertDecision.TriggerAlert
        assertEquals(SmartAlertEntity.TYPE_AQI_ALERT, trigger.alertType)
        assertEquals(SmartAlertEntity.PRIORITY_HIGH, trigger.priority)
        assertEquals(158.0, trigger.triggerValue, 0.01)
        assertEquals(100.0, trigger.thresholdValue, 0.01)
        assertEquals(AlertActionType.EXPLORE_PLACES, trigger.actionType)
        assertNotNull(trigger.recommendedPlace)
        assertEquals("Tranquil Garden", trigger.recommendedPlace?.name)
    }

    @Test
    fun testNoiseThresholdExceededTriggersNoiseAlert() {
        val ranked = samplePlaces.map { ScoredPlaceRecommendation(it, 85, it.peaceScore, "Quiet area") }

        // Current noise is 74 dB which exceeds user threshold of 60 dB
        val decision = AlertRuleEvaluator.evaluateNoiseRule(
            currentNoiseDb = 74,
            userPreferences = defaultPrefs,
            appSettings = defaultSettings,
            rankedPlaces = ranked
        )

        assertNotNull("Noise alert must be triggered", decision)
        assertTrue(decision is AlertDecision.TriggerAlert)
        val trigger = decision as AlertDecision.TriggerAlert
        assertEquals(SmartAlertEntity.TYPE_NOISE_ALERT, trigger.alertType)
        assertEquals(SmartAlertEntity.PRIORITY_HIGH, trigger.priority)
        assertEquals(74.0, trigger.triggerValue, 0.01)
        assertEquals(60.0, trigger.thresholdValue, 0.01)
        assertEquals(AlertActionType.EXPLORE_QUIET_PLACES, trigger.actionType)
    }

    @Test
    fun testMlScoreMeetsThresholdTriggersRecommendationAlert() {
        val topPlace = samplePlaces.first()
        val topRecommendation = ScoredPlaceRecommendation(
            place = topPlace,
            mlSuitabilityScore = 93,
            peaceScore = topPlace.peaceScore,
            isBestMatch = true,
            matchReason = "Exceptional tranquility match for relaxing mood"
        )

        // Threshold is 80, score is 93 -> must trigger
        val decision = AlertRuleEvaluator.evaluateMlRecommendationRule(
            topRecommendation = topRecommendation,
            appSettings = defaultSettings
        )

        assertNotNull("ML recommendation alert must be triggered", decision)
        assertTrue(decision is AlertDecision.TriggerAlert)
        val trigger = decision as AlertDecision.TriggerAlert
        assertEquals(SmartAlertEntity.TYPE_ML_RECOMMENDATION, trigger.alertType)
        assertEquals(93, trigger.mlSuitabilityScore)
        assertEquals("p1", trigger.recommendedPlace?.id)
        assertEquals(AlertActionType.VIEW_PLACE, trigger.actionType)
    }

    @Test
    fun testNotificationsDisabledProducesNoAlert() = runBlocking {
        val disabledSettings = defaultSettings.copy(smartAlertsEnabled = false)
        val dummyDao = DummySmartAlertDao()
        val cooldownManager = AlertCooldownManager(dummyDao)
        val messageGen = AlertMessageGenerator()
        val engine = AlertDecisionEngine(cooldownManager, recommendationEngine, messageGen)

        val decision = engine.evaluate(
            userId = "test_user",
            currentAqi = 180, // severe AQI
            currentNoiseDb = 85, // severe noise
            temperatureC = 28,
            weatherCondition = "Clear",
            candidatePlaces = samplePlaces,
            userPreferences = defaultPrefs,
            appSettings = disabledSettings,
            isNetworkAvailable = false
        )

        assertTrue("When smart alerts are disabled, decision must be NoAlert", decision is AlertDecision.NoAlert)
        val noAlert = decision as AlertDecision.NoAlert
        assertTrue(noAlert.reason.contains("disabled", ignoreCase = true))
    }

    @Test
    fun testWeatherChangeTriggersIndoorSanctuaryAlert() {
        val ranked = samplePlaces.map { ScoredPlaceRecommendation(it, 82, it.peaceScore, "Indoor match") }

        val decision = AlertRuleEvaluator.evaluateWeatherRule(
            weatherCondition = "Heavy Rain",
            temperatureC = 24,
            appSettings = defaultSettings,
            rankedPlaces = ranked
        )

        assertNotNull("Rain weather alert must be triggered", decision)
        assertTrue(decision is AlertDecision.TriggerAlert)
        val trigger = decision as AlertDecision.TriggerAlert
        assertEquals(SmartAlertEntity.TYPE_WEATHER_ALERT, trigger.alertType)
        assertEquals(AlertActionType.EXPLORE_INDOOR, trigger.actionType)
        // Library sanctuary is prioritized during rain
        assertEquals("Silent Library Sanctuary", trigger.recommendedPlace?.name)
    }

    @Test
    fun testAiMessageGenerationFallbackTemplate() {
        val messageGen = AlertMessageGenerator()

        val trigger = AlertDecision.TriggerAlert(
            alertType = SmartAlertEntity.TYPE_AQI_ALERT,
            title = "Air Quality Alert",
            message = "",
            priority = SmartAlertEntity.PRIORITY_HIGH,
            triggerValue = 165.0,
            thresholdValue = 100.0,
            recommendedPlace = samplePlaces.first(),
            mlSuitabilityScore = 91,
            actionType = AlertActionType.EXPLORE_PLACES
        )

        val generated = messageGen.generateLocalTemplate(trigger)
        assertNotNull(generated)
        assertTrue("Message must mention AQI 165", generated.message.contains("165"))
        assertTrue("Message must mention place name", generated.message.contains("Tranquil Garden"))
        assertTrue("Message must mention distance", generated.message.contains("1.5"))
        assertFalse("Template fallback is not online AI", generated.isAiGenerated)
    }

    @Test
    fun testSmartAlertEntityIntegrity() {
        val alert = SmartAlertEntity(
            alertId = "alert_test_1",
            userId = "user_123",
            alertType = SmartAlertEntity.TYPE_AQI_ALERT,
            title = "Test AQI Alert",
            message = "Air quality worsened",
            priority = SmartAlertEntity.PRIORITY_HIGH,
            triggerValue = 155.0,
            thresholdValue = 100.0,
            placeId = "p1",
            mlSuitabilityScore = 89,
            isRead = false,
            isActionTaken = false
        )

        assertEquals("alert_test_1", alert.alertId)
        assertEquals("user_123", alert.userId)
        assertEquals("AQI_ALERT", alert.alertType)
        assertEquals("HIGH", alert.priority)
        assertEquals(155.0, alert.triggerValue, 0.01)
        assertEquals(100.0, alert.thresholdValue, 0.01)
        assertEquals(89, alert.mlSuitabilityScore)
        assertFalse(alert.isRead)
        assertFalse(alert.isActionTaken)
    }

    @Test
    fun testSettingsThresholdChangeReflectsInEvaluation() {
        val ranked = samplePlaces.map { ScoredPlaceRecommendation(it, 80, it.peaceScore, "Ok") }

        // At current AQI 80, threshold 100 -> NO alert
        val decisionBelow = AlertRuleEvaluator.evaluateAqiRule(
            currentAqi = 80,
            userPreferences = defaultPrefs,
            appSettings = defaultSettings, // threshold 100
            rankedPlaces = ranked
        )
        assertNull("AQI below threshold should not trigger", decisionBelow)

        // User changes threshold to 70 in settings -> MUST trigger alert
        val strictSettings = defaultSettings.copy(maxAqiThreshold = 70)
        val decisionAbove = AlertRuleEvaluator.evaluateAqiRule(
            currentAqi = 80,
            userPreferences = defaultPrefs.copy(maxAQI = 50),
            appSettings = strictSettings,
            rankedPlaces = ranked
        )
        assertNotNull("AQI above new threshold must trigger", decisionAbove)
        assertEquals(70.0, (decisionAbove as AlertDecision.TriggerAlert).thresholdValue, 0.01)
    }

    /**
     * Minimal in-memory dummy DAO for unit testing cooldown logic.
     */
    private class DummySmartAlertDao : com.calmpath.ai.data.local.dao.SmartAlertDao {
        val storedAlerts = mutableListOf<SmartAlertEntity>()

        override suspend fun insertAlert(alert: SmartAlertEntity) { storedAlerts.add(alert) }
        override suspend fun insertAlerts(alerts: List<SmartAlertEntity>) { storedAlerts.addAll(alerts) }
        override fun getUserAlertsFlow(userId: String) = kotlinx.coroutines.flow.flowOf(storedAlerts)
        override suspend fun getUserAlerts(userId: String) = storedAlerts
        override fun getUnreadAlertsFlow(userId: String) = kotlinx.coroutines.flow.flowOf(storedAlerts.filter { !it.isRead })
        override fun getUnreadCountFlow(userId: String) = kotlinx.coroutines.flow.flowOf(storedAlerts.count { !it.isRead })
        override suspend fun markAlertAsRead(alertId: String) {}
        override suspend fun markAllAsRead(userId: String) {}
        override suspend fun markActionTaken(alertId: String) {}
        override suspend fun checkRecentDuplicateAlert(userId: String, alertType: String, sinceTimestamp: Long): SmartAlertEntity? {
            return storedAlerts.firstOrNull { it.userId == userId && it.alertType == alertType && it.createdAt >= sinceTimestamp }
        }
        override suspend fun getLatestAlertByType(userId: String, alertType: String): SmartAlertEntity? {
            return storedAlerts.lastOrNull { it.userId == userId && it.alertType == alertType }
        }
        override suspend fun clearAllAlerts(userId: String) { storedAlerts.clear() }
        override suspend fun deleteOldAlerts(beforeTimestamp: Long) {}
    }
}
