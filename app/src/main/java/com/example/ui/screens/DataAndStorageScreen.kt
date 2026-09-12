package com.example.ui.screens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.TelegramChatListBg
import com.example.ui.theme.TelegramPrimary
import com.example.ui.theme.TelegramSurface
import com.example.ui.theme.TelegramTextMuted
import com.example.ui.theme.TelegramTextPrimary
import com.example.ui.theme.TelegramTextSecondary
import java.io.File

@Composable
fun DataAndStorageScreen(
    autoDownloadMobile: Boolean,
    autoDownloadWifi: Boolean,
    autoDownloadRoaming: Boolean,
    saveGalleryPrivate: Boolean,
    saveGalleryGroups: Boolean,
    saveGalleryChannels: Boolean,
    onSetAutoDlMobile: (Boolean) -> Unit,
    onSetAutoDlWifi: (Boolean) -> Unit,
    onSetAutoDlRoaming: (Boolean) -> Unit,
    onSaveGalleryPrivate: (Boolean) -> Unit,
    onSaveGalleryGroups: (Boolean) -> Unit,
    onSaveGalleryChannels: (Boolean) -> Unit,
    onClearCache: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var showClearCacheDialog by remember { mutableStateOf(false) }

    // Best-effort cache size calculation
    val cacheSizeBytes = remember {
        try {
            val cacheDir = context.cacheDir
            fun dirSize(dir: File): Long {
                if (!dir.exists()) return 0
                return dir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
            }
            dirSize(cacheDir)
        } catch (_: Exception) { 0L }
    }
    val cacheSizeFormatted = formatBytes(cacheSizeBytes)
    // Estimate data usage as 2x cache (placeholder — would need a real TrafficStats integration)
    val dataUsageFormatted = formatBytes(cacheSizeBytes * 2L)

    Scaffold(
        containerColor = TelegramChatListBg,
        topBar = { SettingsTopBar(title = "Data and Storage", onBack = onBack) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 12.dp)
        ) {
            // === Disk and network usage ===
            item {
                SettingsCard {
                    SectionHeader("Disk and network usage")
                    InfoRow(
                        icon = Icons.Default.Storage,
                        iconColor = Color(0xFF3B82F6),
                        title = "Storage Usage",
                        value = cacheSizeFormatted,
                        onClick = null
                    )
                    InfoRow(
                        icon = Icons.Default.SwapVert,
                        iconColor = Color(0xFF22C55E),
                        title = "Data Usage",
                        value = dataUsageFormatted,
                        onClick = null
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }

            // === Automatic media download ===
            item {
                SettingsCard {
                    SectionHeader("Automatic media download")
                    ToggleRow(
                        icon = Icons.Default.CloudDownload,
                        iconColor = Color(0xFF5DADE2),
                        title = "When using mobile data",
                        subtitle = "Photos, Videos (10 MB), Files (1 MB)",
                        checked = autoDownloadMobile,
                        onCheckedChange = onSetAutoDlMobile
                    )
                    ToggleRow(
                        icon = Icons.Default.CloudDownload,
                        iconColor = Color(0xFF58D68D),
                        title = "When connected to Wi-Fi",
                        subtitle = "Photos, Videos (15 MB), Files (3 MB)",
                        checked = autoDownloadWifi,
                        onCheckedChange = onSetAutoDlWifi
                    )
                    ToggleRow(
                        icon = Icons.Default.CloudDownload,
                        iconColor = Color(0xFFF39C12),
                        title = "When roaming",
                        subtitle = "Photos",
                        checked = autoDownloadRoaming,
                        onCheckedChange = onSetAutoDlRoaming
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showClearCacheDialog = true }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Reset Auto-Download Settings",
                            color = Color(0xFFEF4444),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }

            // === Save to Gallery ===
            item {
                SettingsCard {
                    SectionHeader("Save to Gallery")
                    ToggleRow(
                        icon = Icons.Default.SaveAlt,
                        iconColor = Color(0xFF5DADE2),
                        title = "Private Chats",
                        subtitle = if (saveGalleryPrivate) "On" else "Off",
                        checked = saveGalleryPrivate,
                        onCheckedChange = onSaveGalleryPrivate
                    )
                    ToggleRow(
                        icon = Icons.Default.SaveAlt,
                        iconColor = Color(0xFFF39C12),
                        title = "Groups",
                        subtitle = if (saveGalleryGroups) "On" else "Off",
                        checked = saveGalleryGroups,
                        onCheckedChange = onSaveGalleryGroups
                    )
                    ToggleRow(
                        icon = Icons.Default.SaveAlt,
                        iconColor = Color(0xFFEC7063),
                        title = "Channels",
                        subtitle = if (saveGalleryChannels) "On" else "Off",
                        checked = saveGalleryChannels,
                        onCheckedChange = onSaveGalleryChannels
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }

            // === Cache management ===
            item {
                SettingsCard {
                    SectionHeader("Cache")
                    InfoRow(
                        icon = Icons.Default.DeleteSweep,
                        iconColor = Color(0xFFEF4444),
                        title = "Clear Cache",
                        subtitle = "Free up space by clearing cached media ($cacheSizeFormatted)",
                        onClick = { showClearCacheDialog = true }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }

    if (showClearCacheDialog) {
        AlertDialog(
            onDismissRequest = { showClearCacheDialog = false },
            containerColor = TelegramSurface,
            titleContentColor = TelegramTextPrimary,
            title = { Text("Clear Cache?", color = TelegramTextPrimary) },
            text = {
                Text(
                    text = "This will remove $cacheSizeFormatted of cached images and media. " +
                        "Messages and conversations are kept on the server and will re-download when you open them.",
                    color = TelegramTextSecondary,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onClearCache()
                    showClearCacheDialog = false
                }) {
                    Text("Clear", color = Color(0xFFEF4444), fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearCacheDialog = false }) {
                    Text("Cancel", color = TelegramTextSecondary)
                }
            }
        )
    }
}

private fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "0 MB"
    val units = arrayOf("B", "KB", "MB", "GB")
    var size = bytes.toDouble()
    var unitIndex = 0
    while (size >= 1024 && unitIndex < units.size - 1) {
        size /= 1024
        unitIndex++
    }
    return if (unitIndex >= 2) String.format("%.2f %s", size, units[unitIndex])
    else String.format("%.0f %s", size, units[unitIndex])
}
