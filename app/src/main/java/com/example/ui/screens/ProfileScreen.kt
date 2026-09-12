package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.api.ApiClient
import com.example.data.api.ApiConversation
import com.example.data.api.ApiUser
import com.example.ui.components.AvatarView
import com.example.ui.components.TelegramBottomNav
import com.example.ui.theme.TelegramChatListBg
import com.example.ui.theme.TelegramPrimary
import com.example.ui.theme.TelegramSurface
import com.example.ui.theme.TelegramSurfaceVariant
import com.example.ui.theme.TelegramTextMuted
import com.example.ui.theme.TelegramTextPrimary
import com.example.ui.theme.TelegramTextSecondary

/**
 * Profile screen — shows the authenticated user's own profile, pulled from
 * /auth/me (passed in as meUser). Includes:
 *   - Large avatar + name + "online" status
 *   - 3 action buttons: Set Photo (multipart PUT /auth/profile with file),
 *     Edit Info (opens the existing UserProfileModal in edit mode),
 *     Settings (switches to Settings tab)
 *   - profileActiveSong pill (visible only when user has one set)
 *   - Channel card (visible only when user owns a channel — fetched from /conversations)
 *   - Info list card: phone / bio / username / birthday — all from /auth/me
 */
@Composable
fun ProfileScreen(
    meUser: ApiUser?,
    myChannel: ApiConversation?,
    isMeLoading: Boolean,
    isProfileUpdating: Boolean,
    selectedBottomNavIndex: Int = 3,
    onSelectBottomNav: (Int) -> Unit,
    onRefreshMe: () -> Unit,
    onUploadAvatar: (Uri) -> Unit,
    onEditInfo: () -> Unit
) {
    val context = LocalContext.current

    // Image picker for "Set Photo" button
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) onUploadAvatar(uri)
    }

    // Refresh on first composition
    LaunchedEffect(Unit) { onRefreshMe() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TelegramChatListBg)
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            // Top app bar row — QR + overflow
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { /* future: QR code */ }) {
                        Icon(
                            imageVector = Icons.Default.QrCode2,
                            contentDescription = "QR",
                            tint = TelegramTextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = { /* overflow menu */ }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More",
                            tint = TelegramTextPrimary
                        )
                    }
                }
            }

            // Header (avatar + name + online)
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val avatarUrl = ApiClient.resolveUrl(meUser?.avatar)
                    if (!avatarUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = avatarUrl,
                            contentDescription = "Avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .background(TelegramSurfaceVariant)
                        )
                    } else {
                        AvatarView(
                            avatarType = com.example.data.model.AvatarType.MOTORCYCLE,
                            size = 100.dp
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = meUser?.name ?: "Loading…",
                        color = TelegramTextPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = TelegramTextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "online",
                            color = TelegramTextSecondary,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            // Action buttons row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ActionButton(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.CameraAlt,
                        label = if (isProfileUpdating) "Uploading…" else "Set Photo",
                        enabled = !isProfileUpdating,
                        onClick = { imagePicker.launch("image/*") }
                    )
                    ActionButton(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Edit,
                        label = "Edit Info",
                        onClick = onEditInfo
                    )
                    ActionButton(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Settings,
                        label = "Settings",
                        onClick = { onSelectBottomNav(2) }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            // profileActiveSong pill (only if user has one)
            meUser?.profileActiveSong?.let { song ->
                if (!song.title.isNullOrBlank() || !song.url.isNullOrBlank()) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(TelegramSurfaceVariant.copy(alpha = 0.6f))
                                .clickable { /* future: play song */ }
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = TelegramPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = song.title ?: "Unknown song",
                                color = TelegramTextPrimary,
                                fontSize = 14.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = TelegramTextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    item { Spacer(modifier = Modifier.height(8.dp)) }
                }
            }

            // Channel card (only if user owns a channel)
            myChannel?.let { channel ->
                item {
                    ChannelCard(channel = channel)
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            // Info list card: phone, bio, username, birthday — all from /auth/me
            item {
                Surface(
                    color = TelegramSurface,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                ) {
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        InfoRow(
                            label = "Mobile",
                            value = meUser?.phone?.takeIf { it.isNotBlank() },
                            placeholder = "—"
                        )
                        InfoRow(
                            label = "Bio",
                            value = meUser?.bio?.takeIf { it.isNotBlank() },
                            placeholder = "—"
                        )
                        InfoRow(
                            label = "Username",
                            value = meUser?.username?.takeIf { it.isNotBlank() }?.let { "@$it" },
                            placeholder = "—"
                        )
                        InfoRow(
                            label = "Birthday",
                            value = meUser?.birthday?.takeIf { it.isNotBlank() },
                            placeholder = "—"
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            // "No posts yet…" footer
            item {
                Text(
                    text = "No posts yet…",
                    color = TelegramTextMuted,
                    fontSize = 13.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 100.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }

        // Loading overlay during initial /auth/me fetch
        if (isMeLoading && meUser == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = TelegramPrimary)
            }
        }

        // Bottom navigation
        TelegramBottomNav(
            selectedIndex = selectedBottomNavIndex,
            onSelect = onSelectBottomNav,
            currentUserAvatarUrl = ApiClient.resolveUrl(meUser?.avatar),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        )
    }
}

@Composable
private fun ActionButton(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Surface(
        color = TelegramSurfaceVariant,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .height(54.dp)
            .clickable(enabled = enabled, onClick = onClick)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (enabled) TelegramTextPrimary else TelegramTextMuted,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                color = if (enabled) TelegramTextPrimary else TelegramTextMuted,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ChannelCard(channel: ApiConversation) {
    Surface(
        color = TelegramSurface,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Channel",
                    color = TelegramPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    color = TelegramPrimary.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${channel.participants?.size ?: 0} subscribers",
                        color = TelegramPrimary,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                AvatarView(
                    avatarUrl = channel.avatar,
                    title = channel.name,
                    size = 40.dp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = channel.name ?: "My Channel",
                        color = TelegramTextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    channel.description?.takeIf { it.isNotBlank() }?.let { desc ->
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = desc,
                            color = TelegramTextSecondary,
                            fontSize = 13.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                channel.lastMessage?.createdAt?.let {
                    Text(
                        text = formatDateShort(it),
                        color = TelegramTextSecondary,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String?,
    placeholder: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(
            text = value ?: placeholder,
            color = if (value.isNullOrBlank()) TelegramTextMuted else TelegramTextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            color = TelegramTextSecondary,
            fontSize = 12.sp
        )
    }
}

private fun formatDateShort(iso: String): String {
    return try {
        val clean = iso.replace("Z", "+0000")
        val parser = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", java.util.Locale.US)
        val date = parser.parse(clean) ?: return iso
        java.text.SimpleDateFormat("MMM dd", java.util.Locale.US).format(date)
    } catch (_: Exception) {
        try {
            val parser = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US)
            val date = parser.parse(iso.substringBefore(".")) ?: return iso
            java.text.SimpleDateFormat("MMM dd", java.util.Locale.US).format(date)
        } catch (_: Exception) {
            iso
        }
    }
}
