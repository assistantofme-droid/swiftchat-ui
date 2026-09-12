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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.api.ApiClient
import com.example.data.api.ApiUser
import com.example.ui.components.AvatarView
import com.example.ui.components.TelegramBottomNav
import com.example.ui.theme.TelegramChatListBg
import com.example.ui.theme.TelegramPrimary
import com.example.ui.theme.TelegramSurface
import com.example.ui.theme.TelegramSurfaceVariant
import com.example.ui.theme.TelegramTextMuted
import com.example.ui.theme.TelegramTextPrimary
import com.example.ui.theme.TelegramTextSecondary

/**
 * Settings screen — top row shows the current user's info from /auth/me,
 * below that are category rows. Categories without a corresponding API are
 * pure UI stubs (clickable but no navigation target yet); the Account row
 * shows real phone / username / bio pulled from the user object.
 */
@Composable
fun SettingsScreen(
    meUser: ApiUser?,
    currentUserAvatarUrl: String?,
    selectedBottomNavIndex: Int = 2,
    onSelectBottomNav: (Int) -> Unit,
    onLogout: () -> Unit
) {
    var featuresEnabled by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = TelegramChatListBg,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TelegramChatListBg)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Settings",
                    color = TelegramTextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { /* search settings — future */ }) {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = TelegramTextSecondary)
                }
                IconButton(onClick = { /* overflow menu — future */ }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More", tint = TelegramTextSecondary)
                }
            }
        },
        bottomBar = {
            TelegramBottomNav(
                selectedIndex = selectedBottomNavIndex,
                onSelect = onSelectBottomNav,
                currentUserAvatarUrl = currentUserAvatarUrl,
                modifier = Modifier.fillMaxWidth()
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 12.dp)
        ) {
            // === Main settings card ===
            item {
                SettingsCard {
                    SettingsItem(
                        icon = Icons.Default.Person,
                        iconColor = Color(0xFF5DADE2),
                        title = "Account",
                        subtitle = buildAccountSubtitle(meUser),
                        onClick = { /* future: navigate to account detail */ }
                    )
                    SettingsItem(
                        icon = Icons.Default.Chat,
                        iconColor = Color(0xFFF39C12),
                        title = "Chat Settings",
                        subtitle = "Wallpaper, Night Mode, Animations",
                        onClick = {}
                    )
                    SettingsItem(
                        icon = Icons.Default.Lock,
                        iconColor = Color(0xFF58D68D),
                        title = "Privacy & Security",
                        subtitle = "Last Seen, Devices, Passkeys",
                        onClick = {}
                    )
                    SettingsItem(
                        icon = Icons.Default.Notifications,
                        iconColor = Color(0xFFEC7063),
                        title = "Notifications",
                        subtitle = "Sounds, Calls, Badges",
                        onClick = {}
                    )
                    SettingsItem(
                        icon = Icons.Default.Storage,
                        iconColor = Color(0xFF7FB3D5),
                        title = "Data and Storage",
                        subtitle = "Media download settings",
                        onClick = {}
                    )
                    SettingsItem(
                        icon = Icons.Default.Folder,
                        iconColor = Color(0xFF5499C7),
                        title = "Chat Folders",
                        subtitle = "Sort chats into folders",
                        onClick = {}
                    )
                    SettingsItem(
                        icon = Icons.Default.Devices,
                        iconColor = Color(0xFF48C9B0),
                        title = "Devices",
                        subtitle = "Manage connected devices",
                        onClick = {}
                    )
                    SettingsItem(
                        icon = Icons.Default.BatteryChargingFull,
                        iconColor = Color(0xFFEB984E),
                        title = "Power Saving",
                        subtitle = "Reduce power usage on low charge",
                        onClick = {}
                    )
                    SettingsItem(
                        icon = Icons.Default.Language,
                        iconColor = Color(0xFFBB8FCE),
                        title = "Language",
                        subtitle = "English",
                        onClick = {}
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }

            // === Help card ===
            item {
                SettingsCard {
                    SectionHeader("Help")
                    SettingsItem(
                        icon = Icons.Default.QuestionAnswer,
                        iconColor = Color(0xFFF39C12),
                        title = "Ask a Question",
                        subtitle = null,
                        onClick = {}
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }

            // === Features toggle ===
            item {
                SettingsCard {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = TelegramTextMuted,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.size(16.dp))
                        Text(
                            text = "7eve9Chat Features",
                            color = TelegramTextSecondary,
                            fontSize = 15.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked = featuresEnabled,
                            onCheckedChange = { featuresEnabled = it }
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }

            // === Logout ===
            item {
                SettingsCard {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onLogout() }
                            .padding(horizontal = 16.dp, vertical = 18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Log Out",
                            color = Color(0xFFEC7063),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Surface(
        color = TelegramSurface,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
    ) {
        Column { content() }
    }
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String?,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconColor.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.size(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = TelegramTextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            if (!subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    color = TelegramTextSecondary,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        color = TelegramPrimary,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 4.dp)
    )
}

/**
 * Build the subtitle for the "Account" row using real data from /auth/me.
 * Shows whichever fields are available: phone, @username, bio.
 */
private fun buildAccountSubtitle(me: ApiUser?): String {
    if (me == null) return "Loading…"
    val parts = mutableListOf<String>()
    me.phone?.takeIf { it.isNotBlank() }?.let { parts.add(it) }
    me.username?.takeIf { it.isNotBlank() }?.let { parts.add("@$it") }
    me.bio?.takeIf { it.isNotBlank() }?.let { parts.add(it) }
    return if (parts.isEmpty()) "Tap to set up your account" else parts.joinToString(", ")
}
