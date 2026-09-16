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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.ui.components.AvatarView
import com.example.ui.components.TelegramBottomNav
import com.example.ui.locale.LocalAppStrings
import com.example.ui.theme.appPalette

@Composable
fun ChatListScreen(
    chats: List<ChatItem>,
    selectedCategoryTab: Int,
    selectedBottomNavIndex: Int,
    isSearching: Boolean,
    searchQuery: String,
    isDarkMode: Boolean,
    isRefreshing: Boolean,
    currentUserName: String?,
    currentUserPhone: String?,
    currentUserAvatar: String?,
    folders: List<ChatFolder>,
    onSearchToggle: (Boolean) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onCategoryTabSelect: (Int) -> Unit,
    onBottomNavSelect: (Int) -> Unit,
    onChatClick: (ChatItem) -> Unit,
    onToggleTheme: () -> Unit,
    onOpenSavedMessages: () -> Unit,
    onNewGroup: () -> Unit
) {
    val palette = appPalette
    val strings = LocalAppStrings.current

    val tabs = remember(strings) {
        listOf(strings.allChats, strings.privateChats, strings.groups, strings.channels)
    }

    val filteredChats = remember(chats, selectedCategoryTab, searchQuery) {
        var list = when (selectedCategoryTab) {
            1 -> chats.filter { !it.isGroup && !it.isChannel }
            2 -> chats.filter { it.isGroup }
            3 -> chats.filter { it.isChannel }
            else -> chats
        }
        if (searchQuery.isNotBlank()) {
            list = list.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                it.subtitle.contains(searchQuery, ignoreCase = true)
            }
        }
        list
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.chatListBg)
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar
            if (isSearching) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        placeholder = { Text(strings.searchPlaceholder, color = palette.textSecondary) },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = palette.textSecondary)
                        },
                        trailingIcon = {
                            IconButton(onClick = { onSearchToggle(false); onSearchQueryChange("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = palette.textSecondary)
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = palette.surfaceVariant.copy(alpha = 0.5f),
                            unfocusedContainerColor = palette.surfaceVariant.copy(alpha = 0.5f),
                            focusedBorderColor = palette.primary,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = palette.textPrimary,
                            unfocusedTextColor = palette.textPrimary
                        )
                    )
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isRefreshing) "Updating..." else "connected",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = palette.textPrimary,
                        modifier = Modifier.weight(1f)
                    )

                    if (isRefreshing) {
                        CircularProgressIndicator(
                            color = palette.primary,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    }

                    IconButton(onClick = { onSearchToggle(true) }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = palette.textSecondary,
                            modifier = Modifier.size(23.dp)
                        )
                    }

                    var showMenu by remember { mutableStateOf(false) }
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Menu",
                                tint = palette.textSecondary,
                                modifier = Modifier.size(23.dp)
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            modifier = Modifier.background(palette.surface)
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = if (isDarkMode) "Light Mode" else "Dark Mode",
                                        color = palette.textPrimary
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    onToggleTheme()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("New Group", color = palette.textPrimary) },
                                onClick = {
                                    showMenu = false
                                    onNewGroup()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Saved Messages", color = palette.textPrimary) },
                                onClick = {
                                    showMenu = false
                                    onOpenSavedMessages()
                                }
                            )
                        }
                    }
                }
            }

            // Category Tab Row
            ScrollableTabRow(
                selectedTabIndex = selectedCategoryTab.coerceIn(0, tabs.lastIndex),
                containerColor = Color.Transparent,
                contentColor = palette.primary,
                edgePadding = 16.dp,
                indicator = { tabPositions ->
                    if (selectedCategoryTab in tabPositions.indices) {
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedCategoryTab]),
                            color = palette.primary
                        )
                    }
                },
                divider = {}
            ) {
                tabs.forEachIndexed { index, title ->
                    val isSelected = selectedCategoryTab == index
                    Tab(
                        selected = isSelected,
                        onClick = { onCategoryTabSelect(index) },
                        text = {
                            Text(
                                text = title,
                                color = if (isSelected) palette.primary else palette.textSecondary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 14.sp
                            )
                        }
                    )
                }
            }

            // Chat items list
            if (filteredChats.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bookmark,
                            contentDescription = null,
                            tint = palette.primary.copy(alpha = 0.6f),
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = strings.noChatsYet,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = palette.textPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = strings.startMessaging,
                            fontSize = 14.sp,
                            color = palette.primary,
                            modifier = Modifier
                                .clickable { onBottomNavSelect(1) } // Navigate to contacts
                                .padding(8.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(filteredChats, key = { it.id }) { chat ->
                        ChatItemRow(
                            chat = chat,
                            onClick = { onChatClick(chat) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(88.dp))
                    }
                }
            }
        }

        // Bottom Navigation
        TelegramBottomNav(
            selectedIndex = selectedBottomNavIndex,
            onSelect = onBottomNavSelect,
            currentUserAvatarUrl = currentUserAvatar,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
        )
    }
}

@Composable
private fun ChatItemRow(
    chat: ChatItem,
    onClick: () -> Unit
) {
    val palette = appPalette

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        Box(contentAlignment = Alignment.Center) {
            if (!chat.avatarUrl.isNullOrBlank()) {
                AsyncImage(
                    model = chat.avatarUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                AvatarView(
                    avatarType = chat.avatarType,
                    size = 52.dp
                )
            }

            if (chat.isOnline) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(Color(0xFF22C55E), CircleShape)
                        .align(Alignment.BottomEnd)
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        // Title and Subtitle
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    if (chat.isGroup) {
                        Icon(
                            Icons.Default.Group,
                            contentDescription = null,
                            tint = palette.primary,
                            modifier = Modifier
                                .size(16.dp)
                                .padding(end = 4.dp)
                        )
                    }
                    Text(
                        text = chat.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = palette.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text(
                    text = chat.time,
                    fontSize = 12.sp,
                    color = palette.textSecondary,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (chat.isTyping) "typing..." else chat.subtitle,
                    fontSize = 14.sp,
                    color = if (chat.isTyping) palette.primary else palette.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                if (chat.unreadCount > 0) {
                    Box(
                        modifier = Modifier
                            .background(
                                if (chat.isMuted) palette.textSecondary else palette.primary,
                                RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 7.dp, vertical = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = chat.unreadCount.toString(),
                            fontSize = 11.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else if (chat.isMuted) {
                    Icon(
                        Icons.Default.VolumeOff,
                        contentDescription = "Muted",
                        tint = palette.textMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
