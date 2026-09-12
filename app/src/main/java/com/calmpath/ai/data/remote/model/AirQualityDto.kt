package com.calmpath.ai.data.remote.model

import com.calmpath.ai.data.model.AqiCategory
import com.google.gson.annotations.SerializedName
import kotlin.math.roundToInt

/**
 * Raw Open-Meteo Air Quality REST API response model (CO5).
 */
data class AirQualityResponse(
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("current") val current: CurrentAirQualityDto?
)

data class CurrentAirQualityDto(
    @SerializedName("time") val time: String? = null,
    @SerializedName("us_aqi") val usAqi: Double? = null,
    @SerializedName("european_aqi") val europeanAqi: Double? = null,
    @SerializedName("pm10") val pm10: Double? = null,
    @SerializedName("pm2_5") val pm25: Double? = null,
    @SerializedName("carbon_monoxide") val carbonMonoxide: Double? = null,
    @SerializedName("nitrogen_dioxide") val nitrogenDioxide: Double? = null,
    @SerializedName("sulphur_dioxide") val sulphurDioxide: Double? = null,
    @SerializedName("ozone") val ozone: Double? = null
)

/**
 * Clean application-level Air Quality domain model for CalmPath AI.
 * Supports both official Indian CPCB National Air Quality Index (NAQI)
 * and US EPA standard AQI.
 */
data class AirQualityInfo(
    val aqi: Int,
    val category: AqiCategory,
    val pm25: Double,
    val pm10: Double,
    val carbonMonoxide: Double,
    val nitrogenDioxide: Double,
    val ozone: Double,
    val usAqi: Int = aqi,
    val indianAqi: Int = aqi
) {
    companion object {
        val default = AirQualityInfo(
            aqi = 45,
            category = AqiCategory.GOOD,
            pm25 = 14.2,
            pm10 = 28.5,
            carbonMonoxide = 260.0,
            nitrogenDioxide = 18.5,
            ozone = 35.0,
            usAqi = 55,
            indianAqi = 45
        )

        fun calculatePm25SubIndex(pm25: Double): Int {
            return when {
                pm25 <= 0.0 -> 0
                pm25 <= 30.0 -> (pm25 * (50.0 / 30.0)).roundToInt()
                pm25 <= 60.0 -> (51.0 + (pm25 - 30.0) * (49.0 / 30.0)).roundToInt()
                pm25 <= 90.0 -> (101.0 + (pm25 - 60.0) * (99.0 / 30.0)).roundToInt()
                pm25 <= 120.0 -> (201.0 + (pm25 - 90.0) * (99.0 / 30.0)).roundToInt()
                pm25 <= 250.0 -> (301.0 + (pm25 - 120.0) * (99.0 / 130.0)).roundToInt()
                else -> (401.0 + (pm25 - 250.0) * (99.0 / 130.0)).roundToInt().coerceAtMost(500)
            }
        }

        fun calculatePm10SubIndex(pm10: Double): Int {
            return when {
                pm10 <= 0.0 -> 0
                pm10 <= 50.0 -> pm10.roundToInt()
                pm10 <= 100.0 -> (51.0 + (pm10 - 50.0) * (49.0 / 50.0)).roundToInt()
                pm10 <= 250.0 -> (101.0 + (pm10 - 100.0) * (99.0 / 150.0)).roundToInt()
                pm10 <= 350.0 -> (201.0 + (pm10 - 250.0) * (99.0 / 100.0)).roundToInt()
                pm10 <= 430.0 -> (301.0 + (pm10 - 350.0) * (99.0 / 80.0)).roundToInt()
                else -> (401.0 + (pm10 - 430.0) * (99.0 / 70.0)).roundToInt().coerceAtMost(500)
            }
        }

        fun calculateIndianNaqi(pm25: Double, pm10: Double): Int {
            val iPm25 = calculatePm25SubIndex(pm25)
            val iPm10 = calculatePm10SubIndex(pm10)
            return maxOf(iPm25, iPm10)
        }

        fun fromDto(dto: CurrentAirQualityDto?): AirQualityInfo {
            if (dto == null) return default
            val pm25Val = dto.pm25 ?: 14.0
            val pm10Val = dto.pm10 ?: 28.0
            val calculatedIndianNaqi = calculateIndianNaqi(pm25Val, pm10Val)
            val usAqiVal = (dto.usAqi ?: dto.europeanAqi)?.roundToInt() ?: calculatedIndianNaqi

            // In India, Indian CPCB NAQI represents true local air quality
            val effectiveAqi = if (calculatedIndianNaqi > 0) calculatedIndianNaqi else usAqiVal

            return AirQualityInfo(
                aqi = effectiveAqi,
                category = AqiCategory.fromAqi(effectiveAqi),
                pm25 = pm25Val,
                pm10 = pm10Val,
                carbonMonoxide = dto.carbonMonoxide ?: 250.0,
                nitrogenDioxide = dto.nitrogenDioxide ?: 18.0,
                ozone = dto.ozone ?: 35.0,
                usAqi = usAqiVal,
                indianAqi = calculatedIndianNaqi
            )
        }
    }
}
