package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.ChatItem
import com.example.data.model.GifItem
import com.example.data.model.MediaPickerItem
import com.example.data.model.MessageItem
import com.example.data.model.StickerItem
import com.example.ui.components.AttachmentBottomSheet
import com.example.ui.components.AvatarView
import com.example.ui.components.CreatePollDialog
import com.example.ui.components.MessageBubble
import com.example.ui.components.PhotoViewerDialog
import com.example.ui.components.SendLocationDialog
import com.example.ui.components.StickerGifPickerSheet
import com.example.ui.components.UserProfileData
import com.example.ui.components.UserProfileModal
import com.example.ui.locale.LocalAppStrings
import com.example.ui.theme.TelegramPrimary
import com.example.ui.theme.appPalette

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    chat: ChatItem,
    messages: List<MessageItem>,
    isAttachmentSheetOpen: Boolean,
    isStickerSheetOpen: Boolean,
    stickers: List<StickerItem>,
    gifs: List<GifItem>,
    isStickersLoading: Boolean,
    activePhotoMessage: MessageItem?,
    activeVideoMessage: MessageItem?,
    isProfileModalOpen: Boolean,
    profileUser: UserProfileData?,
    isProfileUpdating: Boolean,
    onBack: () -> Unit,
    onOpenAttachmentSheet: () -> Unit,
    onCloseAttachmentSheet: () -> Unit,
    onOpenStickerSheet: () -> Unit,
    onCloseStickerSheet: () -> Unit,
    onSelectSticker: (StickerItem) -> Unit,
    onSelectGif: (GifItem) -> Unit,
    onSendMessage: (String) -> Unit,
    onSendMedia: (List<MediaPickerItem>) -> Unit,
    onSendAudio: () -> Unit,
    onSendVideo: () -> Unit,
    onToggleReaction: (String, String) -> Unit,
    onDeleteMessage: (String) -> Unit,
    onEditMessage: (String, String) -> Unit,
    onPinMessage: (MessageItem) -> Unit,
    onSendLocation: (Double, Double, String) -> Unit,
    onSendPoll: (String, List<String>, Boolean) -> Unit,
    onSendFile: (String, String) -> Unit,
    onSendMusic: (String, String) -> Unit,
    onOpenPhotoViewer: (MessageItem) -> Unit,
    onClosePhotoViewer: () -> Unit,
    onOpenVideoPlayer: (MessageItem) -> Unit,
    onCloseVideoPlayer: () -> Unit,
    onOpenUserProfile: (String) -> Unit,
    onCloseUserProfile: () -> Unit,
    onUpdateProfile: (String, String?, String?) -> Unit,
    onVoiceCall: () -> Unit,
    onVideoCall: () -> Unit
) {
    val palette = appPalette
    val strings = LocalAppStrings.current

    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    var showLocationDialog by remember { mutableStateOf(false) }
    var showPollDialog by remember { mutableStateOf(false) }

    // Scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.chatListBg)
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
        ) {
            // Header Bar
            Surface(
                color = palette.glassHeader,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = palette.textPrimary
                        )
                    }

                    // Avatar & Details
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onOpenUserProfile(chat.id) }
                            .padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!chat.avatarUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = chat.avatarUrl,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            AvatarView(
                                avatarType = chat.avatarType,
                                size = 40.dp
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = chat.title,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = palette.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (chat.isOnline) strings.online else strings.offline,
                                fontSize = 12.sp,
                                color = if (chat.isOnline) Color(0xFF22C55E) else palette.textSecondary
                            )
                        }
                    }

                    // Call Actions
                    IconButton(onClick = onVoiceCall) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Voice Call",
                            tint = palette.textPrimary
                        )
                    }

                    IconButton(onClick = onVideoCall) {
                        Icon(
                            imageVector = Icons.Default.Videocam,
                            contentDescription = "Video Call",
                            tint = palette.textPrimary
                        )
                    }
                }
            }

            // Message list
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                items(messages, key = { it.id }) { msg ->
                    MessageBubble(
                        message = msg,
                        onPhotoClick = { onOpenPhotoViewer(msg) },
                        onVideoClick = { onOpenVideoPlayer(msg) },
                        onSelectReaction = { emoji -> onToggleReaction(msg.id, emoji) },
                        onMessageClick = {
                            if (msg.type == com.example.data.model.MessageType.PHOTO) {
                                onOpenPhotoViewer(msg)
                            } else if (msg.type == com.example.data.model.MessageType.VIDEO) {
                                onOpenVideoPlayer(msg)
                            }
                        }
                    )
                }
            }

            // Bottom Input Bar
            Surface(
                color = palette.surface,
                shadowElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onOpenAttachmentSheet) {
                        Icon(
                            imageVector = Icons.Default.AttachFile,
                            contentDescription = "Attach",
                            tint = palette.textSecondary
                        )
                    }

                    IconButton(onClick = onOpenStickerSheet) {
                        Icon(
                            imageVector = Icons.Default.Mood,
                            contentDescription = "Stickers",
                            tint = palette.textSecondary
                        )
                    }

                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text(strings.messageHint, color = palette.textSecondary) },
                        maxLines = 4,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = palette.surfaceVariant.copy(alpha = 0.5f),
                            unfocusedContainerColor = palette.surfaceVariant.copy(alpha = 0.5f),
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = palette.textPrimary,
                            unfocusedTextColor = palette.textPrimary
                        )
                    )

                    if (inputText.isNotBlank()) {
                        IconButton(
                            onClick = {
                                val textToSend = inputText.trim()
                                inputText = ""
                                onSendMessage(textToSend)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send",
                                tint = palette.primary
                            )
                        }
                    } else {
                        IconButton(onClick = onSendAudio) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Voice message",
                                tint = palette.textSecondary
                            )
                        }
                    }
                }
            }
        }

        // Attachments Sheet
        if (isAttachmentSheetOpen) {
            AttachmentBottomSheet(
                visible = true,
                onDismiss = onCloseAttachmentSheet,
                onSendMedia = onSendMedia,
                onSendLocation = { lat, lng, title -> onSendLocation(lat, lng, title) },
                onSendPoll = { q, opts, anon -> onSendPoll(q, opts, anon) },
                onSendFile = { name, uri -> onSendFile(name, uri) },
                onSendMusic = { title, uri -> onSendMusic(title, uri) }
            )
        }

        // Sticker / GIF Picker Sheet
        if (isStickerSheetOpen) {
            StickerGifPickerSheet(
                visible = true,
                onDismiss = onCloseStickerSheet,
                stickers = stickers,
                gifs = gifs,
                isLoading = isStickersLoading,
                onSelectSticker = onSelectSticker,
                onSelectGif = onSelectGif,
                onSelectEmoji = { emoji -> onSendMessage(emoji) }
            )
        }

        // Location Dialog
        if (showLocationDialog) {
            SendLocationDialog(
                visible = true,
                onDismiss = { showLocationDialog = false },
                onSendLocation = { lat, lng, title ->
                    onSendLocation(lat, lng, title)
                    showLocationDialog = false
                }
            )
        }

        // Poll Dialog
        if (showPollDialog) {
            CreatePollDialog(
                visible = true,
                onDismiss = { showPollDialog = false },
                onCreatePoll = { q, opts, anon ->
                    onSendPoll(q, opts, anon)
                    showPollDialog = false
                }
            )
        }

        // Photo Viewer
        if (activePhotoMessage != null) {
            PhotoViewerDialog(
                photoUrl = activePhotoMessage.mediaUrl,
                caption = activePhotoMessage.text,
                senderName = activePhotoMessage.senderName,
                time = activePhotoMessage.time,
                onDismiss = onClosePhotoViewer
            )
        }

        // User Profile Modal
        UserProfileModal(
            visible = isProfileModalOpen,
            user = profileUser,
            isUpdating = isProfileUpdating,
            onDismiss = onCloseUserProfile,
            onUpdateProfile = onUpdateProfile
        )
    }
}
