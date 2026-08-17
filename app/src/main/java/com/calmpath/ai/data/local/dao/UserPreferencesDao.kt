package com.calmpath.ai.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.calmpath.ai.data.local.entities.UserPreferencesEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for User Preferences (CO3).
 */
@Dao
interface UserPreferencesDao {

    @Query("SELECT * FROM user_preferences WHERE id = 1 LIMIT 1")
    fun getPreferencesFlow(): Flow<UserPreferencesEntity?>

    @Query("SELECT * FROM user_preferences WHERE id = 1 LIMIT 1")
    suspend fun getPreferences(): UserPreferencesEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(preferences: UserPreferencesEntity)

    @Query("UPDATE user_preferences SET selectedMood = :mood WHERE id = 1")
    suspend fun updateSelectedMood(mood: String)

    @Query("UPDATE user_preferences SET themeMode = :theme WHERE id = 1")
    suspend fun updateThemeMode(theme: String)

    @Query("UPDATE user_preferences SET notificationsEnabled = :enabled WHERE id = 1")
    suspend fun updateNotifications(enabled: Boolean)

    @Query("UPDATE user_preferences SET maxAqi = :maxAqi, preferredNoiseLevel = :noiseLevel, preferredDistanceKm = :distanceKm WHERE id = 1")
    suspend fun updateEnvironmentalPreferences(maxAqi: Int, noiseLevel: Int, distanceKm: Int)
}
