package com.calmpath.ai.alerts

import com.calmpath.ai.data.local.dao.SmartAlertDao
import com.calmpath.ai.data.local.entities.SmartAlertEntity
import kotlin.math.abs

/**
 * Anti-Spam and Cooldown Manager for CalmPath Smart Alerts (CO10).
 * Prevents redundant notifications, checks environmental deltas,
 * enforces minimum quiet intervals, and ensures maximum notification hygiene.
 */
class AlertCooldownManager(
    private val smartAlertDao: SmartAlertDao
) {

    /**
     * Determines if a candidate alert should be suppressed or allowed through.
     *
     * Rules:
     * 1. If no previous alert of this type exists -> ALLOW.
     * 2. If cooldown window has passed -> ALLOW.
     * 3. If within cooldown window:
     *    - For AQI: ALLOW only if AQI worsened by >= SIGNIFICANT_AQI_DELTA (e.g. +20 points).
     *    - For Noise: ALLOW only if Noise worsened by >= SIGNIFICANT_NOISE_DELTA (e.g. +10 dB).
     *    - For others: SUPPRESS.
     */
    suspend fun shouldAllowAlert(
        userId: String,
        alertType: String,
        currentTriggerValue: Double,
        cooldownMinutes: Int = 30
    ): CooldownCheckResult {
        val cooldownMillis = cooldownMinutes * 60 * 1000L
        val thresholdTimestamp = System.currentTimeMillis() - cooldownMillis

        val recentAlert = smartAlertDao.checkRecentDuplicateAlert(userId, alertType, thresholdTimestamp)
            ?: return CooldownCheckResult(isAllowed = true, reason = "Cooldown window clear")

        // Within cooldown window: check for significant deterioration
        when (alertType) {
            SmartAlertEntity.TYPE_AQI_ALERT -> {
                val aqiWorsening = currentTriggerValue - recentAlert.triggerValue
                if (aqiWorsening >= SIGNIFICANT_AQI_DELTA) {
                    return CooldownCheckResult(
                        isAllowed = true,
                        reason = "Significant AQI deterioration detected (+$aqiWorsening AQI)"
                    )
                }
                return CooldownCheckResult(
                    isAllowed = false,
                    reason = "AQI alert cooldown active. Last alert: ${recentAlert.triggerValue.toInt()} AQI"
                )
            }

            SmartAlertEntity.TYPE_NOISE_ALERT -> {
                val noiseIncrease = currentTriggerValue - recentAlert.triggerValue
                if (noiseIncrease >= SIGNIFICANT_NOISE_DELTA) {
                    return CooldownCheckResult(
                        isAllowed = true,
                        reason = "Significant noise increase detected (+$noiseIncrease dB)"
                    )
                }
                return CooldownCheckResult(
                    isAllowed = false,
                    reason = "Noise alert cooldown active. Last alert: ${recentAlert.triggerValue.toInt()} dB"
                )
            }

            SmartAlertEntity.TYPE_ML_RECOMMENDATION -> {
                return CooldownCheckResult(
                    isAllowed = false,
                    reason = "Recommendation cooldown active. Avoid recommendation fatigue."
                )
            }

            SmartAlertEntity.TYPE_WEATHER_ALERT -> {
                return CooldownCheckResult(
                    isAllowed = false,
                    reason = "Weather alert cooldown active."
                )
            }

            else -> {
                return CooldownCheckResult(
                    isAllowed = false,
                    reason = "Alert cooldown active for $alertType."
                )
            }
        }
    }

    data class CooldownCheckResult(
        val isAllowed: Boolean,
        val reason: String
    )

    companion object {
        const val SIGNIFICANT_AQI_DELTA = 20.0
        const val SIGNIFICANT_NOISE_DELTA = 10.0
    }
}
