package com.calmpath.ai.media

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Lifecycle-aware Audio Manager for CalmPath AI (CO7 Multimedia Services).
 *
 * Responsibilities:
 * - Manages native [MediaPlayer] resources safely without memory or audio channel leaks.
 * - Emits reactive [AudioPlayerUiState] updates for Jetpack Compose UI.
 * - Provides seamless track switching, looping, progress tracking, and error recovery.
 */
class CalmAudioManager(
    context: Context,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) {
    private val tag = "CalmAudioManager"
    private val appContext = context.applicationContext

    private var mediaPlayer: MediaPlayer? = null
    private var progressJob: Job? = null

    private val _playerState = MutableStateFlow(AudioPlayerUiState())
    val playerState: StateFlow<AudioPlayerUiState> = _playerState.asStateFlow()

    /**
     * Starts playback for the requested calming audio track.
     */
    fun playTrack(track: AudioTrack) {
        val currentState = _playerState.value

        // If this track is already loaded and paused, simply resume
        if (currentState.currentTrack?.id == track.id && currentState.state == AudioPlaybackState.PAUSED) {
            resume()
            return
        }

        // Release any existing player instance
        stopAndReleasePlayer()

        try {
            val player = MediaPlayer.create(appContext, track.rawResId)
            if (player == null) {
                _playerState.value = AudioPlayerUiState(
                    state = AudioPlaybackState.ERROR,
                    currentTrack = track,
                    errorMessage = "Audio asset could not be loaded"
                )
                return
            }

            mediaPlayer = player

            player.setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )

            player.isLooping = true

            player.setOnCompletionListener {
                _playerState.value = _playerState.value.copy(
                    state = AudioPlaybackState.STOPPED,
                    currentPositionMs = 0L
                )
                stopProgressTracking()
            }

            player.setOnErrorListener { _, what, extra ->
                Log.e(tag, "MediaPlayer error: what=$what, extra=$extra")
                _playerState.value = _playerState.value.copy(
                    state = AudioPlaybackState.ERROR,
                    errorMessage = "Playback error occurred ($what)"
                )
                stopProgressTracking()
                true
            }

            player.start()

            val duration = player.duration.toLong().coerceAtLeast(1000L)
            _playerState.value = AudioPlayerUiState(
                state = AudioPlaybackState.PLAYING,
                currentTrack = track,
                currentPositionMs = 0L,
                durationMs = duration
            )

            startProgressTracking()
            Log.d(tag, "Started calming audio: ${track.title}")
        } catch (e: Exception) {
            Log.e(tag, "Failed to start audio: ${e.message}", e)
            _playerState.value = AudioPlayerUiState(
                state = AudioPlaybackState.ERROR,
                currentTrack = track,
                errorMessage = e.message ?: "Failed to initialize playback"
            )
        }
    }

    /**
     * Pauses the active audio track without resetting progress.
     */
    fun pause() {
        try {
            mediaPlayer?.let { player ->
                if (player.isPlaying) {
                    player.pause()
                    stopProgressTracking()
                    _playerState.value = _playerState.value.copy(
                        state = AudioPlaybackState.PAUSED,
                        currentPositionMs = player.currentPosition.toLong()
                    )
                    Log.d(tag, "Audio paused at ${_playerState.value.currentPositionMs} ms")
                }
            }
        } catch (e: Exception) {
            Log.w(tag, "Error pausing audio: ${e.message}")
        }
    }

    /**
     * Resumes playback from the current paused position.
     */
    fun resume() {
        try {
            mediaPlayer?.let { player ->
                player.start()
                _playerState.value = _playerState.value.copy(
                    state = AudioPlaybackState.PLAYING
                )
                startProgressTracking()
                Log.d(tag, "Audio resumed")
            }
        } catch (e: Exception) {
            Log.w(tag, "Error resuming audio: ${e.message}")
        }
    }

    /**
     * Stops audio playback and resets progress to 0.
     */
    fun stop() {
        try {
            stopProgressTracking()
            mediaPlayer?.let { player ->
                if (player.isPlaying) {
                    player.pause()
                }
                player.seekTo(0)
            }
            _playerState.value = _playerState.value.copy(
                state = AudioPlaybackState.STOPPED,
                currentPositionMs = 0L
            )
            Log.d(tag, "Audio stopped")
        } catch (e: Exception) {
            Log.w(tag, "Error stopping audio: ${e.message}")
        }
    }

    /**
     * Seeks playback to a specific position in milliseconds.
     */
    fun seekTo(positionMs: Long) {
        try {
            mediaPlayer?.let { player ->
                val clamped = positionMs.coerceIn(0L, _playerState.value.durationMs)
                player.seekTo(clamped.toInt())
                _playerState.value = _playerState.value.copy(currentPositionMs = clamped)
            }
        } catch (e: Exception) {
            Log.w(tag, "Error seeking audio: ${e.message}")
        }
    }

    /**
     * Completely releases the native MediaPlayer hardware resource and cancels background jobs.
     */
    fun release() {
        stopProgressTracking()
        stopAndReleasePlayer()
        _playerState.value = AudioPlayerUiState(state = AudioPlaybackState.IDLE)
        Log.d(tag, "CalmAudioManager released resources")
    }

    private fun startProgressTracking() {
        stopProgressTracking()
        progressJob = scope.launch {
            while (isActive) {
                mediaPlayer?.let { player ->
                    if (player.isPlaying) {
                        _playerState.value = _playerState.value.copy(
                            currentPositionMs = player.currentPosition.toLong()
                        )
                    }
                }
                delay(250L) // Update 4 times per second for smooth progress bar
            }
        }
    }

    private fun stopProgressTracking() {
        progressJob?.cancel()
        progressJob = null
    }

    private fun stopAndReleasePlayer() {
        try {
            mediaPlayer?.let { player ->
                if (player.isPlaying) {
                    player.stop()
                }
                player.reset()
                player.release()
            }
        } catch (e: Exception) {
            Log.w(tag, "Error releasing MediaPlayer: ${e.message}")
        } finally {
            mediaPlayer = null
        }
    }
}
