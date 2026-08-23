package com.calmpath.ai.data.local.entities

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Relational model combining a FavoritePlaceEntity with its associated PlaceEntity.
 */
data class FavoriteWithPlace(
    @Embedded
    val favorite: FavoritePlaceEntity,

    @Relation(
        parentColumn = "placeId",
        entityColumn = "placeId"
    )
    val place: PlaceEntity
)
