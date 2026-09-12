package com.calmpath.ai.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Entity 9: ChatMessageEntity (CO8: AI Chatbot Conversation Storage).
 * Stores chronological conversation history locally in Room database.
 */
@Entity(
    tableName = "chat_messages",
    indices = [
        Index(value = ["userId"]),
        Index(value = ["conversationId"]),
        Index(value = ["timestamp"])
    ]
)
data class ChatMessageEntity(
    @PrimaryKey
    val messageId: String = UUID.randomUUID().toString(),
    val userId: String,
    val conversationId: String = "default_conversation",
    val role: String, // "USER" or "ASSISTANT"
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val recommendedPlaceId: String? = null,
    val mlSuitabilityScore: Int? = null, // CO9: Personalized ML Suitability Score (0-100)
    val action: String? = "NONE" // NONE, SHOW_PLACE, SHOW_ON_MAP, NAVIGATE, CHECK_AQI, CHECK_WEATHER
) {
    val isUser: Boolean
        get() = role.equals("USER", ignoreCase = true)

    val isAssistant: Boolean
        get() = role.equals("ASSISTANT", ignoreCase = true)

    companion object {
        const val ROLE_USER = "USER"
        const val ROLE_ASSISTANT = "ASSISTANT"

        const val ACTION_NONE = "NONE"
        const val ACTION_SHOW_PLACE = "SHOW_PLACE"
        const val ACTION_SHOW_ON_MAP = "SHOW_ON_MAP"
        const val ACTION_NAVIGATE = "NAVIGATE"
        const val ACTION_CHECK_AQI = "CHECK_AQI"
        const val ACTION_CHECK_WEATHER = "CHECK_WEATHER"

        fun createUserMessage(
            userId: String,
            message: String,
            conversationId: String = "default_conversation"
        ): ChatMessageEntity {
            return ChatMessageEntity(
                userId = userId,
                conversationId = conversationId,
                role = ROLE_USER,
                message = message,
                timestamp = System.currentTimeMillis()
            )
        }

        fun createAssistantMessage(
            userId: String,
            message: String,
            recommendedPlaceId: String? = null,
            action: String? = ACTION_NONE,
            conversationId: String = "default_conversation",
            mlSuitabilityScore: Int? = null
        ): ChatMessageEntity {
            return ChatMessageEntity(
                userId = userId,
                conversationId = conversationId,
                role = ROLE_ASSISTANT,
                message = message,
                timestamp = System.currentTimeMillis(),
                recommendedPlaceId = recommendedPlaceId,
                mlSuitabilityScore = mlSuitabilityScore,
                action = action
            )
        }
    }
}
