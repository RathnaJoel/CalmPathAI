package com.calmpath.ai.alerts

import com.calmpath.ai.alerts.ai.AlertMessageGenerator
import com.calmpath.ai.data.local.entities.AppSettingsEntity
import com.calmpath.ai.data.local.entities.SmartAlertEntity
import com.calmpath.ai.data.local.entities.UserPreferencesEntity
import com.calmpath.ai.data.model.Mood
import com.calmpath.ai.data.model.Place
import com.calmpath.ai.ml.RecommendationEngine
import com.calmpath.ai.ml.ScoredPlaceRecommendation

/**
 * AI Alert Decision Engine (CO10).
 * Proactively determines whether alerts are necessary, selects the highest-priority
 * alert type, filters redundant notifications via cooldowns, runs the CO9 ML recommendation
 * engine, and produces final alert decisions.
 *
 * Independent from Compose UI and Activity classes.
 */
class AlertDecisionEngine(
    private val cooldownManager: AlertCooldownManager,
    private val recommendationEngine: RecommendationEngine,
    private val messageGenerator: AlertMessageGenerator
) {

    /**
     * Evaluates real-time telemetry and user preferences to generate an AlertDecision.
     */
    suspend fun evaluate(
        userId: String,
        currentAqi: Int,
        currentNoiseDb: Int,
        temperatureC: Int,
        weatherCondition: String,
        candidatePlaces: List<Place>,
        userPreferences: UserPreferencesEntity,
        appSettings: AppSettingsEntity,
        isNetworkAvailable: Boolean = true
    ): AlertDecision {
        // Master switch check
        if (!appSettings.notificationsEnabled || !appSettings.smartAlertsEnabled) {
            return AlertDecision.NoAlert("Smart alerts are disabled in user settings")
        }

        // Rank candidate places with CO9 Machine Learning
        val activeMood = Mood.fromId(userPreferences.preferredMood)
        val rankedRecommendations = recommendationEngine.rankPlaces(candidatePlaces, activeMood)
        val topRecommendation = rankedRecommendations.firstOrNull()

        // 1. Evaluate AQI Rule (Highest Priority Hazard)
        val aqiDecision = AlertRuleEvaluator.evaluateAqiRule(
            currentAqi = currentAqi,
            userPreferences = userPreferences,
            appSettings = appSettings,
            rankedPlaces = rankedRecommendations
        )
        if (aqiDecision is AlertDecision.TriggerAlert) {
            val cooldownResult = cooldownManager.shouldAllowAlert(
                userId = userId,
                alertType = aqiDecision.alertType,
                currentTriggerValue = aqiDecision.triggerValue,
                cooldownMinutes = appSettings.alertCooldownMinutes
            )
            if (cooldownResult.isAllowed) {
                return enrichWithAiMessage(aqiDecision, userPreferences.preferredMood, isNetworkAvailable)
            }
        }

        // 2. Evaluate Noise Rule (High Priority Acoustic Stress)
        val noiseDecision = AlertRuleEvaluator.evaluateNoiseRule(
            currentNoiseDb = currentNoiseDb,
            userPreferences = userPreferences,
            appSettings = appSettings,
            rankedPlaces = rankedRecommendations
        )
        if (noiseDecision is AlertDecision.TriggerAlert) {
            val cooldownResult = cooldownManager.shouldAllowAlert(
                userId = userId,
                alertType = noiseDecision.alertType,
                currentTriggerValue = noiseDecision.triggerValue,
                cooldownMinutes = appSettings.alertCooldownMinutes
            )
            if (cooldownResult.isAllowed) {
                return enrichWithAiMessage(noiseDecision, userPreferences.preferredMood, isNetworkAvailable)
            }
        }

        // 3. Evaluate Weather Rule (Sudden Rain/Storm)
        val weatherDecision = AlertRuleEvaluator.evaluateWeatherRule(
            weatherCondition = weatherCondition,
            temperatureC = temperatureC,
            appSettings = appSettings,
            rankedPlaces = rankedRecommendations
        )
        if (weatherDecision is AlertDecision.TriggerAlert) {
            val cooldownResult = cooldownManager.shouldAllowAlert(
                userId = userId,
                alertType = weatherDecision.alertType,
                currentTriggerValue = weatherDecision.triggerValue,
                cooldownMinutes = appSettings.alertCooldownMinutes
            )
            if (cooldownResult.isAllowed) {
                return enrichWithAiMessage(weatherDecision, userPreferences.preferredMood, isNetworkAvailable)
            }
        }

        // 4. Evaluate CO9 ML Recommendation Rule
        val mlDecision = AlertRuleEvaluator.evaluateMlRecommendationRule(
            topRecommendation = topRecommendation,
            appSettings = appSettings
        )
        if (mlDecision is AlertDecision.TriggerAlert) {
            val cooldownResult = cooldownManager.shouldAllowAlert(
                userId = userId,
                alertType = mlDecision.alertType,
                currentTriggerValue = mlDecision.triggerValue,
                cooldownMinutes = appSettings.alertCooldownMinutes
            )
            if (cooldownResult.isAllowed) {
                return enrichWithAiMessage(mlDecision, userPreferences.preferredMood, isNetworkAvailable)
            }
        }

        return AlertDecision.NoAlert("All environmental conditions within thresholds and no new recommendations")
    }

    private suspend fun enrichWithAiMessage(
        decision: AlertDecision.TriggerAlert,
        userMood: String,
        isNetworkAvailable: Boolean
    ): AlertDecision.TriggerAlert {
        val generated = messageGenerator.generateMessage(decision, userMood, isNetworkAvailable)
        return decision.copy(
            title = generated.title,
            message = generated.message
        )
    }
}
