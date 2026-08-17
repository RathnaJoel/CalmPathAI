package com.calmpath.ai.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.calmpath.ai.data.model.Place

/**
 * Room Entity for saved/favorited peaceful places (CO3).
 */
@Entity(tableName = "favorite_places")
data class FavoritePlaceEntity(
    @PrimaryKey
    val id: String,
    val placeName: String,
    val category: String,
    val categoryIcon: String = "🌿",
    val latitude: Double,
    val longitude: Double,
    val peaceScore: Int,
    val aqi: Int,
    val noiseLevel: Int,
    val distance: Double,
    val imageUrl: String,
    val address: String = "",
    val description: String = "",
    val recommendationReasons: List<String> = emptyList(),
    val savedAtTimestamp: Long = System.currentTimeMillis(),
    val isSyncedWithCloud: Boolean = false
) {
    fun toPlace(): Place {
        return Place(
            id = id,
            name = placeName,
            category = category,
            categoryIcon = categoryIcon,
            latitude = latitude,
            longitude = longitude,
            distanceKm = distance,
            peaceScore = peaceScore,
            aqi = aqi,
            noiseDb = noiseLevel,
            imageUrl = imageUrl,
            address = address,
            description = description,
            recommendationReasons = recommendationReasons
        )
    }

    companion object {
        fun fromPlace(place: Place, isSynced: Boolean = false): FavoritePlaceEntity {
            return FavoritePlaceEntity(
                id = place.id,
                placeName = place.name,
                category = place.category,
                categoryIcon = place.categoryIcon,
                latitude = place.latitude,
                longitude = place.longitude,
                peaceScore = place.peaceScore,
                aqi = place.aqi,
                noiseLevel = place.noiseDb,
                distance = place.distanceKm,
                imageUrl = place.imageUrl,
                address = place.address,
                description = place.description,
                recommendationReasons = place.recommendationReasons,
                isSyncedWithCloud = isSynced
            )
        }
    }
}
