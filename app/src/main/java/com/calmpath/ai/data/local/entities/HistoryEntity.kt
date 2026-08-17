package com.calmpath.ai.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Room Entity for places previously viewed or visited by user (CO3).
 */
@Entity(tableName = "history_entries")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val placeId: String,
    val placeName: String,
    val category: String,
    val categoryIcon: String = "🌿",
    val viewedAt: Long = System.currentTimeMillis(),
    val peaceScore: Int,
    val aqi: Int,
    val noiseLevel: Int,
    val distance: Double = 1.0,
    val imageUrl: String = "",
    val address: String = ""
) {
    val formattedDate: String
        get() {
            val sdf = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault())
            return sdf.format(Date(viewedAt))
        }
}
