package com.calmpath.ai.data.remote.api

import com.calmpath.ai.data.remote.model.GoogleDirectionsResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit interface for Google Directions REST API (CO5 & CO6).
 * Fetches real turn-by-turn pedestrian street routing between coordinates.
 */
interface DirectionsApiService {

    @GET("maps/api/directions/json")
    suspend fun getWalkingDirections(
        @Query("origin") origin: String, // "lat,lon"
        @Query("destination") destination: String, // "lat,lon"
        @Query("mode") mode: String = "walking",
        @Query("key") apiKey: String
    ): Response<GoogleDirectionsResponse>
}
