package com.calmpath.ai.ui.screens.home

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.calmpath.ai.ui.components.AqiIndicatorCard
import com.calmpath.ai.ui.components.DecibelMeterCard
import com.calmpath.ai.ui.components.HorizontalPlaceCard
import com.calmpath.ai.ui.components.PeaceScoreCard
import com.calmpath.ai.ui.components.VerticalPlaceCard
import com.calmpath.ai.ui.components.WeatherCard
import com.calmpath.ai.ui.theme.Sage100
import com.calmpath.ai.ui.theme.Sage800
import com.calmpath.ai.ui.viewmodel.HomeViewModel
import java.util.Calendar

/**
 * Screen 3 & 5: Home Dashboard & Recommended Places (CO1, CO2, CO3).
 */
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToDetails: (String) -> Unit,
    onChangeMoodClick: () -> Unit,
    onNavigateToExplore: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

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
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 90.dp)
        ) {
            // Header: Greeting & Mood Badge
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
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

            // Environmental Dashboard Section
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Peace Score Summary Card
                    PeaceScoreCard(
                        score = uiState.environmentalSummary.peaceScore,
                        description = uiState.environmentalSummary.peaceDescription
                    )

                    // Decibel / Noise Meter Card
                    DecibelMeterCard(
                        currentDb = uiState.environmentalSummary.noiseDb
                    )

                    // Side-by-Side AQI & Weather Cards
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
                Spacer(modifier = Modifier.height(24.dp))
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
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp),
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
