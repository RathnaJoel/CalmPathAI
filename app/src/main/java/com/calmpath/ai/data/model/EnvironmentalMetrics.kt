package com.calmpath.ai.data.model

import androidx.compose.ui.graphics.Color

/**
 * Environmental noise level evaluation.
 */
enum class NoiseCategory(
    val title: String,
    val rangeDesc: String,
    val minDb: Int,
    val maxDb: Int,
    val colorHex: Long,
    val wellnessTip: String
) {
    VERY_QUIET(
        title = "Very Quiet",
        rangeDesc = "0–30 dB",
        minDb = 0,
        maxDb = 30,
        colorHex = 0xFF2A9D8F, // Serene Teal/Blue
        wellnessTip = "Whisper-quiet surroundings ideal for deep meditation and sleep"
    ),
    QUIET(
        title = "Quiet",
        rangeDesc = "31–50 dB",
        minDb = 31,
        maxDb = 50,
        colorHex = 0xFF52B788, // Forest Green
        wellnessTip = "Tranquil ambient sound, perfect for reading and reflection"
    ),
    MODERATE(
        title = "Moderate",
        rangeDesc = "51–70 dB",
        minDb = 51,
        maxDb = 70,
        colorHex = 0xFFE9C46A, // Golden Yellow
        wellnessTip = "Normal conversation volume; acceptable for brisk walks and study"
    ),
    NOISY(
        title = "Noisy",
        rangeDesc = "71+ dB",
        minDb = 71,
        maxDb = 120,
        colorHex = 0xFFE76F51, // Coral Red
        wellnessTip = "Urban noise interference; seek a calmer route or sanctuary"
    );

    companion object {
        fun fromDecibels(db: Int): NoiseCategory {
            return when {
                db <= 30 -> VERY_QUIET
                db <= 50 -> QUIET
                db <= 70 -> MODERATE
                else -> NOISY
            }
        }
    }
}

/**
 * Air Quality Index categories with EPA color scales and health recommendations.
 */
enum class AqiCategory(
    val title: String,
    val minAqi: Int,
    val maxAqi: Int,
    val colorHex: Long,
    val healthAdvice: String
) {
    GOOD(
        title = "Good",
        minAqi = 0,
        maxAqi = 50,
        colorHex = 0xFF52B788,
        healthAdvice = "Air quality is pristine and poses little to no risk."
    ),
    MODERATE(
        title = "Moderate",
        minAqi = 51,
        maxAqi = 100,
        colorHex = 0xFFE9C46A,
        healthAdvice = "Acceptable air quality; sensitive individuals should monitor exertion."
    ),
    UNHEALTHY_SENSITIVE(
        title = "Unhealthy for Sensitive Groups",
        minAqi = 101,
        maxAqi = 150,
        colorHex = 0xFFF4A261,
        healthAdvice = "Sensitive groups may experience health effects; limit prolonged outdoor exposure."
    ),
    POOR(
        title = "Poor / Polluted",
        minAqi = 151,
        maxAqi = 500,
        colorHex = 0xFFE76F51,
        healthAdvice = "Everyone may begin to experience health effects; seek indoor filtered areas."
    );

    companion object {
        fun fromAqi(aqi: Int): AqiCategory {
            return when {
                aqi <= 50 -> GOOD
                aqi <= 100 -> MODERATE
                aqi <= 150 -> UNHEALTHY_SENSITIVE
                else -> POOR
            }
        }
    }
}

/**
 * Current environmental snapshot at user's location or sanctuary (CO5 extended).
 */
data class EnvironmentalSummary(
    val aqi: Int = 36,
    val aqiCategory: AqiCategory = AqiCategory.GOOD,
    val noiseDb: Int = 42,
    val noiseCategory: NoiseCategory = NoiseCategory.QUIET,
    val temperatureC: Int = 23,
    val weatherCondition: String = "Pleasant & Breezy",
    val weatherIcon: String = "🌤️",
    val humidityPercent: Int = 54,
    val peaceScore: Int = 88,
    val peaceDescription: String = "Excellent environment for relaxation and clarity.",
    val isLive: Boolean = false,
    val isCached: Boolean = false,
    val localityName: String = "Mumbai, Maharashtra",
    val lastUpdatedTimestamp: Long = System.currentTimeMillis()
)
