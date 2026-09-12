package com.calmpath.ai.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.calmpath.ai.data.local.entities.RecommendationInteractionEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for RecommendationInteractionEntity (CO9).
 */
@Dao
interface RecommendationInteractionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInteraction(interaction: RecommendationInteractionEntity)

    @Query("SELECT * FROM recommendation_interactions WHERE userId = :userId ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentInteractions(userId: String, limit: Int = 50): List<RecommendationInteractionEntity>

    @Query("SELECT * FROM recommendation_interactions WHERE userId = :userId ORDER BY timestamp DESC")
    fun getInteractionsFlow(userId: String): Flow<List<RecommendationInteractionEntity>>

    @Query("SELECT AVG(userRating) FROM recommendation_interactions WHERE placeId = :placeId AND userRating IS NOT NULL")
    suspend fun getAverageUserRating(placeId: String): Double?

    @Query("SELECT COUNT(*) FROM recommendation_interactions")
    suspend fun getInteractionCount(): Int

    @Query("DELETE FROM recommendation_interactions WHERE userId = :userId")
    suspend fun clearInteractionsForUser(userId: String)
}
