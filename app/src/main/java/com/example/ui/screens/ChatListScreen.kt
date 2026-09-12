package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatItem
import com.example.ui.components.AvatarView
import com.example.ui.theme.LightChatListBg
import com.example.ui.theme.LightCheckBlue
import com.example.ui.theme.LightGlassBorder
import com.example.ui.theme.LightGlassHeader
import com.example.ui.theme.LightPinIcon
import com.example.ui.theme.LightPrimary
import com.example.ui.theme.LightSurface
import com.example.ui.theme.LightAccent
import com.example.ui.theme.LightTextMuted
import com.example.ui.theme.LightTextPrimary
import com.example.ui.theme.LightTextSecondary
import com.example.ui.theme.LightTypingCyan
import com.example.ui.theme.LightUnreadBadge
import com.example.ui.theme.LightUnreadBadgeText
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
    isDarkMode: Boolean = true,
    isRefreshing: Boolean = false,
    currentUserName: String? = null,
    currentUserPhone: String? = null,
    currentUserAvatar: String? = null,
    onSearchToggle: (Boolean) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onCategoryTabSelect: (String) -> Unit,
    onBottomNavSelect: (Int) -> Unit,
    onChatClick: (ChatItem) -> Unit,
    onToggleTheme: () -> Unit = {},
    onOpenSavedMessages: () -> Unit = {},
    onNewGroup: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    val filteredChats = remember(chats, searchQuery) {
        if (searchQuery.isBlank()) chats
        else chats.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
            it.subtitle.contains(searchQuery, ignoreCase = true)
        }
    }

    val listState = rememberLazyListState()

    // Pick palette based on theme
    val bgColor = if (isDarkMode) TelegramChatListBg else LightChatListBg
    val headerBg = if (isDarkMode) com.example.ui.theme.TelegramGlassHeader else LightGlassHeader
    val headerBorder = if (isDarkMode) com.example.ui.theme.TelegramGlassBorder else LightGlassBorder
    val textPrimary = if (isDarkMode) TelegramTextPrimary else LightTextPrimary
    val textSecondary = if (isDarkMode) TelegramTextSecondary else LightTextSecondary
    val textMuted = if (isDarkMode) TelegramTextMuted else LightTextMuted
    val primaryColor = if (isDarkMode) TelegramPrimary else LightPrimary
    val accentColor = if (isDarkMode) TelegramAccent else LightAccent
    val badgeColor = if (isDarkMode) TelegramUnreadBadge else LightUnreadBadge
    val badgeTextColor = if (isDarkMode) TelegramUnreadBadgeText else LightUnreadBadgeText
    val pinColor = if (isDarkMode) TelegramPinIcon else LightPinIcon
    val checkBlue = if (isDarkMode) TelegramCheckBlue else LightCheckBlue
    val typingCyan = if (isDarkMode) TelegramTypingCyan else LightTypingCyan
    val indicatorColor = if (isDarkMode) Color(0xFF1E2D3D) else Color(0xFFD6E9F7)
    val menuBg = if (isDarkMode) TelegramDarkBg else LightSurface

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top App Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(headerBg)
                    .border(0.8.dp, headerBorder)
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
                            placeholder = { Text("Search chats...", color = textMuted) },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedTextColor = textPrimary,
                                unfocusedTextColor = textPrimary
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { onSearchToggle(false) }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close search",
                                tint = textPrimary
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Title — just "connected" in place of the username
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isRefreshing) "Updating..." else "connected",
                                color = textPrimary,
                                fontSize = 19.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Search Icon
                        IconButton(onClick = { onSearchToggle(true) }) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = textSecondary,
                                modifier = Modifier.size(23.dp)
                            )
                        }

                        // Overflow menu — 3-dot
                        Box {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "Menu",
                                    tint = textSecondary,
                                    modifier = Modifier.size(23.dp)
                                )
                            }
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false },
                                modifier = Modifier.background(menuBg)
                            ) {
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = if (isDarkMode) "Light Mode" else "Dark Mode",
                                            color = textPrimary
                                        )
                                    },
                                    onClick = {
                                        showMenu = false
                                        onToggleTheme()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("New Group", color = textPrimary) },
                                    onClick = {
                                        showMenu = false
                                        onNewGroup()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Saved Messages", color = textPrimary) },
                                    onClick = {
                                        showMenu = false
                                        onOpenSavedMessages()
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
                    .background(headerBg)
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // "All Chats" pill with counter "336"
                CategoryPillTab(
                    title = "All Chats",
                    badge = "336",
                    isSelected = selectedCategoryTab == "All Chats",
                    isDarkMode = isDarkMode,
                    onClick = { onCategoryTabSelect("All Chats") }
                )

                // "Personal" pill
                CategoryPillTab(
                    title = "Personal",
                    badge = null,
                    isSelected = selectedCategoryTab == "Personal",
                    isDarkMode = isDarkMode,
                    onClick = { onCategoryTabSelect("Personal") }
                )

                // "Channels" pill
                CategoryPillTab(
                    title = "Channels",
                    badge = "42",
                    isSelected = selectedCategoryTab == "Channels",
                    isDarkMode = isDarkMode,
                    onClick = { onCategoryTabSelect("Channels") }
                )

                // "Groups" pill
                CategoryPillTab(
                    title = "Groups",
                    badge = "12",
                    isSelected = selectedCategoryTab == "Groups",
                    isDarkMode = isDarkMode,
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
                        isDarkMode = isDarkMode,
                        onClick = { onChatClick(chat) }
                    )
                }
            }

            // Bottom Navigation Bar
            NavigationBar(
                containerColor = headerBg,
                tonalElevation = 0.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(0.8.dp, headerBorder)
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
                            // Real unread badge = total of all chat unread counts
                            val totalUnread = chats.sumOf { it.unreadCount }
                            if (totalUnread > 0) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .background(badgeColor, RoundedCornerShape(10.dp))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = if (totalUnread > 999) "999+" else totalUnread.toString(),
                                        color = badgeTextColor,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    },
                    label = { Text("Chats", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = primaryColor,
                        selectedTextColor = primaryColor,
                        indicatorColor = indicatorColor,
                        unselectedIconColor = textSecondary,
                        unselectedTextColor = textSecondary
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
                        selectedIconColor = primaryColor,
                        selectedTextColor = primaryColor,
                        indicatorColor = indicatorColor,
                        unselectedIconColor = textSecondary,
                        unselectedTextColor = textSecondary
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
                        selectedIconColor = primaryColor,
                        selectedTextColor = primaryColor,
                        indicatorColor = indicatorColor,
                        unselectedIconColor = textSecondary,
                        unselectedTextColor = textSecondary
                    )
                )

                NavigationBarItem(
                    selected = selectedBottomNavIndex == 3,
                    onClick = { onBottomNavSelect(3) },
                    icon = {
                        AvatarView(
                            avatarUrl = currentUserAvatar,
                            title = currentUserName,
                            size = 26.dp
                        )
                    },
                    label = { Text("Profile", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = primaryColor,
                        selectedTextColor = primaryColor,
                        indicatorColor = indicatorColor,
                        unselectedIconColor = textSecondary,
                        unselectedTextColor = textSecondary
                    )
                )
            }
        }

    }
}

