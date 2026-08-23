package com.calmpath.ai.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Entity 8: AppSettings
 * Stores persistent application settings for a user.
 */
@Entity(
    tableName = "app_settings",
    foreignKeys = [
        ForeignKey(
            entity = UserProfileEntity::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["userId"], unique = true)
    ]
)
data class AppSettingsEntity(
    @PrimaryKey
    val settingsId: String = UUID.randomUUID().toString(),
    val userId: String,
    val theme: String = "SYSTEM", // "LIGHT", "DARK", "SYSTEM"
    val notificationsEnabled: Boolean = true,
    val locationEnabled: Boolean = true,
    val distanceUnit: String = "km", // "km", "mi"
    val temperatureUnit: String = "°C", // "°C", "°F"
    val soundUnit: String = "dB" // "dB"
)
