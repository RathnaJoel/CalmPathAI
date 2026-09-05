package com.calmpath.ai.data.remote.model

import com.google.gson.annotations.SerializedName

/**
 * Raw Open-Meteo Weather REST API response model (CO5).
 */
data class WeatherResponse(
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("elevation") val elevation: Double?,
    @SerializedName("current") val current: CurrentWeatherDto?
)

data class CurrentWeatherDto(
    @SerializedName("time") val time: String? = null,
    @SerializedName("temperature_2m") val temperature2m: Double? = null,
    @SerializedName("relative_humidity_2m") val relativeHumidity2m: Double? = null,
    @SerializedName("apparent_temperature") val apparentTemperature: Double? = null,
    @SerializedName("weather_code") val weatherCode: Double? = null,
    @SerializedName("wind_speed_10m") val windSpeed10m: Double? = null
)

/**
 * Clean application-level Weather domain model for CalmPath AI.
 */
data class WeatherInfo(
    val temperatureC: Int,
    val feelsLikeC: Int,
    val humidityPercent: Int,
    val weatherCondition: String,
    val weatherIcon: String,
    val windSpeedKmH: Double
) {
    companion object {
        val default = WeatherInfo(
            temperatureC = 27,
            feelsLikeC = 29,
            humidityPercent = 70,
            weatherCondition = "Partly Cloudy",
            weatherIcon = "⛅",
            windSpeedKmH = 12.0
        )

        fun fromDto(dto: CurrentWeatherDto?): WeatherInfo {
            if (dto == null) return default
            val code = dto.weatherCode?.toInt() ?: 0
            val (condition, icon) = mapWmoWeatherCode(code)
            return WeatherInfo(
                temperatureC = dto.temperature2m?.toInt() ?: default.temperatureC,
                feelsLikeC = dto.apparentTemperature?.toInt() ?: default.feelsLikeC,
                humidityPercent = dto.relativeHumidity2m?.toInt() ?: default.humidityPercent,
                weatherCondition = condition,
                weatherIcon = icon,
                windSpeedKmH = dto.windSpeed10m ?: default.windSpeedKmH
            )
        }

        fun mapWmoWeatherCode(code: Int): Pair<String, String> {
            return when (code) {
                0 -> "Clear Sky" to "☀️"
                1 -> "Mainly Clear" to "🌤️"
                2 -> "Partly Cloudy" to "⛅"
                3 -> "Overcast" to "☁️"
                45, 48 -> "Foggy & Cool" to "🌫️"
                51, 53, 55 -> "Light Drizzle" to "🌦️"
                61, 63, 65 -> "Rainy" to "🌧️"
                71, 73, 75 -> "Snowfall" to "❄️"
                80, 81, 82 -> "Passing Showers" to "🌦️"
                95, 96, 99 -> "Thunderstorm" to "⛈️"
                else -> "Pleasant & Calm" to "🌤️"
            }
        }
    }
}
