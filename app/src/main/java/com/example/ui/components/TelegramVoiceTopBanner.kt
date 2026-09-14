package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.media.AudioPlayerManager
import com.example.ui.theme.TelegramPrimary
import com.example.ui.theme.TelegramTextPrimary
import com.example.ui.theme.TelegramTextSecondary

@Composable
fun TelegramVoiceTopBanner(
    modifier: Modifier = Modifier
) {
    val audioState by AudioPlayerManager.playbackState.collectAsState()
    val context = LocalContext.current

    AnimatedVisibility(
        visible = audioState.activeMessageId != null,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .shadow(8.dp, RoundedCornerShape(16.dp), spotColor = Color(0x66000000))
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xE614212D))
                .border(1.dp, Color(0x3352B8FF), RoundedCornerShape(16.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Play/Pause circular button
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(TelegramPrimary)
                            .clickable {
                                val id = audioState.activeMessageId
                                if (id != null) {
                                    AudioPlayerManager.togglePlay(context, id, "", 30)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (audioState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play/Pause",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // Title & Time Info
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = audioState.senderName ?: "Voice message",
                            color = TelegramTextPrimary,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        val currentSec = audioState.currentPositionMs / 1000
                        val totalSec = audioState.durationMs / 1000
                        Text(
                            text = String.format(
                                "%02d:%02d / %02d:%02d",
                                currentSec / 60,
                                currentSec % 60,
                                totalSec / 60,
                                totalSec % 60
                            ),
                            color = TelegramPrimary,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // 1X / 2X Speed Toggle Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (audioState.speed > 1.0f) TelegramPrimary else Color(0x33FFFFFF))
                            .border(0.8.dp, if (audioState.speed > 1.0f) TelegramPrimary else Color(0x44FFFFFF), RoundedCornerShape(12.dp))
                            .clickable { AudioPlayerManager.toggleSpeed(null as? Context) }
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = if (audioState.speed > 1.0f) "2X" else "1X",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Close (X) button
                    IconButton(
                        onClick = { AudioPlayerManager.stop() },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Audio Player",
                            tint = TelegramTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Progress Bar
                LinearProgressIndicator(
                    progress = { audioState.progress },
                    color = TelegramPrimary,
                    trackColor = Color.White.copy(alpha = 0.15f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.5.dp)
                        .padding(top = 5.dp)
                        .clip(RoundedCornerShape(2.dp))
                )
            }
        }
    }
}
