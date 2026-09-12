package com.calmpath.ai.data.repository

import android.util.Log
import com.calmpath.ai.alerts.AlertDecision
import com.calmpath.ai.alerts.AlertDecisionEngine
import com.calmpath.ai.alerts.NotificationHelper
import com.calmpath.ai.data.local.dao.SmartAlertDao
import com.calmpath.ai.data.local.entities.AppSettingsEntity
import com.calmpath.ai.data.local.entities.PlaceEntity
import com.calmpath.ai.data.local.entities.SmartAlertEntity
import com.calmpath.ai.data.local.entities.UserPreferencesEntity
import com.calmpath.ai.data.model.Place
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * Repository orchestrating smart alerts data persistence, background evaluations,
 * and notification dispatches (CO10).
 */
class SmartAlertRepository(
    private val smartAlertDao: SmartAlertDao,
    private val decisionEngine: AlertDecisionEngine,
    private val notificationHelper: NotificationHelper,
    private val calmPathRepository: CalmPathRepository
) {
    private val tag = "SmartAlertRepository"

    fun getUserAlertsFlow(userId: String): Flow<List<SmartAlertEntity>> {
        return smartAlertDao.getUserAlertsFlow(userId)
    }

    fun getUnreadCountFlow(userId: String): Flow<Int> {
        return smartAlertDao.getUnreadCountFlow(userId)
    }

    suspend fun markAlertAsRead(alertId: String) = withContext(Dispatchers.IO) {
        smartAlertDao.markAlertAsRead(alertId)
    }

    suspend fun markAllAsRead(userId: String) = withContext(Dispatchers.IO) {
        smartAlertDao.markAllAsRead(userId)
    }

    suspend fun markActionTaken(alertId: String) = withContext(Dispatchers.IO) {
        smartAlertDao.markActionTaken(alertId)
    }

    suspend fun clearAllAlerts(userId: String) = withContext(Dispatchers.IO) {
        smartAlertDao.clearAllAlerts(userId)
    }

    /**
     * Gathers real-time environmental context, runs the AlertDecisionEngine,
     * persists the alert if triggered, and fires the Android notification.
     */
    suspend fun evaluateAndTriggerAlerts(
        userId: String = "guest",
        overrideAqi: Int? = null,
        overrideNoiseDb: Int? = null,
        overrideWeather: String? = null
    ): AlertDecision = withContext(Dispatchers.IO) {
        try {
            val appSettings = calmPathRepository.getAppSettings(userId)
            val userPrefs = calmPathRepository.getUserPreferences(userId)

            val currLoc = calmPathRepository.currentLocation.value
            val envSummary = calmPathRepository.fetchLiveEnvironment(currLoc.latitude, currLoc.longitude, currLoc.locality)

            val currentAqi = overrideAqi ?: envSummary.aqi
            val currentNoiseDb = overrideNoiseDb ?: envSummary.noiseDb
            val temperatureC = envSummary.temperatureC
            val weatherCondition = overrideWeather ?: envSummary.weatherCondition

            val candidatePlaces = calmPathRepository.getAllPlaces().map {
                it.toDomainModel(userLat = currLoc.latitude, userLon = currLoc.longitude)
            }
            val isOnline = calmPathRepository.networkMonitor?.isOnline() ?: true

            val decision = decisionEngine.evaluate(
                userId = userId,
                currentAqi = currentAqi,
                currentNoiseDb = currentNoiseDb,
                temperatureC = temperatureC,
                weatherCondition = weatherCondition,
                candidatePlaces = candidatePlaces,
                userPreferences = userPrefs,
                appSettings = appSettings,
                isNetworkAvailable = isOnline
            )

            if (decision is AlertDecision.TriggerAlert) {
                // 1. Persist alert to Room SQLite (Entity 11)
                val alertEntity = SmartAlertEntity(
                    userId = userId,
                    alertType = decision.alertType,
                    title = decision.title,
                    message = decision.message,
                    priority = decision.priority,
                    triggerValue = decision.triggerValue,
                    thresholdValue = decision.thresholdValue,
                    placeId = decision.recommendedPlace?.id,
                    mlSuitabilityScore = decision.mlSuitabilityScore,
                    createdAt = System.currentTimeMillis(),
                    isRead = false,
                    isActionTaken = false
                )
                smartAlertDao.insertAlert(alertEntity)

                // 2. Dispatch Android Notification
                notificationHelper.showNotification(
                    alertId = alertEntity.alertId,
                    type = alertEntity.alertType,
                    title = alertEntity.title,
                    message = alertEntity.message,
                    priority = alertEntity.priority,
                    targetScreen = decision.targetScreen,
                    placeId = decision.recommendedPlace?.id,
                    placeLatitude = decision.recommendedPlace?.latitude,
                    placeLongitude = decision.recommendedPlace?.longitude
                )
            }

            decision
        } catch (e: Exception) {
            Log.e(tag, "Error during alert evaluation: ${e.message}", e)
            AlertDecision.NoAlert("Evaluation failed: ${e.message}")
        }
    }
}
