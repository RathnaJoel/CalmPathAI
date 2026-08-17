package com.calmpath.ai.ui.screens.mood

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.calmpath.ai.data.model.Mood
import com.calmpath.ai.ui.components.MoodCard
import com.calmpath.ai.ui.theme.Sage800

/**
 * Screen 2: Mood Selection Screen (CO1 & CO2).
 */
@Composable
fun MoodSelectionScreen(
    currentMood: Mood = Mood.RELAX,
    onMoodConfirmed: (Mood) -> Unit
) {
    var selectedMood by remember { mutableStateOf(currentMood) }

    val allMoods = listOf(
        Mood.RELAX,
        Mood.MEDITATE,
        Mood.STUDY,
        Mood.EXERCISE,
        Mood.FRESH_AIR,
        Mood.QUIET_TIME
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Header Section
            Column {
                Text(
                    text = "How are you feeling today?",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Select your current state of mind so CalmPath AI can calculate the best environmental sanctuary for you.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 22.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Scrollable Mood Cards List
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(allMoods) { mood ->
                    MoodCard(
                        mood = mood,
                        isSelected = selectedMood == mood,
                        onSelect = { selectedMood = mood }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // "Find My Place" CTA Button
            Button(
                onClick = { onMoodConfirmed(selectedMood) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Sage800)
            ) {
                Text(
                    text = "Find My Place (${selectedMood.emoji} ${selectedMood.title})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                    contentDescription = null,
                    tint = Color.White
                )
            }
        }
    }
}
