package com.example.viewmodel

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.ApiClient
import com.example.data.api.ApiConversation
import com.example.data.api.ApiLocation
import com.example.data.api.ApiMessage
import com.example.data.api.ApiPoll
import com.example.data.api.ApiPollOption
import com.example.data.api.EditMessageRequest
import com.example.data.api.ForwardMessageRequest
import com.example.data.api.PinMessageRequest
import com.example.data.api.PollVoteRequest
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
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
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
            // Refresh current user profile from /auth/me to keep local session in sync
            bootstrapCurrentUser()
            refreshConversations()
            startConversationsPolling()
        }
    }

    // =============================================================
    // Authentication Flow
    // =============================================================

    /** Refresh the current user from /auth/me and persist into SessionManager. */
    private fun bootstrapCurrentUser() {
        viewModelScope.launch {
            try {
                val resp = ApiClient.service.getMe()
                if (resp.isSuccessful && resp.body() != null) {
                    val u = resp.body()!!
                    sessionManager.saveSession(
                        token = sessionManager.token ?: return@launch,
                        id = u._id ?: sessionManager.userId,
                        phone = u.phone ?: sessionManager.phone,
                        name = u.name ?: sessionManager.name,
                        username = u.username ?: sessionManager.username,
                        avatar = u.avatar ?: sessionManager.avatar,
                        bio = u.bio ?: sessionManager.bio
                    )
                    _uiState.update {
                        it.copy(
                            currentUserName = u.name ?: it.currentUserName,
                            currentUserPhone = u.phone ?: it.currentUserPhone,
                            currentUserAvatar = u.avatar ?: it.currentUserAvatar,
                            currentUserBio = u.bio ?: it.currentUserBio
                        )
                    }
                }
            } catch (e: Exception) {
                Log.w("ChatViewModel", "bootstrapCurrentUser: ${e.message}")
            }
        }
    }

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
                    val err = parseErrorBody(response.errorBody()) ?: "Failed to send code (${response.code()})"
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
                        avatar = user?.avatar,
                        bio = user?.bio
                    )

                    ApiClient.setTokenProvider { token }

                    _uiState.update {
                        it.copy(
                            isAuthenticated = true,
                            isAuthLoading = false,
                            authErrorMessage = null,
                            currentUserName = user?.name ?: "User",
                            currentUserPhone = user?.phone ?: cleanPhone,
                            currentUserAvatar = user?.avatar,
                            currentUserBio = user?.bio
                        )
                    }

                    // Pull full profile + chats from server now that we have a token
                    bootstrapCurrentUser()
                    refreshConversations()
                    startConversationsPolling()
                } else {
                    val err = parseErrorBody(response.errorBody())
                        ?: response.body()?.message
                        ?: "Invalid OTP verification code (${response.code()})"
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
        messagePollingJob?.cancel()
        conversationPollingJob?.cancel()
        _uiState.update {
            it.copy(
                isAuthenticated = false,
                currentUserName = null,
                currentUserPhone = null,
                currentUserAvatar = null,
                currentUserBio = null,
                selectedChatId = null,
                chats = emptyList(),
                currentMessages = emptyList()
            )
        }
    }

    // =============================================================
    // Conversations API
    // =============================================================

    fun refreshConversations() {
        _uiState.update { it.copy(isRefreshing = true, networkBannerMessage = null) }
        viewModelScope.launch {
            try {
                // Prefer /messages/conversations (which is the per-user list with lastMessage);
                // fall back to /conversations if the server only has the latter mounted.
                val response = try {
                    val r1 = ApiClient.service.getConversationsAlt()
                    if (r1.isSuccessful && !r1.body().isNullOrEmpty()) r1
                    else ApiClient.service.getConversations()
                } catch (e: Exception) {
                    ApiClient.service.getConversations()
                }

                if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                    val apiConversations = response.body()!!
                    val mappedChats = apiConversations.map { mapApiConversationToChatItem(it) }

                    _uiState.update { state ->
                        state.copy(
                            chats = mappedChats,
                            isRefreshing = false,
                            networkBannerMessage = null
                        )
                    }
                } else {
                    val code = response.code()
                    _uiState.update { state ->
                        state.copy(
                            isRefreshing = false,
                            networkBannerMessage = if (state.chats.isEmpty())
                                "No conversations yet (${code})" else null
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "refreshConversations error", e)
                _uiState.update { state ->
                    state.copy(
                        isRefreshing = false,
                        networkBannerMessage = if (state.chats.isEmpty())
                            "Network error: ${e.localizedMessage ?: "check connection"}" else null
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
                        val resp = ApiClient.service.getConversationsAlt()
                        val body = if (resp.isSuccessful) resp.body() else null
                        if (!body.isNullOrEmpty()) {
                            val mapped = body.map { mapApiConversationToChatItem(it) }
                            _uiState.update { it.copy(chats = mapped) }
                        }
                    } catch (_: Exception) {
                        // silent — network blips during polling shouldn't toast
                    }
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
            else -> "7eve9Chat User"
        }

        val avatar = ApiClient.resolveUrl(
            apiConv.avatar ?: otherParticipant?.avatar ?: otherParticipant?.user?.avatar
        )
        val subtitle = apiConv.lastMessage?.text ?: "No messages yet"
        val time = formatApiTime(apiConv.lastMessage?.createdAt ?: apiConv.lastMessageAt)
        val unread = apiConv.unreadCount ?: 0
        val isPinned = apiConv.pinned == true
        val isMuted = apiConv.isMuted == true
        val isGroup = apiConv.type != "private" && (apiConv.participants?.size ?: 0) > 2
        val isChannel = apiConv.isChannel == true || apiConv.type == "channel"

        return ChatItem(
            id = apiConv._id,
            title = title,
            subtitle = subtitle,
            time = time,
            isPinned = isPinned,
            unreadCount = unread,
            isMuted = isMuted,
            avatarUrl = avatar,
            avatarType = AvatarType.MOTORCYCLE,
            isGroup = isGroup,
            memberCount = apiConv.participants?.size ?: 0,
            isOnline = false
        )
    }

    // =============================================================
    // Messages API
    // =============================================================

    fun selectChat(chatId: String?) {
        _uiState.update { it.copy(selectedChatId = chatId) }
        messagePollingJob?.cancel()

        if (chatId != null) {
            fetchMessages(chatId)
            startMessagePolling(chatId)
            markConversationAsRead(chatId)
        } else {
            _uiState.update { it.copy(currentMessages = emptyList()) }
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
                        state.copy(currentMessages = mappedMessages)
                    }
                } else {
                    val err = parseErrorBody(response.errorBody())
                    _uiState.update { state ->
                        state.copy(
                            currentMessages = emptyList(),
                            networkBannerMessage = "Failed to load messages (${response.code()}): ${err ?: "unknown"}"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "fetchMessages error", e)
                _uiState.update { state ->
                    state.copy(
                        currentMessages = emptyList(),
                        networkBannerMessage = "Network error loading messages: ${e.localizedMessage ?: "check connection"}"
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
                            _uiState.update { it.copy(currentMessages = mapped) }
                        }
                    } catch (_: Exception) {
                        // silent — polling errors should not spam the user
                    }
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
            "file" -> MessageType.FILE
            "location" -> MessageType.LOCATION
            "poll" -> MessageType.POLL
            "album_group", "album" -> MessageType.IMAGE_COLLAGE
            else -> MessageType.TEXT
        }

        val reactions = apiMsg.reactions?.map {
            ReactionItem(emoji = it.emoji, userAvatarType = AvatarType.MOTORCYCLE)
        } ?: emptyList()

        // Determine read state: outgoing messages are "read" if any readBy entry isn't the sender;
        // incoming messages are always considered read once displayed.
        val isRead = when {
            apiMsg.readBy.isNullOrEmpty() -> false
            isOutgoing -> apiMsg.readBy!!.any { it != currentUserId }
            else -> true
        }

        return MessageItem(
            id = apiMsg._id,
            text = apiMsg.text,
            time = formatApiTime(apiMsg.createdAt),
            isOutgoing = isOutgoing,
            type = type,
            mediaUrl = ApiClient.resolveUrl(apiMsg.fileUrl),
            videoThumbnailUrl = ApiClient.resolveUrl(apiMsg.videoThumbnailUrl),
            duration = apiMsg.duration ?: apiMsg.audioMetadata?.duration,
            fileName = apiMsg.fileName,
            senderName = if (!isOutgoing) apiMsg.sender?.name ?: apiMsg.sender?.username else null,
            senderAvatarUrl = ApiClient.resolveUrl(apiMsg.sender?.avatar),
            reactions = reactions,
            isRead = isRead
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

        // Real API call
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
                            if (it.id == tempId) mapped else it
                        }
                        state.copy(currentMessages = updated)
                    }
                } else {
                    // Mark local message as failed (still show it, but update state)
                    val err = parseErrorBody(response.errorBody())
                    Log.e("ChatViewModel", "sendMessage failed: ${response.code()} $err")
                    _uiState.update { state ->
                        state.copy(
                            networkBannerMessage = "Failed to send message (${response.code()})"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "sendMessage error", e)
                _uiState.update { state ->
                    state.copy(
                        networkBannerMessage = "Network error: ${e.localizedMessage ?: "message not sent"}"
                    )
                }
            }
        }
    }

    /**
     * Send the picked media items. For items that have a real content:// URI we
     * upload via multipart POST /messages. For drawable-only items (sample gallery
     * in the attachment sheet) we cannot upload a packaged resource — we log and skip.
     */
    fun sendMediaItems(items: List<MediaPickerItem>) {
        if (items.isEmpty()) return
        val currentChatId = _uiState.value.selectedChatId ?: return

        val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

        // Optimistic local insert for instant feedback
        val localMessages = items.map { item ->
            MessageItem(
                id = UUID.randomUUID().toString(),
                time = currentTime,
                isOutgoing = true,
                type = MessageType.PHOTO,
                photoResId = item.drawableResId,
                mediaUrl = item.uriString,
                isRead = false
            )
        }
        _uiState.update { state ->
            state.copy(
                currentMessages = state.currentMessages + localMessages,
                isAttachmentSheetOpen = false
            )
        }

        // Try to upload each item that has a real uriString
        viewModelScope.launch {
            items.forEachIndexed { index, item ->
                val uriStr = item.uriString
                if (uriStr.isNullOrBlank()) {
                    Log.w("ChatViewModel", "sendMediaItems: skipping item without uri (drawable-only)")
                    return@forEachIndexed
                }
                try {
                    val file = uriToFile(uriStr) ?: return@forEachIndexed
                    val mime = guessMime(uriStr)
                    val reqFile = file.asRequestBody(mime.toMediaTypeOrNull())
                    val filePart = MultipartBody.Part.createFormData("file", file.name, reqFile)

                    val convPart = currentChatId.toRequestBody("text/plain".toMediaTypeOrNull())
                    val typePart = "image".toRequestBody("text/plain".toMediaTypeOrNull())

                    val resp = ApiClient.service.sendMessageMedia(
                        file = filePart,
                        conversationId = convPart,
                        receiverId = null,
                        type = typePart,
                        text = null,
                        replyTo = null,
                        isSilent = "false".toRequestBody("text/plain".toMediaTypeOrNull()),
                        duration = null,
                        audioMetadata = null,
                        location = null
                    )

                    if (resp.isSuccessful && resp.body() != null) {
                        val serverMsg = resp.body()!!
                        val mapped = mapApiMessageToItem(serverMsg)
                        val localId = localMessages.getOrNull(index)?.id
                        if (localId != null) {
                            _uiState.update { state ->
                                state.copy(
                                    currentMessages = state.currentMessages.map {
                                        if (it.id == localId) mapped else it
                                    }
                                )
                            }
                        }
                    } else {
                        val err = parseErrorBody(resp.errorBody())
                        Log.e("ChatViewModel", "sendMediaItems upload failed: ${resp.code()} $err")
                    }
                } catch (e: Exception) {
                    Log.e("ChatViewModel", "sendMediaItems upload error", e)
                }
            }
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

        // Real API call (POST /messages/:messageId/react — server toggles)
        viewModelScope.launch {
            try {
                ApiClient.service.reactMessage(messageId, ReactionRequest(emoji = emoji))
            } catch (e: Exception) {
                Log.e("ChatViewModel", "reactMessage error", e)
            }
        }
    }

    fun deleteMessage(messageId: String) {
        _uiState.update { state ->
            state.copy(currentMessages = state.currentMessages.filter { it.id != messageId })
        }
        viewModelScope.launch {
            try {
                ApiClient.service.deleteMessage(messageId)
            } catch (e: Exception) {
                Log.e("ChatViewModel", "deleteMessage error", e)
            }
        }
    }

    fun editMessage(messageId: String, newText: String) {
        _uiState.update { state ->
            val updated = state.currentMessages.map { msg ->
                if (msg.id == messageId) msg.copy(text = newText) else msg
            }
            state.copy(currentMessages = updated)
        }
        viewModelScope.launch {
            try {
                ApiClient.service.editMessage(messageId, EditMessageRequest(text = newText))
            } catch (e: Exception) {
                Log.e("ChatViewModel", "editMessage error", e)
            }
        }
    }

    fun pinMessage(messageId: String) {
        val currentChatId = _uiState.value.selectedChatId ?: return
        viewModelScope.launch {
            try {
                ApiClient.service.pinMessage(currentChatId, PinMessageRequest(messageId = messageId, action = "pin"))
            } catch (e: Exception) {
                Log.e("ChatViewModel", "pinMessage error", e)
            }
        }
    }

    fun forwardMessage(targetConversationId: String, messageId: String) {
        viewModelScope.launch {
            try {
                ApiClient.service.forwardMessage(
                    ForwardMessageRequest(conversationId = targetConversationId, messageId = messageId)
                )
            } catch (e: Exception) {
                Log.e("ChatViewModel", "forwardMessage error", e)
            }
        }
    }

    fun votePoll(messageId: String, optionId: String) {
        viewModelScope.launch {
            try {
                ApiClient.service.votePoll(messageId, PollVoteRequest(optionId = optionId))
            } catch (e: Exception) {
                Log.e("ChatViewModel", "votePoll error", e)
            }
        }
    }

    fun closePoll(messageId: String) {
        viewModelScope.launch {
            try {
                ApiClient.service.closePoll(messageId)
            } catch (e: Exception) {
                Log.e("ChatViewModel", "closePoll error", e)
            }
        }
    }

    fun openAttachmentSheet() {
        _uiState.update { it.copy(isAttachmentSheetOpen = true) }
    }

    fun closeAttachmentSheet() {
        _uiState.update { it.copy(isAttachmentSheetOpen = false) }
    }

    // =============================================================
    // Stickers & GIFs — real API only (no mock fallback)
    // =============================================================

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

            // 1. Stickers — GET /stickers
            try {
                val res = ApiClient.service.getStickers()
                if (res.isSuccessful && !res.body().isNullOrEmpty()) {
                    res.body()!!.forEach { s ->
                        if (s.url.isNotBlank()) {
                            fetchedStickers.add(
                                StickerItem(
                                    id = s._id,
                                    name = s.name,
                                    url = ApiClient.resolveUrl(s.url) ?: s.url,
                                    pack = s.pack
                                )
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w("ChatViewModel", "Failed to fetch stickers: ${e.message}")
            }

            // 2. GIFs — GET /gifs/global (trending) + GET /gifs (saved)
            try {
                val res = ApiClient.service.getGlobalGifs()
                if (res.isSuccessful && !res.body().isNullOrEmpty()) {
                    res.body()!!.forEach { g ->
                        if (g.url.isNotBlank()) {
                            fetchedGifs.add(
                                GifItem(
                                    id = g._id,
                                    url = ApiClient.resolveUrl(g.url) ?: g.url,
                                    sourceUrl = g.sourceUrl,
                                    width = g.width ?: 240,
                                    height = g.height ?: 240
                                )
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w("ChatViewModel", "Failed to fetch global gifs: ${e.message}")
            }

            try {
                val res = ApiClient.service.getGifs()
                if (res.isSuccessful && !res.body().isNullOrEmpty()) {
                    res.body()!!.forEach { g ->
                        if (g.url.isNotBlank() && fetchedGifs.none { it.id == g._id }) {
                            fetchedGifs.add(
                                GifItem(
                                    id = g._id,
                                    url = ApiClient.resolveUrl(g.url) ?: g.url,
                                    sourceUrl = g.sourceUrl,
                                    width = g.width ?: 240,
                                    height = g.height ?: 240
                                )
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w("ChatViewModel", "Failed to fetch saved gifs: ${e.message}")
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

    fun sendLocation(latitude: Double, longitude: Double, title: String) {
        val currentChatId = _uiState.value.selectedChatId ?: return
        val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        val localMessage = MessageItem(
            id = UUID.randomUUID().toString(),
            text = title,
            time = currentTime,
            isOutgoing = true,
            type = MessageType.LOCATION,
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
                        text = title,
                        type = "location",
                        location = ApiLocation(lat = latitude, lng = longitude, name = title)
                    )
                )
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Failed to send location", e)
            }
        }
    }

    /**
     * Send a poll per spec §8.7: POST /messages with type="poll" and a populated
     * `poll` object containing question, options[], anonymous, multiSelect,
     * allowChangeVote.
     */
    fun sendPoll(question: String, options: List<String>, isAnonymous: Boolean) {
        val currentChatId = _uiState.value.selectedChatId ?: return
        val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        val localMessage = MessageItem(
            id = UUID.randomUUID().toString(),
            text = question,
            time = currentTime,
            isOutgoing = true,
            type = MessageType.POLL,
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
                val poll = ApiPoll(
                    question = question,
                    options = options.map { ApiPollOption(text = it, voters = emptyList()) },
                    anonymous = isAnonymous,
                    multiSelect = false,
                    allowChangeVote = true,
                    closedAt = null
                )
                ApiClient.service.sendMessage(
                    SendMessageRequest(
                        conversationId = currentChatId,
                        text = question,
                        type = "poll",
                        poll = poll
                    )
                )
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Failed to send poll", e)
            }
        }
    }

    fun sendFile(fileName: String, fileUri: String) {
        val currentChatId = _uiState.value.selectedChatId ?: return
        val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        val localMessage = MessageItem(
            id = UUID.randomUUID().toString(),
            text = fileName,
            fileName = fileName,
            mediaUrl = fileUri,
            time = currentTime,
            isOutgoing = true,
            type = MessageType.FILE,
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
                        text = fileName,
                        type = "file",
                        fileName = fileName,
                        fileUrl = fileUri
                    )
                )
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Failed to send file", e)
            }
        }
    }

    fun sendMusic(title: String, audioUri: String) {
        sendAudioMessage(
            audioUrl = audioUri,
            duration = 180,
            fileName = title
        )
    }

    // =============================================================
    // Media Viewers (Photo Zoom & Video Player)
    // =============================================================

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

    // =============================================================
    // Profile Modal Flow — fetch real profile via GET /auth/users/{id}
    // =============================================================

    fun openUserProfile(chatId: String? = null) {
        val targetId = chatId ?: _uiState.value.selectedChatId
        val chat = _uiState.value.chats.find { it.id == targetId }

        // Show modal immediately with whatever we have locally, then enrich from API
        val initial = UserProfileData(
            id = chat?.id ?: "unknown",
            name = chat?.title ?: "User",
            username = chat?.title?.lowercase()?.replace(" ", "_"),
            phone = null,
            bio = null,
            avatarUrl = chat?.avatarUrl,
            avatarType = chat?.avatarType ?: AvatarType.MOTORCYCLE,
            isOnline = false,
            isSelf = false
        )
        _uiState.update { it.copy(profileUser = initial, isProfileModalOpen = true) }

        // Look up the other participant's user id and fetch full profile from API
        viewModelScope.launch {
            try {
                // First fetch the full conversation to populate participants
                val convResp = ApiClient.service.getConversation(targetId ?: return@launch)
                if (!convResp.isSuccessful || convResp.body() == null) return@launch
                val conv = convResp.body()!!
                val other = conv.participants?.firstOrNull { it.user?._id != sessionManager.userId }
                val otherUserId = other?.user?._id ?: return@launch

                val userResp = ApiClient.service.getUser(otherUserId)
                if (userResp.isSuccessful && userResp.body() != null) {
                    val u = userResp.body()!!
                    _uiState.update {
                        it.copy(
                            profileUser = it.profileUser?.copy(
                                id = u._id ?: it.profileUser?.id ?: "unknown",
                                name = u.name ?: it.profileUser?.name ?: "User",
                                username = u.username ?: it.profileUser?.username,
                                phone = u.phone,
                                bio = u.bio,
                                avatarUrl = ApiClient.resolveUrl(u.avatar) ?: it.profileUser?.avatarUrl
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                Log.w("ChatViewModel", "openUserProfile fetch failed: ${e.message}")
            }
        }
    }

    fun openCurrentUserProfile() {
        val profile = UserProfileData(
            id = sessionManager.userId ?: "self",
            name = sessionManager.name ?: "My Profile",
            username = sessionManager.username,
            phone = sessionManager.phone,
            bio = sessionManager.bio ?: "Hey there! I am using 7eve9Chat.",
            avatarUrl = ApiClient.resolveUrl(sessionManager.avatar),
            avatarType = AvatarType.MOTORCYCLE,
            isOnline = true,
            isSelf = true
        )
        _uiState.update { it.copy(profileUser = profile, isProfileModalOpen = true) }

        // Refresh from /auth/me in the background
        viewModelScope.launch {
            try {
                val resp = ApiClient.service.getMe()
                if (resp.isSuccessful && resp.body() != null) {
                    val u = resp.body()!!
                    _uiState.update {
                        it.copy(
                            profileUser = it.profileUser?.copy(
                                name = u.name ?: it.profileUser?.name ?: "User",
                                username = u.username ?: it.profileUser?.username,
                                phone = u.phone ?: it.profileUser?.phone,
                                bio = u.bio ?: it.profileUser?.bio,
                                avatarUrl = ApiClient.resolveUrl(u.avatar) ?: it.profileUser?.avatarUrl,
                                birthday = u.birthday ?: it.profileUser?.birthday
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                Log.w("ChatViewModel", "openCurrentUserProfile refresh failed: ${e.message}")
            }
        }
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
                    sessionManager.updateProfile(
                        user.name ?: name,
                        user.username ?: cleanUsername,
                        user.bio ?: bio
                    )
                    _uiState.update {
                        it.copy(
                            currentUserName = user.name ?: name,
                            currentUserBio = user.bio ?: bio,
                            profileUser = it.profileUser?.copy(
                                name = user.name ?: name,
                                username = user.username ?: cleanUsername,
                                bio = user.bio ?: bio,
                                avatarUrl = ApiClient.resolveUrl(user.avatar) ?: it.profileUser?.avatarUrl
                            ),
                            isProfileUpdating = false
                        )
                    }
                } else {
                    val err = parseErrorBody(response.errorBody())
                    _uiState.update {
                        it.copy(
                            isProfileUpdating = false,
                            networkBannerMessage = "Profile update failed (${response.code()}): ${err ?: "error"}"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Profile update failed", e)
                _uiState.update {
                    it.copy(
                        isProfileUpdating = false,
                        networkBannerMessage = "Network error updating profile: ${e.localizedMessage ?: "check connection"}"
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

    // =============================================================
    // Helpers
    // =============================================================

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

    private fun parseErrorBody(body: okhttp3.ResponseBody?): String? {
        if (body == null) return null
        return try {
            val raw = body.string()
            // Try to extract {"message":"..."} or {"error":"..."} — fall back to raw text
            val msgRegex = Regex("\"(?:message|error|msg)\"\\s*:\\s*\"([^\"]+)\"")
            msgRegex.find(raw)?.groupValues?.getOrNull(1) ?: raw.take(200)
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Resolve a content:// or file:// URI into a real File on disk that we can
     * pass to MultipartBody.Part. Returns null if the URI scheme is unsupported
     * or the file can't be opened.
     */
    private fun uriToFile(uriString: String): File? {
        return try {
            val uri = Uri.parse(uriString)
            val ctx = getApplication<Application>()
            val tmp = File.createTempFile("upload_", ".bin", ctx.cacheDir)
            ctx.contentResolver.openInputStream(uri)?.use { input ->
                tmp.outputStream().use { output -> input.copyTo(output) }
            } ?: return null
            tmp
        } catch (e: Exception) {
            Log.w("ChatViewModel", "uriToFile failed for $uriString: ${e.message}")
            null
        }
    }

    private fun guessMime(uriString: String): String {
        val lower = uriString.lowercase(Locale.ROOT)
        return when {
            lower.endsWith(".jpg") || lower.endsWith(".jpeg") -> "image/jpeg"
            lower.endsWith(".png") -> "image/png"
            lower.endsWith(".webp") -> "image/webp"
            lower.endsWith(".gif") -> "image/gif"
            lower.endsWith(".mp4") -> "video/mp4"
            lower.endsWith(".webm") -> "video/webm"
            lower.endsWith(".mp3") -> "audio/mpeg"
            lower.endsWith(".m4a") -> "audio/mp4"
            lower.endsWith(".ogg") -> "audio/ogg"
            lower.endsWith(".pdf") -> "application/pdf"
            else -> "application/octet-stream"
        }
    }

    override fun onCleared() {
        super.onCleared()
        messagePollingJob?.cancel()
        conversationPollingJob?.cancel()
    }
}
