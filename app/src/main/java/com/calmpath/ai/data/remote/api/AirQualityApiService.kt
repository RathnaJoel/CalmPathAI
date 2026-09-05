package com.calmpath.ai.data.remote.api

import com.calmpath.ai.data.remote.model.AirQualityResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit interface for Air Quality / AQI REST API service (CO5).
 * Connects to Open-Meteo Air Quality API (returns US AQI, PM2.5, PM10, CO, NO2, O3).
 */
interface AirQualityApiService {

    @GET("v1/air-quality")
    suspend fun getAirQuality(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") metrics: String = "us_aqi,european_aqi,pm10,pm2_5,carbon_monoxide,nitrogen_dioxide,sulphur_dioxide,ozone",
        @Query("timezone") timezone: String = "auto"
    ): Response<AirQualityResponse>
}
