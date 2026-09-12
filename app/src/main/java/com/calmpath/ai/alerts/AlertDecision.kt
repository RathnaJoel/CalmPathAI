package com.calmpath.ai.alerts

import com.calmpath.ai.data.model.Place

/**
 * Result of the AI Alert Decision Engine evaluation (CO10).
 */
sealed class AlertDecision {

    data class TriggerAlert(
        val alertType: String,
        val title: String,
        val message: String,
        val priority: String,
        val triggerValue: Double,
        val thresholdValue: Double,
        val recommendedPlace: Place? = null,
        val mlSuitabilityScore: Int? = null,
        val actionType: AlertActionType = AlertActionType.OPEN_CALMPATH,
        val targetScreen: String = "explore"
    ) : AlertDecision()

    data class NoAlert(
        val reason: String
    ) : AlertDecision()
}

/**
 * User action types on notification.
 */
enum class AlertActionType {
    EXPLORE_PLACES,
    EXPLORE_QUIET_PLACES,
    EXPLORE_INDOOR,
    VIEW_PLACE,
    NAVIGATE,
    OPEN_CALMPATH
}

/**
 * Severity categorization for notification delivery.
 */
enum class AlertSeverity {
    HIGH,
    DEFAULT,
    LOW
}
