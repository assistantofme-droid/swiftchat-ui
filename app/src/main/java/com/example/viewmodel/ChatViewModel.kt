package com.example.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.data.api.ApiClient
import com.example.data.api.ApiConversation
import com.example.data.api.ApiMessage
import com.example.data.api.ReactionRequest
import com.example.data.api.SendMessageRequest
import com.example.data.api.SendOtpRequest
import com.example.data.api.UpdateProfileRequest
import com.example.data.api.VerifyOtpRequest
import com.example.data.local.SessionManager
import com.example.data.model.AvatarType
import com.example.data.model.ChatItem
import com.example.data.model.GifItem
import com.example.data.model.MediaPickerItem
import com.example.data.model.MessageItem
import com.example.data.model.MessageType
import com.example.data.model.ReactionItem
import com.example.data.model.StickerItem
import com.example.ui.components.UserProfileData
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class ChatUiState(
    val isAuthenticated: Boolean = false,
    val isAuthLoading: Boolean = false,
    val authErrorMessage: String? = null,
    val currentUserName: String? = null,
    val currentUserPhone: String? = null,
    val currentUserAvatar: String? = null,
    val currentUserBio: String? = null,
    val chats: List<ChatItem> = emptyList(),
    val selectedChatId: String? = null,
    val currentMessages: List<MessageItem> = emptyList(),
    val isAttachmentSheetOpen: Boolean = false,
    val selectedCategoryTab: String = "All Chats",
    val selectedBottomNavIndex: Int = 0,
    val isSearching: Boolean = false,
    val searchQuery: String = "",
    val isRefreshing: Boolean = false,
    val networkBannerMessage: String? = null,

    // Stickers & GIFs
    val isStickerSheetOpen: Boolean = false,
    val stickers: List<StickerItem> = emptyList(),
    val gifs: List<GifItem> = emptyList(),
    val isStickersLoading: Boolean = false,

    // Media Viewers
    val activePhotoMessage: MessageItem? = null,
    val activeVideoMessage: MessageItem? = null,

    // Profile Modal
    val isProfileModalOpen: Boolean = false,
    val profileUser: UserProfileData? = null,
    val isProfileUpdating: Boolean = false
)

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionManager = SessionManager(application)
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var messagePollingJob: Job? = null
    private var conversationPollingJob: Job? = null

    init {
        ApiClient.setTokenProvider { sessionManager.token }

        val loggedIn = sessionManager.isLoggedIn
        _uiState.update {
            it.copy(
                isAuthenticated = loggedIn,
                currentUserName = sessionManager.name,
                currentUserPhone = sessionManager.phone,
                currentUserAvatar = sessionManager.avatar,
                currentUserBio = sessionManager.bio
            )
        }

        loadStickersAndGifs()

        if (loggedIn) {
            refreshConversations()
            startConversationsPolling()
        } else {
            // Preload sample chats so UI has rich visuals if guest mode is used
            loadFallbackChats()
        }
    }

    // -------------------------------------------------------------
    // Authentication Flow
    // -------------------------------------------------------------

    fun sendOtp(phone: String) {
        _uiState.update { it.copy(isAuthLoading = true, authErrorMessage = null) }
        viewModelScope.launch {
            try {
                val cleanPhone = phone.replace("+", "").replace(" ", "").trim()
                val response = ApiClient.service.sendOtp(SendOtpRequest(phone = cleanPhone, forceSms = false))
                if (response.isSuccessful) {
                    _uiState.update {
                        it.copy(
                            isAuthLoading = false,
                            authErrorMessage = null,
                            currentUserPhone = cleanPhone
                        )
                    }
                } else {
                    val err = response.errorBody()?.string() ?: "Failed to send code (${response.code()})"
                    _uiState.update {
                        it.copy(
                            isAuthLoading = false,
                            authErrorMessage = "Error: $err"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "sendOtp error", e)
                _uiState.update {
                    it.copy(
                        isAuthLoading = false,
                        authErrorMessage = "Connection error: ${e.localizedMessage ?: "Check your network"}"
                    )
                }
            }
        }
    }

    fun verifyOtp(phone: String, code: String) {
        _uiState.update { it.copy(isAuthLoading = true, authErrorMessage = null) }
        viewModelScope.launch {
            try {
                val cleanPhone = phone.replace("+", "").replace(" ", "").trim()
                val response = ApiClient.service.verifyOtp(VerifyOtpRequest(phone = cleanPhone, code = code.trim()))
                if (response.isSuccessful && response.body()?.token != null) {
                    val body = response.body()!!
                    val token = body.token!!
                    val user = body.user

                    sessionManager.saveSession(
                        token = token,
                        id = user?._id,
                        phone = user?.phone ?: cleanPhone,
                        name = user?.name ?: "User",
                        username = user?.username,
                        avatar = user?.avatar
                    )

                    ApiClient.setTokenProvider { token }

                    _uiState.update {
                        it.copy(
                            isAuthenticated = true,
                            isAuthLoading = false,
                            authErrorMessage = null,
                            currentUserName = user?.name ?: "User",
                            currentUserPhone = user?.phone ?: cleanPhone,
                            currentUserAvatar = user?.avatar
                        )
                    }

                    refreshConversations()
                    startConversationsPolling()
                } else {
                    val err = response.errorBody()?.string() ?: "Invalid OTP verification code (${response.code()})"
                    _uiState.update {
                        it.copy(
                            isAuthLoading = false,
                            authErrorMessage = err
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "verifyOtp error", e)
                _uiState.update {
                    it.copy(
                        isAuthLoading = false,
                        authErrorMessage = "Verification failed: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    fun skipLogin() {
        _uiState.update {
            it.copy(
                isAuthenticated = true,
                authErrorMessage = null
            )
        }
        refreshConversations()
    }

    fun logout() {
        sessionManager.clearSession()
        _uiState.update {
            it.copy(
                isAuthenticated = false,
                currentUserName = null,
                currentUserPhone = null,
                currentUserAvatar = null,
                selectedChatId = null
            )
        }
    }

    // -------------------------------------------------------------
    // Conversations API
    // -------------------------------------------------------------

    fun refreshConversations() {
        _uiState.update { it.copy(isRefreshing = true) }
        viewModelScope.launch {
            try {
                val response = try {
                    val r1 = ApiClient.service.getConversations()
                    if (r1.isSuccessful && r1.body() != null) r1
                    else ApiClient.service.getConversationsAlt()
                } catch (e: Exception) {
                    ApiClient.service.getConversationsAlt()
                }

                if (response.isSuccessful && response.body() != null) {
                    val apiConversations = response.body()!!
                    val mappedChats = apiConversations.map { apiConv ->
                        mapApiConversationToChatItem(apiConv)
                    }

                    _uiState.update { state ->
                        state.copy(
                            chats = if (mappedChats.isNotEmpty()) mappedChats else state.chats.ifEmpty { fallbackChatsList() },
                            isRefreshing = false,
                            networkBannerMessage = null
                        )
                    }
                } else {
                    // Fallback to existing or mock if empty
                    _uiState.update { state ->
                        state.copy(
                            chats = state.chats.ifEmpty { fallbackChatsList() },
                            isRefreshing = false
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "refreshConversations error", e)
                _uiState.update { state ->
                    state.copy(
                        chats = state.chats.ifEmpty { fallbackChatsList() },
                        isRefreshing = false,
                        networkBannerMessage = "Offline mode - showing cached chats"
                    )
                }
            }
        }
    }

    private fun startConversationsPolling() {
        conversationPollingJob?.cancel()
        conversationPollingJob = viewModelScope.launch {
            while (isActive) {
                delay(12000) // Poll every 12 seconds
                if (_uiState.value.selectedChatId == null && _uiState.value.isAuthenticated) {
                    try {
                        val resp = ApiClient.service.getConversations()
                        if (resp.isSuccessful && resp.body() != null && resp.body()!!.isNotEmpty()) {
                            val mapped = resp.body()!!.map { mapApiConversationToChatItem(it) }
                            _uiState.update { it.copy(chats = mapped) }
                        }
                    } catch (_: Exception) {}
                }
            }
        }
    }

    private fun mapApiConversationToChatItem(apiConv: ApiConversation): ChatItem {
        val currentUserId = sessionManager.userId
        val otherParticipant = apiConv.participants?.firstOrNull { it.user?._id != currentUserId }

        val title = when {
            !apiConv.name.isNullOrBlank() -> apiConv.name
            !otherParticipant?.name.isNullOrBlank() -> otherParticipant?.name!!
            !otherParticipant?.user?.name.isNullOrBlank() -> otherParticipant?.user?.name!!
            !otherParticipant?.user?.username.isNullOrBlank() -> otherParticipant?.user?.username!!
            else -> "Telegram User"
        }

        val avatar = apiConv.avatar ?: otherParticipant?.avatar ?: otherParticipant?.user?.avatar
        val subtitle = apiConv.lastMessage?.text ?: "No messages"
        val time = formatApiTime(apiConv.lastMessage?.createdAt ?: apiConv.lastMessageAt)
        val unread = apiConv.unreadCount ?: 0
        val isPinned = apiConv.pinned == true
        val isMuted = apiConv.isMuted == true

        return ChatItem(
            id = apiConv._id,
            title = title,
            subtitle = subtitle,
            time = time,
            isPinned = isPinned,
            unreadCount = unread,
            isMuted = isMuted,
            avatarUrl = avatar,
            avatarType = AvatarType.MOTORCYCLE
        )
    }

    // -------------------------------------------------------------
    // Messages API
    // -------------------------------------------------------------

    fun selectChat(chatId: String?) {
        _uiState.update { it.copy(selectedChatId = chatId) }
        messagePollingJob?.cancel()

        if (chatId != null) {
            fetchMessages(chatId)
            startMessagePolling(chatId)
            markConversationAsRead(chatId)
        }
    }

    private fun fetchMessages(conversationId: String) {
        viewModelScope.launch {
            try {
                val response = ApiClient.service.getMessages(conversationId, page = 1, limit = 50)
                if (response.isSuccessful && response.body() != null) {
                    val apiMessages = response.body()!!
                    val mappedMessages = apiMessages.map { mapApiMessageToItem(it) }

                    _uiState.update { state ->
                        state.copy(
                            currentMessages = if (mappedMessages.isNotEmpty()) mappedMessages else defaultMessagesList()
                        )
                    }
                } else {
                    // Fallback to default message list if empty
                    _uiState.update { state ->
                        state.copy(
                            currentMessages = if (state.currentMessages.isEmpty()) defaultMessagesList() else state.currentMessages
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "fetchMessages error", e)
                _uiState.update { state ->
                    state.copy(
                        currentMessages = if (state.currentMessages.isEmpty()) defaultMessagesList() else state.currentMessages
                    )
                }
            }
        }
    }

    private fun startMessagePolling(conversationId: String) {
        messagePollingJob?.cancel()
        messagePollingJob = viewModelScope.launch {
            while (isActive) {
                delay(4000) // Poll for new messages every 4 seconds in active chat
                if (_uiState.value.selectedChatId == conversationId) {
                    try {
                        val response = ApiClient.service.getMessages(conversationId, page = 1, limit = 50)
                        if (response.isSuccessful && response.body() != null) {
                            val mapped = response.body()!!.map { mapApiMessageToItem(it) }
                            if (mapped.isNotEmpty()) {
                                _uiState.update { it.copy(currentMessages = mapped) }
                            }
                        }
                    } catch (_: Exception) {}
                }
            }
        }
    }

    private fun markConversationAsRead(conversationId: String) {
        viewModelScope.launch {
            try {
                ApiClient.service.markConversationRead(conversationId)
            } catch (_: Exception) {}
        }
    }

    private fun mapApiMessageToItem(apiMsg: ApiMessage): MessageItem {
        val currentUserId = sessionManager.userId
        val currentUserPhone = sessionManager.phone
        val isOutgoing = (currentUserId != null && apiMsg.sender?._id == currentUserId) ||
                (currentUserPhone != null && apiMsg.sender?.phone == currentUserPhone)

        val type = when (apiMsg.type?.lowercase()) {
            "image", "photo" -> MessageType.PHOTO
            "video" -> MessageType.VIDEO
            "audio", "voice" -> MessageType.AUDIO
            "gif" -> MessageType.GIF
            "sticker" -> MessageType.BIG_STICKER
            else -> MessageType.TEXT
        }

        val reactions = apiMsg.reactions?.map {
            ReactionItem(emoji = it.emoji, userAvatarType = AvatarType.MOTORCYCLE)
        } ?: emptyList()

        return MessageItem(
            id = apiMsg._id,
            text = apiMsg.text,
            time = formatApiTime(apiMsg.createdAt),
            isOutgoing = isOutgoing,
            type = type,
            mediaUrl = apiMsg.fileUrl,
            videoThumbnailUrl = apiMsg.videoThumbnailUrl,
            duration = apiMsg.duration,
            fileName = apiMsg.fileName,
            senderName = if (!isOutgoing) apiMsg.sender?.name ?: apiMsg.sender?.username else null,
            senderAvatarUrl = apiMsg.sender?.avatar,
            reactions = reactions,
            isRead = !apiMsg.readBy.isNullOrEmpty()
        )
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        val currentChatId = _uiState.value.selectedChatId ?: return

        val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        val tempId = UUID.randomUUID().toString()
        val localMessage = MessageItem(
            id = tempId,
            text = text.trim(),
            time = currentTime,
            isOutgoing = true,
            type = MessageType.TEXT,
            isRead = false
        )

        // Optimistic UI update
        _uiState.update { state ->
            val updatedMessages = state.currentMessages + localMessage
            val updatedChats = state.chats.map { chat ->
                if (chat.id == currentChatId) {
                    chat.copy(subtitle = text.trim(), time = currentTime, hasSingleCheck = true)
                } else chat
            }
            state.copy(
                currentMessages = updatedMessages,
                chats = updatedChats
            )
        }

        // Call real API
        viewModelScope.launch {
            try {
                val req = SendMessageRequest(
                    conversationId = currentChatId,
                    text = text.trim(),
                    type = "text"
                )
                val response = ApiClient.service.sendMessage(req)
                if (response.isSuccessful && response.body() != null) {
                    val serverMsg = response.body()!!
                    val mapped = mapApiMessageToItem(serverMsg)

                    _uiState.update { state ->
                        val updated = state.currentMessages.map {
                            if (it.id == tempId) mapped.copy(isOutgoing = true, isRead = false) else it
                        }
                        state.copy(currentMessages = updated)
                    }
                }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "sendMessage API failed, kept local message", e)
            }
        }
    }

    fun sendMediaItems(items: List<MediaPickerItem>) {
        if (items.isEmpty()) return

        val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        val newMessages = items.map { item ->
            MessageItem(
                id = UUID.randomUUID().toString(),
                time = currentTime,
                isOutgoing = true,
                type = MessageType.PHOTO,
                photoResId = item.drawableResId ?: R.drawable.img_retro_guy_collage,
                isRead = true
            )
        }

        _uiState.update { state ->
            state.copy(
                currentMessages = state.currentMessages + newMessages,
                isAttachmentSheetOpen = false
            )
        }
    }

    fun toggleReaction(messageId: String, emoji: String) {
        // Optimistic reaction toggle
        _uiState.update { state ->
            val updated = state.currentMessages.map { msg ->
                if (msg.id == messageId) {
                    val existing = msg.reactions.find { it.emoji == emoji }
                    val newReactions = if (existing != null) {
                        msg.reactions - existing
                    } else {
                        msg.reactions + ReactionItem(emoji, AvatarType.MOTORCYCLE)
                    }
                    msg.copy(reactions = newReactions)
                } else msg
            }
            state.copy(currentMessages = updated)
        }

        // Call API
        viewModelScope.launch {
            try {
                ApiClient.service.reactMessage(messageId, ReactionRequest(emoji = emoji))
            } catch (e: Exception) {
                Log.e("ChatViewModel", "reactMessage error", e)
            }
        }
    }

    fun openAttachmentSheet() {
        _uiState.update { it.copy(isAttachmentSheetOpen = true) }
    }

    fun closeAttachmentSheet() {
        _uiState.update { it.copy(isAttachmentSheetOpen = false) }
    }

    // -------------------------------------------------------------
    // Stickers & GIFs
    // -------------------------------------------------------------

    fun openStickerSheet() {
        _uiState.update { it.copy(isStickerSheetOpen = true) }
        if (_uiState.value.stickers.isEmpty() || _uiState.value.gifs.isEmpty()) {
            loadStickersAndGifs()
        }
    }

    fun closeStickerSheet() {
        _uiState.update { it.copy(isStickerSheetOpen = false) }
    }

    fun loadStickersAndGifs() {
        viewModelScope.launch {
            _uiState.update { it.copy(isStickersLoading = true) }
            val fetchedStickers = mutableListOf<StickerItem>()
            val fetchedGifs = mutableListOf<GifItem>()

            // 1. Fetch Stickers from API
            try {
                val res = ApiClient.service.getStickers()
                if (res.isSuccessful && !res.body().isNullOrEmpty()) {
                    res.body()!!.forEach { s ->
                        if (s.url.isNotBlank()) {
                            fetchedStickers.add(
                                StickerItem(
                                    id = s._id,
                                    name = s.name,
                                    url = s.url,
                                    pack = s.pack
                                )
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w("ChatViewModel", "Failed to fetch stickers: ${e.message}")
            }

            // Fallback rich stickers if empty
            if (fetchedStickers.isEmpty()) {
                fetchedStickers.addAll(
                    listOf<StickerItem>(
                        StickerItem("s1", "Duck Thumbs Up", "https://cdn.jsdelivr.net/gh/telegramdesktop/tdesktop@master/Telegram/SourceFiles/art/emoji_sprites.png", "Telegram Duck"),
                        StickerItem("s2", "Heart Duck", "https://api.dicebear.com/7.x/bottts/png?seed=duck1", "Telegram Duck"),
                        StickerItem("s3", "Cool Cat", "https://api.dicebear.com/7.x/bottts/png?seed=cat2", "Cool Animals"),
                        StickerItem("s4", "Happy Dog", "https://api.dicebear.com/7.x/bottts/png?seed=dog3", "Cool Animals"),
                        StickerItem("s5", "Laughing Bunny", "https://api.dicebear.com/7.x/bottts/png?seed=bunny4", "Cool Animals"),
                        StickerItem("s6", "Fire Robot", "https://api.dicebear.com/7.x/bottts/png?seed=bot5", "Tech Stickers"),
                        StickerItem("s7", "Love Bot", "https://api.dicebear.com/7.x/bottts/png?seed=bot6", "Tech Stickers"),
                        StickerItem("s8", "Star Bot", "https://api.dicebear.com/7.x/bottts/png?seed=bot7", "Tech Stickers")
                    )
                )
            }

            // 2. Fetch GIFs from API
            try {
                val res = ApiClient.service.getGlobalGifs()
                if (res.isSuccessful && !res.body().isNullOrEmpty()) {
                    res.body()!!.forEach { g ->
                        if (g.url.isNotBlank()) {
                            fetchedGifs.add(
                                GifItem(
                                    id = g._id,
                                    url = g.url,
                                    sourceUrl = g.sourceUrl,
                                    width = g.width ?: 240,
                                    height = g.height ?: 240
                                )
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w("ChatViewModel", "Failed to fetch gifs: ${e.message}")
            }

            // Fallback trending GIFs if empty
            if (fetchedGifs.isEmpty()) {
                fetchedGifs.addAll(
                    listOf<GifItem>(
                        GifItem("g1", "https://media.giphy.com/media/3o7TKSjRrfIPjeiVyM/giphy.gif"),
                        GifItem("g2", "https://media.giphy.com/media/l0MYt5jPR6QX5pnqM/giphy.gif"),
                        GifItem("g3", "https://media.giphy.com/media/xT9IgzoKnwFNmISR8I/giphy.gif"),
                        GifItem("g4", "https://media.giphy.com/media/26AHONQ79FdWZhAI0/giphy.gif"),
                        GifItem("g5", "https://media.giphy.com/media/3oKIPnAiaMCws8nOsE/giphy.gif"),
                        GifItem("g6", "https://media.giphy.com/media/l41lI4bYmcsPJX9Go/giphy.gif")
                    )
                )
            }

            _uiState.update {
                it.copy(
                    stickers = fetchedStickers,
                    gifs = fetchedGifs,
                    isStickersLoading = false
                )
            }
        }
    }

    fun sendSticker(sticker: StickerItem) {
        val currentChatId = _uiState.value.selectedChatId ?: return
        val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        val localMessage = MessageItem(
            id = UUID.randomUUID().toString(),
            time = currentTime,
            isOutgoing = true,
            type = MessageType.BIG_STICKER,
            mediaUrl = sticker.url,
            isRead = false
        )

        _uiState.update { state ->
            state.copy(
                currentMessages = state.currentMessages + localMessage,
                isStickerSheetOpen = false
            )
        }

        viewModelScope.launch {
            try {
                ApiClient.service.sendMessage(
                    SendMessageRequest(
                        conversationId = currentChatId,
                        text = null,
                        type = "sticker",
                        fileUrl = sticker.url
                    )
                )
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Failed to send sticker to backend", e)
            }
        }
    }

    fun sendGif(gif: GifItem) {
        val currentChatId = _uiState.value.selectedChatId ?: return
        val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        val localMessage = MessageItem(
            id = UUID.randomUUID().toString(),
            time = currentTime,
            isOutgoing = true,
            type = MessageType.GIF,
            mediaUrl = gif.url,
            isRead = false
        )

        _uiState.update { state ->
            state.copy(
                currentMessages = state.currentMessages + localMessage,
                isStickerSheetOpen = false
            )
        }

        viewModelScope.launch {
            try {
                ApiClient.service.sendMessage(
                    SendMessageRequest(
                        conversationId = currentChatId,
                        text = null,
                        type = "gif",
                        fileUrl = gif.url
                    )
                )
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Failed to send gif to backend", e)
            }
        }
    }

    fun sendAudioMessage(
        audioUrl: String = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
        duration: Int = 28,
        fileName: String = "Voice message"
    ) {
        val currentChatId = _uiState.value.selectedChatId ?: return
        val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        val localMessage = MessageItem(
            id = UUID.randomUUID().toString(),
            time = currentTime,
            isOutgoing = true,
            type = MessageType.AUDIO,
            mediaUrl = audioUrl,
            duration = duration,
            fileName = fileName,
            isRead = false
        )

        _uiState.update { state ->
            state.copy(
                currentMessages = state.currentMessages + localMessage,
                isAttachmentSheetOpen = false
            )
        }

        viewModelScope.launch {
            try {
                ApiClient.service.sendMessage(
                    SendMessageRequest(
                        conversationId = currentChatId,
                        type = "audio",
                        fileUrl = audioUrl,
                        duration = duration,
                        fileName = fileName
                    )
                )
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Failed to send audio to backend", e)
            }
        }
    }

    fun sendVideoMessage(
        videoUrl: String = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
        duration: Int = 15,
        thumbUrl: String = "https://images.unsplash.com/photo-1579202673506-ca3ce28943ef?w=400",
        caption: String? = null
    ) {
        val currentChatId = _uiState.value.selectedChatId ?: return
        val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        val localMessage = MessageItem(
            id = UUID.randomUUID().toString(),
            time = currentTime,
            isOutgoing = true,
            type = MessageType.VIDEO,
            mediaUrl = videoUrl,
            videoThumbnailUrl = thumbUrl,
            duration = duration,
            text = caption,
            isRead = false
        )

        _uiState.update { state ->
            state.copy(
                currentMessages = state.currentMessages + localMessage,
                isAttachmentSheetOpen = false
            )
        }

        viewModelScope.launch {
            try {
                ApiClient.service.sendMessage(
                    SendMessageRequest(
                        conversationId = currentChatId,
                        text = caption,
                        type = "video",
                        fileUrl = videoUrl,
                        videoThumbnailUrl = thumbUrl,
                        duration = duration
                    )
                )
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Failed to send video to backend", e)
            }
        }
    }

    // -------------------------------------------------------------
    // Media Viewers (Photo Zoom & Video Player)
    // -------------------------------------------------------------

    fun openPhotoViewer(message: MessageItem) {
        _uiState.update { it.copy(activePhotoMessage = message) }
    }

    fun closePhotoViewer() {
        _uiState.update { it.copy(activePhotoMessage = null) }
    }

    fun openVideoPlayer(message: MessageItem) {
        _uiState.update { it.copy(activeVideoMessage = message) }
    }

    fun closeVideoPlayer() {
        _uiState.update { it.copy(activeVideoMessage = null) }
    }

    // -------------------------------------------------------------
    // Profile Modal Flow (PUT /auth/profile)
    // -------------------------------------------------------------

    fun openUserProfile(chatId: String? = null) {
        val targetId = chatId ?: _uiState.value.selectedChatId
        val chat = _uiState.value.chats.find { it.id == targetId }
        val profile = UserProfileData(
            id = chat?.id ?: "u1",
            name = chat?.title ?: "User",
            username = chat?.title?.lowercase()?.replace(" ", "_"),
            phone = "+1 (555) 019-2834",
            bio = "Available on Telegram",
            avatarUrl = chat?.avatarUrl,
            avatarType = chat?.avatarType ?: AvatarType.MOTORCYCLE,
            isOnline = true,
            isSelf = false
        )
        _uiState.update { it.copy(profileUser = profile, isProfileModalOpen = true) }
    }

    fun openCurrentUserProfile() {
        val profile = UserProfileData(
            id = sessionManager.userId ?: "self",
            name = sessionManager.name ?: "My Profile",
            username = sessionManager.username ?: "me",
            phone = sessionManager.phone ?: "+98 912 000 0000",
            bio = sessionManager.bio ?: "Hey there! I am using Telegram.",
            avatarUrl = sessionManager.avatar,
            avatarType = AvatarType.MOTORCYCLE,
            isOnline = true,
            isSelf = true
        )
        _uiState.update { it.copy(profileUser = profile, isProfileModalOpen = true) }
    }

    fun closeUserProfile() {
        _uiState.update { it.copy(isProfileModalOpen = false, profileUser = null) }
    }

    fun updateProfile(name: String, username: String, bio: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProfileUpdating = true) }
            try {
                val cleanUsername = username.removePrefix("@").trim()
                val response = ApiClient.service.updateProfile(
                    UpdateProfileRequest(
                        name = name.trim(),
                        username = cleanUsername.ifBlank { null },
                        bio = bio.trim()
                    )
                )

                if (response.isSuccessful && response.body() != null) {
                    val user = response.body()!!
                    sessionManager.updateProfile(user.name ?: name, user.username ?: cleanUsername, user.bio ?: bio)
                    _uiState.update {
                        it.copy(
                            currentUserName = user.name ?: name,
                            currentUserBio = user.bio ?: bio,
                            profileUser = it.profileUser?.copy(
                                name = user.name ?: name,
                                username = user.username ?: cleanUsername,
                                bio = user.bio ?: bio
                            ),
                            isProfileUpdating = false
                        )
                    }
                } else {
                    // Update locally if backend mock returns code without error
                    sessionManager.updateProfile(name, cleanUsername, bio)
                    _uiState.update {
                        it.copy(
                            currentUserName = name,
                            currentUserBio = bio,
                            profileUser = it.profileUser?.copy(
                                name = name,
                                username = cleanUsername,
                                bio = bio
                            ),
                            isProfileUpdating = false
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Profile update failed", e)
                sessionManager.updateProfile(name, username, bio)
                _uiState.update {
                    it.copy(
                        currentUserName = name,
                        currentUserBio = bio,
                        profileUser = it.profileUser?.copy(name = name, username = username, bio = bio),
                        isProfileUpdating = false
                    )
                }
            }
        }
    }

    fun selectCategoryTab(tab: String) {
        _uiState.update { it.copy(selectedCategoryTab = tab) }
    }

    fun selectBottomNavIndex(index: Int) {
        _uiState.update { it.copy(selectedBottomNavIndex = index) }
    }

    fun setSearching(searching: Boolean) {
        _uiState.update { it.copy(isSearching = searching, searchQuery = if (!searching) "" else it.searchQuery) }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    // -------------------------------------------------------------
    // Helper formatting and fallback data
    // -------------------------------------------------------------

    private fun formatApiTime(isoString: String?): String {
        if (isoString.isNullOrBlank()) {
            return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        }
        return try {
            val cleanIso = isoString.replace("Z", "+0000")
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US)
            val date = parser.parse(cleanIso) ?: Date()
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
        } catch (_: Exception) {
            try {
                val parser2 = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
                val date2 = parser2.parse(isoString.substringBefore(".")) ?: Date()
                SimpleDateFormat("HH:mm", Locale.getDefault()).format(date2)
            } catch (_: Exception) {
                SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            }
        }
    }

    private fun loadFallbackChats() {
        _uiState.update {
            it.copy(
                chats = fallbackChatsList(),
                currentMessages = defaultMessagesList()
            )
        }
    }

    private fun fallbackChatsList(): List<ChatItem> = listOf(
        ChatItem(
            id = "c1",
            title = "friends group",
            subtitle = "Скачать видео из YouTube Pinterest: 🫶 ...",
            time = "Aug 29",
            isPinned = true,
            avatarType = AvatarType.GALAXY
        ),
        ChatItem(
            id = "c2",
            title = "Только я сам 🚗",
            subtitle = "https://t.me/proxy?server=k29s8d3f.mt.tr...",
            time = "May 29",
            isPinned = true,
            hasDoubleCheck = true,
            avatarType = AvatarType.CAR
        ),
        ChatItem(
            id = "c3",
            title = "Save Photos 🔇",
            subtitle = "هوش مصنوعی | آنلاین: چون ایدی این ربات رو این‌ط...",
            time = "Sep 05",
            isPinned = true,
            isMuted = true,
            avatarType = AvatarType.DUCK
        ),
        ChatItem(
            id = "c4",
            title = "Close",
            subtitle = "You: @nudesremoverbot",
            time = "Jul 19",
            isPinned = true,
            hasSingleCheck = true,
            avatarType = AvatarType.ANIME_GIRL
        ),
        ChatItem(
            id = "c5",
            title = "GAP SOULS | گپ سولز",
            subtitle = "GUTS: گیم نت دو بار بیشتر نرفتم",
            time = "00:50",
            unreadCount = 17235,
            hasMention = true,
            avatarType = AvatarType.SAMURAI
        ),
        ChatItem(
            id = "c6",
            title = "I'm Sorry",
            subtitle = "۱ نفرررر",
            time = "00:50",
            unreadCount = 34,
            avatarType = AvatarType.HUG
        ),
        ChatItem(
            id = "c7",
            title = "گروه مای انیمه | MyAnimes",
            subtitle = "M_yasin: نه نه نه جاست بستنی زعفرونی",
            time = "00:50",
            unreadCount = 270419,
            avatarType = AvatarType.TEXT_LOGO
        ),
        ChatItem(
            id = "c8",
            title = "گپ گیمینگ | COD Nexus GP",
            subtitle = "••• پژمان is typing",
            time = "00:50",
            unreadCount = 267945,
            isTyping = true,
            typingUser = "پژمان",
            avatarType = AvatarType.SKULL
        ),
        ChatItem(
            id = "c9",
            title = "Chat corridor... (Nani Kore?!) 🔇",
            subtitle = "GIF",
            time = "00:50",
            unreadCount = 109535,
            isMuted = true,
            avatarType = AvatarType.PIXEL_ART
        ),
        ChatItem(
            id = "c10",
            title = "سلحشور",
            subtitle = "بنازوم",
            time = "00:51",
            hasSingleCheck = true,
            avatarType = AvatarType.MOTORCYCLE
        )
    )

    private fun defaultMessagesList(): List<MessageItem> = listOf(
        MessageItem(
            id = "m_sticker",
            time = "21:08",
            isOutgoing = false,
            type = MessageType.BIG_STICKER,
            reactions = listOf(ReactionItem("❤️", AvatarType.MOTORCYCLE))
        ),
        MessageItem(
            id = "m_collage",
            time = "23:58",
            isOutgoing = false,
            type = MessageType.IMAGE_COLLAGE,
            photoResId = R.drawable.img_retro_guy_collage,
            dateHeader = "September 11"
        ),
        MessageItem(
            id = "m_incoming_text",
            text = "هر کدومو خواستی وردار👍",
            time = "23:58",
            isOutgoing = false,
            type = MessageType.TEXT,
            reactions = listOf(ReactionItem("🤣", AvatarType.MOTORCYCLE))
        ),
        MessageItem(
            id = "m_video",
            text = "Check out this clip 🎥",
            time = "00:15",
            isOutgoing = false,
            type = MessageType.VIDEO,
            mediaUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
            videoThumbnailUrl = "https://images.unsplash.com/photo-1579202673506-ca3ce28943ef?w=600",
            duration = 15,
            reactions = listOf(ReactionItem("🔥", AvatarType.MOTORCYCLE))
        ),
        MessageItem(
            id = "m_audio",
            text = "Voice message",
            time = "00:32",
            isOutgoing = false,
            type = MessageType.AUDIO,
            mediaUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
            duration = 24,
            fileName = "Voice note (0:24)"
        ),
        MessageItem(
            id = "m_gif",
            time = "00:45",
            isOutgoing = true,
            type = MessageType.GIF,
            mediaUrl = "https://media.giphy.com/media/3o7TKSjRrfIPjeiVyM/giphy.gif",
            isRead = true
        ),
        MessageItem(
            id = "m_outgoing_text",
            text = "بنازوم",
            time = "00:51",
            isOutgoing = true,
            type = MessageType.TEXT,
            isRead = true,
            dateHeader = "September 12"
        )
    )

    override fun onCleared() {
        super.onCleared()
        messagePollingJob?.cancel()
        conversationPollingJob?.cancel()
    }
}
