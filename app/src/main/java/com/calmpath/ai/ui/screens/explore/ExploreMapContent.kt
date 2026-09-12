package com.calmpath.ai.ui.screens.explore

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.Navigation
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.calmpath.ai.BuildConfig
import com.calmpath.ai.data.model.CalmnessLevel
import com.calmpath.ai.data.model.HeatmapZone
import com.calmpath.ai.data.model.Place
import com.calmpath.ai.ui.theme.OceanTeal
import com.calmpath.ai.ui.theme.QualityGoodGreen
import com.calmpath.ai.ui.theme.QualityModerateYellow
import com.calmpath.ai.ui.theme.QualityPoorRed
import com.calmpath.ai.ui.theme.Sage100
import com.calmpath.ai.ui.theme.Sage800
import com.calmpath.ai.util.NavigationUtils
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState

/**
 * Google Maps Content for CalmPath AI Explore Screen (CO6).
 *
 * Displays:
 * 1. Interactive Google Map centered on user's live GPS coordinates (or default Mumbai, India).
 * 2. Real-time GPS location pin (when permission granted).
 * 3. Peaceful Sanctuary markers with Category icons and Peace Scores.
 * 4. CalmPath Environmental Tranquility Zones (Circles colored Red, Orange, Yellow, Green, Blue).
 * 5. Interactive selected place preview card with [View Details] and [Navigate] actions.
 * 6. "My Location" re-center floating action button.
 */
