package com.calmpath.ai.data.repository

import com.calmpath.ai.data.local.dao.MoodHistoryDao
import com.calmpath.ai.data.local.entities.MoodHistoryEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * Repository for MoodHistoryEntity operations.
 */
class MoodRepository(
    private val moodHistoryDao: MoodHistoryDao
) {

    fun getMoodHistoryFlow(userId: String): Flow<List<MoodHistoryEntity>> =
        moodHistoryDao.getMoodHistoryForUser(userId)

    fun getLatestMoodFlow(userId: String): Flow<MoodHistoryEntity?> =
        moodHistoryDao.getLatestMoodForUser(userId)

    suspend fun getLatestMood(userId: String): MoodHistoryEntity? =
        moodHistoryDao.getLatestMood(userId)

    suspend fun recordMood(
        userId: String,
        mood: String,
        recommendedPlaceId: String? = null,
        selectedPlaceId: String? = null
    ) {
        moodHistoryDao.insertMoodHistory(
            MoodHistoryEntity(
                moodHistoryId = UUID.randomUUID().toString(),
                userId = userId,
                mood = mood,
                selectedAt = System.currentTimeMillis(),
                recommendedPlaceId = recommendedPlaceId,
                selectedPlaceId = selectedPlaceId
            )
        )
    }

    suspend fun clearMoodHistory(userId: String) {
        moodHistoryDao.clearMoodHistoryForUser(userId)
    }

    fun getMoodLogCountFlow(userId: String): Flow<Int> =
        moodHistoryDao.getMoodLogCountFlow(userId)
}
