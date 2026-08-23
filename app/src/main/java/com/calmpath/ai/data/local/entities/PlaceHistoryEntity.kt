package com.calmpath.ai.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Entity 5: PlaceHistory
 * Stores places that the user has viewed or selected along with environmental conditions at visit time.
 */
@Entity(
    tableName = "place_history",
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
            childColumns = ["placeId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["userId"]),
        Index(value = ["placeId"]),
        Index(value = ["viewedAt"])
    ]
)
data class PlaceHistoryEntity(
    @PrimaryKey
    val historyId: String = UUID.randomUUID().toString(),
    val userId: String,
    val placeId: String,
    val viewedAt: Long = System.currentTimeMillis(),
    val peaceScoreAtVisit: Int,
    val aqiAtVisit: Int,
    val noiseLevelAtVisit: Int
)
