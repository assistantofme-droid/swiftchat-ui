package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.TelegramPrimary
import com.example.ui.theme.TelegramTextSecondary

@Composable
fun TelegramFloatingBottomBar(
    selectedIndex: Int,
    onSelectTab: (Int) -> Unit,
    userAvatarUrl: String? = null,
    userName: String? = null,
    chatUnreadCount: String = "337",
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(12.dp, RoundedCornerShape(32.dp), spotColor = Color(0x73000000))
                .clip(RoundedCornerShape(32.dp))
                .background(Color(0xEC15202C))
                .border(1.dp, Color(0x3352B8FF), RoundedCornerShape(32.dp))
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Chats Tab
            BottomBarItem(
                title = "Chats",
                icon = Icons.Default.Chat,
                isSelected = selectedIndex == 0,
                badge = chatUnreadCount,
                onClick = { onSelectTab(0) }
            )

            // 2. Contacts Tab
            BottomBarItem(
                title = "Contacts",
                icon = Icons.Default.Person,
                isSelected = selectedIndex == 1,
                onClick = { onSelectTab(1) }
            )

            // 3. Settings Tab
            BottomBarItem(
                title = "Settings",
                icon = Icons.Default.Settings,
                isSelected = selectedIndex == 2,
                onClick = { onSelectTab(2) }
            )

            // 4. Profile Tab (with mini avatar)
            ProfileBottomBarItem(
                title = "Profile",
                isSelected = selectedIndex == 3,
                avatarUrl = userAvatarUrl,
                userName = userName,
                onClick = { onSelectTab(3) }
            )
        }
    }
}

@Composable
private fun BottomBarItem(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    badge: String? = null,
    onClick: () -> Unit
) {
    val pillShape = RoundedCornerShape(20.dp)
    val bg = if (isSelected) Color(0x332481CC) else Color.Transparent
    val tint = if (isSelected) TelegramPrimary else TelegramTextSecondary

    Box(
        modifier = Modifier
            .clip(pillShape)
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = tint,
                    modifier = Modifier.size(24.dp)
                )
                if (badge != null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(start = 12.dp, bottom = 8.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(TelegramPrimary)
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = badge,
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = title,
                color = tint,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@Composable
private fun ProfileBottomBarItem(
    title: String,
    isSelected: Boolean,
    avatarUrl: String?,
    userName: String?,
    onClick: () -> Unit
) {
    val pillShape = RoundedCornerShape(20.dp)
    val bg = if (isSelected) Color(0x332481CC) else Color.Transparent
    val tint = if (isSelected) TelegramPrimary else TelegramTextSecondary

    Box(
        modifier = Modifier
            .clip(pillShape)
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .border(
                        if (isSelected) 1.5.dp else 1.dp,
                        if (isSelected) TelegramPrimary else Color(0x44FFFFFF),
                        CircleShape
                    )
            ) {
                AvatarView(
                    avatarUrl = avatarUrl,
                    title = userName ?: "Me",
                    size = 24.dp
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = title,
                color = tint,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}
