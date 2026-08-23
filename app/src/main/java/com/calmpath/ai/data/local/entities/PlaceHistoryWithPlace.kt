package com.calmpath.ai.data.local.entities

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Relational model combining PlaceHistoryEntity with its associated PlaceEntity.
 */
data class PlaceHistoryWithPlace(
    @Embedded
    val history: PlaceHistoryEntity,

    @Relation(
        parentColumn = "placeId",
        entityColumn = "placeId"
    )
    val place: PlaceEntity
)
