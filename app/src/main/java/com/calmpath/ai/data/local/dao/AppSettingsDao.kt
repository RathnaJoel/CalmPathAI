package com.calmpath.ai.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.calmpath.ai.data.local.entities.AppSettingsEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for AppSettingsEntity operations.
 */
@Dao
interface AppSettingsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(settings: AppSettingsEntity)

    @Update
    suspend fun updateSettings(settings: AppSettingsEntity)

    @Delete
    suspend fun deleteSettings(settings: AppSettingsEntity)

    @Query("SELECT * FROM app_settings WHERE userId = :userId LIMIT 1")
    suspend fun getSettingsForUser(userId: String): AppSettingsEntity?

    @Query("SELECT * FROM app_settings WHERE userId = :userId LIMIT 1")
    fun getSettingsFlowForUser(userId: String): Flow<AppSettingsEntity?>

    @Query("SELECT * FROM app_settings LIMIT 1")
    suspend fun getDefaultSettings(): AppSettingsEntity?

    @Query("SELECT * FROM app_settings LIMIT 1")
    fun getDefaultSettingsFlow(): Flow<AppSettingsEntity?>

    @Query("UPDATE app_settings SET theme = :theme WHERE userId = :userId")
    suspend fun updateTheme(userId: String, theme: String)

    @Query("UPDATE app_settings SET notificationsEnabled = :enabled WHERE userId = :userId")
    suspend fun updateNotifications(userId: String, enabled: Boolean)

    @Query("UPDATE app_settings SET distanceUnit = :distanceUnit, temperatureUnit = :tempUnit WHERE userId = :userId")
    suspend fun updateUnits(userId: String, distanceUnit: String, tempUnit: String)
}
