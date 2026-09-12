package com.calmpath.ai.data.model

/**
 * Domain model representing a turn-by-turn pedestrian walking route (CO6).
 */
data class WalkingRouteData(
    val coordinates: List<Pair<Double, Double>>,
    val distanceText: String,
    val durationText: String,
    val distanceMeters: Long,
    val durationSeconds: Long,
    val stepInstructions: List<String> = emptyList(),
    val summary: String = ""
)
