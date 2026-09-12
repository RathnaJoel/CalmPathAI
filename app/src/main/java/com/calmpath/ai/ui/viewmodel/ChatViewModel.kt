package com.calmpath.ai.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.calmpath.ai.data.local.entities.ChatMessageEntity
import com.calmpath.ai.data.model.Place
import com.calmpath.ai.data.remote.NetworkStatus
import com.calmpath.ai.data.repository.CalmPathRepository
import com.calmpath.ai.data.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class ChatUiState(
    val messages: List<ChatMessageEntity> = emptyList(),
    val placesMap: Map<String, Place> = emptyMap(),
    val inputText: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isOffline: Boolean = false,
    val currentLocality: String = "Mumbai, Maharashtra"
)

class ChatViewModel(
    private val chatRepository: ChatRepository,
    private val calmPathRepository: CalmPathRepository,
    private val userId: String = "guest"
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        observeMessages()
        observePlacesAndLocation()
        observeNetwork()
    }

    private fun observeMessages() {
        viewModelScope.launch {
            chatRepository.getChatHistoryFlow(userId).collect { msgList ->
                _uiState.value = _uiState.value.copy(messages = msgList)
            }
        }
    }

    private fun observePlacesAndLocation() {
        viewModelScope.launch {
            combine(
                calmPathRepository.placesFlow,
                calmPathRepository.currentLocation
            ) { entities, currLoc ->
                val places = entities.map { entity ->
                    entity.toDomainModel(userLat = currLoc.latitude, userLon = currLoc.longitude)
                }
                val map = places.associateBy { it.id }
                Pair(map, currLoc.locality)
            }.collect { (placesMap, locality) ->
                _uiState.value = _uiState.value.copy(
                    placesMap = placesMap,
                    currentLocality = locality
                )
            }
        }
    }

    private fun observeNetwork() {
        viewModelScope.launch {
            calmPathRepository.networkMonitor?.networkStatus?.collect { status ->
                _uiState.value = _uiState.value.copy(
                    isOffline = status == NetworkStatus.OFFLINE
                )
            }
        }
    }

    fun onInputTextChanged(text: String) {
        _uiState.value = _uiState.value.copy(inputText = text)
    }

    fun sendMessage(customPrompt: String? = null) {
        val promptToSend = (customPrompt ?: _uiState.value.inputText).trim()
        if (promptToSend.isBlank() || _uiState.value.isLoading) return

        _uiState.value = _uiState.value.copy(
            inputText = if (customPrompt == null) "" else _uiState.value.inputText,
            isLoading = true,
            error = null
        )

        viewModelScope.launch {
            val result = chatRepository.sendMessage(
                userId = userId,
                userPrompt = promptToSend
            )
            result.onFailure { ex ->
                _uiState.value = _uiState.value.copy(
                    error = ex.message ?: "Could not reach CalmPath AI Assistant. Please try again."
                )
            }
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            chatRepository.clearChatHistory(userId)
        }
    }

    fun dismissError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun requestInAppNavigation(placeId: String, mlScore: Int? = null) {
        calmPathRepository.requestInAppNavigation(placeId)
        viewModelScope.launch {
            calmPathRepository.recordRecommendationInteraction(
                placeId = placeId,
                mood = "Active",
                mlScore = mlScore ?: 85,
                action = com.calmpath.ai.data.local.entities.RecommendationInteractionEntity.ACTION_NAVIGATED,
                userId = userId
            )
        }
    }

    fun recordPlaceSelection(placeId: String, mlScore: Int? = null) {
        viewModelScope.launch {
            calmPathRepository.recordRecommendationInteraction(
                placeId = placeId,
                mood = "Active",
                mlScore = mlScore ?: 85,
                action = com.calmpath.ai.data.local.entities.RecommendationInteractionEntity.ACTION_SELECTED,
                userId = userId
            )
        }
    }
}

class ChatViewModelFactory(
    private val chatRepository: ChatRepository,
    private val calmPathRepository: CalmPathRepository,
    private val userId: String = "guest"
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ChatViewModel(chatRepository, calmPathRepository, userId) as T
    }
}
