package com.example

import android.app.PictureInPictureParams
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Rational
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.call.CallManager
import com.example.ui.call.CompactPipCallView
import com.example.ui.call.FloatingCallOverlay
import com.example.ui.call.FullCallScreen
import com.example.ui.media.AudioPlayerManager
import com.example.ui.media.MediaNotificationHelper
import com.example.ui.screens.ChatDetailScreen
import com.example.ui.screens.ChatListScreen
import com.example.ui.screens.ContactsScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.ChatViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        MediaNotificationHelper.createNotificationChannel(this)

        setContent {
            TelegramApp()
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && CallManager.callState.value.isActive) {
            try {
                val params = PictureInPictureParams.Builder()
                    .setAspectRatio(Rational(9, 16))
                    .build()
                enterPictureInPictureMode(params)
            } catch (_: Exception) {}
        }
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        CallManager.setInPictureInPicture(isInPictureInPictureMode)
    }

    override fun onDestroy() {
        super.onDestroy()
        AudioPlayerManager.stop(this)
    }
}

@Composable
fun TelegramApp(
    viewModel: ChatViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val callState by CallManager.callState.collectAsState()

    // Theme follows the user's Dark/Light toggle.
    MyApplicationTheme(isDarkMode = uiState.isDarkMode) {
        TelegramAppContent(viewModel = viewModel)
    }
}

