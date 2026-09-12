package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Forward
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MessageItem
import com.example.ui.theme.TelegramGlassBorder
import com.example.ui.theme.TelegramPrimary
import com.example.ui.theme.TelegramTextPrimary
import com.example.ui.theme.TelegramTextSecondary

@Composable
fun MessageContextMenuPopup(
    message: MessageItem?,
    visible: Boolean,
    onDismiss: () -> Unit,
    onReaction: (emoji: String) -> Unit,
    onReply: (MessageItem) -> Unit,
    onPin: (MessageItem) -> Unit,
    onEdit: (MessageItem) -> Unit,
    onForward: (MessageItem) -> Unit,
    onDelete: (MessageItem) -> Unit
) {
    if (!visible || message == null) return

    val clipboardManager = LocalClipboardManager.current
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = {
                Text(
                    text = "Delete Message",
                    color = TelegramTextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete this message? This action cannot be undone.",
                    color = TelegramTextSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmDialog = false
                        onDismiss()
                        onDelete(message)
                    }
                ) {
                    Text("Delete", color = Color(0xFFFF5252), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel", color = TelegramPrimary)
                }
            },
            containerColor = Color(0xFF1E2C3A),
            shape = RoundedCornerShape(20.dp)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.55f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = scaleIn(
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)
            ) + fadeIn(),
            exit = scaleOut() + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .width(280.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xF0172330))
                    .border(1.2.dp, TelegramGlassBorder, RoundedCornerShape(24.dp))
                    .shadow(16.dp, RoundedCornerShape(24.dp))
                    .clickable(enabled = false) {}
                    .padding(vertical = 12.dp)
            ) {
                // Top Emoji Quick Reactions Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val reactions = listOf("👍", "❤️", "🔥", "😂", "👏", "⚡", "🎉")
                    reactions.forEach { emoji ->
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .clickable {
                                    onReaction(emoji)
                                    onDismiss()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = emoji, fontSize = 20.sp)
                        }
                    }
                }

                HorizontalDivider(
                    color = Color.White.copy(alpha = 0.12f),
                    thickness = 0.8.dp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                // Menu Items
                ContextMenuItem(
                    icon = Icons.AutoMirrored.Filled.Reply,
                    title = "Reply",
                    onClick = {
                        onDismiss()
                        onReply(message)
                    }
                )

                if (!message.text.isNullOrBlank()) {
                    ContextMenuItem(
                        icon = Icons.Default.ContentCopy,
                        title = "Copy Text",
                        onClick = {
                            clipboardManager.setText(AnnotatedString(message.text))
                            onDismiss()
                        }
                    )
                }

                ContextMenuItem(
                    icon = Icons.Default.PushPin,
                    title = "Pin",
                    onClick = {
                        onDismiss()
                        onPin(message)
                    }
                )

                if (message.isOutgoing && !message.text.isNullOrBlank()) {
                    ContextMenuItem(
                        icon = Icons.Default.Edit,
                        title = "Edit",
                        onClick = {
                            onDismiss()
                            onEdit(message)
                        }
                    )
                }

                ContextMenuItem(
                    icon = Icons.Default.Forward,
                    title = "Forward",
                    onClick = {
                        onDismiss()
                        onForward(message)
                    }
                )

                HorizontalDivider(
                    color = Color.White.copy(alpha = 0.12f),
                    thickness = 0.8.dp,
                    modifier = Modifier.padding(vertical = 6.dp)
                )

                ContextMenuItem(
                    icon = Icons.Default.Delete,
                    title = "Delete",
                    titleColor = Color(0xFFFF5252),
                    iconColor = Color(0xFFFF5252),
                    onClick = {
                        showDeleteConfirmDialog = true
                    }
                )
            }
        }
    }
}

@Composable
private fun ContextMenuItem(
    icon: ImageVector,
    title: String,
    titleColor: Color = TelegramTextPrimary,
    iconColor: Color = TelegramTextSecondary,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = iconColor,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = title,
            color = titleColor,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
