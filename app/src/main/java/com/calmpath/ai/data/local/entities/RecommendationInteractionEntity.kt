package com.calmpath.ai.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Entity 10: RecommendationInteractionEntity (CO9: ML User Feedback Loop).
 * Records non-sensitive interaction signals (viewed, selected, navigated, favorited, rated)
 * to allow continuous evaluation and future model retraining (Model v2).
 */
@Entity(
    tableName = "recommendation_interactions",
    indices = [
        Index(value = ["userId"]),
        Index(value = ["placeId"]),
        Index(value = ["timestamp"])
    ]
)
data class RecommendationInteractionEntity(
    @PrimaryKey
    val interactionId: String = UUID.randomUUID().toString(),
    val userId: String = "guest",
    val placeId: String,
    val mood: String = "Relax",
    val mlScore: Int,
    val action: String, // "VIEWED", "SELECTED", "NAVIGATED", "FAVORITED", "REJECTED"
    val userRating: Int? = null, // Optional explicit user rating (1..5)
    val timestamp: Long = System.currentTimeMillis()
) {
    companion object {
        const val ACTION_VIEWED = "VIEWED"
        const val ACTION_SELECTED = "SELECTED"
        const val ACTION_NAVIGATED = "NAVIGATED"
        const val ACTION_FAVORITED = "FAVORITED"
        const val ACTION_REJECTED = "REJECTED"
    }
}
