package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.SentimentSatisfiedAlt
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatItem
import com.example.data.model.GifItem
import com.example.data.model.MediaPickerItem
import com.example.data.model.MessageItem
import com.example.data.model.StickerItem
import com.example.ui.components.AttachmentBottomSheet
import com.example.ui.components.AvatarView
import com.example.ui.components.GroupProfileModal
import com.example.ui.components.MessageBubble
import com.example.ui.components.MessageContextMenuPopup
import com.example.ui.components.PhotoViewerDialog
import com.example.ui.components.StickerGifPickerSheet
import com.example.ui.components.TelegramGlassBackground
import com.example.ui.components.TelegramVoiceTopBanner
import com.example.ui.components.UserProfileData
import com.example.ui.components.UserProfileModal
import com.example.ui.components.VideoPlayerDialog
import com.example.ui.media.AudioPlayerManager
import com.example.ui.theme.TelegramDarkBg
import com.example.ui.theme.TelegramGlassBorder
import com.example.ui.theme.TelegramGlassHeader
import com.example.ui.theme.TelegramGlassInput
import com.example.ui.theme.TelegramPrimary
import com.example.ui.theme.TelegramSendFabGradient
import com.example.ui.theme.TelegramTextPrimary
import com.example.ui.theme.TelegramTextSecondary
import kotlinx.coroutines.launch

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
    onSendMedia: (List<MediaPickerItem>) -> Unit = {},
    onSendAudio: () -> Unit = {},
    onSendVideo: () -> Unit = {},
    onToggleReaction: (String, String) -> Unit,
    onDeleteMessage: (String) -> Unit = {},
    onEditMessage: (String, String) -> Unit = { _, _ -> },
    onPinMessage: (MessageItem) -> Unit = {},
    onSendLocation: (Double, Double, String) -> Unit = { _, _, _ -> },
    onSendPoll: (String, List<String>, Boolean) -> Unit = { _, _, _ -> },
    onSendFile: (String, String) -> Unit = { _, _ -> },
    onSendMusic: (String, String) -> Unit = { _, _ -> },
    onOpenPhotoViewer: (MessageItem) -> Unit = {},
    onClosePhotoViewer: () -> Unit = {},
    onOpenVideoPlayer: (MessageItem) -> Unit = {},
    onCloseVideoPlayer: () -> Unit = {},
    onOpenUserProfile: (String?) -> Unit = {},
    onCloseUserProfile: () -> Unit = {},
    onUpdateProfile: ((String, String, String) -> Unit)? = null,
    onVoiceCall: () -> Unit = {},
    onVideoCall: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isPinnedVisible by remember { mutableStateOf(true) }

    // Message click popup / context menu state
    var selectedMessageForPopup by remember { mutableStateOf<MessageItem?>(null) }
    var replyingMessage by remember { mutableStateOf<MessageItem?>(null) }
    var editingMessage by remember { mutableStateOf<MessageItem?>(null) }

    val audioState by AudioPlayerManager.playbackState.collectAsState()
    val density = LocalDensity.current
    val isKeyboardOpen = WindowInsets.ime.getBottom(density) > 0

    // Handle system back navigation priority
    BackHandler {
        when {
            selectedMessageForPopup != null -> selectedMessageForPopup = null
            activePhotoMessage != null -> onClosePhotoViewer()
            activeVideoMessage != null -> onCloseVideoPlayer()
            isProfileModalOpen -> onCloseUserProfile()
            isStickerSheetOpen -> onCloseStickerSheet()
            isAttachmentSheetOpen -> onCloseAttachmentSheet()
            replyingMessage != null -> replyingMessage = null
            editingMessage != null -> {
                editingMessage = null
                inputText = ""
            }
            else -> onBack()
        }
    }

    // Auto-scroll on new messages
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
        // High-end ambient glass canvas background with glowing light orbs
        TelegramGlassBackground()

        Column(modifier = Modifier.fillMaxSize()) {
            // Glassmorphic Top App Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TelegramGlassHeader)
                    .border(0.8.dp, TelegramGlassBorder)
                    .statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    // Contact / Group Avatar
                    Box(modifier = Modifier.clickable { onOpenUserProfile(chat.id) }) {
                        AvatarView(
                            avatarType = chat.avatarType,
                            avatarResId = chat.avatarResId,
                            avatarUrl = chat.avatarUrl,
                            title = chat.title,
                            size = 42.dp
                        )
                        // Emerald Green Online Indicator Dot for 1-on-1 chats
                        if (!chat.isGroup) {
                            Box(
                                modifier = Modifier
                                    .size(11.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF00E676))
                                    .border(1.5.dp, TelegramDarkBg, CircleShape)
                                    .align(Alignment.BottomEnd)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // Contact Name & Status
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onOpenUserProfile(chat.id) }
                    ) {
                        Text(
                            text = chat.title,
                            color = TelegramTextPrimary,
                            fontSize = 16.5.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (chat.isGroup) "44 members, 12 online" else if (chat.isTyping) "typing..." else "online",
                            color = if (chat.isTyping) TelegramPrimary else Color(0xFF5AB4F8),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Voice Call Action
                    IconButton(onClick = onVoiceCall) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Voice Call",
                            tint = TelegramTextSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Video Call Action
                    IconButton(onClick = onVideoCall) {
                        Icon(
                            imageVector = Icons.Default.Videocam,
                            contentDescription = "Video Call",
                            tint = TelegramTextSecondary,
                            modifier = Modifier.size(23.dp)
                        )
                    }

                    // 3-Dots Options -> Opens Profile
                    IconButton(onClick = { onOpenUserProfile(chat.id) }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = TelegramTextSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            // Real Telegram Voice Top Floating Player Banner
            TelegramVoiceTopBanner()

            // Iconic Telegram Glass Pinned Message Banner
            if (isPinnedVisible && messages.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xD0131E2A))
                        .border(0.5.dp, Color(0x22FFFFFF))
                        .clickable {
                            coroutineScope.launch {
                                listState.animateScrollToItem(0)
                            }
                        }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Cyan vertical bar
                        Box(
                            modifier = Modifier
                                .width(2.5.dp)
                                .height(28.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(TelegramPrimary)
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = "Pinned",
                            tint = TelegramPrimary,
                            modifier = Modifier.size(16.dp)
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Pinned Message",
                                color = TelegramPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = messages.firstOrNull()?.text ?: "Welcome to Telegram",
                                color = TelegramTextSecondary,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        IconButton(
                            onClick = { isPinnedVisible = false },
                            modifier = Modifier.size(22.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Unpin",
                                tint = TelegramTextSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            // Message List (Bubbly 60fps LazyColumn)
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
            ) {
                items(messages, key = { it.id }) { message ->
                    MessageBubble(
                        message = message,
                        onReactionClick = {
                            onToggleReaction(message.id, "❤️")
                        },
                        onSelectReaction = { emoji ->
                            onToggleReaction(message.id, emoji)
                        },
                        onPhotoClick = {
                            onOpenPhotoViewer(it)
                        },
                        onVideoClick = {
                            onOpenVideoPlayer(it)
                        },
                        onMessageClick = {
                            selectedMessageForPopup = it
                        }
                    )
                }
            }

            // Replying or Editing Banner above input bar
            AnimatedVisibility(
                visible = replyingMessage != null || editingMessage != null,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xE017212B))
                        .border(0.5.dp, TelegramGlassBorder)
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(32.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(TelegramPrimary)
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (editingMessage != null) "Edit Message" else "Reply to ${replyingMessage?.senderName ?: "User"}",
                                color = TelegramPrimary,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = (if (editingMessage != null) editingMessage?.text else replyingMessage?.text) ?: "Media",
                                color = TelegramTextSecondary,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        IconButton(
                            onClick = {
                                replyingMessage = null
                                if (editingMessage != null) {
                                    editingMessage = null
                                    inputText = ""
                                }
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cancel",
                                tint = TelegramTextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Fixed Bubbly Telegram Input Bar with accurate IME keyboard handling
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (isKeyboardOpen) Modifier.imePadding() else Modifier.navigationBarsPadding())
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Frosted Glass Main Capsule for Input & Attachments
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .shadow(6.dp, RoundedCornerShape(26.dp), spotColor = Color(0x66000000))
                            .clip(RoundedCornerShape(26.dp))
                            .background(TelegramGlassInput)
                            .border(1.dp, TelegramGlassBorder, RoundedCornerShape(26.dp))
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Emoji / Sticker Button
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
                                    contentDescription = "Emoji & Stickers",
                                    tint = if (isStickerSheetOpen) TelegramPrimary else TelegramTextSecondary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            // Clean Material TextField with no underline
                            TextField(
                                value = inputText,
                                onValueChange = { inputText = it },
                                placeholder = {
                                    Text(
                                        text = if (editingMessage != null) "Edit message..." else "Message",
                                        color = TelegramTextSecondary,
                                        fontSize = 16.sp
                                    )
                                },
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    disabledContainerColor = Color.Transparent,
                                    cursorColor = TelegramPrimary,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    focusedTextColor = TelegramTextPrimary,
                                    unfocusedTextColor = TelegramTextPrimary
                                ),
                                modifier = Modifier.weight(1f)
                            )

                            // Paperclip (سنجاق) Attachment Button
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
                                    modifier = Modifier.size(23.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Bubbly Floating Glass Send / Mic Action Button
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .shadow(8.dp, CircleShape, spotColor = Color(0x662481CC))
                            .clip(CircleShape)
                            .background(TelegramSendFabGradient)
                            .border(1.dp, Color(0x4DFFFFFF), CircleShape)
                            .clickable {
                                if (inputText.isNotBlank()) {
                                    val currentEditing = editingMessage
                                    if (currentEditing != null) {
                                        onEditMessage(currentEditing.id, inputText)
                                        editingMessage = null
                                    } else {
                                        onSendMessage(inputText)
                                    }
                                    replyingMessage = null
                                    inputText = ""
                                } else {
                                    onSendAudio()
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (inputText.isBlank()) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Voice note",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Icon(
                                imageVector = if (editingMessage != null) Icons.Default.Edit else Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
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

        // Authentic Telegram Message Context Menu Popup (Reaction bar + Reply, Pin, Edit, Copy, Forward, Delete)
        MessageContextMenuPopup(
            message = selectedMessageForPopup,
            visible = selectedMessageForPopup != null,
            onDismiss = { selectedMessageForPopup = null },
            onReaction = { emoji ->
                selectedMessageForPopup?.let { msg ->
                    onToggleReaction(msg.id, emoji)
                }
            },
            onReply = { msg ->
                replyingMessage = msg
                selectedMessageForPopup = null
            },
            onPin = { msg ->
                onPinMessage(msg)
                isPinnedVisible = true
                selectedMessageForPopup = null
            },
            onEdit = { msg ->
                editingMessage = msg
                inputText = msg.text.orEmpty()
                selectedMessageForPopup = null
            },
            onForward = { msg ->
                selectedMessageForPopup = null
            },
            onDelete = { msg ->
                onDeleteMessage(msg.id)
                selectedMessageForPopup = null
            }
        )

        // Glassy Attachment Bottom Sheet Overlay (Paperclip / سنجاق)
        AttachmentBottomSheet(
            visible = isAttachmentSheetOpen,
            onDismiss = onCloseAttachmentSheet,
            onSendMedia = onSendMedia,
            onSendLocation = onSendLocation,
            onSendPoll = onSendPoll,
            onSendFile = onSendFile,
            onSendMusic = onSendMusic
        )

        // Pinch-to-Zoom Photo Viewer Overlay
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

        // Profile Modal - Displays Group Profile if chat.isGroup, otherwise User Profile Modal
        if (chat.isGroup) {
            GroupProfileModal(
                visible = isProfileModalOpen,
                groupName = chat.title,
                groupAvatarUrl = chat.avatarUrl,
                description = "Official Telegram Group. Share ideas, code, media, and connect with developers worldwide.",
                onDismiss = onCloseUserProfile,
                onMessageClick = onCloseUserProfile
            )
        } else {
            UserProfileModal(
                visible = isProfileModalOpen,
                user = profileUser,
                isUpdating = isProfileUpdating,
                onDismiss = onCloseUserProfile,
                onUpdateProfile = onUpdateProfile
            )
        }
    }
}
