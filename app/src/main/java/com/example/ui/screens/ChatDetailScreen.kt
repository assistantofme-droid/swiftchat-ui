package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.SentimentSatisfiedAlt
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.ChatItem
import com.example.data.model.GifItem
import com.example.data.model.MediaPickerItem
import com.example.data.model.MessageItem
import com.example.data.model.MessageType
import com.example.data.model.StickerItem
import com.example.ui.components.AttachmentBottomSheet
import com.example.ui.components.AvatarView
import com.example.ui.components.MessageBubble
import com.example.ui.components.PhotoViewerDialog
import com.example.ui.components.StickerGifPickerSheet
import com.example.ui.components.UserProfileData
import com.example.ui.components.UserProfileModal
import com.example.ui.components.VideoPlayerDialog
import com.example.ui.theme.TelegramDarkBg
import com.example.ui.theme.TelegramPrimary
import com.example.ui.theme.TelegramTextMuted
import com.example.ui.theme.TelegramTextPrimary
import com.example.ui.theme.TelegramTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    chat: ChatItem,
    messages: List<MessageItem>,
    isAttachmentSheetOpen: Boolean,
    isStickerSheetOpen: Boolean = false,
    stickers: List<StickerItem> = emptyList(),
    gifs: List<GifItem> = emptyList(),
    isStickersLoading: Boolean = false,
    activePhotoMessage: MessageItem? = null,
    activeVideoMessage: MessageItem? = null,
    isProfileModalOpen: Boolean = false,
    profileUser: UserProfileData? = null,
    isProfileUpdating: Boolean = false,
    onBack: () -> Unit,
    onOpenAttachmentSheet: () -> Unit,
    onCloseAttachmentSheet: () -> Unit,
    onOpenStickerSheet: () -> Unit = {},
    onCloseStickerSheet: () -> Unit = {},
    onSelectSticker: (StickerItem) -> Unit = {},
    onSelectGif: (GifItem) -> Unit = {},
    onSendMessage: (String) -> Unit,
    onSendMedia: (List<MediaPickerItem>) -> Unit,
    onSendAudio: () -> Unit = {},
    onSendVideo: () -> Unit = {},
    onToggleReaction: (String, String) -> Unit,
    onOpenPhotoViewer: (MessageItem) -> Unit = {},
    onClosePhotoViewer: () -> Unit = {},
    onOpenVideoPlayer: (MessageItem) -> Unit = {},
    onCloseVideoPlayer: () -> Unit = {},
    onOpenUserProfile: (String?) -> Unit = {},
    onCloseUserProfile: () -> Unit = {},
    onUpdateProfile: ((String, String, String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Handle system back navigation priority
    BackHandler {
        when {
            activePhotoMessage != null -> onClosePhotoViewer()
            activeVideoMessage != null -> onCloseVideoPlayer()
            isProfileModalOpen -> onCloseUserProfile()
            isStickerSheetOpen -> onCloseStickerSheet()
            isAttachmentSheetOpen -> onCloseAttachmentSheet()
            else -> onBack()
        }
    }

    // Smooth scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TelegramDarkBg)
    ) {
        // Chat Wallpaper
        Image(
            painter = painterResource(id = R.drawable.img_chat_wallpaper),
            contentDescription = "Chat Wallpaper",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(modifier = Modifier.fillMaxSize()) {
            // Top Chat App Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TelegramDarkBg.copy(alpha = 0.95f))
                    .statusBarsPadding()
                    .padding(horizontal = 4.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                // Contact Avatar - Clicking opens Profile Modal
                AvatarView(
                    avatarType = chat.avatarType,
                    avatarResId = chat.avatarResId,
                    avatarUrl = chat.avatarUrl,
                    title = chat.title,
                    size = 42.dp,
                    modifier = Modifier
                        .padding(start = 2.dp)
                        .clickable { onOpenUserProfile(chat.id) }
                )

                Spacer(modifier = Modifier.width(10.dp))

                // Name & Status - Clicking opens Profile Modal
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onOpenUserProfile(chat.id) }
                ) {
                    Text(
                        text = chat.title,
                        color = TelegramTextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (chat.isTyping) "typing..." else "online",
                        color = if (chat.isTyping) TelegramPrimary else TelegramTextSecondary,
                        fontSize = 12.sp
                    )
                }

                // Call Icon
                IconButton(onClick = { /* Call */ }) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Call",
                        tint = TelegramTextSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // 3 Dots Menu -> Opens Profile Modal
                IconButton(onClick = { onOpenUserProfile(chat.id) }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Options",
                        tint = TelegramTextSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // Message List (60 FPS smooth rendering)
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp)
            ) {
                items(messages, key = { it.id }) { message ->
                    MessageBubble(
                        message = message,
                        onReactionClick = {
                            onToggleReaction(message.id, "❤️")
                        },
                        onPhotoClick = {
                            onOpenPhotoViewer(it)
                        },
                        onVideoClick = {
                            onOpenVideoPlayer(it)
                        }
                    )
                }
            }

            // Bottom Chat Input Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TelegramDarkBg.copy(alpha = 0.96f))
                    .imePadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sticker / GIF / Emoji Picker Button
                IconButton(
                    onClick = {
                        if (isStickerSheetOpen) {
                            onCloseStickerSheet()
                        } else {
                            if (isAttachmentSheetOpen) onCloseAttachmentSheet()
                            onOpenStickerSheet()
                        }
                    },
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SentimentSatisfiedAlt,
                        contentDescription = "Stickers & GIFs",
                        tint = if (isStickerSheetOpen) TelegramPrimary else TelegramTextSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Message Text Field
                TextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text(
                            text = "Message",
                            color = TelegramTextMuted,
                            fontSize = 15.sp
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = TelegramTextPrimary,
                        unfocusedTextColor = TelegramTextPrimary,
                        cursorColor = TelegramPrimary
                    ),
                    modifier = Modifier.weight(1f)
                )

                // Attachment Button (Paperclip / سنجاق)
                IconButton(
                    onClick = {
                        if (isAttachmentSheetOpen) {
                            onCloseAttachmentSheet()
                        } else {
                            if (isStickerSheetOpen) onCloseStickerSheet()
                            onOpenAttachmentSheet()
                        }
                    },
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AttachFile,
                        contentDescription = "Attach / سنجاق",
                        tint = if (isAttachmentSheetOpen) TelegramPrimary else TelegramTextSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Send or Voice Note Button
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(TelegramPrimary)
                        .clickable {
                            if (inputText.isNotBlank()) {
                                onSendMessage(inputText)
                                inputText = ""
                            } else {
                                // Send voice message
                                onSendAudio()
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (inputText.isBlank()) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Voice message",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Sticker & GIF Picker Bottom Sheet
            StickerGifPickerSheet(
                visible = isStickerSheetOpen,
                stickers = stickers,
                gifs = gifs,
                isLoading = isStickersLoading,
                onDismiss = onCloseStickerSheet,
                onSelectSticker = { sticker ->
                    onSelectSticker(sticker)
                },
                onSelectGif = { gif ->
                    onSelectGif(gif)
                },
                onSelectEmoji = { emoji ->
                    inputText += emoji
                }
            )
        }

        // Attachment Bottom Sheet Overlay (Paperclip / سنجاق)
        AttachmentBottomSheet(
            visible = isAttachmentSheetOpen,
            onDismiss = onCloseAttachmentSheet,
            onSendMedia = onSendMedia
        )

        // Zoomable Photo Viewer Overlay
        activePhotoMessage?.let { photoMsg ->
            val url = photoMsg.mediaUrl
                ?: if (photoMsg.photoResId != null) "android.resource://com.example/${photoMsg.photoResId}" else null
            PhotoViewerDialog(
                photoUrl = url,
                caption = photoMsg.text,
                senderName = if (photoMsg.isOutgoing) "You" else (photoMsg.senderName ?: chat.title),
                time = photoMsg.time,
                onDismiss = onClosePhotoViewer
            )
        }

        // Smooth Video Player Overlay
        activeVideoMessage?.let { videoMsg ->
            VideoPlayerDialog(
                videoUrl = videoMsg.mediaUrl,
                caption = videoMsg.text,
                senderName = if (videoMsg.isOutgoing) "You" else (videoMsg.senderName ?: chat.title),
                onDismiss = onCloseVideoPlayer
            )
        }

        // User Profile Modal (Connected to PUT /auth/profile)
        UserProfileModal(
            visible = isProfileModalOpen,
            user = profileUser,
            isUpdating = isProfileUpdating,
            onDismiss = onCloseUserProfile,
            onUpdateProfile = onUpdateProfile
        )
    }
}
