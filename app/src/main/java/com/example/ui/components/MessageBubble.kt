package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Poll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.api.ApiClient
import com.example.data.model.MessageItem
import com.example.data.model.MessageType
import com.example.ui.media.AudioPlayerManager
import com.example.ui.theme.TelegramBubbleDatePill
import com.example.ui.theme.TelegramCheckBlue
import com.example.ui.theme.TelegramGlassBorder
import com.example.ui.theme.TelegramGlassHighlight
import com.example.ui.theme.TelegramIncomingGradient
import com.example.ui.theme.TelegramOutgoingGradient
import com.example.ui.theme.TelegramPrimary
import com.example.ui.theme.TelegramSendFabGradient
import com.example.ui.theme.TelegramTextPrimary
import com.example.ui.theme.TelegramTextSecondary

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageBubble(
    message: MessageItem,
    modifier: Modifier = Modifier,
    onReactionClick: (() -> Unit)? = null,
    onSelectReaction: ((String) -> Unit)? = null,
    onPhotoClick: ((MessageItem) -> Unit)? = null,
    onVideoClick: ((MessageItem) -> Unit)? = null,
    onMessageClick: ((MessageItem) -> Unit)? = null
) {
    val context = LocalContext.current
    val audioState by AudioPlayerManager.playbackState.collectAsState()
    var showQuickReaction by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 3.dp),
        horizontalAlignment = if (message.isOutgoing) Alignment.End else Alignment.Start
    ) {
        // Frosted Glass Date Pill (e.g. "Today", "Yesterday")
        if (message.dateHeader != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .shadow(4.dp, RoundedCornerShape(16.dp), spotColor = Color.Black)
                        .clip(RoundedCornerShape(16.dp))
                        .background(TelegramBubbleDatePill)
                        .border(0.8.dp, TelegramGlassHighlight, RoundedCornerShape(16.dp))
                        .padding(horizontal = 14.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = message.dateHeader,
                        color = Color.White.copy(alpha = 0.95f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Quick Reaction Floating Popup above bubble
        if (showQuickReaction) {
            TelegramQuickReactionPopup(
                visible = true,
                onSelectReaction = { emoji ->
                    showQuickReaction = false
                    onSelectReaction?.invoke(emoji) ?: onReactionClick?.invoke()
                },
                onDismiss = { showQuickReaction = false },
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        when (message.type) {
            MessageType.BIG_STICKER -> {
                StickerBubble(
                    imageUrl = message.mediaUrl,
                    time = message.time,
                    reactions = message.reactions,
                    onReactionClick = { showQuickReaction = !showQuickReaction }
                )
            }

            MessageType.GIF -> {
                GifBubble(
                    gifUrl = message.mediaUrl,
                    time = message.time,
                    caption = message.text,
                    isOutgoing = message.isOutgoing,
                    reactions = message.reactions,
                    onReactionClick = { showQuickReaction = !showQuickReaction }
                )
            }

            MessageType.VIDEO -> {
                VideoBubble(
                    message = message,
                    onClick = { onVideoClick?.invoke(message) },
                    onLongClick = { showQuickReaction = true },
                    onReactionClick = { showQuickReaction = !showQuickReaction }
                )
            }

            MessageType.AUDIO -> {
                val isThisPlaying = audioState.activeMessageId == message.id && audioState.isPlaying
                val progress = if (audioState.activeMessageId == message.id) audioState.progress else 0f
                val currentSeconds = if (audioState.activeMessageId == message.id) {
                    audioState.currentPositionMs / 1000
                } else {
                    message.duration ?: 24
                }

                AudioMessageBubble(
                    message = message,
                    isPlaying = isThisPlaying,
                    progress = progress,
                    displayedSeconds = currentSeconds,
                    onPlayPause = {
                        val url = message.mediaUrl ?: "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"
                        AudioPlayerManager.togglePlay(context, message.id, url, message.duration ?: 24)
                    },
                    onSeek = { seekRatio ->
                        AudioPlayerManager.seekTo(seekRatio)
                    },
                    onLongClick = { showQuickReaction = true },
                    onReactionClick = { showQuickReaction = !showQuickReaction }
                )
            }

            MessageType.PHOTO -> {
                PhotoBubble(
                    message = message,
                    onPhotoClick = { onPhotoClick?.invoke(message) },
                    onLongClick = { showQuickReaction = true },
                    onReactionClick = { showQuickReaction = !showQuickReaction }
                )
            }

            MessageType.IMAGE_COLLAGE -> {
                ImageCollageView(
                    time = message.time,
                    photoResId = message.photoResId,
                    onClick = { onPhotoClick?.invoke(message) },
                    onLongClick = { showQuickReaction = true }
                )
            }

            MessageType.LOCATION -> {
                LocationBubble(
                    message = message,
                    onClick = { onMessageClick?.invoke(message) },
                    onLongClick = { onMessageClick?.invoke(message) },
                    onReactionClick = { showQuickReaction = !showQuickReaction }
                )
            }

            MessageType.POLL -> {
                PollBubble(
                    message = message,
                    onClick = { onMessageClick?.invoke(message) },
                    onLongClick = { onMessageClick?.invoke(message) },
                    onReactionClick = { showQuickReaction = !showQuickReaction }
                )
            }

            MessageType.FILE -> {
                FileBubble(
                    message = message,
                    onClick = { onMessageClick?.invoke(message) },
                    onLongClick = { onMessageClick?.invoke(message) },
                    onReactionClick = { showQuickReaction = !showQuickReaction }
                )
            }

            MessageType.TEXT -> {
                if (!message.mediaUrl.isNullOrBlank()) {
                    PhotoBubble(
                        message = message,
                        onPhotoClick = { onPhotoClick?.invoke(message) },
                        onLongClick = { onMessageClick?.invoke(message) ?: run { showQuickReaction = true } },
                        onReactionClick = { showQuickReaction = !showQuickReaction }
                    )
                } else {
                    StandardTextBubble(
                        message = message,
                        onClick = { onMessageClick?.invoke(message) },
                        onLongClick = { onMessageClick?.invoke(message) ?: run { showQuickReaction = true } },
                        onReactionClick = { showQuickReaction = !showQuickReaction }
                    )
                }
            }
        }
    }
}

/**
 * Super Bubbly & Glassmorphic Telegram Text Message Bubble
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun StandardTextBubble(
    message: MessageItem,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit,
    onReactionClick: (() -> Unit)? = null
) {
    // Authentic Telegram bubble shape with iconic 20dp smooth curves and corner nip
    val bubbleShape = if (message.isOutgoing) {
        RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 4.dp)
    } else {
        RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 4.dp, bottomEnd = 20.dp)
    }

    val gradient = if (message.isOutgoing) TelegramOutgoingGradient else TelegramIncomingGradient
    val borderColor = if (message.isOutgoing) Color(0x3D72B0E8) else Color(0x24FFFFFF)

    Column(
        horizontalAlignment = if (message.isOutgoing) Alignment.End else Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(min = 78.dp, max = 295.dp)
                .shadow(elevation = 2.dp, shape = bubbleShape, spotColor = Color(0x40000000))
                .clip(bubbleShape)
                .background(gradient)
                .border(0.85.dp, borderColor, bubbleShape)
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick
                )
                .padding(horizontal = 13.dp, vertical = 8.dp)
        ) {
            Column {
                if (!message.isOutgoing && !message.senderName.isNullOrBlank()) {
                    Text(
                        text = message.senderName,
                        color = TelegramPrimary,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 3.dp)
                    )
                }

                Text(
                    text = message.text.orEmpty(),
                    color = TelegramTextPrimary,
                    fontSize = 15.5.sp,
                    lineHeight = 21.sp,
                    modifier = Modifier.padding(bottom = 2.dp)
                )

                // Inline Telegram Time and Status Checkmarks
                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = message.time,
                        color = if (message.isOutgoing) Color(0xCCB7DBF8) else TelegramTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal
                    )

                    if (message.isOutgoing) {
                        Icon(
                            imageVector = if (message.isRead) Icons.Default.DoneAll else Icons.Default.Check,
                            contentDescription = if (message.isRead) "Read" else "Sent",
                            tint = if (message.isRead) TelegramCheckBlue else Color.White.copy(alpha = 0.75f),
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }
        }

        ReactionPillRow(
            reactions = message.reactions,
            isOutgoing = message.isOutgoing,
            onReactionClick = onReactionClick
        )
    }
}

/**
 * Super Bubbly & Glassmorphic Photo Bubble
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PhotoBubble(
    message: MessageItem,
    onPhotoClick: () -> Unit,
    onLongClick: () -> Unit,
    onReactionClick: (() -> Unit)? = null
) {
    val resolvedMedia = ApiClient.resolveUrl(message.mediaUrl.orEmpty())
    val bubbleShape = if (message.isOutgoing) {
        RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 5.dp)
    } else {
        RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 5.dp, bottomEnd = 20.dp)
    }

    val gradient = if (message.isOutgoing) TelegramOutgoingGradient else TelegramIncomingGradient
    val borderColor = if (message.isOutgoing) Color(0x3D72B0E8) else Color(0x24FFFFFF)

    Column(horizontalAlignment = if (message.isOutgoing) Alignment.End else Alignment.Start) {
        Box(
            modifier = Modifier
                .widthIn(max = 285.dp)
                .shadow(3.dp, bubbleShape, spotColor = Color(0x40000000))
                .clip(bubbleShape)
                .background(gradient)
                .border(0.85.dp, borderColor, bubbleShape)
                .combinedClickable(
                    onClick = onPhotoClick,
                    onLongClick = onLongClick
                )
        ) {
            Column {
                AsyncImage(
                    model = resolvedMedia,
                    contentDescription = "Photo",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp)
                        .clip(bubbleShape),
                    contentScale = ContentScale.Crop
                )

                if (!message.text.isNullOrBlank()) {
                    Text(
                        text = message.text,
                        color = TelegramTextPrimary,
                        fontSize = 14.5.sp,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                    )
                }

                // Frosted Glass Time Stamp Overlay
                Row(
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(end = 8.dp, bottom = 6.dp)
                        .background(Color(0x80101820), RoundedCornerShape(10.dp))
                        .border(0.5.dp, Color(0x22FFFFFF), RoundedCornerShape(10.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = message.time,
                        color = Color.White,
                        fontSize = 11.sp
                    )
                    if (message.isOutgoing) {
                        Icon(
                            imageVector = if (message.isRead) Icons.Default.DoneAll else Icons.Default.Check,
                            contentDescription = "Status",
                            tint = if (message.isRead) TelegramCheckBlue else Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }
        }

        ReactionPillRow(
            reactions = message.reactions,
            isOutgoing = message.isOutgoing,
            onReactionClick = onReactionClick
        )
    }
}

/**
 * Super Bubbly & Glassmorphic Video Bubble
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun VideoBubble(
    message: MessageItem,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onReactionClick: (() -> Unit)? = null
) {
    val bubbleShape = if (message.isOutgoing) {
        RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 5.dp)
    } else {
        RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 5.dp, bottomEnd = 20.dp)
    }

    Column(horizontalAlignment = if (message.isOutgoing) Alignment.End else Alignment.Start) {
        Box(
            modifier = Modifier
                .width(265.dp)
                .height(185.dp)
                .shadow(3.dp, bubbleShape, spotColor = Color(0x40000000))
                .clip(bubbleShape)
                .background(Color(0xFF162330))
                .border(0.85.dp, Color(0x33FFFFFF), bubbleShape)
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick
                )
        ) {
            // Video Thumbnail
            if (!message.videoThumbnailUrl.isNullOrBlank()) {
                AsyncImage(
                    model = ApiClient.resolveUrl(message.videoThumbnailUrl),
                    contentDescription = "Video Thumbnail",
                    modifier = Modifier.matchParentSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF1D2E3E), Color(0xFF131D27))
                            )
                        )
                )
            }

            // Glassy Play Button Circle in center
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(Color(0x99101B26))
                    .border(1.2.dp, Color(0x55FFFFFF), CircleShape)
                    .align(Alignment.Center),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play Video",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            // Glass Duration Pill (Top Left)
            val durSec = message.duration ?: 15
            val min = durSec / 60
            val sec = durSec % 60
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .background(Color(0x990E1620), RoundedCornerShape(12.dp))
                    .border(0.6.dp, Color(0x33FFFFFF), RoundedCornerShape(12.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = String.format("%02d:%02d", min, sec),
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Glass Timestamp (Bottom Right)
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .background(Color(0x990E1620), RoundedCornerShape(10.dp))
                    .border(0.6.dp, Color(0x33FFFFFF), RoundedCornerShape(10.dp))
                    .padding(horizontal = 7.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = message.time,
                    color = Color.White,
                    fontSize = 11.sp
                )
                if (message.isOutgoing) {
                    Icon(
                        imageVector = if (message.isRead) Icons.Default.DoneAll else Icons.Default.Check,
                        contentDescription = "Status",
                        tint = if (message.isRead) TelegramCheckBlue else Color.White,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        }

        ReactionPillRow(
            reactions = message.reactions,
            isOutgoing = message.isOutgoing,
            onReactionClick = onReactionClick
        )
    }
}

/**
 * Authentic Telegram Voice Note / Audio Bubble with Real Amplitude Waveform
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AudioMessageBubble(
    message: MessageItem,
    isPlaying: Boolean,
    progress: Float,
    displayedSeconds: Int,
    onPlayPause: () -> Unit,
    onSeek: (Float) -> Unit,
    onLongClick: () -> Unit,
    onReactionClick: (() -> Unit)? = null
) {
    val bubbleShape = if (message.isOutgoing) {
        RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 5.dp)
    } else {
        RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 5.dp, bottomEnd = 20.dp)
    }

    val gradient = if (message.isOutgoing) TelegramOutgoingGradient else TelegramIncomingGradient
    val borderColor = if (message.isOutgoing) Color(0x3D72B0E8) else Color(0x24FFFFFF)

    Column(horizontalAlignment = if (message.isOutgoing) Alignment.End else Alignment.Start) {
        Box(
            modifier = Modifier
                .widthIn(min = 230.dp, max = 290.dp)
                .shadow(2.dp, bubbleShape, spotColor = Color(0x40000000))
                .clip(bubbleShape)
                .background(gradient)
                .border(0.85.dp, borderColor, bubbleShape)
                .combinedClickable(
                    onClick = { /* tap */ },
                    onLongClick = onLongClick
                )
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Glass Play/Pause Button with Fluid Telegram Gradient
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(TelegramSendFabGradient)
                        .border(1.dp, Color(0x40FFFFFF), CircleShape)
                        .clickable(onClick = onPlayPause),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.width(11.dp))

                Column(modifier = Modifier.weight(1f)) {
                    // Authentic Telegram Audio Waveform Visualizer
                    TelegramAudioWaveform(
                        progress = progress,
                        isPlaying = isPlaying,
                        seed = message.id.hashCode(),
                        playedColor = if (message.isOutgoing) Color(0xFF5AB6FD) else TelegramPrimary,
                        unplayedColor = if (message.isOutgoing) Color(0x55B7DBF8) else Color(0x44FFFFFF),
                        onSeek = onSeek,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val min = displayedSeconds / 60
                        val sec = displayedSeconds % 60
                        Text(
                            text = String.format("%02d:%02d", min, sec),
                            color = if (message.isOutgoing) Color(0xCCB7DBF8) else TelegramTextSecondary,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text(
                                text = message.time,
                                color = if (message.isOutgoing) Color(0xCCB7DBF8) else TelegramTextSecondary,
                                fontSize = 11.sp
                            )
                            if (message.isOutgoing) {
                                Icon(
                                    imageVector = if (message.isRead) Icons.Default.DoneAll else Icons.Default.Check,
                                    contentDescription = "Status",
                                    tint = if (message.isRead) TelegramCheckBlue else Color.White.copy(alpha = 0.75f),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        ReactionPillRow(
            reactions = message.reactions,
            isOutgoing = message.isOutgoing,
            onReactionClick = onReactionClick
        )
    }
}

/**
 * Super Bubbly Sticker Bubble
 */
@Composable
private fun StickerBubble(
    imageUrl: String?,
    time: String,
    reactions: List<com.example.data.model.ReactionItem>,
    onReactionClick: (() -> Unit)? = null
) {
    Column(horizontalAlignment = Alignment.End) {
        Box(
            modifier = Modifier
                .size(150.dp)
                .clip(RoundedCornerShape(18.dp))
        ) {
            if (!imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = ApiClient.resolveUrl(imageUrl),
                    contentDescription = "Sticker",
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = ContentScale.Fit
                )
            } else {
                StickerView(time = time, reactions = emptyList<com.example.data.model.ReactionItem>())
            }

            // Glassy timestamp pill
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(5.dp)
                    .background(Color(0x800E1620), RoundedCornerShape(10.dp))
                    .border(0.6.dp, Color(0x28FFFFFF), RoundedCornerShape(10.dp))
                    .padding(horizontal = 7.dp, vertical = 2.dp)
            ) {
                Text(
                    text = time,
                    color = Color.White,
                    fontSize = 11.sp
                )
            }
        }

        ReactionPillRow(
            reactions = reactions,
            isOutgoing = true,
            onReactionClick = onReactionClick
        )
    }
}

/**
 * Super Bubbly GIF Bubble
 */
@Composable
private fun GifBubble(
    gifUrl: String?,
    time: String,
    caption: String?,
    isOutgoing: Boolean,
    reactions: List<com.example.data.model.ReactionItem>,
    onReactionClick: (() -> Unit)? = null
) {
    val bubbleShape = RoundedCornerShape(18.dp)

    Column(horizontalAlignment = if (isOutgoing) Alignment.End else Alignment.Start) {
        Box(
            modifier = Modifier
                .width(230.dp)
                .height(185.dp)
                .shadow(3.dp, bubbleShape, spotColor = Color(0x40000000))
                .clip(bubbleShape)
                .background(Color(0xFF162330))
                .border(0.8.dp, Color(0x33FFFFFF), bubbleShape)
        ) {
            AsyncImage(
                model = ApiClient.resolveUrl(gifUrl.orEmpty()),
                contentDescription = "GIF",
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop
            )

            // Glassy timestamp
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(6.dp)
                    .background(Color(0x800E1620), RoundedCornerShape(10.dp))
                    .border(0.6.dp, Color(0x28FFFFFF), RoundedCornerShape(10.dp))
                    .padding(horizontal = 7.dp, vertical = 2.dp)
            ) {
                Text(
                    text = time,
                    color = Color.White,
                    fontSize = 11.sp
                )
            }
        }

        ReactionPillRow(
            reactions = reactions,
            isOutgoing = isOutgoing,
            onReactionClick = onReactionClick
        )
    }
}

/**
 * Bubbly & Floating Glass Reaction Pills
 */
@Composable
private fun ReactionPillRow(
    reactions: List<com.example.data.model.ReactionItem>,
    isOutgoing: Boolean,
    onReactionClick: (() -> Unit)? = null
) {
    if (reactions.isNotEmpty()) {
        Row(
            modifier = Modifier
                .padding(top = 3.dp, start = if (isOutgoing) 0.dp else 6.dp, end = if (isOutgoing) 6.dp else 0.dp)
                .shadow(2.dp, CircleShape, spotColor = Color(0x33000000))
                .clip(CircleShape)
                .background(Color(0xEB182533))
                .border(1.dp, Color(0x4052B8FF), CircleShape)
                .clickable { onReactionClick?.invoke() }
                .padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            reactions.forEach { reaction ->
                Text(text = reaction.emoji, fontSize = 14.sp)
                Spacer(modifier = Modifier.width(5.dp))
                AvatarView(
                    avatarType = reaction.userAvatarType,
                    size = 16.dp
                )
            }
        }
    }
}

/**
 * Image Collage View
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ImageCollageView(
    time: String,
    photoResId: Int?,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val bubbleShape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 5.dp)

    Box(
        modifier = modifier
            .width(260.dp)
            .height(180.dp)
            .shadow(3.dp, bubbleShape, spotColor = Color(0x40000000))
            .clip(bubbleShape)
            .background(TelegramOutgoingGradient)
            .border(0.85.dp, Color(0x3D72B0E8), bubbleShape)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        androidx.compose.foundation.Image(
            painter = painterResource(id = photoResId ?: R.drawable.img_chat_wallpaper),
            contentDescription = "Media Collage",
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.Crop
        )

        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(8.dp)
                .background(Color(0x800E1620), RoundedCornerShape(10.dp))
                .border(0.5.dp, Color(0x24FFFFFF), RoundedCornerShape(10.dp))
                .padding(horizontal = 7.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(text = time, color = Color.White, fontSize = 11.sp)
            Icon(
                imageVector = Icons.Default.DoneAll,
                contentDescription = null,
                tint = TelegramCheckBlue,
                modifier = Modifier.size(13.dp)
            )
        }
    }
}

/**
 * Super Bubbly Telegram Location Bubble with Map Pin & Coordinates
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LocationBubble(
    message: MessageItem,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onReactionClick: (() -> Unit)? = null
) {
    val bubbleShape = if (message.isOutgoing) {
        RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 4.dp)
    } else {
        RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 4.dp, bottomEnd = 20.dp)
    }

    val gradient = if (message.isOutgoing) TelegramOutgoingGradient else TelegramIncomingGradient
    val borderColor = if (message.isOutgoing) Color(0x3D72B0E8) else Color(0x24FFFFFF)

    Column(horizontalAlignment = if (message.isOutgoing) Alignment.End else Alignment.Start) {
        Box(
            modifier = Modifier
                .widthIn(min = 220.dp, max = 290.dp)
                .shadow(2.dp, bubbleShape, spotColor = Color(0x40000000))
                .clip(bubbleShape)
                .background(gradient)
                .border(0.85.dp, borderColor, bubbleShape)
                .combinedClickable(onClick = onClick, onLongClick = onLongClick)
                .padding(12.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE53935).copy(alpha = 0.2f))
                            .border(1.dp, Color(0xFFE53935).copy(alpha = 0.6f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Location",
                            tint = Color(0xFFEF5350),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = message.text ?: "Current Location",
                            color = TelegramTextPrimary,
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Tap to view on map",
                            color = TelegramPrimary,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = message.time, color = TelegramTextSecondary, fontSize = 11.sp)
                    if (message.isOutgoing) {
                        Spacer(modifier = Modifier.width(3.dp))
                        Icon(
                            imageVector = if (message.isRead) Icons.Default.DoneAll else Icons.Default.Check,
                            contentDescription = null,
                            tint = TelegramCheckBlue,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Super Bubbly Telegram Poll Bubble with voting options
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PollBubble(
    message: MessageItem,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onReactionClick: (() -> Unit)? = null
) {
    val bubbleShape = if (message.isOutgoing) {
        RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 4.dp)
    } else {
        RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 4.dp, bottomEnd = 20.dp)
    }

    val gradient = if (message.isOutgoing) TelegramOutgoingGradient else TelegramIncomingGradient
    val borderColor = if (message.isOutgoing) Color(0x3D72B0E8) else Color(0x24FFFFFF)
    var selectedOption by remember { mutableStateOf<Int?>(null) }

    // Parse options from file/text or defaults
    val question = message.text ?: "What is your favorite framework?"
    val options = listOf("Jetpack Compose", "Flutter", "React Native")

    Column(horizontalAlignment = if (message.isOutgoing) Alignment.End else Alignment.Start) {
        Box(
            modifier = Modifier
                .widthIn(min = 250.dp, max = 310.dp)
                .shadow(2.dp, bubbleShape, spotColor = Color(0x40000000))
                .clip(bubbleShape)
                .background(gradient)
                .border(0.85.dp, borderColor, bubbleShape)
                .combinedClickable(onClick = onClick, onLongClick = onLongClick)
                .padding(14.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Poll,
                        contentDescription = "Poll",
                        tint = TelegramPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Anonymous Poll",
                        color = TelegramTextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = question,
                    color = TelegramTextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Poll Options
                options.forEachIndexed { index, option ->
                    val isChecked = selectedOption == index
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isChecked) TelegramPrimary.copy(alpha = 0.2f) else Color(0x22FFFFFF))
                            .clickable { selectedOption = index }
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isChecked) Icons.Default.CheckCircle else Icons.Default.Check,
                            contentDescription = null,
                            tint = if (isChecked) TelegramPrimary else TelegramTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = option,
                            color = TelegramTextPrimary,
                            fontSize = 13.5.sp,
                            fontWeight = if (isChecked) FontWeight.SemiBold else FontWeight.Normal,
                            modifier = Modifier.weight(1f)
                        )
                        if (selectedOption != null) {
                            Text(
                                text = if (isChecked) "65%" else "17%",
                                color = TelegramPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (selectedOption != null) "1 vote" else "Vote to see results",
                        color = TelegramTextSecondary,
                        fontSize = 11.sp
                    )
                    Text(text = message.time, color = TelegramTextSecondary, fontSize = 11.sp)
                }
            }
        }
    }
}

/**
 * Super Bubbly Telegram File / Document Bubble
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FileBubble(
    message: MessageItem,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onReactionClick: (() -> Unit)? = null
) {
    val bubbleShape = if (message.isOutgoing) {
        RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 4.dp)
    } else {
        RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 4.dp, bottomEnd = 20.dp)
    }

    val gradient = if (message.isOutgoing) TelegramOutgoingGradient else TelegramIncomingGradient
    val borderColor = if (message.isOutgoing) Color(0x3D72B0E8) else Color(0x24FFFFFF)

    Column(horizontalAlignment = if (message.isOutgoing) Alignment.End else Alignment.Start) {
        Box(
            modifier = Modifier
                .widthIn(min = 220.dp, max = 285.dp)
                .shadow(2.dp, bubbleShape, spotColor = Color(0x40000000))
                .clip(bubbleShape)
                .background(gradient)
                .border(0.85.dp, borderColor, bubbleShape)
                .combinedClickable(onClick = onClick, onLongClick = onLongClick)
                .padding(12.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(TelegramPrimary)
                            .border(1.dp, Color(0x40FFFFFF), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = "Document",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = message.fileName ?: message.text ?: "Document.pdf",
                            color = TelegramTextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1
                        )
                        Text(
                            text = "2.4 MB • PDF Document",
                            color = TelegramTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = message.time, color = TelegramTextSecondary, fontSize = 11.sp)
                    if (message.isOutgoing) {
                        Spacer(modifier = Modifier.width(3.dp))
                        Icon(
                            imageVector = if (message.isRead) Icons.Default.DoneAll else Icons.Default.Check,
                            contentDescription = null,
                            tint = TelegramCheckBlue,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }
        }
    }
}
