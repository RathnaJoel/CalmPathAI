package com.calmpath.ai.alerts.ai

import android.util.Log
import com.calmpath.ai.alerts.AlertDecision
import com.calmpath.ai.data.local.entities.SmartAlertEntity
import com.calmpath.ai.data.remote.api.AIChatService
import com.calmpath.ai.data.remote.api.CalmPathContextDto
import com.calmpath.ai.data.remote.api.CandidatePlaceDto
import com.calmpath.ai.data.remote.api.ChatApiRequest
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Generates concise, contextual alert messages (CO10).
 * Reuses the existing CO8 AI Chatbot architecture when online.
 * Uses structured, non-hallucinating fallback templates when offline or on error.
 *
 * Strict Rules:
 * - Never invent AQI.
 * - Never invent noise levels.
 * - Never invent distances.
 * - Never invent places.
 * - Never invent ML scores.
 * - Never claim cached data is live.
 * - Keep notifications short (< 120 chars).
 * - No medical diagnoses or claims.
 */
class AlertMessageGenerator(
    private val aiChatService: AIChatService? = null
) {
    private val tag = "AlertMessageGenerator"

    suspend fun generateMessage(
        decision: AlertDecision.TriggerAlert,
        userMood: String = "Relax",
        isNetworkAvailable: Boolean = true
    ): GeneratedAlertText {
        // Fallback default message
        val localFallback = generateLocalTemplate(decision)

        if (!isNetworkAvailable || aiChatService == null) {
            return localFallback
        }

        // Attempt online generation with short timeout
        return try {
            withTimeoutOrNull(2500L) {
                val place = decision.recommendedPlace
                val placeDtoList = place?.let {
                    listOf(
                        CandidatePlaceDto(
                            id = it.id,
                            name = it.name,
                            category = it.category,
                            distanceKm = it.distanceKm,
                            peaceScore = it.peaceScore,
                            aqi = it.aqi,
                            noiseDb = it.noiseDb,
                            address = it.address
                        )
                    )
                } ?: emptyList()

                val prompt = "Generate a single concise notification alert under 100 characters for: " +
                        "Alert=${decision.alertType}, Trigger=${decision.triggerValue.toInt()}, " +
                        "Place=${place?.name ?: "None"}, ML=${decision.mlSuitabilityScore ?: 0}%, Dist=${place?.distanceKm ?: 0.0}km. " +
                        "Do not include medical claims or fake numbers."

                val request = ChatApiRequest(
                    message = prompt,
                    conversationId = "smart_alerts_gen",
                    userId = "alerts_engine",
                    context = CalmPathContextDto(
                        mood = userMood,
                        aqi = decision.triggerValue.toInt(),
                        places = placeDtoList
                    )
                )

                val response = aiChatService.sendChatMessage(request)
                if (response.isSuccessful && response.body() != null) {
                    val aiText = response.body()!!.message.trim().replace('\n', ' ')
                    if (aiText.isNotEmpty() && aiText.length <= 150) {
                        return@withTimeoutOrNull GeneratedAlertText(
                            title = decision.title,
                            message = aiText,
                            isAiGenerated = true
                        )
                    }
                }
                null
            } ?: localFallback
        } catch (e: Exception) {
            Log.w(tag, "AI message generation error: ${e.message}. Using structured template.", e)
            localFallback
        }
    }

    /**
     * Local contextual template generator (deterministic, factual, and safe).
     */
    fun generateLocalTemplate(decision: AlertDecision.TriggerAlert): GeneratedAlertText {
        val place = decision.recommendedPlace
        val triggerInt = decision.triggerValue.toInt()
        val mlScore = decision.mlSuitabilityScore

        val message = when (decision.alertType) {
            SmartAlertEntity.TYPE_AQI_ALERT -> {
                if (place != null) {
                    "AQI worsened to $triggerInt near you. ${place.name} (${place.distanceKm} km) offers cleaner air."
                } else {
                    "Air quality near you has worsened (AQI $triggerInt). CalmPath recommends staying in cleaner environments."
                }
            }

            SmartAlertEntity.TYPE_NOISE_ALERT -> {
                if (place != null) {
                    "Noise levels are currently high ($triggerInt dB). ${place.name} (${place.distanceKm} km) offers a quiet escape."
                } else {
                    "Noise levels around you reached $triggerInt dB. Seek a quieter space nearby."
                }
            }

            SmartAlertEntity.TYPE_WEATHER_ALERT -> {
                if (place != null) {
                    "Inclement weather approaching. Consider indoor sanctuary ${place.name} (${place.distanceKm} km)."
                } else {
                    "Rain or weather shift expected soon. Consider moving to an indoor peaceful spot."
                }
            }

            SmartAlertEntity.TYPE_ML_RECOMMENDATION -> {
                if (place != null && mlScore != null) {
                    "${place.name} matches your preferences: $mlScore% ML Match, ${place.distanceKm} km away."
                } else if (place != null) {
                    "A peaceful place matching your preferences is nearby: ${place.name} (${place.distanceKm} km)."
                } else {
                    "A personalized peaceful sanctuary matching your preferences is nearby."
                }
            }

            else -> decision.message
        }

        return GeneratedAlertText(
            title = decision.title,
            message = message,
            isAiGenerated = false
        )
    }

    data class GeneratedAlertText(
        val title: String,
        val message: String,
        val isAiGenerated: Boolean = false
    )
}
