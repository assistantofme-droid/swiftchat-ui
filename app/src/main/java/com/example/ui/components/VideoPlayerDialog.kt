package com.example.ui.components

import android.net.Uri
import android.widget.MediaController
import android.widget.VideoView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.api.ApiClient
import com.example.ui.theme.TelegramPrimary
import com.example.ui.theme.TelegramTextPrimary
import com.example.ui.theme.TelegramTextSecondary
import kotlinx.coroutines.delay

@Composable
fun VideoPlayerDialog(
    videoUrl: String?,
    caption: String? = null,
    senderName: String? = null,
    onDismiss: () -> Unit
) {
    if (videoUrl.isNullOrBlank()) return

    val context = LocalContext.current
    val resolvedUrl = ApiClient.resolveUrl(videoUrl)

    var isPlaying by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(true) }
    var durationMs by remember { mutableIntStateOf(0) }
    var currentPositionMs by remember { mutableIntStateOf(0) }
    var videoViewInstance by remember { mutableStateOf<VideoView?>(null) }

    // Progress polling
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            videoViewInstance?.let { vv ->
                if (vv.isPlaying) {
                    currentPositionMs = vv.currentPosition
                    if (vv.duration > 0) {
                        durationMs = vv.duration
                    }
                }
            }
            delay(250)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            try {
                videoViewInstance?.stopPlayback()
            } catch (_: Exception) {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Video View
        AndroidView(
            factory = { ctx ->
                VideoView(ctx).apply {
                    setVideoURI(Uri.parse(resolvedUrl))
                    setOnPreparedListener { mp ->
                        isLoading = false
                        durationMs = mp.duration
                        mp.isLooping = true
                        start()
                        isPlaying = true
                    }
                    setOnErrorListener { _, _, _ ->
                        isLoading = false
                        true
                    }
                    videoViewInstance = this
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .clickable {
                    videoViewInstance?.let { vv ->
                        if (vv.isPlaying) {
                            vv.pause()
                            isPlaying = false
                        } else {
                            vv.start()
                            isPlaying = true
                        }
                    }
                }
        )

        // Loading spinner
        if (isLoading) {
            CircularProgressIndicator(
                color = TelegramPrimary,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // Top App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.6f))
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = senderName ?: "Video",
                    color = TelegramTextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = formatVideoTime(currentPositionMs) + " / " + formatVideoTime(durationMs),
                    color = TelegramTextSecondary,
                    fontSize = 12.sp
                )
            }
        }

        // Bottom Controller Bar
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.65f))
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            if (!caption.isNullOrBlank()) {
                Text(
                    text = caption,
                    color = Color.White,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Play / Pause Button
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(TelegramPrimary)
                        .clickable {
                            videoViewInstance?.let { vv ->
                                if (vv.isPlaying) {
                                    vv.pause()
                                    isPlaying = false
                                } else {
                                    vv.start()
                                    isPlaying = true
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Slider
                val progress = if (durationMs > 0) (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f
                Slider(
                    value = progress,
                    onValueChange = { newProgress ->
                        if (durationMs > 0) {
                            val newPos = (newProgress * durationMs).toInt()
                            currentPositionMs = newPos
                            videoViewInstance?.seekTo(newPos)
                        }
                    },
                    colors = SliderDefaults.colors(
                        thumbColor = TelegramPrimary,
                        activeTrackColor = TelegramPrimary,
                        inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = formatVideoTime(durationMs - currentPositionMs),
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 12.sp
                )
            }
        }
    }
}

private fun formatVideoTime(millis: Int): String {
    val totalSeconds = (millis / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}
