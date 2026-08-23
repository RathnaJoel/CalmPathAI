package com.calmpath.ai.ui.screens.history

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.calmpath.ai.data.local.entities.PlaceHistoryWithPlace
import com.calmpath.ai.ui.components.CalmPathTopAppBar
import com.calmpath.ai.ui.components.EmptyStateView
import com.calmpath.ai.ui.components.PeaceScoreChip
import com.calmpath.ai.ui.theme.QualityGoodGreen
import com.calmpath.ai.ui.theme.QualityPoorRed
import com.calmpath.ai.ui.viewmodel.HistoryViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Screen: Browsing & Exploration History (CO3: PlaceHistoryEntity).
 */
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    onNavigateToDetails: (String) -> Unit,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showClearDialog by remember { mutableStateOf(false) }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear History?") },
            text = { Text("This will permanently remove all viewed sanctuary records from your local Room database.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearHistory()
                        showClearDialog = false
                    }
                ) {
                    Text("Clear All", color = QualityPoorRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top App Bar
            CalmPathTopAppBar(
                title = "Browsing History",
                onBackClick = onBackClick,
                actions = {
                    if (uiState.historyList.isNotEmpty()) {
                        IconButton(onClick = { showClearDialog = true }) {
                            Icon(
                                imageVector = Icons.Rounded.Delete,
                                contentDescription = "Clear History",
                                tint = QualityPoorRed
                            )
                        }
                    }
                }
            )

            if (uiState.historyList.isEmpty()) {
                EmptyStateView(
                    emoji = "🕒",
                    title = "No History Recorded Yet",
                    subtitle = "Places and sanctuaries you inspect on CalmPath will be chronologically logged here in your local Room database.",
                    actionButtonText = "Go Back",
                    onActionClick = onBackClick
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(uiState.historyList) { item ->
                        HistoryCard(
                            item = item,
                            onClick = { onNavigateToDetails(item.place.placeId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryCard(
    item: PlaceHistoryWithPlace,
    onClick: () -> Unit
) {
    val history = item.history
    val place = item.place
    val formattedDate = remember(history.viewedAt) {
        SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault()).format(Date(history.viewedAt))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail Image
            if (place.imageUrl.isNotBlank()) {
                AsyncImage(
                    model = place.imageUrl,
                    contentDescription = place.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
                Spacer(modifier = Modifier.width(12.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = place.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PeaceScoreChip(score = history.peaceScoreAtVisit)
                    Text(
                        text = "AQI ${history.aqiAtVisit}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = QualityGoodGreen
                    )
                    Text(
                        text = "${history.noiseLevelAtVisit} dB",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
