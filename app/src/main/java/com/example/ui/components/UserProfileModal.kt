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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    val isOnline: Boolean = false,
    val isSelf: Boolean = false,
    // No default fake birthday — null means "not set" and the UI hides the row.
    val birthday: String? = null
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

    val clipboardManager = LocalClipboardManager.current
    var isEditProfileDialogOpen by remember { mutableStateOf(false) }
    var editName by remember(user.name) { mutableStateOf(user.name) }
    var editUsername by remember(user.username) { mutableStateOf(user.username.orEmpty()) }
    var editBio by remember(user.bio) { mutableStateOf(user.bio.orEmpty()) }

    var isMuted by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) }
    val profileTabs = listOf("Gifts 🏆🎂", "Media", "Files", "Links", "Voice")

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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Top Action Bar
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
                        if (user.isSelf) {
                            IconButton(onClick = { isEditProfileDialogOpen = true }) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Profile",
                                    tint = TelegramTextSecondary
                                )
                            }
                        }
                        IconButton(onClick = {}) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
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

                // Centered Avatar + Name + Shield Badge matching Screenshot 5
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .border(
                                width = 2.dp,
                                brush = Brush.linearGradient(
                                    listOf(TelegramPrimary, Color(0xFFAB47BC), Color(0xFF00E5FF))
                                ),
                                shape = CircleShape
                            )
                    ) {
                        AvatarView(
                            avatarType = user.avatarType,
                            avatarUrl = user.avatarUrl,
                            title = user.name,
                            size = 100.dp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = user.name,
                        color = TelegramTextPrimary,
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Shield status pill matching Screenshot 5
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
                            contentDescription = "Online Status",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (user.isOnline) "online" else "last seen recently",
                            color = Color(0xFF4CAF50),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // 3 Action Buttons matching Screenshot 5: Message, Mute, Call
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    ProfileActionButton(
                        icon = Icons.Default.Chat,
                        label = "Message",
                        modifier = Modifier.weight(1f),
                        onClick = onDismiss
                    )
                    ProfileActionButton(
                        icon = Icons.Default.Notifications,
                        label = if (isMuted) "Unmute" else "Mute",
                        modifier = Modifier.weight(1f),
                        onClick = { isMuted = !isMuted }
                    )
                    ProfileActionButton(
                        icon = Icons.Default.Call,
                        label = "Call",
                        modifier = Modifier.weight(1f),
                        onClick = {}
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Info Card: Mobile, Bio, Username, Birthday — each row only shows
                // if the user actually has that field set on the server. No fake
                // placeholders, no fake phone numbers.
                val visibleInfoRows = buildList {
                    if (!user.phone.isNullOrBlank()) add("phone")
                    if (!user.bio.isNullOrBlank()) add("bio")
                    if (!user.username.isNullOrBlank()) add("username")
                    if (!user.birthday.isNullOrBlank()) add("birthday")
                }

                if (visibleInfoRows.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0x33202E3D))
                            .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(20.dp))
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        visibleInfoRows.forEachIndexed { index, key ->
                            when (key) {
                                "phone" -> ProfileInfoRow(
                                    title = user.phone.orEmpty(),
                                    subtitle = "Mobile",
                                    trailingAction = {
                                        IconButton(
                                            onClick = {
                                                clipboardManager.setText(AnnotatedString(user.phone.orEmpty()))
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ContentCopy,
                                                contentDescription = "Copy phone",
                                                tint = TelegramTextSecondary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                )
                                "bio" -> ProfileInfoRow(
                                    title = user.bio.orEmpty(),
                                    subtitle = "Bio"
                                )
                                "username" -> ProfileInfoRow(
                                    title = "@${user.username}",
                                    subtitle = "Username",
                                    trailingAction = {
                                        IconButton(onClick = {}, modifier = Modifier.size(28.dp)) {
                                            Icon(
                                                imageVector = Icons.Default.QrCode,
                                                contentDescription = "QR Code",
                                                tint = TelegramPrimary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                )
                                "birthday" -> ProfileInfoRow(
                                    title = user.birthday.orEmpty(),
                                    subtitle = "Birthday",
                                    trailingAction = {
                                        Icon(
                                            imageVector = Icons.Default.Cake,
                                            contentDescription = "Birthday",
                                            tint = Color(0xFFFFB300),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                )
                            }
                            if (index < visibleInfoRows.size - 1) {
                                HorizontalDivider(color = Color.White.copy(alpha = 0.08f), modifier = Modifier.padding(vertical = 4.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Shared Media & Gifts Tabs matching Screenshot 5
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(profileTabs.indices.toList()) { idx ->
                            val isSel = selectedTab == idx
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isSel) TelegramPrimary else Color(0x33202E3D))
                                    .clickable { selectedTab = idx }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = profileTabs[idx],
                                    color = if (isSel) Color.White else TelegramTextSecondary,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (selectedTab == 0) {
                        // Gifts — no fake list. Show an empty state until the
                        // server returns real gifts via GET /gifts/user/{id}.
                        // (Future: wire that up. For now, this is honest.)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "🎁",
                                    fontSize = 36.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "No gifts yet",
                                    color = TelegramTextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Gifts sent to ${user.name} will appear here.",
                                    color = TelegramTextMuted,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No ${profileTabs[selectedTab]} shared yet",
                                color = TelegramTextMuted,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }

    // Edit Profile Dialog for Self
    if (isEditProfileDialogOpen) {
        AlertDialog(
            onDismissRequest = { isEditProfileDialogOpen = false },
            title = {
                Text(
                    text = "Edit Profile",
                    color = TelegramTextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Display Name") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TelegramPrimary,
                            focusedTextColor = TelegramTextPrimary,
                            unfocusedTextColor = TelegramTextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editUsername,
                        onValueChange = { editUsername = it },
                        label = { Text("Username") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TelegramPrimary,
                            focusedTextColor = TelegramTextPrimary,
                            unfocusedTextColor = TelegramTextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editBio,
                        onValueChange = { editBio = it },
                        label = { Text("Bio") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TelegramPrimary,
                            focusedTextColor = TelegramTextPrimary,
                            unfocusedTextColor = TelegramTextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
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
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                    } else {
                        Text("Save")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { isEditProfileDialogOpen = false }) {
                    Text("Cancel", color = TelegramTextSecondary)
                }
            },
            containerColor = Color(0xFF1E2833),
            shape = RoundedCornerShape(20.dp)
        )
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
        Spacer(modifier = Modifier.height(4.dp))
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
    title: String,
    subtitle: String,
    trailingAction: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = TelegramTextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = TelegramTextSecondary,
                fontSize = 12.sp
            )
        }
        trailingAction?.invoke()
    }
}
