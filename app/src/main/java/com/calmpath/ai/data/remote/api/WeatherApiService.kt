package com.calmpath.ai.data.remote.api

import com.calmpath.ai.data.remote.model.WeatherResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit interface for Weather REST API service (CO5).
 * Connects to Open-Meteo Weather API (no API key required, high-precision telemetry).
 */
interface WeatherApiService {

    @GET("v1/forecast")
    suspend fun getCurrentWeather(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") currentMetrics: String = "temperature_2m,relative_humidity_2m,apparent_temperature,weather_code,wind_speed_10m",
        @Query("timezone") timezone: String = "auto"
    ): Response<WeatherResponse>
}
