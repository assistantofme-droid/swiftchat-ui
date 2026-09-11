package com.example.ui.media

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AudioPlaybackState(
    val activeMessageId: String? = null,
    val isPlaying: Boolean = false,
    val currentPositionMs: Int = 0,
    val durationMs: Int = 0,
    val progress: Float = 0f
)

object AudioPlayerManager {
    private var mediaPlayer: MediaPlayer? = null
    private val handler = Handler(Looper.getMainLooper())
    private var progressRunnable: Runnable? = null

    private val _playbackState = MutableStateFlow(AudioPlaybackState())
    val playbackState: StateFlow<AudioPlaybackState> = _playbackState.asStateFlow()

    fun togglePlay(context: Context, messageId: String, audioUrl: String, durationSec: Int = 30) {
        val currentState = _playbackState.value

        // If clicking the same message that is currently playing
        if (currentState.activeMessageId == messageId) {
            if (currentState.isPlaying) {
                pause()
            } else {
                resume()
            }
            return
        }

        // Switching to a new audio track or voice note
        stop()
        startPlaying(context, messageId, audioUrl, durationSec)
    }

    private fun startPlaying(context: Context, messageId: String, audioUrl: String, durationSec: Int) {
        try {
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(audioUrl)
                setOnPreparedListener { mp ->
                    mp.start()
                    val dur = if (mp.duration > 0) mp.duration else durationSec * 1000
                    _playbackState.value = AudioPlaybackState(
                        activeMessageId = messageId,
                        isPlaying = true,
                        currentPositionMs = 0,
                        durationMs = dur,
                        progress = 0f
                    )
                    startProgressTracker()
                }
                setOnCompletionListener {
                    stop()
                }
                setOnErrorListener { _, what, extra ->
                    Log.w("AudioPlayerManager", "Playback error what=$what extra=$extra, simulating timer")
                    // Fallback to simulated smooth playback for preview/emulators without network stream
                    simulatePlayback(messageId, durationSec)
                    true
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            Log.e("AudioPlayerManager", "Failed to start player", e)
            simulatePlayback(messageId, durationSec)
        }
    }

    private fun simulatePlayback(messageId: String, durationSec: Int) {
        val durMs = if (durationSec > 0) durationSec * 1000 else 15000
        _playbackState.value = AudioPlaybackState(
            activeMessageId = messageId,
            isPlaying = true,
            currentPositionMs = 0,
            durationMs = durMs,
            progress = 0f
        )
        startProgressTracker()
    }

    private fun resume() {
        mediaPlayer?.let {
            if (!it.isPlaying) {
                it.start()
                _playbackState.value = _playbackState.value.copy(isPlaying = true)
                startProgressTracker()
            }
        } ?: run {
            _playbackState.value = _playbackState.value.copy(isPlaying = true)
            startProgressTracker()
        }
    }

    fun pause() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
            }
        }
        stopProgressTracker()
        _playbackState.value = _playbackState.value.copy(isPlaying = false)
    }

    fun stop() {
        stopProgressTracker()
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (_: Exception) {}
        mediaPlayer = null
        _playbackState.value = AudioPlaybackState()
    }

    private fun startProgressTracker() {
        stopProgressTracker()
        progressRunnable = object : Runnable {
            override fun run() {
                val state = _playbackState.value
                if (!state.isPlaying || state.activeMessageId == null) return

                val currentMs: Int
                val totalMs = if (state.durationMs > 0) state.durationMs else 15000

                if (mediaPlayer != null && mediaPlayer!!.isPlaying) {
                    currentMs = mediaPlayer!!.currentPosition
                } else {
                    currentMs = (state.currentPositionMs + 200).coerceAtMost(totalMs)
                }

                if (currentMs >= totalMs) {
                    stop()
                    return
                }

                val progress = (currentMs.toFloat() / totalMs.toFloat()).coerceIn(0f, 1f)
                _playbackState.value = state.copy(
                    currentPositionMs = currentMs,
                    progress = progress
                )

                handler.postDelayed(this, 200)
            }
        }
        handler.post(progressRunnable!!)
    }

    private fun stopProgressTracker() {
        progressRunnable?.let { handler.removeCallbacks(it) }
        progressRunnable = null
    }
}
