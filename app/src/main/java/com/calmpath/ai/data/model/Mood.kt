package com.calmpath.ai.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.DirectionsRun
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.LocalCafe
import androidx.compose.material.icons.rounded.Park
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Represents the user's current mood and intent for finding a peaceful sanctuary.
 */
enum class Mood(
    val id: String,
    val title: String,
    val emoji: String,
    val subtitle: String,
    val idealMaxNoiseDb: Int,
    val idealMaxAqi: Int,
    val defaultCategory: String,
    val accentColorHex: Long
) {
    RELAX(
        id = "relax",
        title = "Relax",
        emoji = "😌",
        subtitle = "Unwind and decompress in tranquil nature",
        idealMaxNoiseDb = 42,
        idealMaxAqi = 45,
        defaultCategory = "Parks",
        accentColorHex = 0xFF52B788
    ),
    MEDITATE(
        id = "meditate",
        title = "Meditate",
        emoji = "🧘",
        subtitle = "Deep serenity and mindful quiet spaces",
        idealMaxNoiseDb = 35,
        idealMaxAqi = 35,
        defaultCategory = "Meditation",
        accentColorHex = 0xFF40916C
    ),
    STUDY(
        id = "study",
        title = "Study",
        emoji = "📚",
        subtitle = "Quiet nooks and distraction-free libraries",
        idealMaxNoiseDb = 38,
        idealMaxAqi = 50,
        defaultCategory = "Libraries",
        accentColorHex = 0xFF2D6A4F
    ),
    EXERCISE(
        id = "exercise",
        title = "Exercise",
        emoji = "🏃",
        subtitle = "Clean air trails and outdoor wellness parks",
        idealMaxNoiseDb = 55,
        idealMaxAqi = 40,
        defaultCategory = "Fitness",
        accentColorHex = 0xFF74C69D
    ),
    FRESH_AIR(
        id = "fresh_air",
        title = "Fresh Air",
        emoji = "🌿",
        subtitle = "Botanical gardens and lush green canopies",
        idealMaxNoiseDb = 45,
        idealMaxAqi = 30,
        defaultCategory = "Parks",
        accentColorHex = 0xFF95D5B2
    ),
    QUIET_TIME(
        id = "quiet_time",
        title = "Quiet Time",
        emoji = "☕",
        subtitle = "Peaceful cafes and scenic viewpoints",
        idealMaxNoiseDb = 45,
        idealMaxAqi = 50,
        defaultCategory = "Cafes",
        accentColorHex = 0xFFD8F3DC
    );

    val icon: ImageVector
        get() = when (this) {
            RELAX -> Icons.Rounded.Spa
            MEDITATE -> Icons.Rounded.SelfImprovement
            STUDY -> Icons.AutoMirrored.Rounded.MenuBook
            EXERCISE -> Icons.AutoMirrored.Rounded.DirectionsRun
            FRESH_AIR -> Icons.Rounded.Park
            QUIET_TIME -> Icons.Rounded.LocalCafe
        }

    companion object {
        fun fromId(id: String?): Mood {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: RELAX
        }
    }
}
