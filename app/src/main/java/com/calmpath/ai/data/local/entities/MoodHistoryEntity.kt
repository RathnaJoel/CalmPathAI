package com.calmpath.ai.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Entity 6: MoodHistory
 * Stores the user's selected moods over time and recommended/selected places.
 */
@Entity(
    tableName = "mood_history",
    foreignKeys = [
        ForeignKey(
            entity = UserProfileEntity::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PlaceEntity::class,
            parentColumns = ["placeId"],
            childColumns = ["recommendedPlaceId"],
            onDelete = ForeignKey.SET_NULL,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PlaceEntity::class,
            parentColumns = ["placeId"],
            childColumns = ["selectedPlaceId"],
            onDelete = ForeignKey.SET_NULL,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["userId"]),
        Index(value = ["recommendedPlaceId"]),
        Index(value = ["selectedPlaceId"]),
        Index(value = ["selectedAt"])
    ]
)
data class MoodHistoryEntity(
    @PrimaryKey
    val moodHistoryId: String = UUID.randomUUID().toString(),
    val userId: String,
    val mood: String, // "Relax", "Meditate", "Study", "Exercise", "Fresh Air", "Quiet Time"
    val selectedAt: Long = System.currentTimeMillis(),
    val recommendedPlaceId: String? = null,
    val selectedPlaceId: String? = null
)
