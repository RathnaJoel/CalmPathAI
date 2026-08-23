package com.calmpath.ai.data.repository

import com.calmpath.ai.data.local.dao.PlaceHistoryDao
import com.calmpath.ai.data.local.entities.PlaceHistoryEntity
import com.calmpath.ai.data.local.entities.PlaceHistoryWithPlace
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * Repository for PlaceHistoryEntity operations.
 */
class HistoryRepository(
    private val placeHistoryDao: PlaceHistoryDao
) {

    fun getHistoryWithPlaces(userId: String): Flow<List<PlaceHistoryWithPlace>> =
        placeHistoryDao.getHistoryWithPlaces(userId)

    suspend fun recordPlaceView(
        userId: String,
        placeId: String,
        peaceScore: Int,
        aqi: Int,
        noiseLevel: Int
    ) {
        placeHistoryDao.insertHistory(
            PlaceHistoryEntity(
                historyId = UUID.randomUUID().toString(),
                userId = userId,
                placeId = placeId,
                viewedAt = System.currentTimeMillis(),
                peaceScoreAtVisit = peaceScore,
                aqiAtVisit = aqi,
                noiseLevelAtVisit = noiseLevel
            )
        )
    }

    suspend fun clearHistory(userId: String) {
        placeHistoryDao.clearHistoryForUser(userId)
    }

    fun getHistoryCountFlow(userId: String): Flow<Int> =
        placeHistoryDao.getHistoryCountFlow(userId)
}
