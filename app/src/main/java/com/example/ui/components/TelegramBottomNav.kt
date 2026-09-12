package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AvatarType
import com.example.ui.theme.appPalette

/**
 * Floating glass bottom navigation bar.
 *
 * Designed to be placed as a floating overlay (NOT in a Scaffold bottomBar slot).
 * It's a self-contained rounded pill with translucent glassy background, subtle
 * border, and shadow elevation. The caller should wrap their screen content in
 * a Box and place this at BottomCenter alignment with navigationBarsPadding.
 *
 * Tabs: 0=Chats, 1=Contacts, 2=Settings, 3=Profile
 *
 * Usage in a screen:
 *   Box(Modifier.fillMaxSize()) {
 *       // screen content here
 *       TelegramBottomNav(
 *           selectedIndex = ...,
 *           onSelect = ...,
 *           modifier = Modifier
 *               .align(Alignment.BottomCenter)
 *               .navigationBarsPadding()
 *       )
 *   }
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
    val strings = com.example.ui.locale.LocalAppStrings.current

    Surface(
        color = palette.glassHeader.copy(alpha = 0.92f),
        shape = RoundedCornerShape(28.dp),
        shadowElevation = 12.dp,
        tonalElevation = 0.dp,
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Chats
            FloatingNavItem(
                icon = {
                    Box {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Chat,
                            contentDescription = strings.navChats,
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
                label = strings.navChats,
                isSelected = selectedIndex == 0,
                onClick = { onSelect(0) }
            )

            // Contacts
            FloatingNavItem(
                icon = {
                    Icon(
                        imageVector = Icons.Default.People,
                        contentDescription = strings.navContacts,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = strings.navContacts,
                isSelected = selectedIndex == 1,
                onClick = { onSelect(1) }
            )

            // Settings
            FloatingNavItem(
                icon = {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = strings.navSettings,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = strings.navSettings,
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
                label = strings.navProfile,
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
