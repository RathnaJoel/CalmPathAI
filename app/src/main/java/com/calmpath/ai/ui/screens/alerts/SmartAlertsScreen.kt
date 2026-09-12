package com.calmpath.ai.ui.screens.alerts

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ClearAll
import androidx.compose.material.icons.rounded.CloudSync
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.Hearing
import androidx.compose.material.icons.rounded.NearMe
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Thunderstorm
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.calmpath.ai.data.local.entities.SmartAlertEntity
import com.calmpath.ai.ui.components.CalmPathTopAppBar
import com.calmpath.ai.ui.screens.alerts.AlertCard
import com.calmpath.ai.ui.theme.OceanTeal
import com.calmpath.ai.ui.theme.QualityGoodGreen
import com.calmpath.ai.ui.theme.QualityPoorRed
import com.calmpath.ai.ui.theme.Sage100
import com.calmpath.ai.ui.theme.Sage700
import com.calmpath.ai.ui.theme.Sage800
import com.calmpath.ai.ui.viewmodel.AlertFilter
import com.calmpath.ai.ui.viewmodel.SmartAlertViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Screen: Smart Alerts & Notifications Center (CO10).
 * Displays real-time and historical alerts, read/unread status,
 * filter chips, interactive navigation actions, and lab demo simulation controls.
 */
