package com.calmpath.ai.data.model

/**
 * User Profile information derived from Firebase Auth & local preferences.
 */
data class UserProfile(
    val uid: String = "",
    val displayName: String = "Joel Wellness",
    val email: String = "joel@calmpath.ai",
    val photoUrl: String? = null,
    val selectedMood: Mood = Mood.RELAX,
    val preferredCategory: String = "All",
    val maxAqiTolerance: Int = 60,
    val maxNoiseToleranceDb: Int = 45,
    val maxDistanceRadiusKm: Int = 8,
    val isNotificationsEnabled: Boolean = true,
    val themeMode: String = "SYSTEM", // "LIGHT", "DARK", "SYSTEM"
    val totalPeacefulMinutes: Int = 340,
    val totalSavedPlacesCount: Int = 0,
    val totalVisitsCount: Int = 0
)
