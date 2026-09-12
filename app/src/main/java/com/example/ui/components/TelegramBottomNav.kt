package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AvatarType
import com.example.ui.theme.appPalette

/**
 * Floating glass bottom navigation — detached from the screen edge with
 * rounded pill shape, translucent glassy background, and subtle border.
 * Used by ChatList / Contacts / Settings / Profile screens.
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
    val palette = appPalette

    Surface(
        color = palette.glassHeader.copy(alpha = 0.85f),
        shape = RoundedCornerShape(28.dp),
        shadowElevation = 12.dp,
        tonalElevation = 0.dp,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .border(1.dp, palette.glassBorder, RoundedCornerShape(28.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Chats
            FloatingNavItem(
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
                                    .background(palette.primary, RoundedCornerShape(10.dp))
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
                label = "Chats",
                isSelected = selectedIndex == 0,
                onClick = { onSelect(0) }
            )

            // Contacts
            FloatingNavItem(
                icon = {
                    Icon(
                        imageVector = Icons.Default.People,
                        contentDescription = "Contacts",
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = "Contacts",
                isSelected = selectedIndex == 1,
                onClick = { onSelect(1) }
            )

            // Settings
            FloatingNavItem(
                icon = {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = "Settings",
                isSelected = selectedIndex == 2,
                onClick = { onSelect(2) }
            )

            // Profile
            FloatingNavItem(
                icon = {
                    AvatarView(
                        avatarType = AvatarType.MOTORCYCLE,
                        avatarUrl = currentUserAvatarUrl,
                        size = 26.dp
                    )
                },
                label = "Profile",
                isSelected = selectedIndex == 3,
                onClick = { onSelect(3) }
            )
        }
    }
}

@Composable
private fun FloatingNavItem(
    icon: @Composable () -> Unit,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val palette = appPalette
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) palette.primary.copy(alpha = 0.15f) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            icon()
            Text(
                text = label,
                color = if (isSelected) palette.primary else palette.textSecondary,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
            )
        }
    }
}
