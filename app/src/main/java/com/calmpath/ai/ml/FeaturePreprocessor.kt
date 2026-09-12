package com.calmpath.ai.ml

/**
 * Encodes categorical features and handles missing values with 100% parity
 * to the Python training pipeline (CO9 Feature Preprocessing).
 */
object FeaturePreprocessor {

    const val FEATURE_COUNT = 16

    // Categorical Encodings matching preprocess.py exactly
    val WEATHER_MAP = mapOf(
        "clear" to 0f,
        "partly cloudy" to 1f,
        "cloudy" to 2f,
        "rainy" to 3f,
        "thunderstorm" to 4f,
        "mist/fog" to 5f
    )

    val CATEGORY_MAP = mapOf(
        "parks" to 0f,
        "lakes" to 1f,
        "cafes" to 2f,
        "libraries" to 3f,
        "meditation" to 4f,
        "fitness" to 5f
    )

    val MOOD_MAP = mapOf(
        "relax" to 0f,
        "meditate" to 1f,
        "study" to 2f,
        "exercise" to 3f,
        "fresh air" to 4f,
        "quiet time" to 5f
    )

    val PREFERRED_CATEGORY_MAP = mapOf(
        "all" to 0f,
        "parks" to 1f,
        "lakes" to 2f,
        "cafes" to 3f,
        "libraries" to 4f,
        "meditation" to 5f,
        "fitness" to 6f
    )

    /**
     * Transforms [RawPlaceFeatures] into a 16-element FloatArray ready for Random Forest inference.
     */
    fun preprocess(features: RawPlaceFeatures): FloatArray {
        val array = FloatArray(FEATURE_COUNT)

        array[0] = features.aqi.toFloat().coerceIn(10f, 500f)
        array[1] = features.pm25.toFloat().coerceIn(2f, 300f)
        array[2] = features.pm10.toFloat().coerceIn(5f, 500f)
        array[3] = features.noiseDb.toFloat().coerceIn(20f, 100f)
        array[4] = features.temperature.toFloat().coerceIn(5f, 50f)
        array[5] = features.humidity.toFloat().coerceIn(10f, 100f)
        array[6] = encodeWeather(features.weatherCondition)
        array[7] = features.distanceKm.toFloat().coerceAtLeast(0.1f)
        array[8] = encodeCategory(features.placeCategory)
        array[9] = features.placeRating.toFloat().coerceIn(1f, 5f)
        array[10] = features.timeOfDay.toFloat().coerceIn(0f, 23f)
        array[11] = features.dayOfWeek.toFloat().coerceIn(1f, 7f)
        array[12] = encodeMood(features.userMood)
        array[13] = encodePreferredCategory(features.preferredCategory)
        array[14] = features.maxDistance.toFloat().coerceIn(1f, 50f)
        array[15] = features.userPreviousRating.toFloat().coerceIn(0f, 5f)

        return array
    }

    fun encodeWeather(condition: String?): Float {
        if (condition.isNullOrBlank()) return 0f
        val lower = condition.lowercase()
        return when {
            lower.contains("rain") || lower.contains("drizzle") || lower.contains("shower") -> 3f
            lower.contains("thunder") || lower.contains("storm") -> 4f
            lower.contains("fog") || lower.contains("mist") || lower.contains("haze") -> 5f
            lower.contains("partly") -> 1f
            lower.contains("cloud") || lower.contains("overcast") -> 2f
            else -> 0f // Clear / default
        }
    }

    fun encodeCategory(category: String?): Float {
        if (category.isNullOrBlank()) return 0f
        val lower = category.lowercase().trim()
        return CATEGORY_MAP[lower] ?: 0f
    }

    fun encodeMood(mood: String?): Float {
        if (mood.isNullOrBlank()) return 0f
        val lower = mood.lowercase().trim()
        return MOOD_MAP[lower] ?: 0f
    }

    fun encodePreferredCategory(category: String?): Float {
        if (category.isNullOrBlank()) return 0f
        val lower = category.lowercase().trim()
        return PREFERRED_CATEGORY_MAP[lower] ?: 0f
    }
}
