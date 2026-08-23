package com.calmpath.ai.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.calmpath.ai.data.local.entities.UserPreferencesEntity
import com.calmpath.ai.data.local.entities.UserProfileEntity
import com.calmpath.ai.data.model.UserProfile
import com.calmpath.ai.data.repository.AuthRepository
import com.calmpath.ai.data.repository.CalmPathRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class ProfileUiState(
    val userProfile: UserProfileEntity? = null,
    val authProfile: UserProfile? = null,
    val preferences: UserPreferencesEntity = UserPreferencesEntity(userId = "user_default"),
    val favoritesCount: Int = 0,
    val historyCount: Int = 0,
    val moodLogsCount: Int = 0
)

class ProfileViewModel(
    private val repository: CalmPathRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        observeUserData()
    }

    private fun observeUserData() {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                _uiState.value = _uiState.value.copy(authProfile = user)
            }
        }

        viewModelScope.launch {
            combine(
                repository.userProfileFlow,
                repository.preferencesFlow,
                repository.favoritesWithPlacesFlow,
                repository.historyWithPlacesFlow
            ) { profile, preferences, favorites, history ->
                _uiState.value.copy(
                    userProfile = profile,
                    preferences = preferences,
                    favoritesCount = favorites.size,
                    historyCount = history.size
                )
            }.collect { state ->
                _uiState.value = state
            }
        }

        viewModelScope.launch {
            repository.moodRepository.getMoodLogCountFlow("user_default").collect { count ->
                _uiState.value = _uiState.value.copy(moodLogsCount = count)
            }
        }
    }

    fun logout(onLoggedOut: () -> Unit) {
        authRepository.signOut()
        onLoggedOut()
    }
}

class ProfileViewModelFactory(
    private val repository: CalmPathRepository,
    private val authRepository: AuthRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ProfileViewModel(repository, authRepository) as T
    }
}
