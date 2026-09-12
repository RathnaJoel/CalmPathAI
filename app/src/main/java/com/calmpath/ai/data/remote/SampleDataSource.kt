package com.calmpath.ai.data.remote

import com.calmpath.ai.data.model.CalmnessLevel
import com.calmpath.ai.data.model.HeatmapZone
import com.calmpath.ai.data.model.Mood
import com.calmpath.ai.data.model.Place

/**
 * Rich realistic sample data source representing peaceful sanctuaries,
 * environmental metrics, and heatmap zones for CalmPath AI.
 */
object SampleDataSource {

    val places: List<Place> = com.calmpath.ai.data.local.DatabaseSeeder.samplePlaces.map { it.toDomainModel() }

    val categories: List<String> = listOf(
        "All",
        "Parks",
        "Lakes",
        "Cafes",
        "Libraries",
        "Meditation",
        "Fitness"
    )

    val environmentalSummary = com.calmpath.ai.data.model.EnvironmentalSummary(
        aqi = 36,
        noiseDb = 38,
        peaceScore = 93,
        temperatureC = 26,
        weatherCondition = "Partly Cloudy",
        humidityPercent = 68,
        localityName = "Mumbai, Maharashtra"
    )

    val heatmapZones: List<HeatmapZone> = listOf(
        HeatmapZone(
            id = "zone_hanging_gardens",
            title = "Hanging Gardens Sanctuary",
            relativeX = 0.28f,
            relativeY = 0.35f,
            radiusPx = 95f,
            calmnessLevel = CalmnessLevel.EXCELLENT,
            avgDecibels = 38,
            avgAqi = 42,
            associatedPlaceId = "place_1"
        ),
        HeatmapZone(
            id = "zone_marine_drive",
            title = "Marine Drive Waterfront Bay",
            relativeX = 0.65f,
            relativeY = 0.25f,
            radiusPx = 110f,
            calmnessLevel = CalmnessLevel.GOOD,
            avgDecibels = 44,
            avgAqi = 48,
            associatedPlaceId = "place_2"
        ),
        HeatmapZone(
            id = "zone_asiatic_library",
            title = "Asiatic Society Heritage Quad",
            relativeX = 0.78f,
            relativeY = 0.58f,
            radiusPx = 80f,
            calmnessLevel = CalmnessLevel.EXCELLENT,
            avgDecibels = 28,
            avgAqi = 38,
            associatedPlaceId = "place_3"
        ),
        HeatmapZone(
            id = "zone_vipassana",
            title = "Global Vipassana Peace Dome",
            relativeX = 0.42f,
            relativeY = 0.52f,
            radiusPx = 85f,
            calmnessLevel = CalmnessLevel.EXCELLENT,
            avgDecibels = 22,
            avgAqi = 24,
            associatedPlaceId = "place_6"
        ),
        HeatmapZone(
            id = "zone_western_express",
            title = "Western Express Corridor (High Noise/Traffic)",
            relativeX = 0.50f,
            relativeY = 0.75f,
            radiusPx = 100f,
            calmnessLevel = CalmnessLevel.POOR,
            avgDecibels = 78,
            avgAqi = 125,
            associatedPlaceId = null
        ),
        HeatmapZone(
            id = "zone_dadar_junction",
            title = "Central Transit Interchange (Congested)",
            relativeX = 0.25f,
            relativeY = 0.72f,
            radiusPx = 75f,
            calmnessLevel = CalmnessLevel.MODERATE_POOR,
            avgDecibels = 68,
            avgAqi = 85,
            associatedPlaceId = null
        ),
        HeatmapZone(
            id = "zone_sanjay_gandhi",
            title = "Sanjay Gandhi National Park Canopy",
            relativeX = 0.18f,
            relativeY = 0.18f,
            radiusPx = 90f,
            calmnessLevel = CalmnessLevel.EXCELLENT,
            avgDecibels = 32,
            avgAqi = 26,
            associatedPlaceId = "place_5"
        )
    )
}
