package com.calmpath.ai.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.calmpath.ai.data.local.entities.MoodHistoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for MoodHistoryEntity operations.
 */
@Dao
interface MoodHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMoodHistory(moodRecord: MoodHistoryEntity)

    @Update
    suspend fun updateMoodHistory(moodRecord: MoodHistoryEntity)

    @Delete
    suspend fun deleteMoodHistory(moodRecord: MoodHistoryEntity)

    @Query("SELECT * FROM mood_history WHERE userId = :userId ORDER BY selectedAt DESC")
    fun getMoodHistoryForUser(userId: String): Flow<List<MoodHistoryEntity>>

    @Query("SELECT * FROM mood_history WHERE userId = :userId ORDER BY selectedAt DESC LIMIT 1")
    fun getLatestMoodForUser(userId: String): Flow<MoodHistoryEntity?>

    @Query("SELECT * FROM mood_history WHERE userId = :userId ORDER BY selectedAt DESC LIMIT 1")
    suspend fun getLatestMood(userId: String): MoodHistoryEntity?

    @Query("DELETE FROM mood_history WHERE userId = :userId")
    suspend fun clearMoodHistoryForUser(userId: String)

    @Query("SELECT COUNT(*) FROM mood_history WHERE userId = :userId")
    fun getMoodLogCountFlow(userId: String): Flow<Int>
}
