package com.calmpath.ai.data.model

/**
 * Domain model representing a peaceful place / sanctuary.
 */
data class Place(
    val id: String,
    val name: String,
    val category: String, // "Parks", "Lakes", "Cafes", "Libraries", "Meditation", "Fitness"
    val categoryIcon: String,
    val latitude: Double,
    val longitude: Double,
    val distanceKm: Double,
    val peaceScore: Int, // 0..100
    val aqi: Int,
    val noiseDb: Int,
    val temperatureC: Int = 22,
    val weatherCondition: String = "Clear & Mild",
    val imageUrl: String,
    val address: String,
    val description: String,
    val recommendationReasons: List<String>,
    val suitableMoods: List<Mood> = listOf(Mood.RELAX),
    val openHours: String = "6:00 AM – 9:00 PM",
    val crowdLevel: String = "Low Crowd",
    val greenDensityPercent: Int = 85
) {
    val aqiCategory: AqiCategory
        get() = AqiCategory.fromAqi(aqi)

    val noiseCategory: NoiseCategory
        get() = NoiseCategory.fromDecibels(noiseDb)
}
