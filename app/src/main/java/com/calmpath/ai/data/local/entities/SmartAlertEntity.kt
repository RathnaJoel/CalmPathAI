package com.calmpath.ai.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Entity 11: SmartAlertEntity (CO10: AI-Based Smart Alerts & Notifications).
 * Stores persistent alert notifications, environmental trigger metrics,
 * linked ML recommendations, and user read/action states.
 */
@Entity(
    tableName = "smart_alerts",
    indices = [
        Index(value = ["userId"]),
        Index(value = ["alertType"]),
        Index(value = ["createdAt"])
    ]
)
data class SmartAlertEntity(
    @PrimaryKey
    val alertId: String = UUID.randomUUID().toString(),
    val userId: String,
    val alertType: String,
    val title: String,
    val message: String,
    val priority: String = PRIORITY_DEFAULT,
    val triggerValue: Double = 0.0,
    val thresholdValue: Double = 0.0,
    val placeId: String? = null,
    val mlSuitabilityScore: Int? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val isActionTaken: Boolean = false
) {
    companion object {
        // Alert Types
        const val TYPE_AQI_ALERT = "AQI_ALERT"
        const val TYPE_NOISE_ALERT = "NOISE_ALERT"
        const val TYPE_WEATHER_ALERT = "WEATHER_ALERT"
        const val TYPE_ML_RECOMMENDATION = "ML_RECOMMENDATION"
        const val TYPE_PEACEFUL_PLACE_ALERT = "PEACEFUL_PLACE_ALERT"
        const val TYPE_CUSTOM_ALERT = "CUSTOM_ALERT"

        // Priority Levels
        const val PRIORITY_HIGH = "HIGH"
        const val PRIORITY_DEFAULT = "DEFAULT"
        const val PRIORITY_LOW = "LOW"
    }
}
