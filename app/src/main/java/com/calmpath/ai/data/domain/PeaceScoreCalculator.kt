package com.calmpath.ai.data.domain

import com.calmpath.ai.data.model.AqiCategory
import kotlin.math.max
import kotlin.math.min

/**
 * Multi-factor Peace Score Calculator for CalmPath AI (CO5).
 * Modularity allows dynamic weighting and robust fallback when specific factors are unavailable.
 *
 * Default Weights:
 * - AQI: 30%
 * - Noise: 30%
 * - Weather / Thermal comfort: 15%
 * - Distance / Accessibility: 15%
 * - Place Rating & Category: 10%
 */
class PeaceScoreCalculator(
    val aqiWeight: Double = 0.30,
    val noiseWeight: Double = 0.30,
    val weatherWeight: Double = 0.15,
    val distanceWeight: Double = 0.15,
    val ratingWeight: Double = 0.10
) {

    /**
     * Computes the overall Peace Score (0 to 100).
     */
    fun calculatePeaceScore(
        aqi: Int?,
        noiseDb: Int?,
        temperatureC: Int? = null,
        weatherCondition: String? = null,
        distanceKm: Double? = null,
        userRating: Double? = null
    ): Int {
        var totalWeight = 0.0
        var weightedSum = 0.0

        // 1. AQI Component (0 to 100 subscore, higher is cleaner)
        if (aqi != null) {
            val aqiSubScore = computeAqiSubScore(aqi)
            weightedSum += aqiSubScore * aqiWeight
            totalWeight += aqiWeight
        }

        // 2. Noise Component (0 to 100 subscore, lower dB is higher peace)
        if (noiseDb != null) {
            val noiseSubScore = computeNoiseSubScore(noiseDb)
            weightedSum += noiseSubScore * noiseWeight
            totalWeight += noiseWeight
        }

        // 3. Weather Component (0 to 100 subscore)
        if (temperatureC != null || weatherCondition != null) {
            val weatherSubScore = computeWeatherSubScore(temperatureC, weatherCondition)
            weightedSum += weatherSubScore * weatherWeight
            totalWeight += weatherWeight
        }

        // 4. Distance Component (Closer is better, penalty for > 15 km)
        if (distanceKm != null) {
            val distanceSubScore = computeDistanceSubScore(distanceKm)
            weightedSum += distanceSubScore * distanceWeight
            totalWeight += distanceWeight
        }

        // 5. Rating Component (1 to 5 stars mapped to 0-100)
        if (userRating != null) {
            val ratingSubScore = min(100.0, max(0.0, (userRating / 5.0) * 100.0))
            weightedSum += ratingSubScore * ratingWeight
            totalWeight += ratingWeight
        }

        if (totalWeight <= 0.0) return 75 // Neutral baseline

        val finalScore = (weightedSum / totalWeight).toInt()
        return min(100, max(0, finalScore))
    }

    private fun computeAqiSubScore(aqi: Int): Double {
        return when {
            aqi <= 50 -> 100.0 - (aqi * 0.2) // 100 down to 90
            aqi <= 100 -> 90.0 - ((aqi - 50) * 0.4) // 90 down to 70
            aqi <= 150 -> 70.0 - ((aqi - 100) * 0.5) // 70 down to 45
            aqi <= 200 -> 45.0 - ((aqi - 150) * 0.5) // 45 down to 20
            else -> max(0.0, 20.0 - ((aqi - 200) * 0.2))
        }
    }

    private fun computeNoiseSubScore(noiseDb: Int): Double {
        return when {
            noiseDb <= 30 -> 100.0
            noiseDb <= 45 -> 100.0 - ((noiseDb - 30) * 1.33) // 100 down to 80
            noiseDb <= 60 -> 80.0 - ((noiseDb - 45) * 2.0) // 80 down to 50
            noiseDb <= 75 -> 50.0 - ((noiseDb - 60) * 2.0) // 50 down to 20
            else -> max(0.0, 20.0 - ((noiseDb - 75) * 1.5))
        }
    }

    private fun computeWeatherSubScore(temperatureC: Int?, condition: String?): Double {
        var score = 80.0
        if (temperatureC != null) {
            // Ideal thermal comfort: 20°C - 26°C
            score = when {
                temperatureC in 20..26 -> 95.0
                temperatureC in 15..19 || temperatureC in 27..32 -> 80.0
                temperatureC in 10..14 || temperatureC in 33..38 -> 60.0
                else -> 40.0
            }
        }
        if (condition != null) {
            val cond = condition.lowercase()
            when {
                cond.contains("clear") || cond.contains("sunny") -> score += 5.0
                cond.contains("rain") || cond.contains("shower") -> score -= 15.0
                cond.contains("thunder") || cond.contains("storm") -> score -= 30.0
                cond.contains("fog") || cond.contains("cloud") -> score += 0.0
            }
        }
        return min(100.0, max(0.0, score))
    }

    private fun computeDistanceSubScore(distanceKm: Double): Double {
        return when {
            distanceKm <= 2.0 -> 100.0
            distanceKm <= 5.0 -> 90.0
            distanceKm <= 10.0 -> 75.0
            distanceKm <= 20.0 -> 55.0
            else -> max(20.0, 50.0 - (distanceKm - 20.0))
        }
    }

    companion object {
        val default = PeaceScoreCalculator()
    }
}
