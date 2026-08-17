package com.calmpath.ai.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.calmpath.ai.data.local.entities.UserPreferencesEntity
import com.calmpath.ai.data.model.UserProfile
import com.calmpath.ai.data.repository.AuthRepository
import com.calmpath.ai.data.repository.CalmPathRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(
    val userProfile: UserProfile? = null,
    val preferences: UserPreferencesEntity = UserPreferencesEntity(),
    val favoritesCount: Int = 0,
    val historyCount: Int = 0
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
                _uiState.value = _uiState.value.copy(userProfile = user)
            }
        }

        viewModelScope.launch {
            repository.preferencesFlow.collect { pref ->
                _uiState.value = _uiState.value.copy(preferences = pref)
            }
        }

        viewModelScope.launch {
            repository.favoritesFlow.collect { favs ->
                _uiState.value = _uiState.value.copy(favoritesCount = favs.size)
            }
        }

        viewModelScope.launch {
            repository.historyFlow.collect { hist ->
                _uiState.value = _uiState.value.copy(historyCount = hist.size)
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
