package com.calmpath.ai.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.calmpath.ai.alerts.AlertDecision
import com.calmpath.ai.data.local.entities.SmartAlertEntity
import com.calmpath.ai.data.repository.SmartAlertRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AlertFilter {
    ALL,
    UNREAD,
    AQI,
    NOISE,
    WEATHER,
    RECOMMENDATION
}

data class SmartAlertUiState(
    val alerts: List<SmartAlertEntity> = emptyList(),
    val filteredAlerts: List<SmartAlertEntity> = emptyList(),
    val selectedFilter: AlertFilter = AlertFilter.ALL,
    val unreadCount: Int = 0,
    val isSimulating: Boolean = false,
    val simulationResult: String? = null
)

class SmartAlertViewModel(
    private val smartAlertRepository: SmartAlertRepository,
    private val userId: String = "guest"
) : ViewModel() {

    private val _filter = MutableStateFlow(AlertFilter.ALL)
    private val _isSimulating = MutableStateFlow(false)
    private val _simulationResult = MutableStateFlow<String?>(null)

    val uiState: StateFlow<SmartAlertUiState> = combine(
        smartAlertRepository.getUserAlertsFlow(userId),
        smartAlertRepository.getUnreadCountFlow(userId),
        _filter,
        _isSimulating,
        _simulationResult
    ) { alerts, unreadCount, filter, simulating, simResult ->
        val filtered = when (filter) {
            AlertFilter.ALL -> alerts
            AlertFilter.UNREAD -> alerts.filter { !it.isRead }
            AlertFilter.AQI -> alerts.filter { it.alertType == SmartAlertEntity.TYPE_AQI_ALERT }
            AlertFilter.NOISE -> alerts.filter { it.alertType == SmartAlertEntity.TYPE_NOISE_ALERT }
            AlertFilter.WEATHER -> alerts.filter { it.alertType == SmartAlertEntity.TYPE_WEATHER_ALERT }
            AlertFilter.RECOMMENDATION -> alerts.filter {
                it.alertType == SmartAlertEntity.TYPE_ML_RECOMMENDATION ||
                it.alertType == SmartAlertEntity.TYPE_PEACEFUL_PLACE_ALERT
            }
        }
        SmartAlertUiState(
            alerts = alerts,
            filteredAlerts = filtered,
            selectedFilter = filter,
            unreadCount = unreadCount,
            isSimulating = simulating,
            simulationResult = simResult
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SmartAlertUiState()
    )

    fun setFilter(filter: AlertFilter) {
        _filter.value = filter
    }

    fun markAsRead(alertId: String) {
        viewModelScope.launch {
            smartAlertRepository.markAlertAsRead(alertId)
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            smartAlertRepository.markAllAsRead(userId)
        }
    }

    fun markActionTaken(alertId: String) {
        viewModelScope.launch {
            smartAlertRepository.markActionTaken(alertId)
        }
    }

    fun clearAllAlerts() {
        viewModelScope.launch {
            smartAlertRepository.clearAllAlerts(userId)
        }
    }

    /**
     * Interactive Simulation triggers for Mobile Application Development Lab evaluation (CO10 Demo).
     */
    fun simulateAlertScenario(scenario: String) {
        viewModelScope.launch {
            _isSimulating.value = true
            _simulationResult.value = null
            val decision = when (scenario) {
                "AQI_SPIKE" -> {
                    smartAlertRepository.evaluateAndTriggerAlerts(
                        userId = userId,
                        overrideAqi = 168,
                        overrideNoiseDb = 42
                    )
                }
                "HIGH_NOISE" -> {
                    smartAlertRepository.evaluateAndTriggerAlerts(
                        userId = userId,
                        overrideAqi = 40,
                        overrideNoiseDb = 78
                    )
                }
                "WEATHER_RAIN" -> {
                    smartAlertRepository.evaluateAndTriggerAlerts(
                        userId = userId,
                        overrideAqi = 45,
                        overrideNoiseDb = 40,
                        overrideWeather = "Heavy Rain"
                    )
                }
                "ML_RECOMMENDATION" -> {
                    smartAlertRepository.evaluateAndTriggerAlerts(
                        userId = userId,
                        overrideAqi = 30,
                        overrideNoiseDb = 35
                    )
                }
                else -> {
                    smartAlertRepository.evaluateAndTriggerAlerts(userId = userId)
                }
            }

            _simulationResult.value = when (decision) {
                is AlertDecision.TriggerAlert -> "Triggered: ${decision.title}"
                is AlertDecision.NoAlert -> "Filtered: ${decision.reason}"
            }
            _isSimulating.value = false
        }
    }
}

class SmartAlertViewModelFactory(
    private val smartAlertRepository: SmartAlertRepository,
    private val userId: String = "guest"
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SmartAlertViewModel::class.java)) {
            return SmartAlertViewModel(smartAlertRepository, userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
