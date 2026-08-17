package com.calmpath.ai.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.calmpath.ai.data.local.entities.HistoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Browsing History (CO3).
 */
@Dao
interface HistoryDao {

    @Query("SELECT * FROM history_entries ORDER BY viewedAt DESC")
    fun getAllHistory(): Flow<List<HistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(entry: HistoryEntity)

    @Delete
    suspend fun deleteHistory(entry: HistoryEntity)

    @Query("DELETE FROM history_entries WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM history_entries")
    suspend fun clearAllHistory()

    @Query("SELECT COUNT(*) FROM history_entries")
    fun getHistoryCount(): Flow<Int>
}
