package com.calmpath.ai.media

import androidx.annotation.RawRes
import com.calmpath.ai.R

/**
 * State machine for audio playback in CalmPath AI (CO7 Multimedia).
 */
enum class AudioPlaybackState {
    IDLE,
    PLAYING,
    PAUSED,
    STOPPED,
    ERROR
}

/**
 * Represents a calming audio track available for playback.
 */
data class AudioTrack(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: String,
    @RawRes val rawResId: Int
) {
    companion object {
        val NATURE = AudioTrack(
            id = "track_nature",
            title = "Forest Canopy & Birds",
            subtitle = "Gentle woodland leaves & peaceful bird melodies",
            icon = "🌿",
            rawResId = R.raw.calm_nature
        )

        val MEDITATION = AudioTrack(
            id = "track_meditation",
            title = "Singing Bowl & Chimes",
            subtitle = "432 Hz healing resonance for deep relaxation",
            icon = "🧘",
            rawResId = R.raw.calm_meditation
        )

        val RAIN = AudioTrack(
            id = "track_rain",
            title = "Gentle Rain & Ocean Breeze",
            subtitle = "Soothing raindrops & tranquil maritime white noise",
            icon = "🌧",
            rawResId = R.raw.rain_sounds
        )

        val ALL_TRACKS = listOf(NATURE, MEDITATION, RAIN)

        /**
         * Recommends a primary calming audio track based on sanctuary category.
         */
        fun recommendedForCategory(category: String): AudioTrack {
            return when (category.lowercase()) {
                "parks", "fitness" -> NATURE
                "meditation", "libraries" -> MEDITATION
                "lakes", "cafes" -> RAIN
                else -> NATURE
            }
        }
    }
}

/**
 * UI State exposed to Jetpack Compose for the Calm Experience audio player.
 */
data class AudioPlayerUiState(
    val state: AudioPlaybackState = AudioPlaybackState.IDLE,
    val currentTrack: AudioTrack? = null,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val errorMessage: String? = null
) {
    val isPlaying: Boolean get() = state == AudioPlaybackState.PLAYING
    val isPaused: Boolean get() = state == AudioPlaybackState.PAUSED
    val progressPercent: Float
        get() = if (durationMs > 0L) (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f
}
