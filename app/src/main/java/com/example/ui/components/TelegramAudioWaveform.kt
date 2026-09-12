package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.ui.theme.TelegramPrimary
import kotlin.math.sin
import kotlin.random.Random

/**
 * Authentic Telegram-style Voice Message Audio Waveform
 * Renders vertical rounded amplitude bars with played/unplayed color split and dynamic animation
 */
@Composable
fun TelegramAudioWaveform(
    progress: Float,
    isPlaying: Boolean,
    seed: Int = 42,
    playedColor: Color = TelegramPrimary,
    unplayedColor: Color = Color.White.copy(alpha = 0.35f),
    onSeek: ((Float) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    // Generate realistic voice note bar heights (normalized 0.15f to 1.0f)
    val barCount = 32
    val heights = remember(seed) {
        val rand = Random(seed)
        FloatArray(barCount) { index ->
            val wave = 0.4f + 0.35f * sin(index.toFloat() * 0.4f)
            (wave + rand.nextFloat() * 0.35f).coerceIn(0.18f, 0.98f)
        }
    }

    // Subtle breathing/pulse animation when playing
    val infiniteTransition = rememberInfiniteTransition(label = "waveform_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(450, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(24.dp)
            .pointerInput(onSeek) {
                if (onSeek != null) {
                    detectTapGestures { offset ->
                        val tapRatio = (offset.x / size.width).coerceIn(0f, 1f)
                        onSeek(tapRatio)
                    }
                }
            }
    ) {
        val width = size.width
        val canvasHeight = size.height
        val totalSpacing = width / barCount
        val barWidth = (totalSpacing * 0.58f).coerceAtLeast(2.dp.toPx())
        val cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)

        for (i in 0 until barCount) {
            val barProgressRatio = (i.toFloat() + 0.5f) / barCount.toFloat()
            val isPlayed = barProgressRatio <= progress

            var barH = heights[i] * canvasHeight
            if (isPlaying && isPlayed) {
                barH = (barH * pulseScale).coerceIn(4.dp.toPx(), canvasHeight)
            } else {
                barH = barH.coerceIn(4.dp.toPx(), canvasHeight)
            }

            val x = i * totalSpacing + (totalSpacing - barWidth) / 2f
            val y = (canvasHeight - barH) / 2f

            drawRoundRect(
                color = if (isPlayed) playedColor else unplayedColor,
                topLeft = Offset(x, y),
                size = Size(barWidth, barH),
                cornerRadius = cornerRadius
            )
        }
    }
}
