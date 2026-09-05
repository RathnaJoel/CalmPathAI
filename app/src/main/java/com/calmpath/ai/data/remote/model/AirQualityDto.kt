package com.calmpath.ai.data.remote.model

import com.calmpath.ai.data.model.AqiCategory
import com.google.gson.annotations.SerializedName

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
 */
data class AirQualityInfo(
    val aqi: Int,
    val category: AqiCategory,
    val pm25: Double,
    val pm10: Double,
    val carbonMonoxide: Double,
    val nitrogenDioxide: Double,
    val ozone: Double
) {
    companion object {
        val default = AirQualityInfo(
            aqi = 48,
            category = AqiCategory.GOOD,
            pm25 = 14.2,
            pm10 = 28.5,
            carbonMonoxide = 260.0,
            nitrogenDioxide = 18.5,
            ozone = 35.0
        )

        fun fromDto(dto: CurrentAirQualityDto?): AirQualityInfo {
            if (dto == null) return default
            val rawAqi = (dto.usAqi ?: dto.europeanAqi)?.toInt() ?: 48
            return AirQualityInfo(
                aqi = rawAqi,
                category = AqiCategory.fromAqi(rawAqi),
                pm25 = dto.pm25 ?: 14.0,
                pm10 = dto.pm10 ?: 28.0,
                carbonMonoxide = dto.carbonMonoxide ?: 250.0,
                nitrogenDioxide = dto.nitrogenDioxide ?: 18.0,
                ozone = dto.ozone ?: 35.0
            )
        }
    }
}