@Composable
fun SmartAlertsScreen(
    viewModel: SmartAlertViewModel,
    onBackClick: () -> Unit,
    onNavigateToPlace: (String) -> Unit,
    onNavigateToExplore: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            CalmPathTopAppBar(
                title = "Smart Alerts",
                onBackClick = onBackClick,
                actions = {
                    if (uiState.unreadCount > 0) {
                        IconButton(onClick = { viewModel.markAllAsRead() }) {
                            Icon(
                                imageVector = Icons.Rounded.DoneAll,
                                contentDescription = "Mark all read",
                                tint = Sage800
                            )
                        }
                    }
                    if (uiState.alerts.isNotEmpty()) {
                        IconButton(onClick = { viewModel.clearAllAlerts() }) {
                            Icon(
                                imageVector = Icons.Rounded.ClearAll,
                                contentDescription = "Clear all alerts",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Section 1: CO10 Lab Demonstration Controls
                item {
                    DemoSimulationCard(
                        isSimulating = uiState.isSimulating,
                        simulationResult = uiState.simulationResult,
                        onSimulate = { scenario -> viewModel.simulateAlertScenario(scenario) }
                    )
                }

                // Section 2: Filter Chips
                item {
                    FilterChipsRow(
                        selectedFilter = uiState.selectedFilter,
                        unreadCount = uiState.unreadCount,
                        onSelectFilter = { viewModel.setFilter(it) }
                    )
                }

                // Section 3: Alerts List
                if (uiState.filteredAlerts.isEmpty()) {
                    item {
                        EmptyAlertsView()
                    }
                } else {
                    items(uiState.filteredAlerts, key = { it.alertId }) { alert ->
                        AlertCard(
                            alert = alert,
                            onCardClick = {
                                viewModel.markAsRead(alert.alertId)
                                alert.placeId?.let { onNavigateToPlace(it) } ?: onNavigateToExplore()
                            },
                            onViewPlace = { placeId ->
                                viewModel.markAsRead(alert.alertId)
                                viewModel.markActionTaken(alert.alertId)
                                onNavigateToPlace(placeId)
                            },
                            onExplore = {
                                viewModel.markAsRead(alert.alertId)
                                viewModel.markActionTaken(alert.alertId)
                                onNavigateToExplore()
                            },
                            onMarkRead = { viewModel.markAsRead(alert.alertId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FilterChipsRow(
    selectedFilter: AlertFilter,
    unreadCount: Int,
    onSelectFilter: (AlertFilter) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(AlertFilter.entries) { filter ->
            val label = when (filter) {
                AlertFilter.ALL -> "All"
                AlertFilter.UNREAD -> if (unreadCount > 0) "Unread ($unreadCount)" else "Unread"
                AlertFilter.AQI -> "AQI"
                AlertFilter.NOISE -> "Noise"
                AlertFilter.WEATHER -> "Weather"
                AlertFilter.RECOMMENDATION -> "ML Match"
            }
            FilterChip(
                selected = selectedFilter == filter,
                onClick = { onSelectFilter(filter) },
                label = { Text(label, fontSize = 13.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Sage800,
                    selectedLabelColor = Color.White
                )
            )
        }
    }
}

@Composable
fun DemoSimulationCard(
    isSimulating: Boolean,
    simulationResult: String?,
    onSimulate: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Sage100.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.NotificationsActive,
                        contentDescription = null,
                        tint = Sage800,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "CO10 Lab Evaluation Simulation Panel",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Sage800
                    )
                }
                if (isSimulating) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Trigger real-time evaluation scenarios to test the Decision Engine, Cooldown Manager, and Notifications:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    Button(
                        onClick = { onSimulate("AQI_SPIKE") },
                        colors = ButtonDefaults.buttonColors(containerColor = QualityPoorRed),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text("AQI Spike (168)", fontSize = 12.sp)
                    }
                }
                item {
                    Button(
                        onClick = { onSimulate("HIGH_NOISE") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text("High Noise (78 dB)", fontSize = 12.sp)
                    }
                }
                item {
                    Button(
                        onClick = { onSimulate("WEATHER_RAIN") },
                        colors = ButtonDefaults.buttonColors(containerColor = OceanTeal),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text("Rain Weather", fontSize = 12.sp)
                    }
                }
                item {
                    Button(
                        onClick = { onSimulate("ML_RECOMMENDATION") },
                        colors = ButtonDefaults.buttonColors(containerColor = Sage800),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text("🧠 ML Top Match", fontSize = 12.sp)
                    }
                }
            }

            simulationResult?.let { result ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = result,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Sage800
                )
            }
        }
    }
}

@Composable
fun AlertCard(
    alert: SmartAlertEntity,
    onCardClick: () -> Unit,
    onViewPlace: (String) -> Unit,
    onExplore: () -> Unit,
    onMarkRead: () -> Unit
) {
    val (icon, iconBg, badgeColor) = when (alert.alertType) {
        SmartAlertEntity.TYPE_AQI_ALERT -> Triple(
            Icons.Rounded.Air,
            QualityPoorRed.copy(alpha = 0.15f),
            QualityPoorRed
        )
        SmartAlertEntity.TYPE_NOISE_ALERT -> Triple(
            Icons.Rounded.Hearing,
            Color(0xFFD97706).copy(alpha = 0.15f),
            Color(0xFFD97706)
        )
        SmartAlertEntity.TYPE_WEATHER_ALERT -> Triple(
            Icons.Rounded.Thunderstorm,
            OceanTeal.copy(alpha = 0.15f),
            OceanTeal
        )
        SmartAlertEntity.TYPE_ML_RECOMMENDATION -> Triple(
            Icons.Rounded.AutoAwesome,
            Sage800.copy(alpha = 0.15f),
            Sage800
        )
        else -> Triple(
            Icons.Rounded.WarningAmber,
            Sage800.copy(alpha = 0.15f),
            Sage800
        )
    }

    val formattedDate = SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault()).format(Date(alert.createdAt))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (alert.isRead) MaterialTheme.colorScheme.surface
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (alert.isRead) 1.dp else 3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(iconBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = icon, contentDescription = null, tint = badgeColor, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = alert.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = if (alert.isRead) FontWeight.SemiBold else FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = formattedDate,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (!alert.isRead) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Sage800)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = alert.message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Metric Chip if present
            if (alert.mlSuitabilityScore != null || alert.triggerValue > 0) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (alert.mlSuitabilityScore != null) {
                        MetricChip(text = "ML Match: ${alert.mlSuitabilityScore}%", color = Sage800)
                    }
                    if (alert.alertType == SmartAlertEntity.TYPE_AQI_ALERT) {
                        MetricChip(text = "AQI: ${alert.triggerValue.toInt()}", color = QualityPoorRed)
                    } else if (alert.alertType == SmartAlertEntity.TYPE_NOISE_ALERT) {
                        MetricChip(text = "Noise: ${alert.triggerValue.toInt()} dB", color = Color(0xFFD97706))
                    }
                }
            }

            // Action Buttons
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!alert.isRead) {
                    OutlinedButton(
                        onClick = onMarkRead,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Mark Read", fontSize = 11.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }

                if (alert.placeId != null) {
                    Button(
                        onClick = { onViewPlace(alert.placeId) },
                        colors = ButtonDefaults.buttonColors(containerColor = Sage800),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("View Place", fontSize = 11.sp)
                    }
                } else {
                    Button(
                        onClick = onExplore,
                        colors = ButtonDefaults.buttonColors(containerColor = Sage800),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Explore Places", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun MetricChip(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(text = text, color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun EmptyAlertsView() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(Sage100),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.NotificationsNone,
                contentDescription = null,
                tint = Sage700,
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Tranquility Maintained",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "No environmental alerts or notifications. All monitored parameters are within your serene thresholds.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}
