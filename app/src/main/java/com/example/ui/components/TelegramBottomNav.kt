package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AvatarType
import com.example.ui.theme.TelegramGlassBorder
import com.example.ui.theme.TelegramGlassHeader
import com.example.ui.theme.TelegramPrimary
import com.example.ui.theme.TelegramTextSecondary

/**
 * Shared 4-tab bottom navigation used by ChatList / Contacts / Settings / Profile screens.
 * Keeps the active-tab indicator + Chats unread badge consistent across the app.
 *
 * Tabs: 0=Chats, 1=Contacts, 2=Settings, 3=Profile
 */
@Composable
fun TelegramBottomNav(
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    chatsBadgeCount: Int? = null,
    currentUserAvatarUrl: String? = null,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        containerColor = TelegramGlassHeader,
        tonalElevation = 0.dp,
        modifier = modifier
            .border(0.8.dp, TelegramGlassBorder)
    ) {
        NavigationBarItem(
            selected = selectedIndex == 0,
            onClick = { onSelect(0) },
            icon = {
                Box {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Chat,
                        contentDescription = "Chats",
                        modifier = Modifier.size(24.dp)
                    )
                    chatsBadgeCount?.takeIf { it > 0 }?.let { count ->
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .background(TelegramPrimary, RoundedCornerShape(10.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = if (count > 999) "999+" else count.toString(),
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            },
            label = { Text("Chats", fontSize = 11.sp) },
            colors = navItemColors()
        )

        NavigationBarItem(
            selected = selectedIndex == 1,
            onClick = { onSelect(1) },
            icon = {
                Icon(
                    imageVector = Icons.Default.People,
                    contentDescription = "Contacts",
                    modifier = Modifier.size(24.dp)
                )
            },
            label = { Text("Contacts", fontSize = 11.sp) },
            colors = navItemColors()
        )

        NavigationBarItem(
            selected = selectedIndex == 2,
            onClick = { onSelect(2) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    modifier = Modifier.size(24.dp)
                )
            },
            label = { Text("Settings", fontSize = 11.sp) },
            colors = navItemColors()
        )

        NavigationBarItem(
            selected = selectedIndex == 3,
            onClick = { onSelect(3) },
            icon = {
                AvatarView(
                    avatarType = AvatarType.MOTORCYCLE,
                    avatarUrl = currentUserAvatarUrl,
                    size = 26.dp
                )
            },
            label = { Text("Profile", fontSize = 11.sp) },
            colors = navItemColors()
        )
    }
}

@Composable
private fun navItemColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = TelegramPrimary,
    selectedTextColor = TelegramPrimary,
    indicatorColor = Color(0xFF1E2D3D),
    unselectedIconColor = TelegramTextSecondary,
    unselectedTextColor = TelegramTextSecondary
)
