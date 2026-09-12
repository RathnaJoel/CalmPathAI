package com.calmpath.ai.ml

import com.calmpath.ai.data.local.entities.UserPreferencesEntity
import com.calmpath.ai.data.model.Mood
import com.calmpath.ai.data.model.Place
import java.util.Calendar

/**
 * Raw feature representation before numerical encoding for CO9 Machine Learning inference.
 */
data class RawPlaceFeatures(
    val aqi: Double,
    val pm25: Double,
    val pm10: Double,
    val noiseDb: Double,
    val temperature: Double,
    val humidity: Double,
    val weatherCondition: String,
    val distanceKm: Double,
    val placeCategory: String,
    val placeRating: Double,
    val timeOfDay: Double,
    val dayOfWeek: Double,
    val userMood: String,
    val preferredCategory: String,
    val maxDistance: Double,
    val userPreviousRating: Double
)

/**
 * Responsible for constructing the structured feature vector from CalmPath AI entities (CO9).
 */
object PlaceFeatureBuilder {

    /**
     * Builds a [RawPlaceFeatures] instance for a candidate [Place] given user preferences, mood, and history.
     */
    fun buildFeatures(
        place: Place,
        mood: Mood,
        userPreferences: UserPreferencesEntity? = null,
        userPreviousRating: Double = 0.0,
        calendar: Calendar = Calendar.getInstance()
    ): RawPlaceFeatures {
        val aqiVal = place.aqi.toDouble()
        // Compute PM2.5 and PM10 proxies if not individually measured
        val pm25Val = (aqiVal * 0.52).coerceIn(5.0, 300.0)
        val pm10Val = (pm25Val * 1.85).coerceIn(10.0, 500.0)

        val hour = calendar.get(Calendar.HOUR_OF_DAY).toDouble()
        // Day of week: Calendar returns 1 for Sunday, 2 for Monday, etc.
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK).toDouble()

        val maxDist = userPreferences?.maxDistance ?: 10.0
        val prefCategory = userPreferences?.preferredCategory ?: "All"

        return RawPlaceFeatures(
            aqi = aqiVal,
            pm25 = pm25Val,
            pm10 = pm10Val,
            noiseDb = place.noiseDb.toDouble(),
            temperature = place.temperatureC.toDouble(),
            humidity = 55.0, // Default comfortable ambient humidity
            weatherCondition = place.weatherCondition,
            distanceKm = place.distanceKm,
            placeCategory = place.category,
            placeRating = 4.6, // Standard sanctuary baseline rating
            timeOfDay = hour,
            dayOfWeek = dayOfWeek,
            userMood = mood.title,
            preferredCategory = prefCategory,
            maxDistance = maxDist,
            userPreviousRating = userPreviousRating
        )
    }
}
