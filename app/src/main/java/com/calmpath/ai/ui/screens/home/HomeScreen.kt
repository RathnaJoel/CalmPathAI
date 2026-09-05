package com.calmpath.ai.ui.screens.home

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.calmpath.ai.data.remote.NetworkStatus
import com.calmpath.ai.ui.components.AqiIndicatorCard
import com.calmpath.ai.ui.components.DecibelMeterCard
import com.calmpath.ai.ui.components.HorizontalPlaceCard
import com.calmpath.ai.ui.components.PeaceScoreCard
import com.calmpath.ai.ui.components.VerticalPlaceCard
import com.calmpath.ai.ui.components.WeatherCard
import com.calmpath.ai.ui.theme.OceanTeal
import com.calmpath.ai.ui.theme.Sage100
import com.calmpath.ai.ui.theme.Sage700
import com.calmpath.ai.ui.theme.Sage800
import com.calmpath.ai.ui.viewmodel.DataLoadState
import com.calmpath.ai.ui.viewmodel.HomeViewModel
import java.util.Calendar

/**
 * Screen 3 & 5: Home Dashboard & Recommended Places (CO1, CO2, CO3, CO4, CO5).
 * Integrates live REST API environmental telemetry, Location Services, and Room caching.
 */
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToDetails: (String) -> Unit,
    onChangeMoodClick: () -> Unit,
    onNavigateToExplore: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    // Location Permission Launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        viewModel.onLocationPermissionResult(fineGranted || coarseGranted)
    }

    LaunchedEffect(Unit) {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    val greeting = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
        in 5..11 -> "Good morning!"
        in 12..16 -> "Good afternoon!"
        in 17..21 -> "Good evening!"
        else -> "Peaceful night!"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            // Header: Greeting & Mood Badge
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = greeting,
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Where would you like to go today?",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Mood selector pill button
                        Card(
                            modifier = Modifier.clickable(onClick = onChangeMoodClick),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Sage100)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = uiState.selectedMood.emoji,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = uiState.selectedMood.title,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Sage800
                                )
                                Icon(
                                    imageVector = Icons.Rounded.Edit,
                                    contentDescription = "Change Mood",
                                    tint = Sage800,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }

            // CO5: Location Banner & Network Telemetry Pill
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.LocationOn,
                                contentDescription = "Location",
                                tint = Sage800,
                                modifier = Modifier.size(18.dp)
                            )
                            Column {
                                Text(
                                    text = uiState.currentLocality,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Updated just now",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                                )
                            }
                        }

                        // Status Badge: Live API vs Cached Room
                        val isLive = uiState.environmentalSummary.isLive
                        val badgeBg = if (isLive) Sage100 else MaterialTheme.colorScheme.surface
                        val badgeColor = if (isLive) Sage800 else OceanTeal
                        val badgeText = if (isLive) "● Live API" else "○ Room Cache"

                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(badgeBg)
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = badgeText,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = badgeColor
                            )
                        }
                    }
                }
            }

            // CO5: Offline Banner with Retry Button
            item {
                val isOffline = uiState.networkStatus == NetworkStatus.OFFLINE || uiState.dataLoadState is DataLoadState.OfflineCached
                AnimatedVisibility(visible = isOffline) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.CloudOff,
                                    contentDescription = "Offline",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "No internet connection. Showing last available data.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Button(
                                onClick = { viewModel.refreshEnvironmentalData() },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Sage800),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Refresh,
                                    contentDescription = "Retry",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "Retry", style = MaterialTheme.typography.labelSmall, color = Color.White)
                            }
                        }
                    }
                }
            }

            // CO5: Outside India Notice
            item {
                AnimatedVisibility(visible = uiState.dataLoadState is DataLoadState.OutsideIndia) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Public,
                                contentDescription = "Country Notice",
                                tint = Sage800,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "CalmPath is currently available only in India.",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Loading state indicator
            item {
                AnimatedVisibility(visible = uiState.isLoading) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 4.dp)
                    ) {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp),
                            color = Sage800,
                            trackColor = Sage100
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Updating environmental data...",
                            style = MaterialTheme.typography.labelSmall,
                            color = Sage700
                        )
                    }
                }
            }

            // Environmental Dashboard Section (CO5)
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Peace Score Summary Card (Calculated via PeaceScoreCalculator)
                    PeaceScoreCard(
                        score = uiState.environmentalSummary.peaceScore,
                        description = uiState.environmentalSummary.peaceDescription
                    )

                    // Decibel / Noise Meter Card (Clearly distinguished as baseline estimate)
                    Column {
                        DecibelMeterCard(
                            currentDb = uiState.environmentalSummary.noiseDb
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Acoustic baseline: Estimated peaceful ambient",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.padding(start = 6.dp)
                        )
                    }

                    // Side-by-Side Live AQI & Weather Cards
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AqiIndicatorCard(
                            currentAqi = uiState.environmentalSummary.aqi,
                            modifier = Modifier.weight(1f)
                        )

                        WeatherCard(
                            temperatureC = uiState.environmentalSummary.temperatureC,
                            weatherCondition = uiState.environmentalSummary.weatherCondition,
                            humidityPercent = uiState.environmentalSummary.humidityPercent,
                            weatherIcon = uiState.environmentalSummary.weatherIcon,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Recommended Places Section
            item {
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Places You Might Like",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Curated for '${uiState.selectedMood.title}' mood",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Text(
                        text = "Explore All",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Sage800,
                        modifier = Modifier.clickable(onClick = onNavigateToExplore)
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Horizontal Carousel of places
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(uiState.recommendedPlaces.take(4)) { place ->
                        HorizontalPlaceCard(
                            place = place,
                            isFavorite = uiState.favoritePlaceIds.contains(place.id),
                            onPlaceClick = {
                                viewModel.onPlaceClicked(place, onNavigateToDetails)
                            },
                            onFavoriteToggle = {
                                viewModel.toggleFavorite(place)
                            }
                        )
                    }
                }
            }

            // Additional Vertical Places Section
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Nearby Quiet Sanctuaries",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            items(uiState.recommendedPlaces.drop(4)) { place ->
                Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
                    VerticalPlaceCard(
                        place = place,
                        isFavorite = uiState.favoritePlaceIds.contains(place.id),
                        onPlaceClick = {
                            viewModel.onPlaceClicked(place, onNavigateToDetails)
                        },
                        onFavoriteToggle = {
                            viewModel.toggleFavorite(place)
                        }
                    )
                }
            }
        }
    }
}
