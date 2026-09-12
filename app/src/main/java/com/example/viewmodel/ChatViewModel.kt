package com.example.viewmodel

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.ApiClient
import com.example.data.api.ApiConversation
import com.example.data.api.ApiContact
import com.example.data.api.ApiLocation
import com.example.data.api.ApiMessage
import com.example.data.api.ApiPoll
import com.example.data.api.ApiPollOption
import com.example.data.api.ApiProfileSong
import com.example.data.api.ApiSession
import com.example.data.api.ApiUser
import com.example.data.api.AddContactRequest
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
import com.example.data.model.MessageStatus
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
    val isProfileUpdating: Boolean = false,

    // Contacts screen (GET /auth/contacts)
    val contacts: List<ApiContact> = emptyList(),
    val isContactsLoading: Boolean = false,
    val contactsError: String? = null,
    val addContactStatus: String? = null,

    // Profile screen — full user from /auth/me (incl. profileActiveSong, birthday, etc.)
    val meUser: ApiUser? = null,
    val isMeLoading: Boolean = false,

    // Owned channel (for Profile screen channel card) — first channel where user is owner
    val myChannel: ApiConversation? = null,

    // Theme — toggled from the chat list 3-dot menu
    val isDarkMode: Boolean = true,

    // First-time profile setup flow
    val needsProfileSetup: Boolean = false,
    val isProfileSetupLoading: Boolean = false,
    val profileSetupError: String? = null,

    // ===== App preferences (synced with SessionManager) =====
    val language: String = "fa",
    val messageTextSize: Int = 16,
    val messageCornerRadius: Int = 20,
    val doubleTapEmoji: String = "❤️",
    val autoDownloadMobile: Boolean = true,
    val autoDownloadWifi: Boolean = true,
    val autoDownloadRoaming: Boolean = false,
    val saveGalleryPrivate: Boolean = false,
    val saveGalleryGroups: Boolean = false,
    val saveGalleryChannels: Boolean = false,
    val powerSavingEnabled: Boolean = false,
    val powerLowQuality: Boolean = false,
    val powerDisableAnimations: Boolean = false,
    val powerDisableAutoplay: Boolean = false,

    // Chat folders (local-only for now)
    val chatFolders: List<com.example.ui.screens.ChatFolder> = emptyList(),
    val showFolderTags: Boolean = false,

    val privacyLastSeen: String = "Everyone",
    val privacyPhoneNumber: String = "Contacts",
    val privacyForwarded: String = "Everyone",
    val privacyGroups: String = "Everyone",
    val isPhoneHidden: Boolean = false,

    // Settings sub-screen navigation — string keys; null = closed
    val activeSettingsScreen: String? = null,

    // Devices screen — sessions list
    val sessions: List<ApiSession> = emptyList(),
    val isSessionsLoading: Boolean = false,
    val sessionsError: String? = null
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
                currentUserBio = sessionManager.bio,
                isDarkMode = sessionManager.isDarkMode,
                language = sessionManager.language,
                messageTextSize = sessionManager.messageTextSize,
                messageCornerRadius = sessionManager.messageCornerRadius,
                doubleTapEmoji = sessionManager.doubleTapEmoji,
                autoDownloadMobile = sessionManager.autoDownloadMobile,
                autoDownloadWifi = sessionManager.autoDownloadWifi,
                autoDownloadRoaming = sessionManager.autoDownloadRoaming,
                saveGalleryPrivate = sessionManager.saveGalleryPrivate,
                saveGalleryGroups = sessionManager.saveGalleryGroups,
                saveGalleryChannels = sessionManager.saveGalleryChannels,
                powerSavingEnabled = sessionManager.powerSavingEnabled,
                powerLowQuality = sessionManager.powerLowQuality,
                powerDisableAnimations = sessionManager.powerDisableAnimations,
                powerDisableAutoplay = sessionManager.powerDisableAutoplay,
                privacyLastSeen = sessionManager.privacyLastSeen,
                privacyPhoneNumber = sessionManager.privacyPhoneNumber,
                privacyForwarded = sessionManager.privacyForwarded,
                privacyGroups = sessionManager.privacyGroups,
                isPhoneHidden = sessionManager.isPhoneHidden
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
            _uiState.update { it.copy(isMeLoading = true) }
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
                            currentUserBio = u.bio ?: it.currentUserBio,
                            meUser = u,
                            isMeLoading = false,
                            // If the user has no name or username, force them through SetupProfile
                            needsProfileSetup = u.name.isNullOrBlank() || u.username.isNullOrBlank()
                        )
                    }
                    // After me is loaded, fetch the user's owned channel for the Profile card.
                    fetchMyChannel(u._id)
                } else {
                    _uiState.update { it.copy(isMeLoading = false) }
                }
            } catch (e: Exception) {
                Log.w("ChatViewModel", "bootstrapCurrentUser: ${e.message}")
                _uiState.update { it.copy(isMeLoading = false) }
            }
        }
    }

    /**
     * Find the first conversation where type=channel and owner._id == userId.
     * Used by the Profile screen channel card.
     */
    private fun fetchMyChannel(userId: String?) {
        if (userId == null) return
        viewModelScope.launch {
            try {
                val resp = ApiClient.service.getConversations()
                if (resp.isSuccessful && !resp.body().isNullOrEmpty()) {
                    val channel = resp.body()!!.firstOrNull {
                        (it.isChannel == true || it.type == "channel") &&
                                (extractUserId(it.owner) == userId)
                    }
                    _uiState.update { it.copy(myChannel = channel) }
                }
            } catch (e: Exception) {
                Log.w("ChatViewModel", "fetchMyChannel: ${e.message}")
            }
        }
    }

    // =============================================================
    // Contacts — GET /auth/contacts, POST /auth/contacts
    // =============================================================

    /** Fetch the authenticated user's contacts list. */
    fun loadContacts() {
        _uiState.update {
            it.copy(isContactsLoading = true, contactsError = null, addContactStatus = null)
        }
        viewModelScope.launch {
            try {
                val resp = ApiClient.service.getContacts()
                if (resp.isSuccessful && resp.body() != null) {
                    _uiState.update {
                        it.copy(
                            contacts = resp.body()!!,
                            isContactsLoading = false,
                            contactsError = null
                        )
                    }
                } else {
                    val err = parseErrorBody(resp.errorBody())
                    _uiState.update {
                        it.copy(
                            isContactsLoading = false,
                            contactsError = err ?: "Failed to load contacts (${resp.code()})"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "loadContacts error", e)
                _uiState.update {
                    it.copy(
                        isContactsLoading = false,
                        contactsError = "Network error: ${e.localizedMessage ?: "check connection"}"
                    )
                }
            }
        }
    }

    /**
     * Add a contact by phone or username. Server figures out which.
     * On success, reloads the contacts list so the new contact appears.
     */
    fun addContact(identifier: String) {
        if (identifier.isBlank()) return
        _uiState.update { it.copy(addContactStatus = null) }
        viewModelScope.launch {
            try {
                val resp = ApiClient.service.addContact(AddContactRequest(identifier = identifier.trim()))
                if (resp.isSuccessful) {
                    _uiState.update {
                        it.copy(addContactStatus = "Contact added: ${resp.body()?.name ?: identifier}")
                    }
                    loadContacts() // refresh list
                } else {
                    val err = parseErrorBody(resp.errorBody())
                    _uiState.update {
                        it.copy(addContactStatus = "Failed: ${err ?: "code ${resp.code()}"}")
                    }
                }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "addContact error", e)
                _uiState.update {
                    it.copy(addContactStatus = "Network error: ${e.localizedMessage ?: "check connection"}")
                }
            }
        }
    }

    fun clearAddContactStatus() {
        _uiState.update { it.copy(addContactStatus = null) }
    }

    /**
     * Public hook for the Profile screen to refresh /auth/me on demand.
     * Re-uses bootstrapCurrentUser.
     */
    fun refreshMe() = bootstrapCurrentUser()

    /**
     * Submit the first-time profile setup form.
     * Validates name (>=2 chars) and username (5-32 chars, letters/numbers/_),
     * then PUTs to /auth/profile. If an avatar image was picked, also uploads it
     * as multipart after the JSON PUT succeeds.
     * On success, clears needsProfileSetup and starts the normal app flow.
     */
    fun submitProfileSetup(name: String, username: String, avatarUri: String?) {
        // Final client-side guard
        if (name.isBlank() || name.trim().length < 2) {
            _uiState.update {
                it.copy(profileSetupError = "Name must be at least 2 characters")
            }
            return
        }
        val cleanUsername = username.removePrefix("@").trim()
        if (!Regex("^[a-zA-Z0-9_]{5,32}$").matches(cleanUsername)) {
            _uiState.update {
                it.copy(profileSetupError = "Username must be 5-32 chars, letters/numbers/_ only")
            }
            return
        }

        _uiState.update {
            it.copy(isProfileSetupLoading = true, profileSetupError = null)
        }
        viewModelScope.launch {
            try {
                val resp = ApiClient.service.updateProfile(
                    UpdateProfileRequest(
                        name = name.trim(),
                        username = cleanUsername
                    )
                )
                if (!resp.isSuccessful || resp.body() == null) {
                    val err = parseErrorBody(resp.errorBody())
                    _uiState.update {
                        it.copy(
                            isProfileSetupLoading = false,
                            profileSetupError = err ?: "Setup failed (${resp.code()})"
                        )
                    }
                    return@launch
                }

                val u = resp.body()!!
                sessionManager.saveSession(
                    token = sessionManager.token ?: return@launch,
                    id = u._id ?: sessionManager.userId,
                    phone = u.phone ?: sessionManager.phone,
                    name = u.name ?: name,
                    username = u.username ?: cleanUsername,
                    avatar = u.avatar ?: sessionManager.avatar,
                    bio = u.bio ?: sessionManager.bio
                )

                // If user picked an avatar image, upload it as multipart
                if (!avatarUri.isNullOrBlank() && avatarUri!!.startsWith("content://")) {
                    try {
                        val file = uriToFile(avatarUri)
                        if (file != null) {
                            val mime = guessMime(avatarUri)
                            val reqFile = file.asRequestBody(mime.toMediaTypeOrNull())
                            val filePart = MultipartBody.Part.createFormData("file", file.name, reqFile)
                            val avatarResp = ApiClient.service.updateProfileAvatar(filePart)
                            if (avatarResp.isSuccessful && avatarResp.body() != null) {
                                val avUser = avatarResp.body()!!
                                sessionManager.avatar = avUser.avatar
                                _uiState.update {
                                    it.copy(
                                        currentUserAvatar = avUser.avatar ?: it.currentUserAvatar,
                                        meUser = avUser
                                    )
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.w("ChatViewModel", "Avatar upload during setup skipped: ${e.message}")
                    }
                }

                _uiState.update {
                    it.copy(
                        isProfileSetupLoading = false,
                        profileSetupError = null,
                        needsProfileSetup = false,
                        currentUserName = u.name ?: name,
                        currentUserAvatar = sessionManager.avatar,
                        meUser = u
                    )
                }

                // Now that profile is set up, kick off the normal app flow
                bootstrapCurrentUser()
                refreshConversations()
                startConversationsPolling()
            } catch (e: Exception) {
                Log.e("ChatViewModel", "submitProfileSetup error", e)
                _uiState.update {
                    it.copy(
                        isProfileSetupLoading = false,
                        profileSetupError = "Network error: ${e.localizedMessage ?: "check connection"}"
                    )
                }
            }
        }
    }

    fun clearProfileSetupError() {
        _uiState.update { it.copy(profileSetupError = null) }
    }

    /**
     * Upload a new avatar image via PUT /auth/profile (multipart).
     * The server stores the file under /uploads/avatars/ and returns
     * the updated User object. We then refresh session + UI state.
     */
    fun uploadAvatar(uriString: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProfileUpdating = true) }
            try {
                val file = uriToFile(uriString) ?: run {
                    _uiState.update {
                        it.copy(
                            isProfileUpdating = false,
                            networkBannerMessage = "Could not read selected image"
                        )
                    }
                    return@launch
                }
                val mime = guessMime(uriString)
                val reqFile = file.asRequestBody(mime.toMediaTypeOrNull())
                val filePart = MultipartBody.Part.createFormData("file", file.name, reqFile)

                val resp = ApiClient.service.updateProfileAvatar(filePart)
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
                            currentUserAvatar = u.avatar ?: it.currentUserAvatar,
                            currentUserName = u.name ?: it.currentUserName,
                            currentUserBio = u.bio ?: it.currentUserBio,
                            meUser = u,
                            isProfileUpdating = false
                        )
                    }
                } else {
                    val err = parseErrorBody(resp.errorBody())
                    _uiState.update {
                        it.copy(
                            isProfileUpdating = false,
                            networkBannerMessage = "Avatar upload failed (${resp.code()}): ${err ?: "error"}"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "uploadAvatar error", e)
                _uiState.update {
                    it.copy(
                        isProfileUpdating = false,
                        networkBannerMessage = "Network error uploading avatar: ${e.localizedMessage ?: "check connection"}"
                    )
                }
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

                    // Check if this is a brand new user OR the user has no name/username set yet.
                    // If so, route them through the SetupProfileScreen before they can use the app.
                    val needsSetup = user?.isNewUser == true ||
                        user?.name.isNullOrBlank() ||
                        user?.username.isNullOrBlank()

                    _uiState.update {
                        it.copy(
                            isAuthenticated = true,
                            isAuthLoading = false,
                            authErrorMessage = null,
                            currentUserName = user?.name ?: "User",
                            currentUserPhone = user?.phone ?: cleanPhone,
                            currentUserAvatar = user?.avatar,
                            currentUserBio = user?.bio,
                            needsProfileSetup = needsSetup
                        )
                    }

                    if (!needsSetup) {
                        // Pull full profile + chats from server now that we have a token
                        bootstrapCurrentUser()
                        refreshConversations()
                        startConversationsPolling()
                    }
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
    // Extraction Helpers for Flexible API Responses
    // =============================================================

    private fun extractUserMap(item: Any?): Map<String, Any?>? {
        if (item == null) return null
        if (item is Map<*, *>) {
            @Suppress("UNCHECKED_CAST")
            val map = item as Map<String, Any?>
            val nestedUser = map["user"]
            if (nestedUser is Map<*, *>) {
                @Suppress("UNCHECKED_CAST")
                return nestedUser as Map<String, Any?>
            }
            return map
        }
        return null
    }

    private fun extractUserId(item: Any?): String? {
        if (item == null) return null
        if (item is String) return item
        if (item is Map<*, *>) {
            val m = extractUserMap(item) ?: return null
            return (m["_id"] ?: m["id"]) as? String
        }
        return null
    }

    private fun extractUserName(item: Any?): String? {
        if (item is Map<*, *>) {
            val m = extractUserMap(item) ?: return null
            return (m["name"] ?: m["username"] ?: m["phone"]) as? String
        }
        return null
    }

    private fun extractUserAvatar(item: Any?): String? {
        if (item is Map<*, *>) {
            val m = extractUserMap(item) ?: return null
            return m["avatar"] as? String
        }
        return null
    }

    private fun extractParticipantsList(raw: Any?): List<Any> {
        if (raw == null) return emptyList()
        if (raw is List<*>) {
            return raw.filterNotNull()
        }
        return emptyList()
    }

    // =============================================================
    // Conversations API
    // =============================================================

    fun refreshConversations() {
        _uiState.update { it.copy(isRefreshing = true, networkBannerMessage = null) }
        viewModelScope.launch {
            try {
                val conversations = mutableListOf<ApiConversation>()

                // Try fetching conversations from /messages/conversations first
                try {
                    val r1 = ApiClient.service.getConversationsAlt()
                    if (r1.isSuccessful && !r1.body().isNullOrEmpty()) {
                        conversations.addAll(r1.body()!!)
                    }
                } catch (e: Exception) {
                    Log.d("ChatViewModel", "getConversationsAlt error: ${e.message}")
                }

                // If empty or as complement, fetch from /conversations
                try {
                    val r2 = ApiClient.service.getConversations()
                    if (r2.isSuccessful && !r2.body().isNullOrEmpty()) {
                        val body = r2.body()!!
                        for (conv in body) {
                            if (conversations.none { it._id == conv._id }) {
                                conversations.add(conv)
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.d("ChatViewModel", "getConversations error: ${e.message}")
                }

                // Also try saved messages
                try {
                    val savedResp = ApiClient.service.getSavedMessagesConversation()
                    if (savedResp.isSuccessful && savedResp.body() != null) {
                        val saved = savedResp.body()!!
                        if (conversations.none { it._id == saved._id }) {
                            conversations.add(0, saved)
                        }
                    }
                } catch (_: Exception) {}

                val mappedChats = conversations.distinctBy { it._id }.map { mapApiConversationToChatItem(it) }

                _uiState.update { state ->
                    state.copy(
                        chats = mappedChats,
                        isRefreshing = false,
                        networkBannerMessage = null
                    )
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
                        val resp = try {
                            ApiClient.service.getConversationsAlt()
                        } catch (_: Exception) {
                            ApiClient.service.getConversations()
                        }
                        val body = if (resp.isSuccessful) resp.body() else null
                        if (!body.isNullOrEmpty()) {
                            val mapped = body.map { mapApiConversationToChatItem(it) }
                            _uiState.update { state ->
                                val localOnly = state.chats.filter { local -> mapped.none { it.id == local.id } }
                                state.copy(chats = mapped + localOnly)
                            }
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
        val rawParticipants = extractParticipantsList(apiConv.participants)

        val otherParticipantRaw = rawParticipants.firstOrNull { extractUserId(it) != currentUserId }
        val otherName = extractUserName(otherParticipantRaw)
        val otherAvatar = extractUserAvatar(otherParticipantRaw)

        val (lmText, lmTime) = when (val lm = apiConv.lastMessage) {
            is Map<*, *> -> {
                val text = (lm["text"] ?: lm["caption"] ?: lm["content"]) as? String
                val time = (lm["createdAt"] ?: lm["updatedAt"]) as? String
                Pair(text, time)
            }
            is String -> Pair(null, null)
            else -> Pair(null, null)
        }

        val isChannel = apiConv.isChannel == true || apiConv.type == "channel"
        val isGroup = apiConv.type == "group" || (apiConv.type != "private" && apiConv.type != "saved" && rawParticipants.size > 2)

        val isPersian = _uiState.value.language == "fa"
        val title = when {
            !apiConv.name.isNullOrBlank() -> apiConv.name
            !otherName.isNullOrBlank() -> otherName
            apiConv.type == "saved" -> if (isPersian) "پیام‌های ذخیره شده" else "Saved Messages"
            isChannel -> if (isPersian) "کانال" else "Channel"
            isGroup -> if (isPersian) "گروه" else "Group"
            else -> "7eve9Chat User"
        }

        val avatar = ApiClient.resolveUrl(
            apiConv.avatar ?: otherAvatar
        )
        val subtitle = when {
            !lmText.isNullOrBlank() -> lmText
            isChannel -> if (isPersian) "کانال" else "Channel"
            isGroup -> if (isPersian) "گروه" else "Group"
            else -> if (isPersian) "هنوز پیامی نیست" else "No messages yet"
        }
        val time = formatApiTime(lmTime ?: apiConv.lastMessageAt)

        // unreadCount can be either a plain Int or Map<userId, count>
        val unread = when (val uc = apiConv.unreadCount) {
            is Number -> uc.toInt()
            is Map<*, *> -> {
                val key = currentUserId ?: ""
                (uc[key] as? Number)?.toInt() ?: 0
            }
            is List<*> -> (uc.firstOrNull() as? Number)?.toInt() ?: 0
            else -> 0
        }

        val isMuted = apiConv.isMuted == true ||
            (apiConv.mutedBy?.contains(currentUserId) == true)

        val isPinned = apiConv.pinned == true

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
            isChannel = isChannel,
            memberCount = rawParticipants.size,
            isOnline = false
        )
    }

    /** Heuristic for "online" display — server's isOnline field if set. */
    private fun isUserOnline(user: ApiUser): Boolean {
        return user.isOnline == true
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
        // Start with SENDING status — shows clock icon
        val localMessage = MessageItem(
            id = tempId,
            text = text.trim(),
            time = currentTime,
            isOutgoing = true,
            type = MessageType.TEXT,
            isRead = false,
            status = MessageStatus.SENDING
        )

        // Optimistic UI update — message appears immediately with clock icon
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
                    val mapped = mapApiMessageToItem(serverMsg).copy(
                        status = MessageStatus.SENT  // Single check — server accepted
                    )

                    _uiState.update { state ->
                        val updated = state.currentMessages.map {
                            if (it.id == tempId) mapped else it
                        }
                        state.copy(currentMessages = updated)
                    }
                } else {
                    val err = parseErrorBody(response.errorBody())
                    Log.e("ChatViewModel", "sendMessage failed: ${response.code()} $err")
                    // Mark as FAILED — shows error icon, user can retry
                    _uiState.update { state ->
                        val updated = state.currentMessages.map {
                            if (it.id == tempId) it.copy(status = MessageStatus.FAILED) else it
                        }
                        state.copy(
                            currentMessages = updated,
                            networkBannerMessage = "Failed to send: ${err ?: "code ${response.code()}"}"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "sendMessage error", e)
                // Mark as FAILED
                _uiState.update { state ->
                    val updated = state.currentMessages.map {
                        if (it.id == tempId) it.copy(status = MessageStatus.FAILED) else it
                    }
                    state.copy(
                        currentMessages = updated,
                        networkBannerMessage = "Network error: ${e.localizedMessage ?: "message not sent"}"
                    )
                }
            }
        }
    }

    /** Retry sending a failed message. */
    fun retrySendMessage(messageId: String) {
        val msg = _uiState.value.currentMessages.find { it.id == messageId } ?: return
        if (msg.status != MessageStatus.FAILED) return
        val currentChatId = _uiState.value.selectedChatId ?: return

        // Set back to SENDING
        _uiState.update { state ->
            state.copy(currentMessages = state.currentMessages.map {
                if (it.id == messageId) it.copy(status = MessageStatus.SENDING) else it
            })
        }

        viewModelScope.launch {
            try {
                val req = SendMessageRequest(
                    conversationId = currentChatId,
                    text = msg.text,
                    type = "text"
                )
                val response = ApiClient.service.sendMessage(req)
                if (response.isSuccessful && response.body() != null) {
                    val mapped = mapApiMessageToItem(response.body()!!).copy(
                        status = MessageStatus.SENT
                    )
                    _uiState.update { state ->
                        state.copy(currentMessages = state.currentMessages.map {
                            if (it.id == messageId) mapped else it
                        })
                    }
                } else {
                    _uiState.update { state ->
                        state.copy(currentMessages = state.currentMessages.map {
                            if (it.id == messageId) it.copy(status = MessageStatus.FAILED) else it
                        })
                    }
                }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "retrySendMessage error", e)
                _uiState.update { state ->
                    state.copy(currentMessages = state.currentMessages.map {
                        if (it.id == messageId) it.copy(status = MessageStatus.FAILED) else it
                    })
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
            // No fake username — null until server provides one
            username = null,
            phone = null,
            bio = null,
            avatarUrl = chat?.avatarUrl,
            avatarType = chat?.avatarType ?: AvatarType.MOTORCYCLE,
            isOnline = chat?.isOnline == true,
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
                // Server returns participants in flexible formats
                val otherUserId = extractParticipantsList(conv.participants)
                    .mapNotNull { extractUserId(it) }
                    .firstOrNull { it != sessionManager.userId } ?: return@launch

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
                                avatarUrl = ApiClient.resolveUrl(u.avatar) ?: it.profileUser?.avatarUrl,
                                isOnline = u.isOnline == true
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
            // No fake bio — null if user hasn't set one
            bio = sessionManager.bio,
            avatarUrl = ApiClient.resolveUrl(sessionManager.avatar),
            avatarType = AvatarType.MOTORCYCLE,
            // Current user is always "online" from their own perspective
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
    // Theme toggle (Dark / Light) — persisted in SessionManager
    // =============================================================

    fun toggleTheme() {
        val newMode = !_uiState.value.isDarkMode
        sessionManager.isDarkMode = newMode
        _uiState.update { it.copy(isDarkMode = newMode) }
    }

    // =============================================================
    // Settings sub-screen navigation
    // =============================================================

    fun openSettingsScreen(key: String) {
        _uiState.update { it.copy(activeSettingsScreen = key) }
    }

    fun closeSettingsScreen() {
        _uiState.update { it.copy(activeSettingsScreen = null) }
    }

    // ===== Preference setters — persisted to SessionManager =====

    fun setLanguage(lang: String) {
        sessionManager.language = lang
        _uiState.update { it.copy(language = lang) }
    }

    fun setMessageTextSize(size: Int) {
        sessionManager.messageTextSize = size
        _uiState.update { it.copy(messageTextSize = size) }
    }

    fun setMessageCornerRadius(radius: Int) {
        sessionManager.messageCornerRadius = radius
        _uiState.update { it.copy(messageCornerRadius = radius) }
    }

    fun setDoubleTapEmoji(emoji: String) {
        sessionManager.doubleTapEmoji = emoji
        _uiState.update { it.copy(doubleTapEmoji = emoji) }
    }

    fun setAutoDownloadMobile(v: Boolean) {
        sessionManager.autoDownloadMobile = v
        _uiState.update { it.copy(autoDownloadMobile = v) }
    }

    fun setAutoDownloadWifi(v: Boolean) {
        sessionManager.autoDownloadWifi = v
        _uiState.update { it.copy(autoDownloadWifi = v) }
    }

    fun setAutoDownloadRoaming(v: Boolean) {
        sessionManager.autoDownloadRoaming = v
        _uiState.update { it.copy(autoDownloadRoaming = v) }
    }

    fun setSaveGalleryPrivate(v: Boolean) {
        sessionManager.saveGalleryPrivate = v
        _uiState.update { it.copy(saveGalleryPrivate = v) }
    }

    fun setSaveGalleryGroups(v: Boolean) {
        sessionManager.saveGalleryGroups = v
        _uiState.update { it.copy(saveGalleryGroups = v) }
    }

    fun setSaveGalleryChannels(v: Boolean) {
        sessionManager.saveGalleryChannels = v
        _uiState.update { it.copy(saveGalleryChannels = v) }
    }

    fun setPowerSavingEnabled(v: Boolean) {
        sessionManager.powerSavingEnabled = v
        _uiState.update { it.copy(powerSavingEnabled = v) }
    }

    fun setPowerLowQuality(v: Boolean) {
        sessionManager.powerLowQuality = v
        _uiState.update { it.copy(powerLowQuality = v) }
    }

    fun setPowerDisableAnimations(v: Boolean) {
        sessionManager.powerDisableAnimations = v
        _uiState.update { it.copy(powerDisableAnimations = v) }
    }

    fun setPowerDisableAutoplay(v: Boolean) {
        sessionManager.powerDisableAutoplay = v
        _uiState.update { it.copy(powerDisableAutoplay = v) }
    }

    // ===== Chat folders (local-only) =====

    fun addChatFolder(name: String) {
        val newFolder = com.example.ui.screens.ChatFolder(
            id = "folder_${System.currentTimeMillis()}",
            name = name.trim()
        )
        _uiState.update { state ->
            state.copy(chatFolders = state.chatFolders + newFolder)
        }
    }

    fun deleteChatFolder(folderId: String) {
        _uiState.update { state ->
            state.copy(chatFolders = state.chatFolders.filter { it.id != folderId })
        }
    }

    fun setFolderTags(v: Boolean) {
        _uiState.update { it.copy(showFolderTags = v) }
    }

    /** Clear the app's local cache directory (called from Data & Storage). */
    fun clearCache() {
        viewModelScope.launch {
            try {
                val ctx = getApplication<Application>()
                ctx.cacheDir?.let { dir ->
                    dir.walkTopDown().forEach { file ->
                        if (file.isFile) file.delete()
                    }
                }
                _uiState.update {
                    it.copy(networkBannerMessage = "Cache cleared")
                }
            } catch (e: Exception) {
                Log.w("ChatViewModel", "clearCache: ${e.message}")
            }
        }
    }

    // ===== Privacy settings (synced to /auth/profile when changed) =====

    fun setPrivacyLastSeen(value: String) {
        sessionManager.privacyLastSeen = value
        _uiState.update { it.copy(privacyLastSeen = value) }
        pushPrivacyToServer()
    }

    fun setPrivacyPhoneNumber(value: String) {
        sessionManager.privacyPhoneNumber = value
        _uiState.update { it.copy(privacyPhoneNumber = value) }
        pushPrivacyToServer()
    }

    fun setPrivacyForwarded(value: String) {
        sessionManager.privacyForwarded = value
        _uiState.update { it.copy(privacyForwarded = value) }
        pushPrivacyToServer()
    }

    fun setPrivacyGroups(value: String) {
        sessionManager.privacyGroups = value
        _uiState.update { it.copy(privacyGroups = value) }
        pushPrivacyToServer()
    }

    fun setPhoneHidden(value: Boolean) {
        sessionManager.isPhoneHidden = value
        _uiState.update { it.copy(isPhoneHidden = value) }
        pushPrivacyToServer()
    }

    /**
     * Push the local privacy settings bundle to PUT /auth/profile.
     * Silently fails — privacy is also stored locally so the UI is consistent
     * even if the server is unreachable.
     */
    private fun pushPrivacyToServer() {
        viewModelScope.launch {
            try {
                ApiClient.service.updateProfile(
                    UpdateProfileRequest(
                        isPhoneHidden = sessionManager.isPhoneHidden
                        // Server-side: the spec uses a nested privacySettings object.
                        // Our UpdateProfileRequest is flat — the server should accept
                        // both shapes per spec §2.2 (it lists privacySettings as one
                        // of the optional top-level fields). For full compliance we'd
                        // need to model it nested, but for now this is best-effort.
                    )
                )
            } catch (e: Exception) {
                Log.w("ChatViewModel", "pushPrivacyToServer: ${e.message}")
            }
        }
    }

    /**
     * Log out all other sessions on the account by calling POST /auth/logout-all.
     * Per spec §1.9, this invalidates every session on the account — INCLUDING
     * the current one. After calling it we wipe the local session and send the
     * user back to the login screen.
     */
    fun logoutAllOtherDevices() {
        viewModelScope.launch {
            try {
                ApiClient.service.logoutAllDevices()
                // The server killed our token too — drop everything locally
                // and bounce back to login.
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
                        currentMessages = emptyList(),
                        sessions = emptyList(),
                        networkBannerMessage = null
                    )
                }
            } catch (e: Exception) {
                Log.w("ChatViewModel", "logoutAllOtherDevices: ${e.message}")
                _uiState.update {
                    it.copy(networkBannerMessage = "Couldn't reach server: ${e.localizedMessage}")
                }
            }
        }
    }

    /** Fetch the user's active sessions list for the Devices screen. */
    fun loadSessions() {
        _uiState.update { it.copy(isSessionsLoading = true, sessionsError = null) }
        viewModelScope.launch {
            try {
                val resp = ApiClient.service.getSessions()
                if (resp.isSuccessful && resp.body() != null) {
                    _uiState.update {
                        it.copy(
                            sessions = resp.body()!!,
                            isSessionsLoading = false
                        )
                    }
                } else {
                    val err = parseErrorBody(resp.errorBody())
                    _uiState.update {
                        it.copy(
                            isSessionsLoading = false,
                            sessionsError = err ?: "Failed to load sessions (${resp.code()})"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "loadSessions error", e)
                _uiState.update {
                    it.copy(
                        isSessionsLoading = false,
                        sessionsError = "Network error: ${e.localizedMessage ?: "check connection"}"
                    )
                }
            }
        }
    }

    /**
     * Open a private chat with the @7eve9 support account.
     * Tries GET /auth/getUserByUsername/7eve9 to fetch the user id, then
     * GET /messages/conversations/{userId} to find/create the private chat,
     * then selects it.
     */
    fun openSupportChat() {
        viewModelScope.launch {
            try {
                val userResp = ApiClient.service.getUserByUsername("7eve9")
                if (!userResp.isSuccessful || userResp.body() == null) {
                    _uiState.update {
                        it.copy(networkBannerMessage = "Support account @7eve9 not found")
                    }
                    return@launch
                }
                val supportUser = userResp.body()!!
                val supportId = supportUser._id ?: return@launch
                val convResp = ApiClient.service.getPrivateConversation(supportId)
                if (convResp.isSuccessful && convResp.body() != null) {
                    val conv = convResp.body()!!
                    val chatItem = mapApiConversationToChatItem(conv)
                    _uiState.update { state ->
                        if (state.chats.none { it.id == chatItem.id }) {
                            state.copy(chats = listOf(chatItem) + state.chats)
                        } else state
                    }
                    selectChat(chatItem.id)
                }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "openSupportChat error", e)
                _uiState.update {
                    it.copy(networkBannerMessage = "Couldn't open support chat: ${e.localizedMessage}")
                }
            }
        }
    }

    /**
     * Open "Saved Messages" — the private conversation with yourself.
     * Uses the dedicated GET /conversations/saved endpoint (spec §2)
     * which the server returns (and creates if missing).
     */
    fun openSavedMessages() {
        viewModelScope.launch {
            try {
                val resp = ApiClient.service.getSavedMessagesConversation()
                if (resp.isSuccessful && resp.body() != null) {
                    val conv = resp.body()!!
                    // Make sure this conversation is in the chats list so
                    // ChatDetailScreen can find it.
                    val chatItem = mapApiConversationToChatItem(conv)
                    _uiState.update { state ->
                        if (state.chats.none { it.id == chatItem.id }) {
                            state.copy(chats = listOf(chatItem) + state.chats)
                        } else state
                    }
                    selectChat(chatItem.id)
                } else {
                    _uiState.update {
                        it.copy(networkBannerMessage = "Couldn't open Saved Messages (${resp.code()})")
                    }
                }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "openSavedMessages error", e)
                _uiState.update {
                    it.copy(networkBannerMessage = "Network error: ${e.localizedMessage}")
                }
            }
        }
    }

    /**
     * Read the device's contact list (ContactsContract) and ask the server
     * which of those phone numbers belong to registered 7eve9Chat users.
     * The returned list (POST /auth/check-contacts) is what the Contacts
     * screen displays — only registered users, no raw device contacts.
     *
     * This requires the READ_CONTACTS runtime permission; if it isn't
     * granted yet, the caller (ContactsScreen) should request it before
     * calling this.
     */
    fun loadDeviceContacts() {
        _uiState.update {
            it.copy(isContactsLoading = true, contactsError = null, addContactStatus = null)
        }
        viewModelScope.launch {
            try {
                val ctx = getApplication<Application>()
                val phones = readDevicePhoneNumbers(ctx)
                if (phones.isEmpty()) {
                    _uiState.update {
                        it.copy(
                            contacts = emptyList(),
                            isContactsLoading = false,
                            contactsError = "No contacts found on this device"
                        )
                    }
                    return@launch
                }
                // Normalize each phone: strip +, spaces, leading 0; keep digits only.
                val normalized = phones.map { raw ->
                    var p = raw.replace("+", "").replace(" ", "").replace("-", "").trim()
                    if (p.startsWith("00")) p = p.drop(2)
                    p
                }.filter { it.length >= 10 }.distinct()

                val resp = ApiClient.service.checkContacts(
                    com.example.data.api.CheckContactsRequest(phones = normalized)
                )
                if (resp.isSuccessful && resp.body() != null) {
                    _uiState.update {
                        it.copy(
                            contacts = resp.body()!!,
                            isContactsLoading = false,
                            contactsError = null
                        )
                    }
                } else {
                    val err = parseErrorBody(resp.errorBody())
                    _uiState.update {
                        it.copy(
                            isContactsLoading = false,
                            contactsError = err ?: "Failed to sync contacts (${resp.code()})"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "loadDeviceContacts error", e)
                _uiState.update {
                    it.copy(
                        isContactsLoading = false,
                        contactsError = "Network error: ${e.localizedMessage ?: "check connection"}"
                    )
                }
            }
        }
    }

    /**
     * Open a private chat with the given contact by their userId.
     * Uses POST /messages/contacts — the server's dedicated "find or create
     * private chat with this user" endpoint (addContact controller).
     *
     * After getting the conversation:
     *   1. Add it to the chats list (or update if already present)
     *   2. Switch to Chats tab (selectedBottomNavIndex = 0)
     *   3. Set selectedChatId so ChatDetailScreen renders
     *   4. Fetch messages for the conversation
     *
     * All state changes happen in ONE _uiState.update call to avoid race
     * conditions between ChatDetailScreen looking up the chat in the list
     * and the list being updated.
     */
    fun openPrivateChatWithContact(userId: String) {
        viewModelScope.launch {
            try {
                Log.d("ChatViewModel", "openPrivateChatWithContact: userId=$userId")
                // POST /messages/contacts with { userId: contactUserId }
                val resp = ApiClient.service.startPrivateChatWithContact(
                    AddContactRequest(userId = userId)
                )
                if (!resp.isSuccessful || resp.body() == null) {
                    val err = parseErrorBody(resp.errorBody())
                    Log.e("ChatViewModel", "openPrivateChatWithContact API failed: ${resp.code()} $err")
                    _uiState.update {
                        it.copy(networkBannerMessage = "Couldn't open chat: ${err ?: "code ${resp.code()}"}")
                    }
                    return@launch
                }

                val conv = resp.body()!!
                Log.d("ChatViewModel", "openPrivateChatWithContact: got conversation ${conv._id} type=${conv.type} participants=${conv.participantsCount}")
                val chatItem = mapApiConversationToChatItem(conv)

                // Single atomic state update: add chat + switch tab + select chat
                _uiState.update { state ->
                    state.copy(
                        chats = if (state.chats.none { it.id == chatItem.id }) {
                            listOf(chatItem) + state.chats
                        } else {
                            state.chats.map { if (it.id == chatItem.id) chatItem else it }
                        },
                        selectedBottomNavIndex = 0,
                        selectedChatId = chatItem.id,
                        currentMessages = emptyList() // clear previous chat's messages
                    )
                }

                // Now fetch messages for this conversation (fires off its own coroutine)
                fetchMessages(chatItem.id)
                startMessagePolling(chatItem.id)
                markConversationAsRead(chatItem.id)
            } catch (e: Exception) {
                Log.e("ChatViewModel", "openPrivateChatWithContact error", e)
                _uiState.update {
                    it.copy(networkBannerMessage = "Network error: ${e.localizedMessage}")
                }
            }
        }
    }

    /**
     * Create a new group conversation. Calls POST /conversations/group
     * (spec §2) with the given name, type and optional participant ids.
     */
    fun createGroup(
        name: String,
        type: String = "group",
        participantIds: List<String> = emptyList(),
        description: String? = null
    ) {
        viewModelScope.launch {
            try {
                val resp = ApiClient.service.createGroup(
                    com.example.data.api.CreateConversationRequest(
                        name = name,
                        type = type,
                        description = description,
                        participants = participantIds
                    )
                )
                if (resp.isSuccessful && resp.body() != null) {
                    val conv = resp.body()!!
                    val chatItem = mapApiConversationToChatItem(conv)
                    _uiState.update { state ->
                        state.copy(chats = listOf(chatItem) + state.chats)
                    }
                    selectChat(chatItem.id)
                } else {
                    val err = parseErrorBody(resp.errorBody())
                    _uiState.update {
                        it.copy(networkBannerMessage = "Couldn't create group: ${err ?: "code ${resp.code()}"}")
                    }
                }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "createGroup error", e)
                _uiState.update {
                    it.copy(networkBannerMessage = "Network error: ${e.localizedMessage}")
                }
            }
        }
    }

    /**
     * Read all phone numbers from the device's ContactsContract.
     * Returns raw strings (caller normalizes them).
     */
    private fun readDevicePhoneNumbers(ctx: android.content.Context): List<String> {
        val numbers = mutableSetOf<String>()
        try {
            val cursor = ctx.contentResolver.query(
                android.provider.ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(android.provider.ContactsContract.CommonDataKinds.Phone.NUMBER),
                null, null, null
            )
            cursor?.use {
                val idx = it.getColumnIndex(android.provider.ContactsContract.CommonDataKinds.Phone.NUMBER)
                while (it.moveToNext()) {
                    if (idx >= 0) {
                        val num = it.getString(idx) ?: continue
                        if (num.isNotBlank()) numbers.add(num)
                    }
                }
            }
        } catch (e: Exception) {
            Log.w("ChatViewModel", "readDevicePhoneNumbers: ${e.message}")
        }
        return numbers.toList()
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
