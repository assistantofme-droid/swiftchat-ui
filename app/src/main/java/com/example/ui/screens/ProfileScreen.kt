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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
fun ProfileScreen(
    currentUserName: String?,
    currentUserPhone: String?,
    currentUserAvatar: String?,
    currentUserBio: String?,
    onBottomNavSelect: (Int) -> Unit,
    onEditProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        bottomBar = {
            TelegramFloatingBottomBar(
                selectedIndex = 3,
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
            // Header: 4-dot menu + More Vert
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.Default.GridView,
                        contentDescription = "Menu",
                        tint = TelegramTextSecondary
                    )
                }
                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Options",
                        tint = TelegramTextSecondary
                    )
                }
            }

            // Big Avatar + Name + Status Badge
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(108.dp)
                        .clip(CircleShape)
                        .border(
                            width = 2.5.dp,
                            brush = Brush.linearGradient(
                                listOf(TelegramPrimary, Color(0xFFAB47BC), Color(0xFF00E5FF))
                            ),
                            shape = CircleShape
                        )
                ) {
                    AvatarView(
                        avatarUrl = currentUserAvatar,
                        title = currentUserName ?: "Darling 404",
                        size = 108.dp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = currentUserName ?: "Darling 404",
                    color = TelegramTextPrimary,
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Online Shield Badge matching Screenshot 1
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x332E7D32))
                        .border(0.8.dp, Color(0x664CAF50), RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Protected",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "1 online",
                        color = Color(0xFF4CAF50),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3 Action Buttons matching Screenshot 1: [Set Photo], [Edit Info], [Settings]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ProfileActionButton(
                    icon = Icons.Default.CameraAlt,
                    label = "Set Photo",
                    modifier = Modifier.weight(1f),
                    onClick = onEditProfileClick
                )
                ProfileActionButton(
                    icon = Icons.Default.Edit,
                    label = "Edit Info",
                    modifier = Modifier.weight(1f),
                    onClick = onEditProfileClick
                )
                ProfileActionButton(
                    icon = Icons.Default.Settings,
                    label = "Settings",
                    modifier = Modifier.weight(1f),
                    onClick = { onBottomNavSelect(2) }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Music status player card matching Screenshot 1
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0x33202E3D))
                    .border(1.dp, Color(0x2252B8FF), RoundedCornerShape(18.dp))
                    .clickable {}
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(TelegramPrimary.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = "Music",
                        tint = TelegramPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Sajad Shahi, Ashkan Kagan - Khastam",
                        color = TelegramTextPrimary,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Music status",
                        color = TelegramTextSecondary,
                        fontSize = 11.5.sp
                    )
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Next",
                    tint = TelegramTextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Channel Card matching Screenshot 1
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0x33202E3D))
                    .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(18.dp))
                    .clickable {}
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF8E24AA)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "📢", fontSize = 22.sp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "My Channel",
                            color = TelegramTextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Verified",
                            tint = TelegramPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Text(
                        text = "16 subscribers",
                        color = TelegramTextSecondary,
                        fontSize = 12.sp
                    )
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Go",
                    tint = TelegramTextMuted
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Info Card (Phone, Bio, Username, Birthday)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0x33202E3D))
                    .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(20.dp))
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                ProfileInfoRow(
                    label = currentUserPhone ?: "+98 912 345 6789",
                    sublabel = "Mobile"
                )
                HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                ProfileInfoRow(
                    label = currentUserBio ?: "Living in the rhythm ⚡",
                    sublabel = "Bio"
                )
                HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                ProfileInfoRow(
                    label = "@username_404",
                    sublabel = "Username"
                )
                HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                ProfileInfoRow(
                    label = "Aug 23",
                    sublabel = "Birthday"
                )
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
private fun ProfileActionButton(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0x33202E3D))
            .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(TelegramPrimary.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = TelegramPrimary,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            color = TelegramTextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ProfileInfoRow(
    label: String,
    sublabel: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
    ) {
        Text(
            text = label,
            color = TelegramTextPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = sublabel,
            color = TelegramTextSecondary,
            fontSize = 12.sp
        )
    }
}