@Composable
fun ExploreMapContent(
    places: List<Place>,
    heatmapZones: List<HeatmapZone>,
    selectedPlace: Place?,
    currentLat: Double,
    currentLon: Double,
    cameraTargetLat: Double,
    cameraTargetLon: Double,
    cameraMoveTrigger: Long,
    hasLocationPermission: Boolean,
    activeRouteDestination: Place? = null,
    routeCoordinates: List<Pair<Double, Double>> = emptyList(),
    onMarkerClick: (Place) -> Unit,
    onDismissPreview: () -> Unit,
    onViewDetailsClick: (String) -> Unit,
    onMyLocationClick: () -> Unit,
    onStartInAppNavigation: (Place) -> Unit = {},
    onStopInAppNavigation: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val initialPosition = LatLng(cameraTargetLat, cameraTargetLon)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialPosition, 13f)
    }

    // Animate camera whenever user taps a marker, clicks "My Location", or GPS position updates
    LaunchedEffect(cameraMoveTrigger) {
        if (cameraMoveTrigger > 0L) {
            val target = LatLng(cameraTargetLat, cameraTargetLon)
            val zoom = if (activeRouteDestination != null) 14.5f else 14f
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(target, zoom),
                durationMs = 900
            )
        }
    }

    val isDemoApiKey = BuildConfig.MAPS_API_KEY.isBlank() || BuildConfig.MAPS_API_KEY == "YOUR_API_KEY_HERE"

    Box(modifier = modifier.fillMaxSize()) {
        // 1. Interactive Google Map
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isMyLocationEnabled = hasLocationPermission
            ),
            uiSettings = MapUiSettings(
                myLocationButtonEnabled = false,
                zoomControlsEnabled = false,
                compassEnabled = true
            ),
            onMapClick = {
                onDismissPreview()
            }
        ) {
            // CalmPath Environmental Tranquility Overlays (CO6)
            // Color-coded zones around sanctuaries: Red, Orange, Yellow, Green, Blue
            places.forEach { place ->
                val level = CalmnessLevel.fromScore(place.peaceScore)
                val zoneColor = Color(level.colorHex)
                Circle(
                    center = LatLng(place.latitude, place.longitude),
                    radius = 450.0, // 450 meters tranquil boundary
                    fillColor = zoneColor.copy(alpha = 0.22f),
                    strokeColor = zoneColor.copy(alpha = 0.70f),
                    strokeWidth = 3f
                )
            }

            // Peaceful Sanctuary Place Markers
            places.forEach { place ->
                Marker(
                    state = MarkerState(position = LatLng(place.latitude, place.longitude)),
                    title = "${place.categoryIcon} ${place.name}",
                    snippet = "Peace Score: ${place.peaceScore}/100 • AQI: ${place.aqi} • ${place.noiseDb} dB",
                    onClick = {
                        onMarkerClick(place)
                        false // Allow default info window display while selecting card
                    }
                )
            }

            // In-App Navigation Route Polyline (CO6)
            if (routeCoordinates.isNotEmpty()) {
                val routeLatLngs = routeCoordinates.map { LatLng(it.first, it.second) }
                // Outer tranquil glow path
                Polyline(
                    points = routeLatLngs,
                    color = OceanTeal.copy(alpha = 0.85f),
                    width = 14f
                )
                // Inner solid trail line
                Polyline(
                    points = routeLatLngs,
                    color = Sage800,
                    width = 6f
                )
                // Origin location pin
                Marker(
                    state = MarkerState(position = LatLng(currentLat, currentLon)),
                    title = "📍 Your Location",
                    snippet = "Navigation Starting Point"
                )
            }
        }

        // 2. Top-End Badge: Environmental Overlay Legend
        Surface(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 10.dp, end = 16.dp),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
            shadowElevation = 3.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "🌱 Tranquility Zones",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                // Legend dots
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(QualityPoorRed))
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(QualityModerateYellow))
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(QualityGoodGreen))
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(OceanTeal))
            }
        }

        // 3. Demo API Key Notice (if developer hasn't replaced placeholder in local.properties)
        if (isDemoApiKey) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.92f),
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Info,
                        contentDescription = "Maps Info",
                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Demo Mode: Add MAPS_API_KEY in local.properties for production tiles.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // 3b. Active In-App Navigation HUD Banner (CO6)
        AnimatedVisibility(
            visible = activeRouteDestination != null,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = if (isDemoApiKey) 40.dp else 10.dp),
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
        ) {
            activeRouteDestination?.let { dest ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(QualityGoodGreen)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "🧭 In-App Route Active",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Sage800
                                )
                            }
                            IconButton(
                                onClick = onStopInAppNavigation,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "Stop Navigation",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "${dest.categoryIcon} Walking to ${dest.name}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            text = "${dest.distanceKm} km • ~${(dest.distanceKm * 12).toInt()} min walk • 🌿 Low Noise Corridor",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = onStopInAppNavigation,
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                ),
                                modifier = Modifier.weight(1f).height(38.dp)
                            ) {
                                Text(
                                    text = "Stop Route",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Button(
                                onClick = {
                                    NavigationUtils.launchGoogleMapsNavigation(
                                        context = context,
                                        destinationLat = dest.latitude,
                                        destinationLon = dest.longitude,
                                        destinationName = dest.name,
                                        mode = "w"
                                    )
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Sage800),
                                modifier = Modifier.weight(1.3f).height(38.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Navigation,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "External App",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. Floating "My Location" Button
        FloatingActionButton(
            onClick = onMyLocationClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = if (selectedPlace != null) 210.dp else 16.dp),
            shape = CircleShape,
            containerColor = Sage800,
            contentColor = Color.White
        ) {
            Icon(
                imageVector = Icons.Rounded.MyLocation,
                contentDescription = "My Location",
                modifier = Modifier.size(22.dp)
            )
        }

        // 5. Selected Place Bottom Card (CO6 Place Preview & Turn-by-Turn Navigation)
        AnimatedVisibility(
            visible = selectedPlace != null,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp),
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            selectedPlace?.let { place ->
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // Header: Category Badge & Dismiss Button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Sage100)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "${place.categoryIcon} ${place.category}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Sage800
                                )
                            }

                            IconButton(
                                onClick = onDismissPreview,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "Dismiss",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Place Name
                        Text(
                            text = place.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        // Address & Distance
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.LocationOn,
                                contentDescription = "Address",
                                tint = Sage800,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${place.address} • ${place.distanceKm} km away",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Environmental Metrics Chips Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Peace Score Chip
                            MetricPillChip(
                                label = "Peace Score",
                                value = "${place.peaceScore}/100",
                                color = Sage800,
                                modifier = Modifier.weight(1.1f)
                            )
                            // AQI Chip
                            MetricPillChip(
                                label = "Air Quality",
                                value = "${place.aqi} AQI",
                                color = OceanTeal,
                                modifier = Modifier.weight(1f)
                            )
                            // Noise Chip
                            MetricPillChip(
                                label = "Noise",
                                value = "${place.noiseDb} dB",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(0.9f)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Actions Row: [ Details ], [ 🧭 In-App Route ], and [ ↗ External App ]
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = { onViewDetailsClick(place.id) },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                            ) {
                                Text(
                                    text = "Details",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            // Primary Action: [ 🧭 In-App Route ] (Renders route polyline on the map inside the app!)
                            Button(
                                onClick = { onStartInAppNavigation(place) },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Sage800),
                                modifier = Modifier
                                    .weight(1.3f)
                                    .height(46.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Navigation,
                                    contentDescription = "In-App Route",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "🧭 In-App Route",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            // Secondary Action: External Google Maps App
                            IconButton(
                                onClick = {
                                    NavigationUtils.launchGoogleMapsNavigation(
                                        context = context,
                                        destinationLat = place.latitude,
                                        destinationLon = place.longitude,
                                        destinationName = place.name,
                                        mode = "w"
                                    )
                                },
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Sage100)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.LocationOn,
                                    contentDescription = "Open Google Maps App",
                                    tint = Sage800,
                                    modifier = Modifier.size(20.dp)
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
private fun MetricPillChip(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}
