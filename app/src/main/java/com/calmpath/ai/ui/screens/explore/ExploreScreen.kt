package com.calmpath.ai.ui.screens.explore

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.automirrored.rounded.FormatListBulleted
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.calmpath.ai.ui.components.EmptyStateView
import com.calmpath.ai.ui.components.EnvironmentalHeatmapCanvas
import com.calmpath.ai.ui.components.FilterChipGroup
import com.calmpath.ai.ui.components.VerticalPlaceCard
import com.calmpath.ai.ui.theme.Sage800
import com.calmpath.ai.ui.viewmodel.ExploreViewModel

/**
 * Screen 4: Explore Screen with Search, Filter Chips, and Environmental Heatmap (CO1 & CO2).
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
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = { viewModel.onSearchQueryChanged(it) },
                        placeholder = { Text("Search peaceful places...") },
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

                Spacer(modifier = Modifier.height(10.dp))

                // Filter Chips
                FilterChipGroup(
                    categories = uiState.categories,
                    selectedCategory = uiState.selectedCategory,
                    onCategorySelected = { viewModel.onCategorySelected(it) }
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
                        title = "No peaceful spots found",
                        subtitle = "Try adjusting your search query or category filter to discover more places.",
                        actionButtonText = "Reset Filters",
                        onActionClick = {
                            viewModel.onSearchQueryChanged("")
                            viewModel.onCategorySelected("All")
                        }
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
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
