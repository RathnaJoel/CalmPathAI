package com.calmpath.ai.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.calmpath.ai.data.model.Mood
import com.calmpath.ai.data.model.Place

/**
 * Entity 3: Place
 * Central entity containing information about places/sanctuaries displayed in CalmPath.
 */
@Entity(
    tableName = "places",
    indices = [
        Index(value = ["category"]),
        Index(value = ["peaceScore"])
    ]
)
data class PlaceEntity(
    @PrimaryKey
    val placeId: String,
    val name: String,
    val category: String, // Parks, Lakes, Cafes, Libraries, Meditation, Fitness
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val description: String,
    val imageUrl: String,
    val averageAQI: Int,
    val averageNoiseLevel: Int, // dB
    val peaceScore: Int // 0 - 100
) {
    fun toDomainModel(snapshot: EnvironmentalSnapshotEntity? = null): Place {
        val categoryEmoji = when (category.lowercase()) {
            "parks" -> "🌿"
            "lakes" -> "🌊"
            "libraries" -> "📚"
            "cafes" -> "☕"
            "meditation" -> "🧘"
            "fitness" -> "🏃"
            else -> "📍"
        }
        val currentAqi = snapshot?.aqi ?: averageAQI
        val currentNoise = snapshot?.noiseLevelDb ?: averageNoiseLevel
        val currentPeaceScore = snapshot?.peaceScore ?: peaceScore

        return Place(
            id = placeId,
            name = name,
            category = category,
            categoryIcon = categoryEmoji,
            latitude = latitude,
            longitude = longitude,
            distanceKm = 1.5,
            peaceScore = currentPeaceScore,
            aqi = currentAqi,
            noiseDb = currentNoise,
            temperatureC = (snapshot?.temperature ?: 22.0).toInt(),
            weatherCondition = snapshot?.weatherCondition ?: "Pleasant & Clear",
            imageUrl = imageUrl,
            address = address,
            description = description,
            recommendationReasons = listOf(
                "🌿 Whisper-quiet ambient sound ($currentNoise dB)",
                "✨ Clean atmospheric air with AQI $currentAqi",
                "🧘 High Peace Score of $currentPeaceScore/100"
            ),
            suitableMoods = when (category.lowercase()) {
                "parks" -> listOf(Mood.RELAX, Mood.FRESH_AIR)
                "lakes" -> listOf(Mood.RELAX, Mood.QUIET_TIME)
                "libraries" -> listOf(Mood.STUDY, Mood.QUIET_TIME)
                "cafes" -> listOf(Mood.STUDY, Mood.QUIET_TIME)
                "meditation" -> listOf(Mood.MEDITATE, Mood.RELAX)
                "fitness" -> listOf(Mood.EXERCISE, Mood.FRESH_AIR)
                else -> listOf(Mood.RELAX)
            }
        )
    }

    companion object {
        fun fromDomainModel(place: Place): PlaceEntity {
            return PlaceEntity(
                placeId = place.id,
                name = place.name,
                category = place.category,
                address = place.address,
                latitude = place.latitude,
                longitude = place.longitude,
                description = place.description,
                imageUrl = place.imageUrl,
                averageAQI = place.aqi,
                averageNoiseLevel = place.noiseDb,
                peaceScore = place.peaceScore
            )
        }
    }
}
