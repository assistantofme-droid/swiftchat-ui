package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.ChatDetailScreen
import com.example.ui.screens.ChatListScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TelegramDarkBg
import com.example.viewmodel.ChatViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = TelegramDarkBg
                ) {
                    TelegramApp()
                }
            }
        }
    }
}

@Composable
fun TelegramApp(
    viewModel: ChatViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

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
                onVerifyOtp = { phone, code -> viewModel.verifyOtp(phone, code) },
                onSkipLogin = { viewModel.skipLogin() }
            )
        } else {
            AnimatedContent(
                targetState = uiState.selectedChatId,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "ChatNavigation"
            ) { selectedChatId ->
                if (selectedChatId == null) {
                    ChatListScreen(
                        chats = uiState.chats,
                        selectedCategoryTab = uiState.selectedCategoryTab,
                        selectedBottomNavIndex = uiState.selectedBottomNavIndex,
                        isSearching = uiState.isSearching,
                        searchQuery = uiState.searchQuery,
                        isRefreshing = uiState.isRefreshing,
                        currentUserName = uiState.currentUserName,
                        currentUserPhone = uiState.currentUserPhone,
                        currentUserAvatar = uiState.currentUserAvatar,
                        onRefresh = { viewModel.refreshConversations() },
                        onLogout = { viewModel.logout() },
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
                        }
                    )
                } else {
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
                            onOpenPhotoViewer = { viewModel.openPhotoViewer(it) },
                            onClosePhotoViewer = { viewModel.closePhotoViewer() },
                            onOpenVideoPlayer = { viewModel.openVideoPlayer(it) },
                            onCloseVideoPlayer = { viewModel.closeVideoPlayer() },
                            onOpenUserProfile = { viewModel.openUserProfile(it) },
                            onCloseUserProfile = { viewModel.closeUserProfile() },
                            onUpdateProfile = { name, username, bio ->
                                viewModel.updateProfile(name, username, bio)
                            }
                        )
                    }
                }
            }

            // Global Profile Modal if opened from main list
            if (uiState.selectedChatId == null) {
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
