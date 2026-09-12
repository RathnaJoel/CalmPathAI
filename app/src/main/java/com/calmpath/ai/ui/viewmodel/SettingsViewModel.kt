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
    // CO10 Alert Controls
    val smartAlertsEnabled: Boolean = true,
    val environmentalAlertsEnabled: Boolean = true,
    val aqiAlertsEnabled: Boolean = true,
    val noiseAlertsEnabled: Boolean = true,
    val weatherAlertsEnabled: Boolean = true,
    val mlRecommendationsEnabled: Boolean = true,
    val minMlScore: Float = 80f,
    val alertCooldownMinutes: Int = 30,
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
                    preferredDistanceKm = preferences.maxDistance.toFloat(),
                    smartAlertsEnabled = settings.smartAlertsEnabled,
                    environmentalAlertsEnabled = settings.environmentalAlertsEnabled,
                    aqiAlertsEnabled = settings.aqiAlertsEnabled,
                    noiseAlertsEnabled = settings.noiseAlertsEnabled,
                    weatherAlertsEnabled = settings.weatherAlertsEnabled,
                    mlRecommendationsEnabled = settings.mlRecommendationsEnabled,
                    minMlScore = settings.minMlScoreThreshold.toFloat(),
                    alertCooldownMinutes = settings.alertCooldownMinutes
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
            repository.saveAlertSettings(
                smartAlertsEnabled = state.smartAlertsEnabled,
                environmentalAlertsEnabled = state.environmentalAlertsEnabled,
                aqiAlertsEnabled = state.aqiAlertsEnabled,
                noiseAlertsEnabled = state.noiseAlertsEnabled,
                weatherAlertsEnabled = state.weatherAlertsEnabled,
                mlRecommendationsEnabled = state.mlRecommendationsEnabled,
                maxAqiThreshold = state.maxAqi.toInt(),
                maxNoiseThreshold = state.preferredNoiseDb.toInt(),
                minMlScoreThreshold = state.minMlScore.toInt(),
                alertCooldownMinutes = state.alertCooldownMinutes
            )
            _uiState.value = state.copy(isSavedSuccess = true)
        }
    }

    fun onSmartAlertsToggled(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(smartAlertsEnabled = enabled)
        persistAlertSettings()
    }

    fun onEnvironmentalAlertsToggled(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(environmentalAlertsEnabled = enabled)
        persistAlertSettings()
    }

    fun onAqiAlertsToggled(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(aqiAlertsEnabled = enabled)
        persistAlertSettings()
    }

    fun onNoiseAlertsToggled(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(noiseAlertsEnabled = enabled)
        persistAlertSettings()
    }

    fun onWeatherAlertsToggled(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(weatherAlertsEnabled = enabled)
        persistAlertSettings()
    }

    fun onMlRecommendationsToggled(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(mlRecommendationsEnabled = enabled)
        persistAlertSettings()
    }

    fun onMinMlScoreChanged(value: Float) {
        _uiState.value = _uiState.value.copy(minMlScore = value)
    }

    fun onAlertCooldownChanged(minutes: Int) {
        _uiState.value = _uiState.value.copy(alertCooldownMinutes = minutes)
        persistAlertSettings()
    }

    private fun persistAlertSettings() {
        val state = _uiState.value
        viewModelScope.launch {
            repository.saveAlertSettings(
                smartAlertsEnabled = state.smartAlertsEnabled,
                environmentalAlertsEnabled = state.environmentalAlertsEnabled,
                aqiAlertsEnabled = state.aqiAlertsEnabled,
                noiseAlertsEnabled = state.noiseAlertsEnabled,
                weatherAlertsEnabled = state.weatherAlertsEnabled,
                mlRecommendationsEnabled = state.mlRecommendationsEnabled,
                maxAqiThreshold = state.maxAqi.toInt(),
                maxNoiseThreshold = state.preferredNoiseDb.toInt(),
                minMlScoreThreshold = state.minMlScore.toInt(),
                alertCooldownMinutes = state.alertCooldownMinutes
            )
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
