package com.calmpath.ai.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.calmpath.ai.data.local.entities.UserPreferencesEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for UserPreferencesEntity operations.
 */
@Dao
interface UserPreferencesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(preferences: UserPreferencesEntity)

    @Update
    suspend fun updatePreferences(preferences: UserPreferencesEntity)

    @Delete
    suspend fun deletePreferences(preferences: UserPreferencesEntity)

    @Query("SELECT * FROM user_preferences WHERE userId = :userId LIMIT 1")
    suspend fun getPreferencesForUser(userId: String): UserPreferencesEntity?

    @Query("SELECT * FROM user_preferences WHERE userId = :userId LIMIT 1")
    fun getPreferencesFlowForUser(userId: String): Flow<UserPreferencesEntity?>

    @Query("SELECT * FROM user_preferences LIMIT 1")
    suspend fun getDefaultPreferences(): UserPreferencesEntity?

    @Query("SELECT * FROM user_preferences LIMIT 1")
    fun getDefaultPreferencesFlow(): Flow<UserPreferencesEntity?>

    @Query("UPDATE user_preferences SET preferredMood = :mood WHERE userId = :userId")
    suspend fun updatePreferredMood(userId: String, mood: String)

    @Query("UPDATE user_preferences SET maxAQI = :maxAqi, maxNoiseLevel = :noiseLevel, maxDistance = :distanceKm WHERE userId = :userId")
    suspend fun updateEnvironmentalTolerances(userId: String, maxAqi: Int, noiseLevel: Int, distanceKm: Double)
}
