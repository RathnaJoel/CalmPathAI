package com.calmpath.ai.data.repository

import com.calmpath.ai.data.local.dao.AppSettingsDao
import com.calmpath.ai.data.local.entities.AppSettingsEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repository for AppSettingsEntity operations.
 */
class SettingsRepository(
    private val appSettingsDao: AppSettingsDao
) {

    fun getSettingsFlow(userId: String): Flow<AppSettingsEntity?> =
        appSettingsDao.getSettingsFlowForUser(userId)

    fun getDefaultSettingsFlow(): Flow<AppSettingsEntity?> =
        appSettingsDao.getDefaultSettingsFlow()

    suspend fun getSettings(userId: String): AppSettingsEntity? =
        appSettingsDao.getSettingsForUser(userId)

    suspend fun updateTheme(userId: String, theme: String) {
        appSettingsDao.updateTheme(userId, theme)
    }

    suspend fun updateNotifications(userId: String, enabled: Boolean) {
        appSettingsDao.updateNotifications(userId, enabled)
    }

    suspend fun updateUnits(userId: String, distanceUnit: String, tempUnit: String) {
        appSettingsDao.updateUnits(userId, distanceUnit, tempUnit)
    }

    suspend fun updateSettings(settings: AppSettingsEntity) {
        appSettingsDao.insertOrUpdate(settings)
    }
}
