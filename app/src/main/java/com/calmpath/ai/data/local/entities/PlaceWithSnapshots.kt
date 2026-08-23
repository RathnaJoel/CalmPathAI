package com.calmpath.ai.data.local.entities

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Relational model combining PlaceEntity with its latest EnvironmentalSnapshotEntity records.
 */
data class PlaceWithSnapshots(
    @Embedded
    val place: PlaceEntity,

    @Relation(
        parentColumn = "placeId",
        entityColumn = "placeId"
    )
    val snapshots: List<EnvironmentalSnapshotEntity>
)
