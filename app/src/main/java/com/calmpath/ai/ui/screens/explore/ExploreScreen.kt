package com.calmpath.ai.ui.screens.explore

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.FormatListBulleted
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.calmpath.ai.data.remote.NetworkStatus
import com.calmpath.ai.ui.components.EmptyStateView
import com.calmpath.ai.ui.components.EnvironmentalHeatmapCanvas
import com.calmpath.ai.ui.components.FilterChipGroup
import com.calmpath.ai.ui.components.VerticalPlaceCard
import com.calmpath.ai.ui.theme.Sage100
import com.calmpath.ai.ui.theme.Sage700
import com.calmpath.ai.ui.theme.Sage800
import com.calmpath.ai.ui.viewmodel.ExploreViewModel

/**
 * Screen 4: Explore Screen with Search, Filter Chips, Environmental Heatmap,
 * Location Awareness, and Offline Fallback (CO1, CO2, CO5).
 */
@Composable
fun ExploreScreen(
    viewModel: ExploreViewModel,
    onNavigateToDetails: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp)
        ) {
            // Top Section: Search Bar & Toggle View
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                // Location & Online/Offline Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.LocationOn,
                            contentDescription = "Location",
                            tint = Sage800,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = uiState.currentLocality,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    val isOnline = uiState.networkStatus == NetworkStatus.ONLINE
                    Text(
                        text = if (isOnline) "● Live GPS" else "○ Offline Mode",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isOnline) Sage800 else MaterialTheme.colorScheme.error
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = { viewModel.onSearchQueryChanged(it) },
                        placeholder = { Text("Search peaceful places in India...") },
                        leadingIcon = {
                            Icon(Icons.Rounded.Search, contentDescription = "Search", tint = Sage800)
                        },
                        trailingIcon = {
                            if (uiState.searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                    Icon(Icons.Rounded.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        shape = RoundedCornerShape(20.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Sage800,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        ),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(
                        onClick = { viewModel.toggleViewMode() },
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(48.dp)
                    ) {
                        Icon(
                            imageVector = if (uiState.isMapView) Icons.AutoMirrored.Rounded.FormatListBulleted else Icons.Rounded.Map,
                            contentDescription = "Toggle View",
                            tint = Sage800,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Filter Chips
                FilterChipGroup(
                    categories = uiState.categories,
                    selectedCategory = uiState.selectedCategory,
                    onCategorySelected = { viewModel.onCategorySelected(it) }
                )
            }

            // CO5: Offline Banner with Retry Button
            AnimatedVisibility(visible = uiState.networkStatus == NetworkStatus.OFFLINE) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CloudOff,
                                contentDescription = "Offline",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "No internet connection. Showing cached places from Room.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Button(
                            onClick = { viewModel.refreshNearbyPlaces() },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Sage800),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Refresh,
                                contentDescription = "Retry",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Retry", style = MaterialTheme.typography.labelSmall, color = Color.White)
                        }
                    }
                }
            }

            // CO5: Outside India Notice
            AnimatedVisibility(visible = uiState.isOutsideIndia) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Public,
                            contentDescription = "Country Notice",
                            tint = Sage800,
                            modifier = Modifier.size(20.dp)
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

            // Loading indicator
            AnimatedVisibility(visible = uiState.isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp),
                    color = Sage800,
                    trackColor = Sage100
                )
            }

            // Main Content: Map Heatmap vs List View
            if (uiState.isMapView) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    EnvironmentalHeatmapCanvas(
                        zones = uiState.heatmapZones,
                        places = uiState.filteredPlaces,
                        selectedPlace = uiState.selectedPlaceForPreview,
                        onSelectPlace = { viewModel.onSelectPlace(it) },
                        onViewPlaceDetails = onNavigateToDetails,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            } else {
                if (uiState.filteredPlaces.isEmpty()) {
                    EmptyStateView(
                        emoji = "🔍",
                        title = "No Peaceful Places Found",
                        subtitle = "Try adjusting your search query or choosing another category.",
                        actionButtonText = "Reset Filters",
                        onActionClick = {
                            viewModel.onSearchQueryChanged("")
                            viewModel.onCategorySelected("All")
                        }
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.filteredPlaces) { place ->
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
    }
}
