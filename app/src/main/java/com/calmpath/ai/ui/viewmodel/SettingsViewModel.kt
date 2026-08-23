package com.calmpath.ai.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.calmpath.ai.data.repository.AuthRepository
import com.calmpath.ai.data.repository.CalmPathRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class SettingsUiState(
    val themeMode: String = "SYSTEM", // "LIGHT", "DARK", "SYSTEM"
    val notificationsEnabled: Boolean = true,
    val locationEnabled: Boolean = true,
    val distanceUnit: String = "km",
    val temperatureUnit: String = "°C",
    val soundUnit: String = "dB",
    val maxAqi: Float = 60f,
    val preferredNoiseDb: Float = 45f,
    val preferredDistanceKm: Float = 10f,
    val isSavedSuccess: Boolean = false
)

class SettingsViewModel(
    private val repository: CalmPathRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettingsAndPreferences()
    }

    private fun loadSettingsAndPreferences() {
        viewModelScope.launch {
            combine(
                repository.settingsFlow,
                repository.preferencesFlow
            ) { settings, preferences ->
                _uiState.value.copy(
                    themeMode = settings.theme,
                    notificationsEnabled = settings.notificationsEnabled,
                    locationEnabled = settings.locationEnabled,
                    distanceUnit = settings.distanceUnit,
                    temperatureUnit = settings.temperatureUnit,
                    soundUnit = settings.soundUnit,
                    maxAqi = preferences.maxAQI.toFloat(),
                    preferredNoiseDb = preferences.maxNoiseLevel.toFloat(),
                    preferredDistanceKm = preferences.maxDistance.toFloat()
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun onThemeSelected(theme: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(themeMode = theme)
            repository.saveTheme(theme)
        }
    }

    fun onNotificationsToggled(enabled: Boolean) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(notificationsEnabled = enabled)
            repository.saveNotifications(enabled)
        }
    }

    fun onMaxAqiChanged(value: Float) {
        _uiState.value = _uiState.value.copy(maxAqi = value)
    }

    fun onPreferredNoiseChanged(value: Float) {
        _uiState.value = _uiState.value.copy(preferredNoiseDb = value)
    }

    fun onPreferredDistanceChanged(value: Float) {
        _uiState.value = _uiState.value.copy(preferredDistanceKm = value)
    }

    fun saveEnvironmentalPreferences() {
        val state = _uiState.value
        viewModelScope.launch {
            repository.saveEnvironmentalPreferences(
                maxAqi = state.maxAqi.toInt(),
                noiseLevel = state.preferredNoiseDb.toInt(),
                distanceKm = state.preferredDistanceKm.toDouble()
            )
            _uiState.value = state.copy(isSavedSuccess = true)
        }
    }

    fun clearFavorites() {
        viewModelScope.launch {
            repository.clearAllFavorites()
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    fun logout(onLoggedOut: () -> Unit) {
        authRepository.signOut()
        onLoggedOut()
    }
}

class SettingsViewModelFactory(
    private val repository: CalmPathRepository,
    private val authRepository: AuthRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SettingsViewModel(repository, authRepository) as T
    }
}