@Composable
private fun CategoryPillTab(
    title: String,
    badge: String?,
    isSelected: Boolean,
    isDarkMode: Boolean = true,
    onClick: () -> Unit
) {
    val pillShape = RoundedCornerShape(20.dp)
    val bgBrush = if (isSelected) {
        androidx.compose.ui.graphics.Brush.horizontalGradient(
            listOf(Color(0xFF2B5C87), Color(0xFF1D4262))
        )
    } else {
        if (isDarkMode) {
            androidx.compose.ui.graphics.Brush.horizontalGradient(
                listOf(Color(0xA6182534), Color(0x80121C26))
            )
        } else {
            androidx.compose.ui.graphics.Brush.horizontalGradient(
                listOf(Color(0x0D000000), Color(0x08000000))
            )
        }
    }
    val borderStroke = if (isSelected) Color(0x6652B8FF) else if (isDarkMode) Color(0x24FFFFFF) else Color(0x14000000)
    val textColor = if (isSelected) Color.White else if (isDarkMode) TelegramTextSecondary else LightTextSecondary
    val badgeBg = if (isSelected) (if (isDarkMode) TelegramPrimary else LightPrimary) else if (isDarkMode) Color(0xFF293749) else Color(0xFFD6DEE5)

    Row(
        modifier = Modifier
            .shadow(if (isSelected) 3.dp else 0.dp, pillShape, spotColor = Color(0x552481CC))
            .clip(pillShape)
            .background(bgBrush)
            .border(0.85.dp, borderStroke, pillShape)
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
                    .background(badgeBg)
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
    isDarkMode: Boolean = true,
    onClick: () -> Unit
) {
    val textPrimary = if (isDarkMode) TelegramTextPrimary else LightTextPrimary
    val textSecondary = if (isDarkMode) TelegramTextSecondary else LightTextSecondary
    val textMuted = if (isDarkMode) TelegramTextMuted else LightTextMuted
    val checkBlue = if (isDarkMode) TelegramCheckBlue else LightCheckBlue
    val typingCyan = if (isDarkMode) TelegramTypingCyan else LightTypingCyan
    val pinColor = if (isDarkMode) TelegramPinIcon else LightPinIcon
    val badgeColor = if (isDarkMode) TelegramUnreadBadge else LightUnreadBadge
    val badgeTextColor = if (isDarkMode) TelegramUnreadBadgeText else LightUnreadBadgeText
    val accentColor = if (isDarkMode) TelegramAccent else LightAccent
    val mentionBg = if (isDarkMode) Color(0xFF26384C) else Color(0xFFD6E9F7)

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
                    color = textPrimary,
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
                        tint = textMuted,
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
                        color = typingCyan,
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
                            tint = checkBlue,
                            modifier = Modifier
                                .size(16.dp)
                                .padding(end = 3.dp)
                        )
                    } else if (chat.hasSingleCheck) {
                        Icon(
                            imageVector = Icons.Default.Done,
                            contentDescription = "Sent",
                            tint = checkBlue,
                            modifier = Modifier
                                .size(14.dp)
                                .padding(end = 3.dp)
                        )
                    }

                    Text(
                        text = chat.subtitle,
                        color = textSecondary,
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
                color = textSecondary,
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
                        tint = pinColor,
                        modifier = Modifier.size(15.dp)
                    )
                }

                if (chat.hasMention) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(mentionBg)
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "@",
                            color = accentColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (chat.unreadCount > 0) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(badgeColor)
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (chat.unreadCount > 9999) "${chat.unreadCount}" else "${chat.unreadCount}",
                            color = badgeTextColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
