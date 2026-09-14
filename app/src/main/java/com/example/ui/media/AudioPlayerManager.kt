package com.example.ui.media

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
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

data class AudioPlaybackState(
    val activeMessageId: String? = null,
    val isPlaying: Boolean = false,
    val progress: Float = 0f,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val senderName: String? = null,
    val speed: Float = 1.0f
)

object AudioPlayerManager {
    private const val TAG = "AudioPlayerManager"
    private var mediaPlayer: MediaPlayer? = null
    private var progressJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    private val _playbackState = MutableStateFlow(AudioPlaybackState())
    val playbackState: StateFlow<AudioPlaybackState> = _playbackState.asStateFlow()

    fun togglePlay(context: Context, messageId: String, url: String, durationSec: Int) {
        val currentState = _playbackState.value

        if (currentState.activeMessageId == messageId && currentState.isPlaying) {
            pause()
            return
        }

        if (currentState.activeMessageId == messageId && mediaPlayer != null) {
            mediaPlayer?.start()
            _playbackState.value = currentState.copy(isPlaying = true)
            startProgressTracker()
            return
        }

        playNew(context, messageId, url, durationSec)
    }

    private fun playNew(context: Context, messageId: String, url: String, durationSec: Int) {
        releasePlayer()

        _playbackState.value = AudioPlaybackState(
            activeMessageId = messageId,
            isPlaying = true,
            progress = 0f,
            currentPositionMs = 0L,
            durationMs = (durationSec * 1000).toLong()
        )

        try {
            val player = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(url)
                setOnPreparedListener { mp ->
                    mp.start()
                    val realDuration = mp.duration.toLong().coerceAtLeast(1000L)
                    _playbackState.value = _playbackState.value.copy(
                        isPlaying = true,
                        durationMs = realDuration
                    )
                    startProgressTracker()
                }
                setOnCompletionListener {
                    _playbackState.value = _playbackState.value.copy(
                        isPlaying = false,
                        progress = 0f,
                        currentPositionMs = 0L
                    )
                    progressJob?.cancel()
                }
                setOnErrorListener { _, what, extra ->
                    Log.e(TAG, "MediaPlayer error: what=$what, extra=$extra")
                    _playbackState.value = AudioPlaybackState()
                    true
                }
                prepareAsync()
            }
            mediaPlayer = player
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start audio playback", e)
            _playbackState.value = AudioPlaybackState()
        }
    }

    private fun pause() {
        mediaPlayer?.pause()
        progressJob?.cancel()
        _playbackState.value = _playbackState.value.copy(isPlaying = false)
    }

    fun seekTo(seekRatio: Float) {

    fun toggleSpeed(context: Context) {
        // Toggle between 1x and 2x speed
        val newSpeed = if (_playbackState.value.speed > 1.0f) 1.0f else 2.0f
        _playbackState.update { it.copy(speed = newSpeed) }
        try {
            mediaPlayer?.let {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    it.playbackParams = it.playbackParams.setSpeed(newSpeed)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "toggleSpeed: ${e.message}")
        }
    }
        val player = mediaPlayer ?: return
        val duration = _playbackState.value.durationMs
        if (duration > 0) {
            val targetMs = (duration * seekRatio.coerceIn(0f, 1f)).toInt()
            try {
                player.seekTo(targetMs)
                _playbackState.value = _playbackState.value.copy(
                    progress = seekRatio.coerceIn(0f, 1f),
                    currentPositionMs = targetMs.toLong()
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to seek", e)
            }
        }
    }

    fun stop(context: Context? = null) {
        releasePlayer()
        _playbackState.value = AudioPlaybackState()
    }

    private fun releasePlayer() {
        progressJob?.cancel()
        progressJob = null
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (_: Exception) {}
        mediaPlayer = null
    }

    private fun startProgressTracker() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive) {
                val player = mediaPlayer
                if (player != null && player.isPlaying) {
                    val current = player.currentPosition.toLong()
                    val total = player.duration.toLong().coerceAtLeast(1L)
                    val progress = (current.toFloat() / total.toFloat()).coerceIn(0f, 1f)
                    _playbackState.value = _playbackState.value.copy(
                        progress = progress,
                        currentPositionMs = current
                    )
                }
                delay(100)
            }
        }
    }
}

object MediaNotificationHelper {
    private const val CHANNEL_ID = "telegram_media_playback"
    private const val CHANNEL_NAME = "Media Playback"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows media playback controls"
                setShowBadge(false)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            manager?.createNotificationChannel(channel)
        }
    }
}
