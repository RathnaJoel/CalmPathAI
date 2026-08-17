package com.calmpath.ai.data.model

/**
 * Represents a geographical region with environmental peacefulness index.
 * Color Scale:
 * - 1: POOR (Red #E76F51) -> High noise / elevated AQI
 * - 2: MODERATE_POOR (Orange #F4A261)
 * - 3: MODERATE (Yellow #E9C46A)
 * - 4: GOOD (Green #52B788) -> Low noise / clean air
 * - 5: EXCELLENT (Blue #2A9D8F) -> Pristine sanctuary
 */
enum class CalmnessLevel(
    val level: Int,
    val title: String,
    val colorHex: Long,
    val description: String
) {
    POOR(1, "Poor", 0xFFE76F51, "High noise & traffic congestion"),
    MODERATE_POOR(2, "Moderate-Poor", 0xFFF4A261, "Urban hum, average air quality"),
    MODERATE(3, "Moderate", 0xFFE9C46A, "Acceptable ambient noise"),
    GOOD(4, "Good", 0xFF52B788, "Quiet parklands and clean air"),
    EXCELLENT(5, "Excellent", 0xFF2A9D8F, "Pristine nature, whisper quiet");

    companion object {
        fun fromScore(peaceScore: Int): CalmnessLevel {
            return when {
                peaceScore >= 85 -> EXCELLENT
                peaceScore >= 70 -> GOOD
                peaceScore >= 55 -> MODERATE
                peaceScore >= 40 -> MODERATE_POOR
                else -> POOR
            }
        }
    }
}

/**
 * Heatmap point or polygon representation for the Explore Canvas Map.
 */
data class HeatmapZone(
    val id: String,
    val title: String,
    val relativeX: Float, // 0f..1f within canvas view
    val relativeY: Float, // 0f..1f within canvas view
    val radiusPx: Float,
    val calmnessLevel: CalmnessLevel,
    val avgDecibels: Int,
    val avgAqi: Int,
    val associatedPlaceId: String? = null
)
