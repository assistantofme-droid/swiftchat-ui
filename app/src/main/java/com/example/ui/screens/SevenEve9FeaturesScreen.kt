package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.ui.theme.TelegramTextPrimary
import com.example.ui.theme.TelegramTextSecondary

/**
 * 7eve9 Features — exclusive AI-powered features that elevate 7eve9Chat
 * beyond a standard messenger. Each feature is a toggle so the user can
 * opt-in to the ones they want.
 */
@Composable
fun SevenEve9FeaturesScreen(
    smartRepliesEnabled: Boolean,
    messageSummaryEnabled: Boolean,
    autoTranslateEnabled: Boolean,
    voiceToTextEnabled: Boolean,
    smartSearchEnabled: Boolean,
    onSetSmartReplies: (Boolean) -> Unit,
    onSetMessageSummary: (Boolean) -> Unit,
    onSetAutoTranslate: (Boolean) -> Unit,
    onSetVoiceToText: (Boolean) -> Unit,
    onSetSmartSearch: (Boolean) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        containerColor = TelegramChatListBg,
        topBar = { SettingsTopBar(title = "7eve9Chat Features", onBack = onBack) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 12.dp)
        ) {
            // === Hero ===
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                androidx.compose.ui.graphics.Brush.horizontalGradient(
                                    listOf(Color(0xFF7B68EE), Color(0xFF50A7EA))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(44.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "AI-Powered Messaging",
                        color = TelegramTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Smart features to make your conversations faster, clearer and more expressive.",
                        color = TelegramTextSecondary,
                        fontSize = 13.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }

            // === Feature toggles ===
            item {
                SettingsCard {
                    SectionHeader("Smart Features")
                    FeatureRow(
                        icon = Icons.Default.Psychology,
                        iconColor = Color(0xFF7B68EE),
                        title = "Smart Replies",
                        subtitle = "Suggest quick AI-crafted replies based on the conversation",
                        checked = smartRepliesEnabled,
                        onCheckedChange = onSetSmartReplies
                    )
                    FeatureRow(
                        icon = Icons.Default.Insights,
                        iconColor = Color(0xFF50A7EA),
                        title = "Message Summary",
                        subtitle = "Summarize long chats and channels with one tap",
                        checked = messageSummaryEnabled,
                        onCheckedChange = onSetMessageSummary
                    )
                    FeatureRow(
                        icon = Icons.Default.Translate,
                        iconColor = Color(0xFF58D68D),
                        title = "Auto-Translate",
                        subtitle = "Translate incoming messages to your language automatically",
                        checked = autoTranslateEnabled,
                        onCheckedChange = onSetAutoTranslate
                    )
                    FeatureRow(
                        icon = Icons.Default.GraphicEq,
                        iconColor = Color(0xFFF39C12),
                        title = "Voice-to-Text",
                        subtitle = "Transcribe voice messages so you can read them silently",
                        checked = voiceToTextEnabled,
                        onCheckedChange = onSetVoiceToText
                    )
                    FeatureRow(
                        icon = Icons.Default.Speed,
                        iconColor = Color(0xFFEC7063),
                        title = "Smart Search",
                        subtitle = "Search messages by meaning, not just by keyword",
                        checked = smartSearchEnabled,
                        onCheckedChange = onSetSmartSearch
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }

            // === Info ===
            item {
                SettingsCard {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Column {
                            Text(
                                text = "✨ About 7eve9Chat Features",
                                color = TelegramPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "These features use on-device and server-side AI models " +
                                    "to enhance your messaging. Your messages stay end-to-end " +
                                    "private — only the requested feature touches the content, " +
                                    "and nothing is stored after processing.",
                                color = TelegramTextSecondary,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun FeatureRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(iconColor.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = TelegramTextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = TelegramTextSecondary,
                fontSize = 13.sp
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
