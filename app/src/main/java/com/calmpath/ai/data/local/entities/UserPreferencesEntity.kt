package com.calmpath.ai.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Entity storing local user preferences, mood, and environmental thresholds (CO3).
 */
@Entity(tableName = "user_preferences")
data class UserPreferencesEntity(
    @PrimaryKey
    val id: Int = 1, // Singleton row for current active profile
    val selectedMood: String = "relax",
    val preferredCategory: String = "All",
    val maxAqi: Int = 60,
    val preferredNoiseLevel: Int = 45,
    val preferredDistanceKm: Int = 8,
    val notificationsEnabled: Boolean = true,
    val themeMode: String = "SYSTEM", // "LIGHT", "DARK", "SYSTEM"
    val lastSyncedTimestamp: Long = 0L
)
