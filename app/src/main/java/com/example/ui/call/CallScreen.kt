package com.example.ui.call

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.CloseFullscreen
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

import com.example.ui.theme.TelegramDarkBg
import com.example.ui.theme.TelegramGlassBorder
import com.example.ui.theme.TelegramPrimary
import kotlin.math.roundToInt

/**
 * Full-screen Telegram Call View (Audio & Video)
 */
@Composable
fun FullCallScreen(
    onMinimize: () -> Unit,
    modifier: Modifier = Modifier
) {
    val callState by CallManager.callState.collectAsState()
    if (!callState.isActive) return

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TelegramDarkBg)
    ) {
        Box(modifier = Modifier.fillMaxSize().background(TelegramDarkBg.copy(alpha = 0.95f))) { }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar with Minimize / PiP Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onMinimize,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0x33FFFFFF))
                ) {
                    Icon(
                        imageVector = Icons.Default.PictureInPictureAlt,
                        contentDescription = "Minimize / Floating",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x33FFFFFF))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (callState.isVideo) "Video Call" else "Voice Call",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.size(42.dp))
            }

            Spacer(modifier = Modifier.weight(0.5f))

            // Pulsing Glowing Ring Around Avatar
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(240.dp)
            ) {
                // Outer Ripple
                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(TelegramPrimary.copy(alpha = 0.25f), Color.Transparent)
                            )
                        )
                )

                // Inner Ripple
                Box(
                    modifier = Modifier
                        .size(170.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(TelegramPrimary.copy(alpha = 0.45f), Color.Transparent)
                            )
                        )
                )

                // Contact Avatar
                Box(
                    modifier = Modifier
                        .size(128.dp)
                        .shadow(16.dp, CircleShape, spotColor = TelegramPrimary)
                        .clip(CircleShape)
                        .background(Color(0xFF2481CC))
                        .border(3.dp, Color(0x66FFFFFF), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (!callState.contactAvatarUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = callState.contactAvatarUrl,
                            contentDescription = "Avatar",
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text(
                            text = callState.contactName.take(1).uppercase(),
                            color = Color.White,
                            fontSize = 44.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Contact Name
            Text(
                text = callState.contactName,
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Live Call Duration Timer
            Text(
                text = CallManager.formatDuration(callState.durationSeconds),
                color = TelegramPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.weight(1f))

            // Glassmorphic Call Actions Dock
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(Color(0xCC16222D))
                    .border(1.dp, TelegramGlassBorder, RoundedCornerShape(32.dp))
                    .padding(vertical = 18.dp, horizontal = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Mute Button
                    CallActionButton(
                        icon = if (callState.isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                        label = if (callState.isMuted) "Unmute" else "Mute",
                        isActive = callState.isMuted,
                        activeColor = Color(0xFFEF5350),
                        onClick = { CallManager.toggleMute() }
                    )

                    // Speaker Button
                    CallActionButton(
                        icon = if (callState.isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                        label = "Speaker",
                        isActive = callState.isSpeakerOn,
                        activeColor = TelegramPrimary,
                        onClick = { CallManager.toggleSpeaker() }
                    )

                    // Video Camera Toggle
                    CallActionButton(
                        icon = if (callState.isCameraOff) Icons.Default.VideocamOff else Icons.Default.Videocam,
                        label = "Video",
                        isActive = !callState.isCameraOff,
                        activeColor = TelegramPrimary,
                        onClick = { CallManager.toggleCamera() }
                    )

                    // Flip Camera (only for video)
                    if (callState.isVideo) {
                        CallActionButton(
                            icon = Icons.Default.Cameraswitch,
                            label = "Flip",
                            isActive = false,
                            onClick = { CallManager.flipCamera() }
                        )
                    }

                    // Red End Call Button
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .shadow(12.dp, CircleShape, spotColor = Color(0xFFE53935))
                            .clip(CircleShape)
                            .background(Color(0xFFE53935))
                            .clickable { CallManager.endCall() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CallEnd,
                            contentDescription = "End Call",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun CallActionButton(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    activeColor: Color = TelegramPrimary,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(if (isActive) activeColor else Color(0x33FFFFFF))
                .border(1.dp, if (isActive) activeColor else Color(0x40FFFFFF), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * In-App Draggable Floating Call Overlay (PiP Bubble)
 * Allows user to chat, browse, and control call (Mute, Speaker, End) while in the background or navigating
 */
@Composable
fun FloatingCallOverlay(
    onExpand: () -> Unit,
    modifier: Modifier = Modifier
) {
    val callState by CallManager.callState.collectAsState()
    if (!callState.isActive || !callState.isFloating) return

    var offsetX by remember { mutableFloatStateOf(16f) }
    var offsetY by remember { mutableFloatStateOf(120f) }

    Box(
        modifier = modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    offsetX += dragAmount.x
                    offsetY += dragAmount.y
                }
            }
            .shadow(16.dp, RoundedCornerShape(24.dp), spotColor = Color(0x99000000))
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xEB16222D))
            .border(1.2.dp, TelegramGlassBorder, RoundedCornerShape(24.dp))
            .clickable { onExpand() }
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Pulsing online avatar
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(TelegramPrimary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = callState.contactName.take(1).uppercase(),
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Info column
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF00E676))
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = callState.contactName,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = CallManager.formatDuration(callState.durationSeconds),
                    color = TelegramPrimary,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Quick Mute Toggle
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(if (callState.isMuted) Color(0xFFEF5350) else Color(0x33FFFFFF))
                    .clickable { CallManager.toggleMute() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (callState.isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                    contentDescription = "Mute",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }

            // Quick Speaker Toggle
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(if (callState.isSpeakerOn) TelegramPrimary else Color(0x33FFFFFF))
                    .clickable { CallManager.toggleSpeaker() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (callState.isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                    contentDescription = "Speaker",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }

            // Quick End Call
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE53935))
                    .clickable { CallManager.endCall() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CallEnd,
                    contentDescription = "End Call",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }

            // Expand Icon
            Icon(
                imageVector = Icons.Default.OpenInFull,
                contentDescription = "Expand",
                tint = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

/**
 * Compact View for System Android Picture-in-Picture (PiP) Mode
 */
@Composable
fun CompactPipCallView(
    modifier: Modifier = Modifier
) {
    val callState by CallManager.callState.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0E1621)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(TelegramPrimary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = callState.contactName.take(1).uppercase(),
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = callState.contactName,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )

            Text(
                text = CallManager.formatDuration(callState.durationSeconds),
                color = TelegramPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(if (callState.isMuted) Color(0xFFEF5350) else Color(0x33FFFFFF))
                        .clickable { CallManager.toggleMute() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (callState.isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "Mute",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE53935))
                        .clickable { CallManager.endCall() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CallEnd,
                        contentDescription = "End Call",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}
