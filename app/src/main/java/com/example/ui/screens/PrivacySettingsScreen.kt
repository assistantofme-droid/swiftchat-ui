package com.example.ui.screens

import com.example.ui.theme.appPalette
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
/** Privacy options: "Everyone" | "Contacts" | "Nobody" */
private val PRIVACY_OPTIONS = listOf("Everyone", "Contacts", "Nobody")
@Composable
fun PrivacySettingsScreen(
    privacyLastSeen: String,
    privacyPhoneNumber: String,
    privacyForwarded: String,
    privacyGroups: String,
    isPhoneHidden: Boolean,
    onSetLastSeen: (String) -> Unit,
    onSetPhone: (String) -> Unit,
    onSetForwarded: (String) -> Unit,
    onSetGroups: (String) -> Unit,
    onSetPhoneHidden: (Boolean) -> Unit,
    onBack: () -> Unit
) {
    var dialogField by remember { mutableStateOf<String?>(null) }
    Scaffold(
        containerColor = appPalette.chatListBg,
        topBar = {
            SettingsTopBar(title = "Privacy & Security", onBack = onBack)
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 12.dp)
        ) {
            item {
                SettingsCard {
                    SectionHeader("Privacy")
                    PrivacyRow(
                        icon = Icons.Default.VisibilityOff,
                        iconColor = Color(0xFF58D68D),
                        title = "Last Seen & Online",
                        value = privacyLastSeen,
                        onClick = { dialogField = "lastSeen" }
                    )
                        icon = Icons.Default.Phone,
                        iconColor = Color(0xFF5DADE2),
                        title = "Phone Number",
                        value = privacyPhoneNumber,
                        onClick = { dialogField = "phone" }
                        icon = Icons.Default.Forum,
                        iconColor = Color(0xFFF39C12),
                        title = "Forwarded Messages",
                        value = privacyForwarded,
                        onClick = { dialogField = "forwarded" }
                        icon = Icons.Default.Person,
                        iconColor = Color(0xFFBB8FCE),
                        title = "Groups & Channels",
                        value = privacyGroups,
                        onClick = { dialogField = "groups" }
                }
            }
            item { Spacer(modifier = Modifier.height(12.dp)) }
                    SectionHeader("Security")
                    ToggleRow(
                        icon = Icons.Default.Lock,
                        iconColor = Color(0xFFEC7063),
                        title = "Hide My Phone Number",
                        subtitle = "Other users won't see your phone on your profile",
                        checked = isPhoneHidden,
                        onCheckedChange = onSetPhoneHidden
                    SectionHeader("Sessions")
                    InfoRow(
                        icon = Icons.Default.Devices,
                        iconColor = Color(0xFF48C9B0),
                        title = "Active Sessions",
                        subtitle = "Manage devices logged into your account",
                        onClick = { /* navigates to Devices screen via Settings */ }
            item { Spacer(modifier = Modifier.height(80.dp)) }
    }
    // Privacy option picker dialog
    dialogField?.let { field ->
        val current = when (field) {
            "lastSeen" -> privacyLastSeen
            "phone" -> privacyPhoneNumber
            "forwarded" -> privacyForwarded
            "groups" -> privacyGroups
            else -> "Everyone"
        val title = when (field) {
            "lastSeen" -> "Last Seen & Online"
            "phone" -> "Phone Number"
            "forwarded" -> "Forwarded Messages"
            "groups" -> "Groups & Channels"
            else -> "Privacy"
        AlertDialog(
            onDismissRequest = { dialogField = null },
            containerColor = appPalette.surface,
            titleContentColor = appPalette.textPrimary,
            title = { Text(title, color = appPalette.textPrimary) },
            text = {
                Column {
                    PRIVACY_OPTIONS.forEach { option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    when (field) {
                                        "lastSeen" -> onSetLastSeen(option)
                                        "phone" -> onSetPhone(option)
                                        "forwarded" -> onSetForwarded(option)
                                        "groups" -> onSetGroups(option)
                                    }
                                    dialogField = null
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = option,
                                color = if (option == current) appPalette.primary else appPalette.textPrimary,
                                fontSize = 16.sp,
                                fontWeight = if (option == current) FontWeight.SemiBold else FontWeight.Normal,
                                modifier = Modifier.weight(1f)
                            )
                            if (option == current) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = appPalette.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
            },
            confirmButton = {
                TextButton(onClick = { dialogField = null }) {
                    Text("Cancel", color = appPalette.textSecondary)
        )
}
internal fun SettingsTopBar(title: String, onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(appPalette.chatListBg)
            .statusBarsPadding()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = appPalette.textPrimary
            )
        Text(
            text = title,
            color = appPalette.textPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 8.dp)
internal fun SettingsCard(content: @Composable () -> Unit) {
    Surface(
        color = appPalette.surface,
        shape = RoundedCornerShape(20.dp),
            .padding(horizontal = 12.dp)
        Column { content() }
internal fun SectionHeader(text: String) {
    Text(
        text = text,
        color = appPalette.primary,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(start = 16.dp, top = 14.dp, bottom = 6.dp)
    )
internal fun PrivacyRow(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    value: String,
    onClick: () -> Unit
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        Box(
                .size(40.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(iconColor.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(22.dp)
        Spacer(modifier = Modifier.size(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = appPalette.textPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            text = value,
            color = appPalette.textSecondary,
            fontSize = 14.sp
internal fun ToggleRow(
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
            .padding(horizontal = 16.dp, vertical = 12.dp),
            if (!subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    color = appPalette.textSecondary,
                    fontSize = 13.sp
                )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
internal fun InfoRow(
    value: String? = null,
    onClick: (() -> Unit)? = null
            .let { if (onClick != null) it.clickable(onClick = onClick) else it }
        if (!value.isNullOrBlank()) {
                text = value,
                color = appPalette.primary,
                fontSize = 15.sp,
/**
 * Settings item row with chevron — used on the main Settings screen to
 * navigate into a sub-screen.
 */
internal fun SettingsItem(
    subtitle: String?,
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = appPalette.textSecondary,
            modifier = Modifier.size(20.dp)
