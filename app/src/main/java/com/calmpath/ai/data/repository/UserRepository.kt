package com.calmpath.ai.data.repository

import com.calmpath.ai.data.local.dao.UserPreferencesDao
import com.calmpath.ai.data.local.dao.UserProfileDao
import com.calmpath.ai.data.local.entities.UserPreferencesEntity
import com.calmpath.ai.data.local.entities.UserProfileEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repository for UserProfileEntity and UserPreferencesEntity operations.
 */
class UserRepository(
    private val userProfileDao: UserProfileDao,
    private val userPreferencesDao: UserPreferencesDao
) {

    fun getUserFlow(userId: String): Flow<UserProfileEntity?> = userProfileDao.getUserFlow(userId)

    fun getActiveUserFlow(): Flow<UserProfileEntity?> = userProfileDao.getActiveUserFlow()

    suspend fun getUser(userId: String): UserProfileEntity? = userProfileDao.getUserById(userId)

    suspend fun getActiveUser(): UserProfileEntity? = userProfileDao.getActiveUser()

    suspend fun insertOrUpdateUser(user: UserProfileEntity) {
        userProfileDao.insertUser(user)
    }

    suspend fun updateLastLogin(userId: String) {
        userProfileDao.updateLastLogin(userId)
    }

    fun getPreferencesFlow(userId: String): Flow<UserPreferencesEntity?> =
        userPreferencesDao.getPreferencesFlowForUser(userId)

    fun getDefaultPreferencesFlow(): Flow<UserPreferencesEntity?> =
        userPreferencesDao.getDefaultPreferencesFlow()

    suspend fun getPreferences(userId: String): UserPreferencesEntity? =
        userPreferencesDao.getPreferencesForUser(userId)

    suspend fun updatePreferences(preferences: UserPreferencesEntity) {
        userPreferencesDao.insertOrUpdate(preferences)
    }

    suspend fun updatePreferredMood(userId: String, mood: String) {
        userPreferencesDao.updatePreferredMood(userId, mood)
    }

    suspend fun updateEnvironmentalTolerances(
        userId: String,
        maxAqi: Int,
        noiseLevel: Int,
        distanceKm: Double
    ) {
        userPreferencesDao.updateEnvironmentalTolerances(userId, maxAqi, noiseLevel, distanceKm)
    }
}
