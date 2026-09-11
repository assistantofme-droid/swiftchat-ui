package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.ui.theme.TelegramCheckBlue
import com.example.ui.theme.TelegramIncomingBubble
import com.example.ui.theme.TelegramOutgoingBubble
import com.example.ui.theme.TelegramPrimary
import com.example.ui.theme.TelegramTextPrimary
import com.example.ui.theme.TelegramTextSecondary

@Composable
fun MessageBubble(
    message: MessageItem,
    modifier: Modifier = Modifier,
    onReactionClick: (() -> Unit)? = null,
    onPhotoClick: ((MessageItem) -> Unit)? = null,
    onVideoClick: ((MessageItem) -> Unit)? = null
) {
    val context = LocalContext.current
    val audioState by AudioPlayerManager.playbackState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 3.dp),
        horizontalAlignment = if (message.isOutgoing) Alignment.End else Alignment.Start
    ) {
        // Optional date header above message
        if (message.dateHeader != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .background(Color(0x9917212B), RoundedCornerShape(14.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = message.dateHeader,
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        when (message.type) {
            MessageType.BIG_STICKER -> {
                if (!message.mediaUrl.isNullOrBlank()) {
                    StickerBubble(
                        imageUrl = message.mediaUrl,
                        time = message.time,
                        reactions = message.reactions,
                        onReactionClick = onReactionClick
                    )
                } else {
                    StickerView(
                        time = message.time,
                        reactions = message.reactions,
                        onReactionClick = onReactionClick
                    )
                }
            }

            MessageType.GIF -> {
                GifBubble(
                    gifUrl = message.mediaUrl,
                    time = message.time,
                    caption = message.text,
                    isOutgoing = message.isOutgoing,
                    reactions = message.reactions,
                    onReactionClick = onReactionClick
                )
            }

            MessageType.VIDEO -> {
                VideoBubble(
                    message = message,
                    onClick = { onVideoClick?.invoke(message) },
                    onReactionClick = onReactionClick
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
                    onReactionClick = onReactionClick
                )
            }

            MessageType.PHOTO -> {
                PhotoBubble(
                    message = message,
                    onPhotoClick = { onPhotoClick?.invoke(message) },
                    onReactionClick = onReactionClick
                )
            }

            MessageType.IMAGE_COLLAGE -> {
                ImageCollageView(
                    time = message.time,
                    photoResId = message.photoResId,
                    onClick = { onPhotoClick?.invoke(message) }
                )
            }

            MessageType.TEXT -> {
                // Check if it has an attached mediaUrl
                if (!message.mediaUrl.isNullOrBlank()) {
                    PhotoBubble(
                        message = message,
                        onPhotoClick = { onPhotoClick?.invoke(message) },
                        onReactionClick = onReactionClick
                    )
                } else {
                    StandardTextBubble(
                        message = message,
                        onReactionClick = onReactionClick
                    )
                }
            }
        }
    }
}

@Composable
private fun StandardTextBubble(
    message: MessageItem,
    onReactionClick: (() -> Unit)? = null
) {
    val bubbleColor = if (message.isOutgoing) TelegramOutgoingBubble else TelegramIncomingBubble
    val bubbleShape = if (message.isOutgoing) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 4.dp)
    } else {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 16.dp)
    }

    Column(
        horizontalAlignment = if (message.isOutgoing) Alignment.End else Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(min = 70.dp, max = 290.dp)
                .clip(bubbleShape)
                .background(bubbleColor)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Column {
                if (!message.isOutgoing && !message.senderName.isNullOrBlank()) {
                    Text(
                        text = message.senderName,
                        color = TelegramPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }

                Text(
                    text = message.text.orEmpty(),
                    color = TelegramTextPrimary,
                    fontSize = 15.sp,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(bottom = 2.dp)
                )

                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = message.time,
                        color = if (message.isOutgoing) Color.White.copy(alpha = 0.75f) else TelegramTextSecondary,
                        fontSize = 11.sp
                    )

                    if (message.isOutgoing) {
                        Icon(
                            imageVector = if (message.isRead) Icons.Default.DoneAll else Icons.Default.Check,
                            contentDescription = if (message.isRead) "Read" else "Sent",
                            tint = if (message.isRead) TelegramCheckBlue else Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(14.dp)
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

@Composable
private fun PhotoBubble(
    message: MessageItem,
    onPhotoClick: () -> Unit,
    onReactionClick: (() -> Unit)? = null
) {
    val resolvedMedia = ApiClient.resolveUrl(message.mediaUrl.orEmpty())
    val bubbleShape = if (message.isOutgoing) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 4.dp)
    } else {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 16.dp)
    }

    Column(horizontalAlignment = if (message.isOutgoing) Alignment.End else Alignment.Start) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(bubbleShape)
                .background(if (message.isOutgoing) TelegramOutgoingBubble else TelegramIncomingBubble)
        ) {
            Column {
                AsyncImage(
                    model = resolvedMedia,
                    contentDescription = "Photo",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(bubbleShape)
                        .clickable(onClick = onPhotoClick),
                    contentScale = ContentScale.Crop
                )

                if (!message.text.isNullOrBlank()) {
                    Text(
                        text = message.text,
                        color = TelegramTextPrimary,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }

                // Time & Status stamp
                Row(
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(end = 8.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = message.time,
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 11.sp
                    )
                    if (message.isOutgoing) {
                        Icon(
                            imageVector = if (message.isRead) Icons.Default.DoneAll else Icons.Default.Check,
                            contentDescription = "Status",
                            tint = if (message.isRead) TelegramCheckBlue else Color.White.copy(alpha = 0.7f),
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

@Composable
private fun VideoBubble(
    message: MessageItem,
    onClick: () -> Unit,
    onReactionClick: (() -> Unit)? = null
) {
    val bubbleShape = if (message.isOutgoing) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 4.dp)
    } else {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 16.dp)
    }

    Column(horizontalAlignment = if (message.isOutgoing) Alignment.End else Alignment.Start) {
        Box(
            modifier = Modifier
                .width(260.dp)
                .height(180.dp)
                .clip(bubbleShape)
                .background(Color(0xFF1E2C3A))
                .clickable(onClick = onClick)
        ) {
            // Thumbnail
            if (!message.videoThumbnailUrl.isNullOrBlank()) {
                AsyncImage(
                    model = ApiClient.resolveUrl(message.videoThumbnailUrl),
                    contentDescription = "Video Thumbnail",
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF182533))
                )
            }

            // Big Play Button in center
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.6f))
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

            // Duration Pill Top Left
            val durSec = message.duration ?: 15
            val min = durSec / 60
            val sec = durSec % 60
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = String.format("%02d:%02d", min, sec),
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Time & checks bottom right
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
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

        ReactionPillRow(
            reactions = message.reactions,
            isOutgoing = message.isOutgoing,
            onReactionClick = onReactionClick
        )
    }
}

@Composable
private fun AudioMessageBubble(
    message: MessageItem,
    isPlaying: Boolean,
    progress: Float,
    displayedSeconds: Int,
    onPlayPause: () -> Unit,
    onReactionClick: (() -> Unit)? = null
) {
    val bubbleColor = if (message.isOutgoing) TelegramOutgoingBubble else TelegramIncomingBubble
    val bubbleShape = if (message.isOutgoing) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 4.dp)
    } else {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 16.dp)
    }

    Column(horizontalAlignment = if (message.isOutgoing) Alignment.End else Alignment.Start) {
        Box(
            modifier = Modifier
                .widthIn(min = 220.dp, max = 280.dp)
                .clip(bubbleShape)
                .background(bubbleColor)
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Play / Pause Circle
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(TelegramPrimary)
                        .clickable(onClick = onPlayPause),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = message.fileName ?: (if (message.text.isNullOrBlank()) "Voice message" else message.text),
                        color = TelegramTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Audio track progress
                    LinearProgressIndicator(
                        progress = { progress },
                        color = TelegramPrimary,
                        trackColor = Color.White.copy(alpha = 0.2f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val min = displayedSeconds / 60
                        val sec = displayedSeconds % 60
                        Text(
                            text = String.format("%02d:%02d", min, sec),
                            color = TelegramTextSecondary,
                            fontSize = 11.sp
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text(
                                text = message.time,
                                color = if (message.isOutgoing) Color.White.copy(alpha = 0.75f) else TelegramTextSecondary,
                                fontSize = 11.sp
                            )
                            if (message.isOutgoing) {
                                Icon(
                                    imageVector = if (message.isRead) Icons.Default.DoneAll else Icons.Default.Check,
                                    contentDescription = "Status",
                                    tint = if (message.isRead) TelegramCheckBlue else Color.White.copy(alpha = 0.7f),
                                    modifier = Modifier.size(13.dp)
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

@Composable
private fun StickerBubble(
    imageUrl: String,
    time: String,
    reactions: List<com.example.data.model.ReactionItem>,
    onReactionClick: (() -> Unit)? = null
) {
    Column(horizontalAlignment = Alignment.End) {
        Box(
            modifier = Modifier
                .size(140.dp)
                .clip(RoundedCornerShape(12.dp))
        ) {
            AsyncImage(
                model = ApiClient.resolveUrl(imageUrl),
                contentDescription = "Sticker",
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.Fit
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(4.dp)
                    .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
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

@Composable
private fun GifBubble(
    gifUrl: String?,
    time: String,
    caption: String?,
    isOutgoing: Boolean,
    reactions: List<com.example.data.model.ReactionItem>,
    onReactionClick: (() -> Unit)? = null
) {
    Column(horizontalAlignment = if (isOutgoing) Alignment.End else Alignment.Start) {
        Box(
            modifier = Modifier
                .width(220.dp)
                .height(180.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF182533))
        ) {
            AsyncImage(
                model = ApiClient.resolveUrl(gifUrl.orEmpty()),
                contentDescription = "GIF",
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(6.dp)
                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
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

@Composable
private fun ReactionPillRow(
    reactions: List<com.example.data.model.ReactionItem>,
    isOutgoing: Boolean,
    onReactionClick: (() -> Unit)? = null
) {
    if (reactions.isNotEmpty()) {
        Row(
            modifier = Modifier
                .padding(top = 2.dp, start = if (isOutgoing) 0.dp else 4.dp, end = if (isOutgoing) 4.dp else 0.dp)
                .background(Color(0xEE1E2C3A), CircleShape)
                .clip(CircleShape)
                .clickable { onReactionClick?.invoke() }
                .padding(horizontal = 7.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            reactions.forEach { reaction ->
                Text(text = reaction.emoji, fontSize = 13.sp)
                Spacer(modifier = Modifier.width(4.dp))
                AvatarView(
                    avatarType = reaction.userAvatarType,
                    size = 15.dp
                )
            }
        }
    }
}
