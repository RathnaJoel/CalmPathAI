package com.calmpath.ai.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.calmpath.ai.data.local.entities.PlaceHistoryEntity
import com.calmpath.ai.data.local.entities.PlaceHistoryWithPlace
import kotlinx.coroutines.flow.Flow

/**
 * DAO for PlaceHistoryEntity operations.
 */
@Dao
interface PlaceHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: PlaceHistoryEntity)

    @Update
    suspend fun updateHistory(history: PlaceHistoryEntity)

    @Delete
    suspend fun deleteHistory(history: PlaceHistoryEntity)

    @Query("DELETE FROM place_history WHERE userId = :userId")
    suspend fun clearHistoryForUser(userId: String)

    @Transaction
    @Query("SELECT * FROM place_history WHERE userId = :userId ORDER BY viewedAt DESC")
    fun getHistoryWithPlaces(userId: String): Flow<List<PlaceHistoryWithPlace>>

    @Transaction
    @Query("SELECT * FROM place_history ORDER BY viewedAt DESC")
    fun getAllHistoryWithPlaces(): Flow<List<PlaceHistoryWithPlace>>

    @Query("SELECT COUNT(*) FROM place_history WHERE userId = :userId")
    fun getHistoryCountFlow(userId: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM place_history WHERE userId = :userId")
    suspend fun getHistoryCount(userId: String): Int
}
