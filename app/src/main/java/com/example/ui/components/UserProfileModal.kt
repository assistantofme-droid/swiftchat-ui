package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.api.ApiClient
import com.example.data.model.AvatarType
import com.example.ui.theme.TelegramDarkBg
import com.example.ui.theme.TelegramPrimary
import com.example.ui.theme.TelegramTextMuted
import com.example.ui.theme.TelegramTextPrimary
import com.example.ui.theme.TelegramTextSecondary

data class UserProfileData(
    val id: String,
    val name: String,
    val username: String? = null,
    val phone: String? = null,
    val bio: String? = null,
    val avatarUrl: String? = null,
    val avatarType: AvatarType = AvatarType.MOTORCYCLE,
    val isOnline: Boolean = true,
    val isSelf: Boolean = false
)

@Composable
fun UserProfileModal(
    visible: Boolean,
    user: UserProfileData?,
    isUpdating: Boolean = false,
    onDismiss: () -> Unit,
    onUpdateProfile: ((name: String, username: String, bio: String) -> Unit)? = null
) {
    if (!visible || user == null) return

    var isEditProfileDialogOpen by remember { mutableStateOf(false) }
    var editName by remember(user.name) { mutableStateOf(user.name) }
    var editUsername by remember(user.username) { mutableStateOf(user.username.orEmpty()) }
    var editBio by remember(user.bio) { mutableStateOf(user.bio.orEmpty()) }

    var isMuted by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TelegramDarkBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Hero Top Section with Avatar & Gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
            ) {
                // Background Gradient or Cover
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF2B5278),
                                    Color(0xFF182533)
                                )
                            )
                        )
                )

                // Top Controls
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    Row {
                        if (user.isSelf) {
                            IconButton(onClick = { isEditProfileDialogOpen = true }) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Profile",
                                    tint = Color.White
                                )
                            }
                        }
                        IconButton(onClick = { /* Share */ }) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                tint = Color.White
                            )
                        }
                        IconButton(onClick = { /* More */ }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More",
                                tint = Color.White
                            )
                        }
                    }
                }

                // Avatar and Name Container
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 20.dp, bottom = 16.dp)
                ) {
                    AvatarView(
                        avatarType = user.avatarType,
                        avatarUrl = user.avatarUrl,
                        title = user.name,
                        size = 80.dp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = user.name,
                        color = TelegramTextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = if (user.isOnline) "online" else "last seen recently",
                        color = if (user.isOnline) TelegramPrimary else TelegramTextSecondary,
                        fontSize = 13.sp
                    )
                }
            }

            // Quick Actions Bar (Call, Video, Mute, Search)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E2833))
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                QuickActionButton(
                    icon = Icons.Default.Chat,
                    label = "Message",
                    onClick = onDismiss
                )
                QuickActionButton(
                    icon = Icons.Default.Call,
                    label = "Audio",
                    onClick = {}
                )
                QuickActionButton(
                    icon = Icons.Default.Videocam,
                    label = "Video",
                    onClick = {}
                )
                QuickActionButton(
                    icon = Icons.Default.Notifications,
                    label = if (isMuted) "Unmute" else "Mute",
                    onClick = { isMuted = !isMuted }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Info Card (Account Details)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E2833))
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Text(
                    text = "Account",
                    color = TelegramPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                // Phone number
                if (!user.phone.isNullOrBlank()) {
                    ProfileInfoRow(
                        title = user.phone,
                        subtitle = "Mobile"
                    )
                    HorizontalDivider(
                        color = Color.White.copy(alpha = 0.08f),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                // Username
                if (!user.username.isNullOrBlank()) {
                    ProfileInfoRow(
                        title = "@${user.username}",
                        subtitle = "Username"
                    )
                    HorizontalDivider(
                        color = Color.White.copy(alpha = 0.08f),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                // Bio
                ProfileInfoRow(
                    title = user.bio ?: "Hey there! I am using Telegram.",
                    subtitle = "Bio"
                )

                HorizontalDivider(
                    color = Color.White.copy(alpha = 0.08f),
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                // Notifications Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Notifications", color = TelegramTextPrimary, fontSize = 15.sp)
                        Text(text = if (isMuted) "Disabled" else "Enabled", color = TelegramTextSecondary, fontSize = 13.sp)
                    }
                    Switch(
                        checked = !isMuted,
                        onCheckedChange = { isMuted = !it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = TelegramPrimary,
                            uncheckedThumbColor = Color.Gray,
                            uncheckedTrackColor = Color.DarkGray
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Shared Media Tabs
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E2833))
                    .navigationBarsPadding()
            ) {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = TelegramPrimary,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = TelegramPrimary,
                            height = 2.dp
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Media", fontSize = 13.sp) },
                        selectedContentColor = TelegramPrimary,
                        unselectedContentColor = TelegramTextSecondary
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Files", fontSize = 13.sp) },
                        selectedContentColor = TelegramPrimary,
                        unselectedContentColor = TelegramTextSecondary
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Audio", fontSize = 13.sp) },
                        selectedContentColor = TelegramPrimary,
                        unselectedContentColor = TelegramTextSecondary
                    )
                    Tab(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        text = { Text("Links", fontSize = 13.sp) },
                        selectedContentColor = TelegramPrimary,
                        unselectedContentColor = TelegramTextSecondary
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (selectedTab) {
                            0 -> "Shared photos and videos appear here"
                            1 -> "Shared documents and files appear here"
                            2 -> "Shared audio files and voice notes appear here"
                            else -> "Shared links and articles appear here"
                        },
                        color = TelegramTextMuted,
                        fontSize = 13.sp
                    )
                }
            }
        }

        // Edit Profile Dialog (connected to PUT /auth/profile)
        if (isEditProfileDialogOpen) {
            AlertDialog(
                onDismissRequest = { isEditProfileDialogOpen = false },
                containerColor = Color(0xFF1E2833),
                title = {
                    Text(text = "Edit Profile", color = TelegramTextPrimary, fontWeight = FontWeight.Bold)
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = editName,
                            onValueChange = { editName = it },
                            label = { Text("Name", color = TelegramTextSecondary) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TelegramTextPrimary,
                                unfocusedTextColor = TelegramTextPrimary,
                                focusedBorderColor = TelegramPrimary,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = editUsername,
                            onValueChange = { editUsername = it },
                            label = { Text("Username", color = TelegramTextSecondary) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TelegramTextPrimary,
                                unfocusedTextColor = TelegramTextPrimary,
                                focusedBorderColor = TelegramPrimary,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = editBio,
                            onValueChange = { editBio = it },
                            label = { Text("Bio", color = TelegramTextSecondary) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TelegramTextPrimary,
                                unfocusedTextColor = TelegramTextPrimary,
                                focusedBorderColor = TelegramPrimary,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 3
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onUpdateProfile?.invoke(editName, editUsername, editBio)
                            isEditProfileDialogOpen = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TelegramPrimary)
                    ) {
                        if (isUpdating) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp))
                        } else {
                            Text("Save", color = Color.White)
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { isEditProfileDialogOpen = false }) {
                        Text("Cancel", color = TelegramTextSecondary)
                    }
                }
            )
        }
    }
}

@Composable
private fun QuickActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = TelegramPrimary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, color = TelegramTextSecondary, fontSize = 12.sp)
    }
}

@Composable
private fun ProfileInfoRow(title: String, subtitle: String) {
    Column {
        Text(
            text = title,
            color = TelegramTextPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = subtitle,
            color = TelegramTextSecondary,
            fontSize = 12.sp
        )
    }
}
