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
    val senderName: String? = null,
    val isPlaying: Boolean = false,
    val currentPositionMs: Int = 0,
    val durationMs: Int = 0,
    val progress: Float = 0f,
    val speed: Float = 1.0f
)

object AudioPlayerManager {
    private var mediaPlayer: MediaPlayer? = null
    private val handler = Handler(Looper.getMainLooper())
    private var progressRunnable: Runnable? = null
    private var lastContext: Context? = null
    private var currentTitle: String = "Voice message"

    private val _playbackState = MutableStateFlow(AudioPlaybackState())
    val playbackState: StateFlow<AudioPlaybackState> = _playbackState.asStateFlow()

    fun toggleSpeed() {
        val currentSpeed = _playbackState.value.speed
        val newSpeed = if (currentSpeed == 1.0f) 2.0f else 1.0f
        try {
            mediaPlayer?.let {
                it.playbackParams = it.playbackParams.setSpeed(newSpeed)
            }
        } catch (_: Exception) {}
        _playbackState.value = _playbackState.value.copy(speed = newSpeed)
    }

    fun togglePlay(context: Context, messageId: String, audioUrl: String, durationSec: Int = 30, senderName: String? = null) {
        lastContext = context.applicationContext
        currentTitle = if (!senderName.isNullOrBlank()) "Voice note from $senderName" else "Voice Message"
        val currentState = _playbackState.value

        // If clicking the same message that is currently playing
        if (currentState.activeMessageId == messageId) {
            if (currentState.isPlaying) {
                pause(context)
            } else {
                resume(context)
            }
            return
        }

        // Switching to a new audio track or voice note
        stop(context)
        startPlaying(context, messageId, audioUrl, durationSec, senderName)
    }

    fun togglePlayPause(context: Context) {
        val ctx = context.applicationContext
        lastContext = ctx
        if (_playbackState.value.isPlaying) {
            pause(ctx)
        } else {
            resume(ctx)
        }
    }

    fun skipNext(context: Context) {
        val state = _playbackState.value
        val totalMs = if (state.durationMs > 0) state.durationMs else 15000
        val targetMs = (state.currentPositionMs + 10000).coerceAtMost(totalMs)
        try {
            mediaPlayer?.seekTo(targetMs)
        } catch (_: Exception) {}
        val ratio = (targetMs.toFloat() / totalMs.toFloat()).coerceIn(0f, 1f)
        _playbackState.value = state.copy(currentPositionMs = targetMs, progress = ratio)
        updateNotification(context)
    }

    fun skipPrevious(context: Context) {
        val state = _playbackState.value
        val totalMs = if (state.durationMs > 0) state.durationMs else 15000
        val targetMs = (state.currentPositionMs - 10000).coerceAtLeast(0)
        try {
            mediaPlayer?.seekTo(targetMs)
        } catch (_: Exception) {}
        val ratio = (targetMs.toFloat() / totalMs.toFloat()).coerceIn(0f, 1f)
        _playbackState.value = state.copy(currentPositionMs = targetMs, progress = ratio)
        updateNotification(context)
    }

    private fun startPlaying(context: Context, messageId: String, audioUrl: String, durationSec: Int, senderName: String? = null) {
        lastContext = context.applicationContext
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
                    try {
                        val currentSpeed = _playbackState.value.speed
                        mp.playbackParams = mp.playbackParams.setSpeed(currentSpeed)
                    } catch (_: Exception) {}
                    mp.start()
                    val dur = if (mp.duration > 0) mp.duration else durationSec * 1000
                    _playbackState.value = AudioPlaybackState(
                        activeMessageId = messageId,
                        senderName = senderName,
                        isPlaying = true,
                        currentPositionMs = 0,
                        durationMs = dur,
                        progress = 0f,
                        speed = _playbackState.value.speed
                    )
                    startProgressTracker()
                    updateNotification(context)
                }
                setOnCompletionListener {
                    stop(context)
                }
                setOnErrorListener { _, what, extra ->
                    Log.w("AudioPlayerManager", "Playback error what=$what extra=$extra, simulating timer")
                    simulatePlayback(context, messageId, durationSec, senderName)
                    true
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            Log.e("AudioPlayerManager", "Failed to start player", e)
            simulatePlayback(context, messageId, durationSec, senderName)
        }
    }

    private fun simulatePlayback(context: Context, messageId: String, durationSec: Int, senderName: String? = null) {
        val durMs = if (durationSec > 0) durationSec * 1000 else 15000
        _playbackState.value = AudioPlaybackState(
            activeMessageId = messageId,
            senderName = senderName,
            isPlaying = true,
            currentPositionMs = 0,
            durationMs = durMs,
            progress = 0f,
            speed = _playbackState.value.speed
        )
        startProgressTracker()
        updateNotification(context)
    }

    fun resume(context: Context? = lastContext) {
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
        context?.let { updateNotification(it) }
    }

    fun pause(context: Context? = lastContext) {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
            }
        }
        stopProgressTracker()
        _playbackState.value = _playbackState.value.copy(isPlaying = false)
        context?.let { updateNotification(it) }
    }

    fun stop(context: Context? = lastContext) {
        stopProgressTracker()
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (_: Exception) {}
        mediaPlayer = null
        _playbackState.value = AudioPlaybackState()
        context?.let {
            MediaNotificationHelper.hidePlaybackNotification(it)
        }
    }

    fun seekTo(ratio: Float) {
        val state = _playbackState.value
        val totalMs = if (state.durationMs > 0) state.durationMs else 15000
        val targetMs = (totalMs * ratio).toInt()
        try {
            mediaPlayer?.seekTo(targetMs)
        } catch (_: Exception) {}
        _playbackState.value = state.copy(
            currentPositionMs = targetMs,
            progress = ratio.coerceIn(0f, 1f)
        )
        lastContext?.let { updateNotification(it) }
    }

    private fun updateNotification(context: Context) {
        val state = _playbackState.value
        if (state.activeMessageId == null) {
            MediaNotificationHelper.hidePlaybackNotification(context)
            return
        }
        val currentSec = state.currentPositionMs / 1000
        val totalSec = state.durationMs / 1000
        val timeText = String.format("%02d:%02d / %02d:%02d", currentSec / 60, currentSec % 60, totalSec / 60, totalSec % 60)
        val statusText = if (state.isPlaying) "Playing • $timeText" else "Paused • $timeText"
        MediaNotificationHelper.showPlaybackNotification(
            context = context,
            title = currentTitle,
            subtitle = statusText,
            isPlaying = state.isPlaying
        )
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
                    stop(lastContext)
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
