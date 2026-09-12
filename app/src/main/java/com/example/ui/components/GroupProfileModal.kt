package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.ui.theme.TelegramDarkBg
import com.example.ui.theme.TelegramPrimary
import com.example.ui.theme.TelegramTextMuted
import com.example.ui.theme.TelegramTextPrimary
import com.example.ui.theme.TelegramTextSecondary

data class GroupMemberItem(
    val id: String,
    val name: String,
    val roleBadge: String? = null,
    val isOnline: Boolean = false,
    val avatarUrl: String? = null
)

@Composable
fun GroupProfileModal(
    visible: Boolean,
    groupName: String,
    groupAvatarUrl: String?,
    memberCount: Int = 44,
    description: String? = null,
    members: List<GroupMemberItem> = emptyList(),
    onDismiss: () -> Unit,
    onMessageClick: () -> Unit
) {
    if (!visible) return

    val sampleMembers = remember(members) {
        if (members.isNotEmpty()) members
        else listOf(
            GroupMemberItem("1", "Hossein", "owner", true),
            GroupMemberItem("2", "Farshad Tech", "admin", false),
            GroupMemberItem("3", "Ali Reza", "مجاهد سبیل الله", true),
            GroupMemberItem("4", "Sadegh Dev", "عضو ارشد", false),
            GroupMemberItem("5", "Nima", null, false),
            GroupMemberItem("6", "Mohammad", null, true)
        )
    }

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Members ($memberCount)", "Media", "Files", "Links", "Music")

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(TelegramDarkBg)
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                // Top Header Row
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = TelegramTextPrimary
                            )
                        }
                        Row {
                            IconButton(onClick = {}) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit",
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
                }

                // Group Avatar + Name + Subtitle
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .border(
                                    width = 2.dp,
                                    brush = Brush.linearGradient(
                                        listOf(Color(0xFF00E5FF), TelegramPrimary, Color(0xFF7C4DFF))
                                    ),
                                    shape = CircleShape
                                )
                        ) {
                            AvatarView(
                                avatarUrl = groupAvatarUrl,
                                title = groupName,
                                size = 100.dp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = groupName,
                            color = TelegramTextPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "$memberCount members, 1 online",
                            color = TelegramTextSecondary,
                            fontSize = 13.sp
                        )
                    }
                }

                // 5 Action Buttons Row matching Screenshot 4: Message, Unmute, Video Chat, Add Story, Leave
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        GroupActionButton(
                            icon = Icons.Default.Chat,
                            label = "Message",
                            onClick = {
                                onDismiss()
                                onMessageClick()
                            }
                        )
                        GroupActionButton(
                            icon = Icons.Default.Notifications,
                            label = "Unmute",
                            onClick = {}
                        )
                        GroupActionButton(
                            icon = Icons.Default.Videocam,
                            label = "Video Chat",
                            onClick = {}
                        )
                        GroupActionButton(
                            icon = Icons.Default.Add,
                            label = "Add Story",
                            onClick = {}
                        )
                        GroupActionButton(
                            icon = Icons.AutoMirrored.Filled.ExitToApp,
                            label = "Leave",
                            iconColor = Color(0xFFFF5252),
                            onClick = onDismiss
                        )
                    }
                }

                // Description card
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0x33202E3D))
                            .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(20.dp))
                            .padding(14.dp)
                    ) {
                        Text(
                            text = description ?: "Official group for developer sync and collaboration.",
                            color = TelegramTextPrimary,
                            fontSize = 14.5.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Description",
                            color = TelegramTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                // Add Members Action Row
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0x33202E3D))
                            .border(1.dp, Color(0x2252B8FF), RoundedCornerShape(18.dp))
                            .clickable {}
                            .padding(horizontal = 14.dp, vertical = 12.dp),
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
                                imageVector = Icons.Default.PersonAdd,
                                contentDescription = "Add Members",
                                tint = TelegramPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Add Members",
                            color = TelegramPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Tab Pills Row (Members, Media, Files, Links, Music)
                item {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(tabs.indices.toList()) { index ->
                            val isSelected = selectedTab == index
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isSelected) TelegramPrimary else Color(0x33202E3D))
                                    .clickable { selectedTab = index }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = tabs[index],
                                    color = if (isSelected) Color.White else TelegramTextSecondary,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // Members List matching Screenshot 4
                items(sampleMembers, key = { it.id }) { member ->
                    GroupMemberRow(member = member)
                }
            }
        }
    }
}

@Composable
private fun GroupActionButton(
    icon: ImageVector,
    label: String,
    iconColor: Color = TelegramPrimary,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(Color(0x33202E3D))
                .border(1.dp, Color(0x22FFFFFF), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconColor,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = TelegramTextSecondary,
            fontSize = 11.sp
        )
    }
}

@Composable
private fun GroupMemberRow(member: GroupMemberItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {}
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
        ) {
            AvatarView(
                avatarUrl = member.avatarUrl,
                title = member.name,
                size = 44.dp
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = member.name,
                color = TelegramTextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = if (member.isOnline) "online" else "last seen recently",
                color = if (member.isOnline) TelegramPrimary else TelegramTextSecondary,
                fontSize = 12.sp
            )
        }

        // Role Badge Pill matching Screenshot 4
        if (member.roleBadge != null) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0x334CAF50))
                    .border(0.8.dp, Color(0x664CAF50), RoundedCornerShape(10.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = member.roleBadge,
                    color = Color(0xFF4CAF50),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
