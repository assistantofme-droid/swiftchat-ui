package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.HorizontalDivider
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
import com.example.ui.components.AvatarView
import com.example.ui.components.TelegramFloatingBottomBar
import com.example.ui.theme.TelegramDarkBg
import com.example.ui.theme.TelegramPrimary
import com.example.ui.theme.TelegramTextMuted
import com.example.ui.theme.TelegramTextPrimary
import com.example.ui.theme.TelegramTextSecondary

@Composable
fun SettingsScreen(
    currentUserName: String?,
    currentUserPhone: String?,
    currentUserAvatar: String?,
    onBottomNavSelect: (Int) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        bottomBar = {
            TelegramFloatingBottomBar(
                selectedIndex = 2,
                onSelectTab = onBottomNavSelect,
                userAvatarUrl = currentUserAvatar,
                userName = currentUserName
            )
        },
        containerColor = TelegramDarkBg,
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
        ) {
            // Header: Settings + Search + More
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Settings",
                    color = TelegramTextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Row {
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = TelegramTextSecondary
                        )
                    }
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More",
                            tint = TelegramTextSecondary
                        )
                    }
                }
            }

            // User Info Banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0x33202E3D))
                    .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(20.dp))
                    .clickable { onBottomNavSelect(3) }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                ) {
                    AvatarView(
                        avatarUrl = currentUserAvatar,
                        title = currentUserName ?: "Me",
                        size = 54.dp
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = currentUserName ?: "Telegram User",
                        color = TelegramTextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = currentUserPhone ?: "+98 912 345 6789",
                        color = TelegramTextSecondary,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "@username",
                        color = TelegramPrimary,
                        fontSize = 12.sp
                    )
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Go",
                    tint = TelegramTextMuted
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Settings Group 1
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0x33202E3D))
                    .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(20.dp))
            ) {
                SettingsItem(
                    icon = Icons.Default.Person,
                    iconBg = Color(0xFF2481CC),
                    title = "Account",
                    subtitle = "Tap to change phone number or username",
                    onClick = {}
                )
                HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                SettingsItem(
                    icon = Icons.AutoMirrored.Filled.Chat,
                    iconBg = Color(0xFFFFA000),
                    title = "Chat Settings",
                    subtitle = "Wallpaper, animations, text size",
                    onClick = {}
                )
                HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                SettingsItem(
                    icon = Icons.Default.Lock,
                    iconBg = Color(0xFF4CAF50),
                    title = "Privacy and Security",
                    subtitle = "Passcode, 2-Step Verification",
                    onClick = {}
                )
                HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                SettingsItem(
                    icon = Icons.Default.Notifications,
                    iconBg = Color(0xFFF44336),
                    title = "Notifications and Sounds",
                    subtitle = "Message sounds, badge count",
                    onClick = {}
                )
                HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                SettingsItem(
                    icon = Icons.Default.Storage,
                    iconBg = Color(0xFF00ACC1),
                    title = "Data and Storage",
                    subtitle = "Network usage, auto-download",
                    onClick = {}
                )
                HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                SettingsItem(
                    icon = Icons.Default.Folder,
                    iconBg = Color(0xFF29B6F6),
                    title = "Chat Folders",
                    subtitle = "Organize chats into tabs",
                    onClick = {}
                )
                HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                SettingsItem(
                    icon = Icons.Default.Devices,
                    iconBg = Color(0xFF26A69A),
                    title = "Devices",
                    subtitle = "Manage active sessions",
                    onClick = {}
                )
                HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                SettingsItem(
                    icon = Icons.Default.BatteryChargingFull,
                    iconBg = Color(0xFFFF7043),
                    title = "Power Saving",
                    subtitle = "Optimize background consumption",
                    onClick = {}
                )
                HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                SettingsItem(
                    icon = Icons.Default.Language,
                    iconBg = Color(0xFFAB47BC),
                    title = "Language",
                    subtitle = "English",
                    onClick = {}
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Help",
                color = TelegramPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)
            )

            // Settings Group 2 (Help)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0x33202E3D))
                    .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(20.dp))
            ) {
                SettingsItem(
                    icon = Icons.Default.QuestionAnswer,
                    iconBg = Color(0xFF42A5F5),
                    title = "Ask a Question",
                    onClick = {}
                )
                HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                SettingsItem(
                    icon = Icons.Default.Lightbulb,
                    iconBg = Color(0xFFFFB300),
                    title = "Telegram Features",
                    onClick = {}
                )
                HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                SettingsItem(
                    icon = Icons.AutoMirrored.Filled.Help,
                    iconBg = Color(0xFF66BB6A),
                    title = "Log Out",
                    titleColor = Color(0xFFFF5252),
                    onClick = onLogout
                )
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    iconBg: Color,
    title: String,
    subtitle: String? = null,
    titleColor: Color = TelegramTextPrimary,
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
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = titleColor,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    color = TelegramTextSecondary,
                    fontSize = 12.sp
                )
            }
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "Next",
            tint = TelegramTextMuted,
            modifier = Modifier.size(20.dp)
        )
    }
}
