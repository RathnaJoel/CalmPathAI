package com.calmpath.ai.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Entity 7: EnvironmentalSnapshot
 * Stores environmental conditions for a place at a particular time (AQI + noise dB + temperature + humidity + weather + peace score).
 */
@Entity(
    tableName = "environmental_snapshots",
    foreignKeys = [
        ForeignKey(
            entity = PlaceEntity::class,
            parentColumns = ["placeId"],
            childColumns = ["placeId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["placeId"]),
        Index(value = ["recordedAt"])
    ]
)
data class EnvironmentalSnapshotEntity(
    @PrimaryKey
    val snapshotId: String = UUID.randomUUID().toString(),
    val placeId: String,
    val aqi: Int,
    val noiseLevelDb: Int,
    val temperature: Double,
    val humidity: Int,
    val weatherCondition: String, // "Clear", "Partly Cloudy", "Breezy", "Sunny", "Overcast"
    val peaceScore: Int,
    val recordedAt: Long = System.currentTimeMillis()
)
