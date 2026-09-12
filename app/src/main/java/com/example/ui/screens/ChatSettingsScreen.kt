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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.TelegramChatListBg
import com.example.ui.theme.TelegramPrimary
import com.example.ui.theme.TelegramSurface
import com.example.ui.theme.TelegramTextMuted
import com.example.ui.theme.TelegramTextPrimary
import com.example.ui.theme.TelegramTextSecondary

/** Common emojis for double-tap reaction picker */
private val QUICK_EMOJIS = listOf("❤️", "👍", "🔥", "😂", "😮", "😢", "👏", "🙏", "🎉", "💯", "👀", "✨")

@Composable
fun ChatSettingsScreen(
    messageTextSize: Int,
    messageCornerRadius: Int,
    doubleTapEmoji: String,
    isDarkMode: Boolean,
    onSetTextSize: (Int) -> Unit,
    onSetCornerRadius: (Int) -> Unit,
    onSetDoubleTapEmoji: (String) -> Unit,
    onToggleDarkMode: () -> Unit,
    onBack: () -> Unit
) {
    var showEmojiPicker by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = TelegramChatListBg,
        topBar = { SettingsTopBar(title = "Chat Settings", onBack = onBack) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 12.dp)
        ) {
            // === Message text size ===
            item {
                SettingsCard {
                    SectionHeader("Message text size")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        var sliderValue by remember(messageTextSize) {
                            mutableFloatStateOf(messageTextSize.toFloat())
                        }
                        Slider(
                            value = sliderValue,
                            onValueChange = { sliderValue = it },
                            onValueChangeFinished = { onSetTextSize(sliderValue.toInt()) },
                            valueRange = 12f..22f,
                            steps = 9,
                            colors = SliderDefaults.colors(
                                thumbColor = TelegramPrimary,
                                activeTrackColor = TelegramPrimary,
                                inactiveTrackColor = TelegramTextMuted.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = messageTextSize.toString(),
                            color = TelegramPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.width(28.dp)
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }

            // === Wallpaper & Name Color ===
            item {
                SettingsCard {
                    SectionHeader("Appearance")
                    InfoRow(
                        icon = Icons.Default.Wallpaper,
                        iconColor = Color(0xFF5DADE2),
                        title = "Change Chat Wallpaper",
                        subtitle = "Pick a background for your chats",
                        onClick = { /* TODO: wallpaper picker — future */ }
                    )
                    InfoRow(
                        icon = Icons.Default.Palette,
                        iconColor = Color(0xFFBB8FCE),
                        title = "Change Name Color",
                        subtitle = "Color your name in chats",
                        value = "Default",
                        onClick = { /* TODO: color picker — future */ }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }

            // === Display Mode ===
            item {
                SettingsCard {
                    SectionHeader("Display Mode")
                    ToggleRow(
                        icon = Icons.Default.BrightnessMedium,
                        iconColor = Color(0xFFF39C12),
                        title = if (isDarkMode) "Switch to Day Mode" else "Switch to Night Mode",
                        subtitle = if (isDarkMode) "Currently using dark theme" else "Currently using light theme",
                        checked = !isDarkMode,
                        onCheckedChange = { onToggleDarkMode() }
                    )
                    InfoRow(
                        icon = Icons.Default.FormatPaint,
                        iconColor = Color(0xFFEC7063),
                        title = "Browse Themes",
                        subtitle = "Choose from preset themes",
                        onClick = { /* TODO: theme browser — future */ }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }

            // === Message corners slider ===
            item {
                SettingsCard {
                    SectionHeader("Message corners")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        var sliderValue by remember(messageCornerRadius) {
                            mutableFloatStateOf(messageCornerRadius.toFloat())
                        }
                        Slider(
                            value = sliderValue,
                            onValueChange = { sliderValue = it },
                            onValueChangeFinished = { onSetCornerRadius(sliderValue.toInt()) },
                            valueRange = 0f..28f,
                            steps = 27,
                            colors = SliderDefaults.colors(
                                thumbColor = TelegramPrimary,
                                activeTrackColor = TelegramPrimary,
                                inactiveTrackColor = TelegramTextMuted.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = messageCornerRadius.toString(),
                            color = TelegramPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.width(28.dp)
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }

            // === Double-tap reaction emoji ===
            item {
                SettingsCard {
                    SectionHeader("Double-tap Reaction")
                    InfoRow(
                        icon = Icons.Default.TouchApp,
                        iconColor = Color(0xFFEC7063),
                        title = "Quick Reaction",
                        subtitle = "Tap a message twice to send this emoji",
                        value = doubleTapEmoji,
                        onClick = { showEmojiPicker = true }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }

    // Emoji picker dialog
    if (showEmojiPicker) {
        AlertDialog(
            onDismissRequest = { showEmojiPicker = false },
            containerColor = TelegramSurface,
            titleContentColor = TelegramTextPrimary,
            title = { Text("Quick Reaction", color = TelegramTextPrimary) },
            text = {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(QUICK_EMOJIS) { emoji ->
                        Box(
                            modifier = Modifier
                                .padding(8.dp)
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(
                                    if (emoji == doubleTapEmoji) TelegramPrimary.copy(alpha = 0.2f)
                                    else Color.Transparent
                                )
                                .clickable {
                                    onSetDoubleTapEmoji(emoji)
                                    showEmojiPicker = false
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = emoji, fontSize = 26.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showEmojiPicker = false }) {
                    Text("Close", color = TelegramTextSecondary)
                }
            }
        )
    }
}
