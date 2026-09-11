package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AvatarType
import com.example.data.model.ChatItem
import com.example.ui.components.AvatarView
import com.example.ui.theme.TelegramAccent
import com.example.ui.theme.TelegramChatListBg
import com.example.ui.theme.TelegramCheckBlue
import com.example.ui.theme.TelegramDarkBg
import com.example.ui.theme.TelegramPinIcon
import com.example.ui.theme.TelegramPrimary
import com.example.ui.theme.TelegramTextMuted
import com.example.ui.theme.TelegramTextPrimary
import com.example.ui.theme.TelegramTextSecondary
import com.example.ui.theme.TelegramTypingCyan
import com.example.ui.theme.TelegramUnreadBadge
import com.example.ui.theme.TelegramUnreadBadgeText

@Composable
fun ChatListScreen(
    chats: List<ChatItem>,
    selectedCategoryTab: String,
    selectedBottomNavIndex: Int,
    isSearching: Boolean,
    searchQuery: String,
    onSearchToggle: (Boolean) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onCategoryTabSelect: (String) -> Unit,
    onBottomNavSelect: (Int) -> Unit,
    onChatClick: (ChatItem) -> Unit,
    onNewChatClick: () -> Unit,
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit = {},
    currentUserName: String? = null,
    currentUserPhone: String? = null,
    currentUserAvatar: String? = null,
    onLogout: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    var showProfileDialog by remember { mutableStateOf(false) }

    // If user selects profile tab in bottom nav, open profile dialog
    if (selectedBottomNavIndex == 3) {
        showProfileDialog = true
    }

    val filteredChats = remember(chats, searchQuery) {
        if (searchQuery.isBlank()) chats
        else chats.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
            it.subtitle.contains(searchQuery, ignoreCase = true)
        }
    }

    val listState = rememberLazyListState()

    // Smooth subtle pulsing animation for "Updating..." text
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val updateAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "update_pulse"
    )

    // Profile Dialog
    if (showProfileDialog) {
        AlertDialog(
            onDismissRequest = {
                showProfileDialog = false
                if (selectedBottomNavIndex == 3) onBottomNavSelect(0)
            },
            title = {
                Text(
                    text = "Account & Profile",
                    color = TelegramTextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                ) {
                    AvatarView(
                        avatarUrl = currentUserAvatar,
                        title = currentUserName ?: "Telegram User",
                        size = 72.dp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = currentUserName ?: "7eve9chat User",
                        color = TelegramTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (!currentUserPhone.isNullOrBlank()) {
                        Text(
                            text = "+$currentUserPhone",
                            color = TelegramTextSecondary,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Connected Server: https://7eve9craft.ir",
                        color = TelegramPrimary,
                        fontSize = 12.sp
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showProfileDialog = false
                        onLogout()
                    }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = null,
                        tint = Color(0xFFFF5252),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Log Out / خروج", color = Color(0xFFFF5252), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showProfileDialog = false
                        if (selectedBottomNavIndex == 3) onBottomNavSelect(0)
                    }
                ) {
                    Text("Close", color = TelegramPrimary)
                }
            },
            containerColor = TelegramDarkBg
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TelegramChatListBg)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top App Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TelegramDarkBg)
                    .statusBarsPadding()
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                if (isSearching) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextField(
                            value = searchQuery,
                            onValueChange = onSearchQueryChange,
                            placeholder = { Text("Search chats...", color = TelegramTextMuted) },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedTextColor = TelegramTextPrimary,
                                unfocusedTextColor = TelegramTextPrimary
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { onSearchToggle(false) }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close search",
                                tint = Color.White
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left lightning badge or Avatar
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF9A825))
                                .clickable { showProfileDialog = true },
                            contentAlignment = Alignment.Center
                        ) {
                            if (!currentUserAvatar.isNullOrBlank()) {
                                AvatarView(
                                    avatarUrl = currentUserAvatar,
                                    title = currentUserName,
                                    size = 36.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.FlashOn,
                                    contentDescription = "Turbo / Proxy",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        // Title
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onRefresh() }
                        ) {
                            Text(
                                text = if (isRefreshing) "Updating..." else (currentUserName ?: "7eve9chat"),
                                color = TelegramTextPrimary,
                                fontSize = 19.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = if (isRefreshing) Modifier.alpha(updateAlpha) else Modifier
                            )
                            if (!isRefreshing) {
                                Text(
                                    text = "connected",
                                    color = TelegramPrimary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        // Refresh Action Icon
                        IconButton(onClick = onRefresh) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh",
                                tint = if (isRefreshing) TelegramPrimary else TelegramTextSecondary,
                                modifier = Modifier.size(23.dp)
                            )
                        }

                        // Search Icon
                        IconButton(onClick = { onSearchToggle(true) }) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = TelegramTextSecondary,
                                modifier = Modifier.size(23.dp)
                            )
                        }

                        // Overflow menu
                        Box {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "Menu",
                                    tint = TelegramTextSecondary,
                                    modifier = Modifier.size(23.dp)
                                )
                            }
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false },
                                modifier = Modifier.background(TelegramDarkBg)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Profile", color = TelegramTextPrimary) },
                                    onClick = {
                                        showMenu = false
                                        showProfileDialog = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Refresh Chats", color = TelegramTextPrimary) },
                                    onClick = {
                                        showMenu = false
                                        onRefresh()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Log Out", color = Color(0xFFFF5252)) },
                                    onClick = {
                                        showMenu = false
                                        onLogout()
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Categories pill tabs row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TelegramDarkBg)
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // "All Chats" pill with counter "336"
                CategoryPillTab(
                    title = "All Chats",
                    badge = "336",
                    isSelected = selectedCategoryTab == "All Chats",
                    onClick = { onCategoryTabSelect("All Chats") }
                )

                // "Personal" pill
                CategoryPillTab(
                    title = "Personal",
                    badge = null,
                    isSelected = selectedCategoryTab == "Personal",
                    onClick = { onCategoryTabSelect("Personal") }
                )

                // "Channels" pill
                CategoryPillTab(
                    title = "Channels",
                    badge = "42",
                    isSelected = selectedCategoryTab == "Channels",
                    onClick = { onCategoryTabSelect("Channels") }
                )

                // "Groups" pill
                CategoryPillTab(
                    title = "Groups",
                    badge = "12",
                    isSelected = selectedCategoryTab == "Groups",
                    onClick = { onCategoryTabSelect("Groups") }
                )
            }

            // Chat List Items
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                items(filteredChats, key = { it.id }) { chat ->
                    ChatListItem(
                        chat = chat,
                        onClick = { onChatClick(chat) }
                    )
                }
            }

            // Bottom Navigation Bar
            NavigationBar(
                containerColor = TelegramDarkBg,
                tonalElevation = 0.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                NavigationBarItem(
                    selected = selectedBottomNavIndex == 0,
                    onClick = { onBottomNavSelect(0) },
                    icon = {
                        Box {
                            Icon(
                                imageVector = Icons.Default.Chat,
                                contentDescription = "Chats",
                                modifier = Modifier.size(24.dp)
                            )
                            // Badge 333 on Chats
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .background(TelegramPrimary, RoundedCornerShape(10.dp))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "333",
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    },
                    label = { Text("Chats", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = TelegramPrimary,
                        selectedTextColor = TelegramPrimary,
                        indicatorColor = Color(0xFF1E2D3D),
                        unselectedIconColor = TelegramTextSecondary,
                        unselectedTextColor = TelegramTextSecondary
                    )
                )

                NavigationBarItem(
                    selected = selectedBottomNavIndex == 1,
                    onClick = { onBottomNavSelect(1) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.People,
                            contentDescription = "Contacts",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = { Text("Contacts", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = TelegramPrimary,
                        selectedTextColor = TelegramPrimary,
                        indicatorColor = Color(0xFF1E2D3D),
                        unselectedIconColor = TelegramTextSecondary,
                        unselectedTextColor = TelegramTextSecondary
                    )
                )

                NavigationBarItem(
                    selected = selectedBottomNavIndex == 2,
                    onClick = { onBottomNavSelect(2) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = { Text("Settings", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = TelegramPrimary,
                        selectedTextColor = TelegramPrimary,
                        indicatorColor = Color(0xFF1E2D3D),
                        unselectedIconColor = TelegramTextSecondary,
                        unselectedTextColor = TelegramTextSecondary
                    )
                )

                NavigationBarItem(
                    selected = selectedBottomNavIndex == 3,
                    onClick = { onBottomNavSelect(3) },
                    icon = {
                        AvatarView(
                            avatarType = AvatarType.MOTORCYCLE,
                            size = 26.dp
                        )
                    },
                    label = { Text("Profile", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = TelegramPrimary,
                        selectedTextColor = TelegramPrimary,
                        indicatorColor = Color(0xFF1E2D3D),
                        unselectedIconColor = TelegramTextSecondary,
                        unselectedTextColor = TelegramTextSecondary
                    )
                )
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = onNewChatClick,
            containerColor = TelegramPrimary,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 90.dp, end = 16.dp)
                .size(54.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "New Chat",
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun CategoryPillTab(
    title: String,
    badge: String?,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bg = if (isSelected) Color(0xFF233244) else Color(0xFF16212D)
    val textColor = if (isSelected) Color.White else TelegramTextSecondary

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(bg)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = textColor,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )

        if (badge != null) {
            Spacer(modifier = Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) TelegramPrimary else Color(0xFF293749))
                    .padding(horizontal = 6.dp, vertical = 1.dp)
            ) {
                Text(
                    text = badge,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ChatListItem(
    chat: ChatItem,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        AvatarView(
            avatarType = chat.avatarType,
            avatarResId = chat.avatarResId,
            avatarUrl = chat.avatarUrl,
            title = chat.title,
            size = 54.dp
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Text details
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 4.dp)
        ) {
            // Title and Mute icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = chat.title,
                    color = TelegramTextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                if (chat.isMuted) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.VolumeOff,
                        contentDescription = "Muted",
                        tint = TelegramTextMuted,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(3.dp))

            // Subtitle or Typing Indicator
            if (chat.isTyping) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "••• ${chat.typingUser ?: "User"} is typing",
                        color = TelegramTypingCyan,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (chat.hasDoubleCheck) {
                        Icon(
                            imageVector = Icons.Default.DoneAll,
                            contentDescription = "Read",
                            tint = TelegramCheckBlue,
                            modifier = Modifier
                                .size(16.dp)
                                .padding(end = 3.dp)
                        )
                    } else if (chat.hasSingleCheck) {
                        Icon(
                            imageVector = Icons.Default.Done,
                            contentDescription = "Sent",
                            tint = TelegramCheckBlue,
                            modifier = Modifier
                                .size(14.dp)
                                .padding(end = 3.dp)
                        )
                    }

                    Text(
                        text = chat.subtitle,
                        color = TelegramTextSecondary,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Right side: Time, Pin, Badges
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = chat.time,
                color = TelegramTextSecondary,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (chat.isPinned) {
                    Icon(
                        imageVector = Icons.Default.PushPin,
                        contentDescription = "Pinned",
                        tint = TelegramPinIcon,
                        modifier = Modifier.size(15.dp)
                    )
                }

                if (chat.hasMention) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color(0xFF26384C))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "@",
                            color = TelegramAccent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (chat.unreadCount > 0) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(TelegramUnreadBadge)
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (chat.unreadCount > 9999) "${chat.unreadCount}" else "${chat.unreadCount}",
                            color = TelegramUnreadBadgeText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
