package com.calmpath.ai.data.remote

import com.calmpath.ai.data.remote.api.AirQualityApiService
import com.calmpath.ai.data.remote.api.PlacesApiService
import com.calmpath.ai.data.remote.api.WeatherApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Singleton configuring Retrofit REST clients and OkHttp instances for CalmPath AI (CO5).
 */
object RetrofitClient {

    private const val WEATHER_BASE_URL = "https://api.open-meteo.com/"
    private const val AIR_QUALITY_BASE_URL = "https://air-quality-api.open-meteo.com/"
    private const val OVERPASS_BASE_URL = "https://overpass-api.de/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .retryOnConnectionFailure(true)
            .build()
    }

    val weatherApi: WeatherApiService by lazy {
        Retrofit.Builder()
            .baseUrl(WEATHER_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherApiService::class.java)
    }

    val airQualityApi: AirQualityApiService by lazy {
        Retrofit.Builder()
            .baseUrl(AIR_QUALITY_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AirQualityApiService::class.java)
    }

    val placesApi: PlacesApiService by lazy {
        Retrofit.Builder()
            .baseUrl(OVERPASS_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PlacesApiService::class.java)
    }
}
