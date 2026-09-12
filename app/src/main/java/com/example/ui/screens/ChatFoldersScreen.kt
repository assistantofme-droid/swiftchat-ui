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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
data class ChatFolder(
    val id: String,
    val name: String,
    val isDefault: Boolean = false
)
@Composable
fun ChatFoldersScreen(
    folders: List<ChatFolder>,
    showFolderTags: Boolean,
    onAddFolder: (String) -> Unit,
    onDeleteFolder: (String) -> Unit,
    onToggleFolderTags: (Boolean) -> Unit,
    onBack: () -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var newFolderName by remember { mutableStateOf("") }
    // Recommended folders — preset suggestions the user can add in one tap
    val recommendedFolders = remember {
        listOf(
            ChatFolder(id = "rec_unread", name = "Unread"),
            ChatFolder(id = "rec_personal", name = "Personal"),
            ChatFolder(id = "rec_groups", name = "Groups"),
            ChatFolder(id = "rec_channels", name = "Channels"),
            ChatFolder(id = "rec_bots", name = "Bots")
        )
    }.filter { rec -> folders.none { it.name.equals(rec.name, ignoreCase = true) } }
    Scaffold(
        containerColor = appPalette.chatListBg,
        topBar = { SettingsTopBar(title = "Chat Folders", onBack = onBack) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 12.dp)
        ) {
            // === Hero illustration ===
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(appPalette.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            tint = appPalette.primary,
                            modifier = Modifier.size(56.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Create folders for different groups of chats and quickly switch between them.",
                        color = appPalette.textSecondary,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
            // === Recommended Folders ===
            if (recommendedFolders.isNotEmpty()) {
                item {
                    SettingsCard {
                        SectionHeader("Recommended Folders")
                        recommendedFolders.forEach { folder ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = folder.name,
                                        color = appPalette.textPrimary,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                        text = folderDescription(folder.name),
                                        color = appPalette.textSecondary,
                                        fontSize = 13.sp
                                }
                                Button(
                                    onClick = { onAddFolder(folder.name) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = appPalette.primary
                                    ),
                                    shape = RoundedCornerShape(20.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                        horizontal = 20.dp,
                                        vertical = 6.dp
                                ) {
                                        text = "Add",
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                            }
                        }
                item { Spacer(modifier = Modifier.height(12.dp)) }
            // === Custom folders ===
                SettingsCard {
                    SectionHeader("Your Folders")
                    if (folders.isEmpty()) {
                        Text(
                            text = "No folders yet. Create one to organize your chats.",
                            color = appPalette.textSecondary,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
                    } else {
                        folders.forEachIndexed { index, folder ->
                            FolderRow(
                                folder = folder,
                                onDelete = { onDeleteFolder(folder.id) }
                            )
                            if (index < folders.size - 1) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp)
                                        .height(0.5.dp)
                                        .background(appPalette.textMuted.copy(alpha = 0.2f))
                                )
                    // Create new folder row
                    Row(
                            .fillMaxWidth()
                            .clickable { showCreateDialog = true }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                            imageVector = Icons.Default.Add,
                            modifier = Modifier.size(24.dp)
                        Spacer(modifier = Modifier.width(16.dp))
                            text = "Create New Folder",
                            color = appPalette.primary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
            item { Spacer(modifier = Modifier.height(12.dp)) }
            // === Show Folder Tags toggle ===
                    ToggleRow(
                        icon = Icons.Default.Folder,
                        iconColor = appPalette.primary,
                        title = "Show Folder Tags",
                        subtitle = "Display folder names for each chat in the chat list",
                        checked = showFolderTags,
                        onCheckedChange = onToggleFolderTags
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
    // Create folder dialog
    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = {
                showCreateDialog = false
                newFolderName = ""
            },
            containerColor = appPalette.surface,
            titleContentColor = appPalette.textPrimary,
            title = { Text("New Folder", color = appPalette.textPrimary) },
            text = {
                OutlinedTextField(
                    value = newFolderName,
                    onValueChange = { newFolderName = it.take(32) },
                    placeholder = { Text("Folder name", color = appPalette.textMuted) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = appPalette.primary,
                        unfocusedBorderColor = appPalette.textMuted.copy(alpha = 0.3f),
                        focusedTextColor = appPalette.textPrimary,
                        unfocusedTextColor = appPalette.textPrimary,
                        cursorColor = appPalette.primary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newFolderName.isNotBlank()) {
                            onAddFolder(newFolderName.trim())
                            newFolderName = ""
                        showCreateDialog = false
                    Text("Create", color = appPalette.primary, fontWeight = FontWeight.SemiBold)
            dismissButton = {
                TextButton(onClick = {
                    showCreateDialog = false
                    newFolderName = ""
                }) {
                    Text("Cancel", color = appPalette.textSecondary)
}
private fun FolderRow(folder: ChatFolder, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.DragHandle,
            contentDescription = "Drag",
            tint = appPalette.textMuted,
            modifier = Modifier.size(24.dp)
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = folder.name,
            color = appPalette.textPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
            imageVector = Icons.Default.MoreVert,
            contentDescription = "More",
            tint = appPalette.textSecondary,
                .size(24.dp)
                .clickable(onClick = onDelete)
private fun folderDescription(name: String): String = when {
    name.equals("Unread", ignoreCase = true) -> "New messages from all chats."
    name.equals("Personal", ignoreCase = true) -> "One-on-one private conversations."
    name.equals("Groups", ignoreCase = true) -> "Group conversations you're a member of."
    name.equals("Channels", ignoreCase = true) -> "Channels you're subscribed to."
    name.equals("Bots", ignoreCase = true) -> "Conversations with bots."
    else -> "Custom folder"
