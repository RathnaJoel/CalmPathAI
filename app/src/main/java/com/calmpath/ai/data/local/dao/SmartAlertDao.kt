package com.calmpath.ai.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.calmpath.ai.data.local.entities.SmartAlertEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for SmartAlertEntity (CO10).
 */
@Dao
interface SmartAlertDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: SmartAlertEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlerts(alerts: List<SmartAlertEntity>)

    @Query("SELECT * FROM smart_alerts WHERE userId = :userId ORDER BY createdAt DESC")
    fun getUserAlertsFlow(userId: String): Flow<List<SmartAlertEntity>>

    @Query("SELECT * FROM smart_alerts WHERE userId = :userId ORDER BY createdAt DESC")
    suspend fun getUserAlerts(userId: String): List<SmartAlertEntity>

    @Query("SELECT * FROM smart_alerts WHERE userId = :userId AND isRead = 0 ORDER BY createdAt DESC")
    fun getUnreadAlertsFlow(userId: String): Flow<List<SmartAlertEntity>>

    @Query("SELECT COUNT(*) FROM smart_alerts WHERE userId = :userId AND isRead = 0")
    fun getUnreadCountFlow(userId: String): Flow<Int>

    @Query("UPDATE smart_alerts SET isRead = 1 WHERE alertId = :alertId")
    suspend fun markAlertAsRead(alertId: String)

    @Query("UPDATE smart_alerts SET isRead = 1 WHERE userId = :userId")
    suspend fun markAllAsRead(userId: String)

    @Query("UPDATE smart_alerts SET isActionTaken = 1 WHERE alertId = :alertId")
    suspend fun markActionTaken(alertId: String)

    @Query("SELECT * FROM smart_alerts WHERE userId = :userId AND alertType = :alertType AND createdAt >= :sinceTimestamp ORDER BY createdAt DESC LIMIT 1")
    suspend fun checkRecentDuplicateAlert(userId: String, alertType: String, sinceTimestamp: Long): SmartAlertEntity?

    @Query("SELECT * FROM smart_alerts WHERE userId = :userId AND alertType = :alertType ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLatestAlertByType(userId: String, alertType: String): SmartAlertEntity?

    @Query("DELETE FROM smart_alerts WHERE userId = :userId")
    suspend fun clearAllAlerts(userId: String)

    @Query("DELETE FROM smart_alerts WHERE createdAt < :beforeTimestamp")
    suspend fun deleteOldAlerts(beforeTimestamp: Long)
}
