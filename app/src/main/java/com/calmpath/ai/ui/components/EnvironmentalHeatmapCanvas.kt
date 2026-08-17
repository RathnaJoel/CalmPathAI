package com.calmpath.ai.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.calmpath.ai.data.model.CalmnessLevel
import com.calmpath.ai.data.model.HeatmapZone
import com.calmpath.ai.data.model.Place
import com.calmpath.ai.ui.theme.QualityGoodGreen
import com.calmpath.ai.ui.theme.QualityModerateYellow
import com.calmpath.ai.ui.theme.QualityPoorRed
import com.calmpath.ai.ui.theme.QualityPristineBlue
import com.calmpath.ai.ui.theme.QualityUnhealthyOrange
import com.calmpath.ai.ui.theme.Sage800

/**
 * Environmental Heatmap Canvas (CO1 & CO2).
 * Visualizes geographic calmness, noise levels, and air quality using a 5-tier color scale.
 */
@Composable
fun EnvironmentalHeatmapCanvas(
    zones: List<HeatmapZone>,
    places: List<Place>,
    selectedPlace: Place?,
    onSelectPlace: (Place) -> Unit,
    onViewPlaceDetails: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var activeZoneId by remember { mutableStateOf<String?>(null) }
    var zoomLevel by remember { mutableStateOf(1.0f) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFFE9F0EA))
    ) {
        // Map Surface Canvas
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { tapOffset ->
                        val w = size.width
                        val h = size.height

                        // Find closest zone
                        val tappedZone = zones.minByOrNull { zone ->
                            val zX = zone.relativeX * w
                            val zY = zone.relativeY * h
                            val dx = tapOffset.x - zX
                            val dy = tapOffset.y - zY
                            dx * dx + dy * dy
                        }

                        if (tappedZone != null) {
                            val zX = tappedZone.relativeX * w
                            val zY = tappedZone.relativeY * h
                            val dist = kotlin.math.sqrt(((tapOffset.x - zX) * (tapOffset.x - zX) + (tapOffset.y - zY) * (tapOffset.y - zY)).toDouble())
                            if (dist <= tappedZone.radiusPx * 1.5) {
                                activeZoneId = tappedZone.id
                                val matchedPlace = places.firstOrNull { it.id == tappedZone.associatedPlaceId }
                                if (matchedPlace != null) {
                                    onSelectPlace(matchedPlace)
                                }
                            }
                        }
                    }
                }
        ) {
            val w = size.width
            val h = size.height

            // 1. Draw subtle map grid lines & streets
            val gridSpacing = 40.dp.toPx()
            for (x in 0..(w / gridSpacing).toInt()) {
                drawLine(
                    color = Color(0xFFD3E0D6).copy(alpha = 0.5f),
                    start = Offset(x * gridSpacing, 0f),
                    end = Offset(x * gridSpacing, h),
                    strokeWidth = 1.dp.toPx()
                )
            }
            for (y in 0..(h / gridSpacing).toInt()) {
                drawLine(
                    color = Color(0xFFD3E0D6).copy(alpha = 0.5f),
                    start = Offset(0f, y * gridSpacing),
                    end = Offset(w, y * gridSpacing),
                    strokeWidth = 1.dp.toPx()
                )
            }

            // 2. Draw curved river / blue waterway
            val riverPath = Path().apply {
                moveTo(w * 0.1f, 0f)
                cubicTo(
                    w * 0.35f, h * 0.3f,
                    w * 0.6f, h * 0.2f,
                    w * 0.9f, h * 0.45f
                )
                cubicTo(
                    w * 0.98f, h * 0.6f,
                    w * 0.85f, h * 0.85f,
                    w * 0.7f, h
                )
            }
            drawPath(
                path = riverPath,
                color = Color(0xFFBEE1E6).copy(alpha = 0.6f),
                style = Stroke(width = 16.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
            )

            // 3. Draw Heatmap Gradient Blobs for Each Environmental Zone
            zones.forEach { zone ->
                val center = Offset(zone.relativeX * w, zone.relativeY * h)
                val color = Color(zone.calmnessLevel.colorHex)
                val radius = zone.radiusPx * zoomLevel

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            color.copy(alpha = 0.45f),
                            color.copy(alpha = 0.20f),
                            Color.Transparent
                        ),
                        center = center,
                        radius = radius
                    ),
                    radius = radius,
                    center = center
                )

                // Calmness ring
                drawCircle(
                    color = color.copy(alpha = 0.6f),
                    radius = radius * 0.6f,
                    center = center,
                    style = Stroke(width = 1.5.dp.toPx())
                )
            }

            // 4. Draw Place Pin Markers
            places.forEach { place ->
                val z = zones.firstOrNull { it.associatedPlaceId == place.id }
                val pinCenter = if (z != null) {
                    Offset(z.relativeX * w, z.relativeY * h)
                } else {
                    Offset(w * 0.5f, h * 0.5f)
                }

                val isSelected = selectedPlace?.id == place.id
                val markerColor = if (isSelected) Sage800 else Color(0xFF2D6A4F)

                // Outer pulsing ring if selected
                if (isSelected) {
                    drawCircle(
                        color = markerColor.copy(alpha = 0.25f),
                        radius = 24.dp.toPx(),
                        center = pinCenter
                    )
                }

                // Marker background
                drawCircle(
                    color = Color.White,
                    radius = 16.dp.toPx(),
                    center = pinCenter
                )
                drawCircle(
                    color = markerColor,
                    radius = 14.dp.toPx(),
                    center = pinCenter
                )
                // Center inner dot
                drawCircle(
                    color = Color.White,
                    radius = 5.dp.toPx(),
                    center = pinCenter
                )
            }
        }

        // Top Overlay: Legend & Controls
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Environmental Quality Legend
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                        Text(
                            text = "Environmental Quality",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            LegendItem(color = QualityPoorRed, label = "Poor")
                            LegendItem(color = QualityUnhealthyOrange, label = "Mod-Poor")
                            LegendItem(color = QualityModerateYellow, label = "Moderate")
                            LegendItem(color = QualityGoodGreen, label = "Good")
                            LegendItem(color = QualityPristineBlue, label = "Pristine")
                        }
                    }
                }

                // Map controls (Zoom & Recenter)
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    MapActionButton(
                        icon = Icons.Rounded.Add,
                        onClick = { zoomLevel = (zoomLevel + 0.2f).coerceAtMost(1.8f) }
                    )
                    MapActionButton(
                        icon = Icons.Rounded.Remove,
                        onClick = { zoomLevel = (zoomLevel - 0.2f).coerceAtLeast(0.8f) }
                    )
                    MapActionButton(
                        icon = Icons.Rounded.MyLocation,
                        onClick = {
                            zoomLevel = 1.0f
                            activeZoneId = null
                        }
                    )
                }
            }
        }

        // Bottom Selected Place Preview Card
        AnimatedVisibility(
            visible = selectedPlace != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(14.dp)
        ) {
            if (selectedPlace != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = selectedPlace.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${selectedPlace.category} • ${selectedPlace.distanceKm} km away",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            PeaceScoreChip(score = selectedPlace.peaceScore)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(
                                    text = "🍃 AQI: ${selectedPlace.aqi}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = QualityGoodGreen
                                )
                                Text(
                                    text = "🔊 Noise: ${selectedPlace.noiseDb} dB",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Button(
                                onClick = { onViewPlaceDetails(selectedPlace.id) },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Sage800),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "View Details",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun MapActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(18.dp)
        )
    }
}
