package com.calmpath.ai.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.calmpath.ai.data.model.AqiCategory
import com.calmpath.ai.ui.theme.OceanTeal
import com.calmpath.ai.ui.theme.QualityGoodGreen
import com.calmpath.ai.ui.theme.QualityModerateYellow
import com.calmpath.ai.ui.theme.QualityPoorRed
import com.calmpath.ai.ui.theme.QualityUnhealthyOrange

/**
 * Air Quality Index Card (CO1, CO5).
 * Features synchronized 168.dp equal-height layout, non-wrapping typography,
 * and standard EPA color indicator bar.
 */
@Composable
fun AqiIndicatorCard(
    currentAqi: Int = 36,
    pm25: Double = 14.2,
    modifier: Modifier = Modifier
) {
    val category = AqiCategory.fromAqi(currentAqi)
    val aqiColor = Color(category.colorHex)
    val categoryAdvice = when (category) {
        AqiCategory.GOOD -> "Clean Airflow"
        AqiCategory.MODERATE -> "Acceptable Quality"
        AqiCategory.UNHEALTHY_SENSITIVE -> "Sensitive Groups Alert"
        AqiCategory.POOR -> "High Pollutant Alert"
    }

    Card(
        modifier = modifier.height(172.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header Row: Icon + Title + Status Chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(aqiColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Air,
                            contentDescription = "AQI",
                            tint = aqiColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(QualityGoodGreen)
                        )
                        Text(
                            text = "Air Quality",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(aqiColor.copy(alpha = 0.18f))
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = category.title,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = aqiColor,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Primary Metric Value (Synchronized Baseline)
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "$currentAqi",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "AQI",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = aqiColor,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            // Subtitle Condition (Justified Single Line)
            Text(
                text = "${category.title} • $categoryAdvice",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.weight(1f))

            // Footer: EPA Multi-color Scale Bar with Live PM2.5
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(3.dp))
                        .height(5.dp)
                ) {
                    Box(modifier = Modifier.weight(50f).fillMaxWidth().background(QualityGoodGreen))
                    Box(modifier = Modifier.weight(50f).fillMaxWidth().background(QualityModerateYellow))
                    Box(modifier = Modifier.weight(50f).fillMaxWidth().background(QualityUnhealthyOrange))
                    Box(modifier = Modifier.weight(50f).fillMaxWidth().background(QualityPoorRed))
                }
                Text(
                    text = "PM2.5: ${String.format(java.util.Locale.US, "%.1f", pm25)} µg/m³ • EPA Scale",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.outline,
                    maxLines = 1
                )
            }
        }
    }
}

/**
 * Weather Conditions Card (CO1, CO5).
 * Features synchronized 168.dp equal-height layout matching AqiIndicatorCard,
 * non-wrapping typography, and humidity/thermal indicators.
 */
@Composable
fun WeatherCard(
    temperatureC: Int = 23,
    weatherCondition: String = "Pleasant & Breezy",
    humidityPercent: Int = 54,
    weatherIcon: String = "🌤️",
    windSpeedKmH: Double = 12.0,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(172.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header Row: Icon + Title + Humidity Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(OceanTeal.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = weatherIcon, fontSize = 14.sp)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(QualityGoodGreen)
                        )
                        Text(
                            text = "Weather",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(OceanTeal.copy(alpha = 0.12f))
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "💧 $humidityPercent%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = OceanTeal,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Primary Metric Value (Synchronized Baseline)
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "$temperatureC°C",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = weatherIcon,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            // Subtitle Condition (Justified Single Line)
            Text(
                text = weatherCondition,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.weight(1f))

            // Footer: Ambient comfort and wind indicator
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "💨 ${String.format(java.util.Locale.US, "%.1f", windSpeedKmH)} km/h • Thermal Comfort",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = "Live telemetry from Open-Meteo",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.outline,
                    maxLines = 1
                )
            }
        }
    }
}