@Composable
private fun TelegramAppContent(viewModel: ChatViewModel) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val callState by CallManager.callState.collectAsState()

    // If device is in Picture-in-Picture mode during a call, show compact PiP layout
    if (callState.isInPip) {
        CompactPipCallView()
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = uiState.isAuthenticated,
            transitionSpec = {
                fadeIn() togetherWith fadeOut()
            },
            label = "AuthOrAppNavigation"
        ) { isAuthenticated ->
            if (!isAuthenticated) {
                LoginScreen(
                    isLoading = uiState.isAuthLoading,
                    errorMessage = uiState.authErrorMessage,
                    onSendOtp = { phone -> viewModel.sendOtp(phone) },
                    onVerifyOtp = { phone, code -> viewModel.verifyOtp(phone, code) }
                )
            } else {
                AnimatedContent(
                    targetState = uiState.selectedChatId,
                    transitionSpec = {
                        fadeIn() togetherWith fadeOut()
                    },
                    label = "ChatNavigation"
                ) { selectedChatId ->
                    if (selectedChatId != null) {
                        // Chat detail always takes priority when a chat is open
                        val selectedChat = uiState.chats.find { it.id == selectedChatId }
                            ?: uiState.chats.firstOrNull()
                        if (selectedChat != null) {
                            ChatDetailScreen(
                                chat = selectedChat,
                                messages = uiState.currentMessages,
                                isAttachmentSheetOpen = uiState.isAttachmentSheetOpen,
                                isStickerSheetOpen = uiState.isStickerSheetOpen,
                                stickers = uiState.stickers,
                                gifs = uiState.gifs,
                                isStickersLoading = uiState.isStickersLoading,
                                activePhotoMessage = uiState.activePhotoMessage,
                                activeVideoMessage = uiState.activeVideoMessage,
                                isProfileModalOpen = uiState.isProfileModalOpen,
                                profileUser = uiState.profileUser,
                                isProfileUpdating = uiState.isProfileUpdating,
                                onBack = { viewModel.selectChat(null) },
                                onOpenAttachmentSheet = { viewModel.openAttachmentSheet() },
                                onCloseAttachmentSheet = { viewModel.closeAttachmentSheet() },
                                onOpenStickerSheet = { viewModel.openStickerSheet() },
                                onCloseStickerSheet = { viewModel.closeStickerSheet() },
                                onSelectSticker = { viewModel.sendSticker(it) },
                                onSelectGif = { viewModel.sendGif(it) },
                                onSendMessage = { viewModel.sendMessage(it) },
                                onSendMedia = { viewModel.sendMediaItems(it) },
                                onSendAudio = { viewModel.sendAudioMessage() },
                                onSendVideo = { viewModel.sendVideoMessage() },
                                onToggleReaction = { messageId, emoji ->
                                    viewModel.toggleReaction(messageId, emoji)
                                },
                                onDeleteMessage = { viewModel.deleteMessage(it) },
                                onEditMessage = { id, text -> viewModel.editMessage(id, text) },
                                onPinMessage = { viewModel.pinMessage(it.id) },
                                onSendLocation = { lat, lng, title -> viewModel.sendLocation(lat, lng, title) },
                                onSendPoll = { q, opts, anon -> viewModel.sendPoll(q, opts, anon) },
                                onSendFile = { name, uri -> viewModel.sendFile(name, uri) },
                                onSendMusic = { title, uri -> viewModel.sendMusic(title, uri) },
                                onOpenPhotoViewer = { viewModel.openPhotoViewer(it) },
                                onClosePhotoViewer = { viewModel.closePhotoViewer() },
                                onOpenVideoPlayer = { viewModel.openVideoPlayer(it) },
                                onCloseVideoPlayer = { viewModel.closeVideoPlayer() },
                                onOpenUserProfile = { viewModel.openUserProfile(it) },
                                onCloseUserProfile = { viewModel.closeUserProfile() },
                                onUpdateProfile = { name, username, bio ->
                                    viewModel.updateProfile(name, username, bio)
                                },
                                onVoiceCall = {
                                    CallManager.startCall(
                                        context = context,
                                        contactName = selectedChat.title,
                                        contactAvatarUrl = selectedChat.avatarUrl,
                                        isVideo = false,
                                        recipientId = selectedChat.id
                                    )
                                },
                                onVideoCall = {
                                    CallManager.startCall(
                                        context = context,
                                        contactName = selectedChat.title,
                                        contactAvatarUrl = selectedChat.avatarUrl,
                                        isVideo = true,
                                        recipientId = selectedChat.id
                                    )
                                }
                            )
                        }
                    } else {
                        // Bottom-nav tab routing: 0=Chats, 1=Contacts, 2=Settings, 3=Profile
                        when (uiState.selectedBottomNavIndex) {
                            1 -> ContactsScreen(
                                contacts = uiState.contacts,
                                isLoading = uiState.isContactsLoading,
                                errorMessage = uiState.contactsError,
                                addContactStatus = uiState.addContactStatus,
                                currentUserAvatarUrl = uiState.currentUserAvatar,
                                onLaunchLoad = { viewModel.loadContacts() },
                                onAddContact = { viewModel.addContact(it) },
                                onClearAddStatus = { viewModel.clearAddContactStatus() },
                                onSelectBottomNav = { viewModel.selectBottomNavIndex(it) },
                                selectedBottomNavIndex = 1
                            )
                            2 -> SettingsScreen(
                                meUser = uiState.meUser,
                                currentUserAvatarUrl = uiState.currentUserAvatar,
                                selectedBottomNavIndex = 2,
                                onSelectBottomNav = { viewModel.selectBottomNavIndex(it) },
                                onLogout = { viewModel.logout() }
                            )
                            3 -> ProfileScreen(
                                meUser = uiState.meUser,
                                myChannel = uiState.myChannel,
                                isMeLoading = uiState.isMeLoading,
                                isProfileUpdating = uiState.isProfileUpdating,
                                selectedBottomNavIndex = 3,
                                onSelectBottomNav = { viewModel.selectBottomNavIndex(it) },
                                onRefreshMe = { viewModel.refreshMe() },
                                onUploadAvatar = { uri -> viewModel.uploadAvatar(uri.toString()) },
                                onEditInfo = { viewModel.openCurrentUserProfile() }
                            )
                            else -> ChatListScreen(
                                chats = uiState.chats,
                                selectedCategoryTab = uiState.selectedCategoryTab,
                                selectedBottomNavIndex = uiState.selectedBottomNavIndex,
                                isSearching = uiState.isSearching,
                                searchQuery = uiState.searchQuery,
                                isDarkMode = uiState.isDarkMode,
                                isRefreshing = uiState.isRefreshing,
                                currentUserName = uiState.currentUserName,
                                currentUserPhone = uiState.currentUserPhone,
                                currentUserAvatar = uiState.currentUserAvatar,
                                onSearchToggle = { viewModel.setSearching(it) },
                                onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                                onCategoryTabSelect = { viewModel.selectCategoryTab(it) },
                                onBottomNavSelect = { viewModel.selectBottomNavIndex(it) },
                                onChatClick = { chat ->
                                    viewModel.selectChat(chat.id)
                                },
                                onNewChatClick = {
                                    if (uiState.chats.isNotEmpty()) {
                                        viewModel.selectChat(uiState.chats.first().id)
                                    }
                                },
                                onToggleTheme = { viewModel.toggleTheme() },
                                onOpenSavedMessages = { viewModel.openSavedMessages() },
                                onNewGroup = { /* TODO: open group creation flow — future update */ }
                            )
                        }

                        // Global Profile Modal — available from list screens too
                        com.example.ui.components.UserProfileModal(
                            visible = uiState.isProfileModalOpen,
                            user = uiState.profileUser,
                            isUpdating = uiState.isProfileUpdating,
                            onDismiss = { viewModel.closeUserProfile() },
                            onUpdateProfile = { name, username, bio ->
                                viewModel.updateProfile(name, username, bio)
                            }
                        )
                    }
                }
            }
        }

        // Full Screen Call View (when active and not minimized into floating mode)
        if (callState.isActive && !callState.isFloating) {
            FullCallScreen(
                onMinimize = {
                    CallManager.setFloating(true)
                }
            )
        }

        // In-App Draggable Floating Call Overlay (Allows chatting and controls while in call)
        if (callState.isActive && callState.isFloating) {
            FloatingCallOverlay(
                onExpand = {
                    CallManager.setFloating(false)
                }
            )
        }
    }
}
