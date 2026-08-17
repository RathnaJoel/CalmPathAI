package com.calmpath.ai.ui.screens.favorites

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.calmpath.ai.ui.components.EmptyStateView
import com.calmpath.ai.ui.components.VerticalPlaceCard
import com.calmpath.ai.ui.theme.QualityPoorRed
import com.calmpath.ai.ui.theme.Sage800
import com.calmpath.ai.ui.viewmodel.FavoritesViewModel

/**
 * Screen 6: Favorites Screen (CO1, CO2, CO3).
 */
@Composable
fun FavoritesScreen(
    viewModel: FavoritesViewModel,
    onNavigateToDetails: (String) -> Unit,
    onExploreClick: () -> Unit
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
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Saved Sanctuaries",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "${uiState.favoritePlaces.size} peaceful places saved in Room DB",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (uiState.favoritePlaces.isNotEmpty()) {
                    TextButton(onClick = { viewModel.clearAllFavorites() }) {
                        Text(
                            text = "Clear All",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = QualityPoorRed
                        )
                    }
                }
            }

            if (uiState.favoritePlaces.isEmpty()) {
                EmptyStateView(
                    emoji = "🌿",
                    title = "Your peaceful places will appear here.",
                    subtitle = "Browse recommendations and bookmark your favorite clean-air and low-noise spots for instant access.",
                    actionButtonText = "Explore Places",
                    onActionClick = onExploreClick
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.favoritePlaces) { place ->
                        VerticalPlaceCard(
                            place = place,
                            isFavorite = true,
                            onPlaceClick = { onNavigateToDetails(place.id) },
                            onFavoriteToggle = { viewModel.removeFavorite(place) }
                        )
                    }
                }
            }
        }
    }
}
