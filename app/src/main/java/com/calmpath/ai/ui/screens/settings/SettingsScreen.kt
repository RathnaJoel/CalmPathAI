package com.calmpath.ai.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.calmpath.ai.ui.components.CalmPathTopAppBar
import com.calmpath.ai.ui.theme.OceanTeal
import com.calmpath.ai.ui.theme.QualityGoodGreen
import com.calmpath.ai.ui.theme.QualityPoorRed
import com.calmpath.ai.ui.theme.Sage100
import com.calmpath.ai.ui.theme.Sage800
import com.calmpath.ai.ui.viewmodel.SettingsViewModel

/**
 * Screen 14: Settings Screen (CO1, CO2, CO3, CO4).
 */
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBackClick: () -> Unit,
    onLogoutSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    var showClearFavsToast by remember { mutableStateOf(false) }
    var showClearHistToast by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            CalmPathTopAppBar(
                title = "Settings",
                onBackClick = onBackClick
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Section 1: Appearance & Theme
                SettingsSection(title = "Appearance", icon = Icons.Rounded.Palette) {
                    Text(
                        text = "App Theme",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ThemeOptionChip(
                            title = "☀️ Light",
                            isSelected = uiState.themeMode.equals("LIGHT", ignoreCase = true),
                            onSelect = { viewModel.onThemeSelected("LIGHT") },
                            modifier = Modifier.weight(1f)
                        )
                        ThemeOptionChip(
                            title = "🌙 Dark",
                            isSelected = uiState.themeMode.equals("DARK", ignoreCase = true),
                            onSelect = { viewModel.onThemeSelected("DARK") },
                            modifier = Modifier.weight(1f)
                        )
                        ThemeOptionChip(
                            title = "⚙️ System",
                            isSelected = uiState.themeMode.equals("SYSTEM", ignoreCase = true),
                            onSelect = { viewModel.onThemeSelected("SYSTEM") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Section 2: Notifications
                SettingsSection(title = "Notifications & AI Alerts", icon = Icons.Rounded.Notifications) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Calmness & AQI Alerts",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Receive gentle alerts when nearby areas reach pristine peace scores.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = uiState.notificationsEnabled,
                            onCheckedChange = { viewModel.onNotificationsToggled(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Sage800
                            )
                        )
                    }
                }

                // Section 3: Environmental Tolerances (Sliders)
                SettingsSection(title = "Environmental Tolerances", icon = Icons.Rounded.Tune) {
                    // Max AQI Slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Maximum Acceptable AQI",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${uiState.maxAqi.toInt()} AQI",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = QualityGoodGreen
                            )
                        }
                        Slider(
                            value = uiState.maxAqi,
                            onValueChange = { viewModel.onMaxAqiChanged(it) },
                            valueRange = 10f..150f,
                            steps = 13,
                            colors = SliderDefaults.colors(
                                thumbColor = Sage800,
                                activeTrackColor = Sage800
                            )
                        )
                        Text(
                            text = "CalmPath will filter out places exceeding this air pollution threshold.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Preferred Noise Level Slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Preferred Max Noise Level",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${uiState.preferredNoiseDb.toInt()} dB",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = OceanTeal
                            )
                        }
                        Slider(
                            value = uiState.preferredNoiseDb,
                            onValueChange = { viewModel.onPreferredNoiseChanged(it) },
                            valueRange = 25f..75f,
                            steps = 9,
                            colors = SliderDefaults.colors(
                                thumbColor = OceanTeal,
                                activeTrackColor = OceanTeal
                            )
                        )
                        Text(
                            text = "Acoustic recommendations will stay within this ambient sound range.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Preferred Distance Radius Slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Preferred Search Radius",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${uiState.preferredDistanceKm.toInt()} km",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Sage800
                            )
                        }
                        Slider(
                            value = uiState.preferredDistanceKm,
                            onValueChange = { viewModel.onPreferredDistanceChanged(it) },
                            valueRange = 1f..25f,
                            steps = 23,
                            colors = SliderDefaults.colors(
                                thumbColor = Sage800,
                                activeTrackColor = Sage800
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { viewModel.saveEnvironmentalPreferences() },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Sage800),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Rounded.Save, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save Tolerance Preferences", fontWeight = FontWeight.Bold)
                    }
                }

                // Section 4: Data Management (Room DB clearing)
                SettingsSection(title = "Local Database & Storage (CO3)", icon = Icons.Rounded.DeleteOutline) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(
                            onClick = {
                                viewModel.clearFavorites()
                                showClearFavsToast = true
                            },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (showClearFavsToast) "✓ Favorites Cleared" else "Clear Local Favorites (Room DB)",
                                color = QualityPoorRed,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.clearHistory()
                                showClearHistToast = true
                            },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (showClearHistToast) "✓ History Cleared" else "Clear Browsing History (Room DB)",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // Section 5: Account & Logout
                SettingsSection(title = "Account", icon = Icons.AutoMirrored.Rounded.Logout) {
                    Button(
                        onClick = { viewModel.logout(onLogoutSuccess) },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = QualityPoorRed),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Log Out of CalmPath", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Sage100),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Sage800,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            content()
        }
    }
}

@Composable
private fun ThemeOptionChip(
    title: String,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = isSelected,
        onClick = onSelect,
        label = {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        },
        leadingIcon = if (isSelected) {
            { Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
        } else null,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = Sage800,
            selectedLabelColor = Color.White,
            selectedLeadingIconColor = Color.White
        ),
        modifier = modifier
    )
}
