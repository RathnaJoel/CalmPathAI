package com.calmpath.ai.data.remote.api

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Retrofit API interface for CalmPath AI Chatbot proxy communication (CO8).
 * Keeps AI API keys completely isolated on the server-side proxy.
 */
interface AIChatService {

    @POST("api/chat")
    suspend fun sendChatMessage(
        @Body request: ChatApiRequest
    ): Response<ChatApiResponse>
}

data class ChatApiRequest(
    @SerializedName("message") val message: String,
    @SerializedName("conversationId") val conversationId: String = "default_conversation",
    @SerializedName("userId") val userId: String = "guest",
    @SerializedName("context") val context: CalmPathContextDto
)

data class CalmPathContextDto(
    @SerializedName("mood") val mood: String = "Relax",
    @SerializedName("latitude") val latitude: Double = 19.0760,
    @SerializedName("longitude") val longitude: Double = 72.8777,
    @SerializedName("locality") val locality: String = "Mumbai, Maharashtra",
    @SerializedName("aqi") val aqi: Int = 35,
    @SerializedName("weather") val weather: String = "27°C, Clear",
    @SerializedName("temperatureC") val temperatureC: Int = 27,
    @SerializedName("noiseDb") val noiseDb: Int = 38,
    @SerializedName("isOutsideIndia") val isOutsideIndia: Boolean = false,
    @SerializedName("places") val places: List<CandidatePlaceDto> = emptyList(),
    @SerializedName("preferences") val preferences: UserPreferencesDto? = null
)

data class CandidatePlaceDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("category") val category: String,
    @SerializedName("distanceKm") val distanceKm: Double,
    @SerializedName("peaceScore") val peaceScore: Int,
    @SerializedName("aqi") val aqi: Int,
    @SerializedName("noiseDb") val noiseDb: Int,
    @SerializedName("address") val address: String
)

data class UserPreferencesDto(
    @SerializedName("maxDistanceKm") val maxDistanceKm: Int = 8,
    @SerializedName("maxAqi") val maxAqi: Int = 60,
    @SerializedName("maxNoiseDb") val maxNoiseDb: Int = 45
)

data class ChatApiResponse(
    @SerializedName("message") val message: String,
    @SerializedName("recommendedPlaceId") val recommendedPlaceId: String? = null,
    @SerializedName("action") val action: String? = "NONE",
    @SerializedName("mlSuitabilityScore") val mlSuitabilityScore: Int? = null
)
