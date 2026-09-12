package com.calmpath.ai.alerts

import com.calmpath.ai.data.local.entities.AppSettingsEntity
import com.calmpath.ai.data.local.entities.SmartAlertEntity
import com.calmpath.ai.data.local.entities.UserPreferencesEntity
import com.calmpath.ai.data.model.Place
import com.calmpath.ai.ml.ScoredPlaceRecommendation

/**
 * Evaluates environmental telemetry and candidate places against user thresholds (CO10).
 */
object AlertRuleEvaluator {

    /**
     * Checks if current AQI violates user configured threshold and selects the cleanest sanctuary.
     */
    fun evaluateAqiRule(
        currentAqi: Int,
        userPreferences: UserPreferencesEntity,
        appSettings: AppSettingsEntity,
        rankedPlaces: List<ScoredPlaceRecommendation>
    ): AlertDecision? {
        if (!appSettings.environmentalAlertsEnabled || !appSettings.aqiAlertsEnabled) {
            return null
        }

        val effectiveThreshold = appSettings.maxAqiThreshold.coerceAtLeast(userPreferences.maxAQI)
        if (currentAqi <= effectiveThreshold) {
            return null
        }

        // Find a sanctuary with lower AQI than current ambient
        val recommendedScored = rankedPlaces.firstOrNull { it.place.aqi < currentAqi }
            ?: rankedPlaces.firstOrNull()

        val recPlace = recommendedScored?.place
        val mlScore = recommendedScored?.mlSuitabilityScore

        return AlertDecision.TriggerAlert(
            alertType = SmartAlertEntity.TYPE_AQI_ALERT,
            title = "Air Quality Alert: AQI $currentAqi",
            message = if (recPlace != null) {
                "Air quality near you has worsened (AQI $currentAqi). CalmPath found cleaner air at ${recPlace.name}."
            } else {
                "Air quality near you has worsened (AQI $currentAqi). Consider staying indoors."
            },
            priority = SmartAlertEntity.PRIORITY_HIGH,
            triggerValue = currentAqi.toDouble(),
            thresholdValue = effectiveThreshold.toDouble(),
            recommendedPlace = recPlace,
            mlSuitabilityScore = mlScore,
            actionType = AlertActionType.EXPLORE_PLACES,
            targetScreen = "explore"
        )
    }

    /**
     * Checks if current ambient noise exceeds user threshold and selects a quiet sanctuary.
     */
    fun evaluateNoiseRule(
        currentNoiseDb: Int,
        userPreferences: UserPreferencesEntity,
        appSettings: AppSettingsEntity,
        rankedPlaces: List<ScoredPlaceRecommendation>
    ): AlertDecision? {
        if (!appSettings.environmentalAlertsEnabled || !appSettings.noiseAlertsEnabled) {
            return null
        }

        val effectiveThreshold = appSettings.maxNoiseThreshold.coerceAtLeast(userPreferences.maxNoiseLevel)
        if (currentNoiseDb <= effectiveThreshold) {
            return null
        }

        // Find a sanctuary with lower noise than current ambient
        val recommendedScored = rankedPlaces.firstOrNull { it.place.noiseDb < currentNoiseDb }
            ?: rankedPlaces.firstOrNull()

        val recPlace = recommendedScored?.place
        val mlScore = recommendedScored?.mlSuitabilityScore

        return AlertDecision.TriggerAlert(
            alertType = SmartAlertEntity.TYPE_NOISE_ALERT,
            title = "High Noise Alert: $currentNoiseDb dB",
            message = if (recPlace != null) {
                "Noise around you is high ($currentNoiseDb dB). ${recPlace.name} offers a tranquil sanctuary nearby."
            } else {
                "Noise levels around you are currently high ($currentNoiseDb dB). Seek a quiet area."
            },
            priority = SmartAlertEntity.PRIORITY_HIGH,
            triggerValue = currentNoiseDb.toDouble(),
            thresholdValue = effectiveThreshold.toDouble(),
            recommendedPlace = recPlace,
            mlSuitabilityScore = mlScore,
            actionType = AlertActionType.EXPLORE_QUIET_PLACES,
            targetScreen = "explore"
        )
    }

    /**
     * Checks for impending rainfall/storm and recommends a quiet indoor sanctuary.
     */
    fun evaluateWeatherRule(
        weatherCondition: String,
        temperatureC: Int,
        appSettings: AppSettingsEntity,
        rankedPlaces: List<ScoredPlaceRecommendation>
    ): AlertDecision? {
        if (!appSettings.environmentalAlertsEnabled || !appSettings.weatherAlertsEnabled) {
            return null
        }

        val isRainyOrStorm = weatherCondition.contains("Rain", ignoreCase = true) ||
                weatherCondition.contains("Thunder", ignoreCase = true) ||
                weatherCondition.contains("Drizzle", ignoreCase = true)

        if (!isRainyOrStorm) {
            return null
        }

        // Find indoor sanctuary (Library, Cafe, Meditation Center)
        val indoorRecommendation = rankedPlaces.firstOrNull {
            it.place.category.equals("Libraries", ignoreCase = true) ||
            it.place.category.equals("Cafes", ignoreCase = true) ||
            it.place.category.equals("Meditation", ignoreCase = true)
        } ?: rankedPlaces.firstOrNull()

        val recPlace = indoorRecommendation?.place
        val mlScore = indoorRecommendation?.mlSuitabilityScore

        return AlertDecision.TriggerAlert(
            alertType = SmartAlertEntity.TYPE_WEATHER_ALERT,
            title = "Weather Alert: $weatherCondition",
            message = if (recPlace != null) {
                "Rain is expected or occurring ($weatherCondition). Consider visiting indoor sanctuary ${recPlace.name}."
            } else {
                "Inclement weather detected ($weatherCondition). Consider heading indoors."
            },
            priority = SmartAlertEntity.PRIORITY_DEFAULT,
            triggerValue = temperatureC.toDouble(),
            thresholdValue = 25.0,
            recommendedPlace = recPlace,
            mlSuitabilityScore = mlScore,
            actionType = AlertActionType.EXPLORE_INDOOR,
            targetScreen = recPlace?.let { "place_details/${it.id}" } ?: "explore"
        )
    }

    /**
     * Checks if top CO9 ML recommendation exceeds user's minimum recommendation threshold.
     */
    fun evaluateMlRecommendationRule(
        topRecommendation: ScoredPlaceRecommendation?,
        appSettings: AppSettingsEntity
    ): AlertDecision? {
        if (!appSettings.mlRecommendationsEnabled || topRecommendation == null) {
            return null
        }

        val score = topRecommendation.mlSuitabilityScore
        val threshold = appSettings.minMlScoreThreshold

        if (score < threshold) {
            return null
        }

        val place = topRecommendation.place
        return AlertDecision.TriggerAlert(
            alertType = SmartAlertEntity.TYPE_ML_RECOMMENDATION,
            title = "Personalized Recommendation Nearby",
            message = "${place.name} matches your preferences with an exceptional $score% ML suitability match.",
            priority = SmartAlertEntity.PRIORITY_DEFAULT,
            triggerValue = score.toDouble(),
            thresholdValue = threshold.toDouble(),
            recommendedPlace = place,
            mlSuitabilityScore = score,
            actionType = AlertActionType.VIEW_PLACE,
            targetScreen = "place_details/${place.id}"
        )
    }
}
