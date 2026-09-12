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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.api.ApiUser
import com.example.ui.theme.appPalette
import com.example.ui.components.TelegramBottomNav
object SettingsRoute {
    const val PRIVACY = "privacy"
    const val CHAT = "chat"
    const val DATA = "data"
    const val FOLDERS = "folders"
    const val DEVICES = "devices"
    const val POWER = "power"
    const val LANGUAGE = "language"
}
@Composable
fun SettingsScreen(
    meUser: ApiUser?,
    currentUserAvatarUrl: String?,
    selectedBottomNavIndex: Int = 2,
    onSelectBottomNav: (Int) -> Unit,
    onLogout: () -> Unit,
    onOpenRoute: (String) -> Unit,
    onAskQuestion: () -> Unit
) {
    Scaffold(
        containerColor = appPalette.chatListBg,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(appPalette.chatListBg)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Settings",
                    color = appPalette.textPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { /* search settings — future */ }) {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = appPalette.textSecondary)
                }
                IconButton(onClick = { /* overflow menu — future */ }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More", tint = appPalette.textSecondary)
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
                        onClick = { /* opens profile screen — user can edit there */ }
                    )
                        icon = Icons.Default.Chat,
                        iconColor = Color(0xFFF39C12),
                        title = "Chat Settings",
                        subtitle = "Wallpaper, Theme, Animations, Reactions",
                        onClick = { onOpenRoute(SettingsRoute.CHAT) }
                        icon = Icons.Default.Lock,
                        iconColor = Color(0xFF58D68D),
                        title = "Privacy & Security",
                        subtitle = "Last Seen, Phone, Devices",
                        onClick = { onOpenRoute(SettingsRoute.PRIVACY) }
                        icon = Icons.Default.Storage,
                        iconColor = Color(0xFF7FB3D5),
                        title = "Data and Storage",
                        subtitle = "Cache, Auto-download, Gallery",
                        onClick = { onOpenRoute(SettingsRoute.DATA) }
                        icon = Icons.Default.Folder,
                        iconColor = Color(0xFF5499C7),
                        title = "Chat Folders",
                        subtitle = "Organize chats into folders",
                        onClick = { onOpenRoute(SettingsRoute.FOLDERS) }
                        icon = Icons.Default.Devices,
                        iconColor = Color(0xFF48C9B0),
                        title = "Devices",
                        subtitle = "Active sessions, Log out all",
                        onClick = { onOpenRoute(SettingsRoute.DEVICES) }
                        icon = Icons.Default.BatteryChargingFull,
                        iconColor = Color(0xFFEB984E),
                        title = "Power Saving",
                        subtitle = "Reduce battery usage",
                        onClick = { onOpenRoute(SettingsRoute.POWER) }
                        icon = Icons.Default.Language,
                        iconColor = Color(0xFFBB8FCE),
                        title = "Language",
                        subtitle = if (meUser != null) "Persian / English" else "Select language",
                        onClick = { onOpenRoute(SettingsRoute.LANGUAGE) }
            item { Spacer(modifier = Modifier.height(12.dp)) }
            // === Help card ===
                    SectionHeader("Help")
                        icon = Icons.Default.QuestionAnswer,
                        title = "Ask a Question",
                        subtitle = "Chat with @7eve9 support",
                        onClick = onAskQuestion
            item { Spacer(modifier = Modifier.height(20.dp)) }
            // === Logout ===
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
            item { Spacer(modifier = Modifier.height(80.dp)) }
    }
/**
 * Build the subtitle for the "Account" row using real data from /auth/me.
 * Shows whichever fields are available: phone, @username, bio.
 */
internal fun buildAccountSubtitle(me: ApiUser?): String {
    if (me == null) return "Loading…"
    val parts = mutableListOf<String>()
    me.phone?.takeIf { it.isNotBlank() }?.let { parts.add(it) }
    me.username?.takeIf { it.isNotBlank() }?.let { parts.add("@$it") }
    me.bio?.takeIf { it.isNotBlank() }?.let { parts.add(it) }
    return if (parts.isEmpty()) "Tap to set up your account" else parts.joinToString(", ")
