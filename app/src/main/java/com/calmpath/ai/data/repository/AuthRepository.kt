package com.calmpath.ai.data.repository

import com.calmpath.ai.data.auth.AuthManager
import com.calmpath.ai.data.auth.AuthState
import com.calmpath.ai.data.local.CalmPathDatabase
import com.calmpath.ai.data.local.entities.AppSettingsEntity
import com.calmpath.ai.data.local.entities.UserPreferencesEntity
import com.calmpath.ai.data.local.entities.UserProfileEntity
import com.calmpath.ai.data.model.UserProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Authentication Repository handling user credentials, sessions, and profile data (CO4).
 * Synchronizes authenticated users into the Room local database (CO3).
 */
class AuthRepository(
    private val authManager: AuthManager,
    private val database: CalmPathDatabase? = null,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    val authState: StateFlow<AuthState> = authManager.authState
    val currentUser: StateFlow<UserProfile?> = authManager.currentUser

    private fun syncUserToRoom(user: UserProfile) {
        val db = database ?: return
        scope.launch {
            try {
                val profileDao = db.userProfileDao()
                val prefDao = db.userPreferencesDao()
                val settingsDao = db.appSettingsDao()

                // Upsert UserProfile in Room
                profileDao.insertUser(
                    UserProfileEntity(
                        userId = user.uid,
                        name = user.displayName,
                        email = user.email,
                        profileImage = user.photoUrl,
                        createdAt = System.currentTimeMillis(),
                        lastLogin = System.currentTimeMillis()
                    )
                )

                // Ensure default preferences exist
                if (prefDao.getPreferencesForUser(user.uid) == null) {
                    prefDao.insertOrUpdate(
                        UserPreferencesEntity(
                            preferenceId = "pref_${user.uid}",
                            userId = user.uid,
                            preferredMood = "Relax",
                            preferredCategory = "All",
                            maxDistance = 10.0,
                            maxAQI = 60,
                            maxNoiseLevel = 45,
                            preferredTemperature = 22.0
                        )
                    )
                }

                // Ensure default settings exist
                if (settingsDao.getSettingsForUser(user.uid) == null) {
                    settingsDao.insertOrUpdate(
                        AppSettingsEntity(
                            settingsId = "settings_${user.uid}",
                            userId = user.uid,
                            theme = "SYSTEM",
                            notificationsEnabled = true,
                            locationEnabled = true,
                            distanceUnit = "km",
                            temperatureUnit = "°C",
                            soundUnit = "dB"
                        )
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("AuthRepository", "Error syncing user to Room: ${e.message}", e)
            }
        }
    }

    suspend fun signIn(email: String, pass: String): Result<UserProfile> {
        val result = authManager.signIn(email, pass)
        result.onSuccess { user -> syncUserToRoom(user) }
        return result
    }

    suspend fun signUp(name: String, email: String, pass: String): Result<UserProfile> {
        val result = authManager.signUp(name, email, pass)
        result.onSuccess { user -> syncUserToRoom(user) }
        return result
    }

    fun signInAsGuest(name: String = "Calm Traveler", email: String = "guest@calmpath.ai") {
        authManager.signInAsDemoGuest(name, email)
        authManager.currentUser.value?.let { user ->
            syncUserToRoom(user)
        }
    }

    fun signOut() {
        authManager.signOut()
    }
}
