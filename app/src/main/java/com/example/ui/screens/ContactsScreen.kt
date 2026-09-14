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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.api.ApiContact
import com.example.ui.components.TelegramBottomNav
import com.example.ui.locale.LocalAppStrings
import com.example.ui.theme.appPalette

@Composable
fun ContactsScreen(
    contacts: List<ApiContact>,
    isLoading: Boolean,
    errorMessage: String?,
    addContactStatus: String?,
    currentUserAvatarUrl: String?,
    onLaunchLoad: () -> Unit,
    onAddContact: (String) -> Unit,
    onClearAddStatus: () -> Unit,
    onSelectBottomNav: (Int) -> Unit,
    onContactClick: (ApiContact) -> Unit,
    onCreateGroup: (name: String, type: String, description: String?) -> Unit,
    selectedBottomNavIndex: Int = 1
) {
    val palette = appPalette
    val strings = LocalAppStrings.current

    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var newContactPhone by remember { mutableStateOf("") }

    var showGroupDialog by remember { mutableStateOf(false) }
    var groupName by remember { mutableStateOf("") }
    var groupType by remember { mutableStateOf("group") }

    LaunchedEffect(Unit) {
        onLaunchLoad()
    }

    val filteredContacts = remember(contacts, searchQuery) {
        if (searchQuery.isBlank()) contacts
        else contacts.filter {
            (it.name?.contains(searchQuery, ignoreCase = true) == true) ||
            (it.phone?.contains(searchQuery, ignoreCase = true) == true) ||
            (it.username?.contains(searchQuery, ignoreCase = true) == true)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.chatListBg)
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = strings.contactsTitle,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = palette.textPrimary
                )

                Row {
                    TextButton(onClick = { showGroupDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.GroupAdd,
                            contentDescription = "New Group",
                            tint = palette.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(strings.newGroup, color = palette.primary, fontSize = 14.sp)
                    }
                }
            }

            // Search input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(strings.searchContacts, color = palette.textSecondary) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = palette.textSecondary)
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = palette.surfaceVariant.copy(alpha = 0.5f),
                    unfocusedContainerColor = palette.surfaceVariant.copy(alpha = 0.5f),
                    focusedBorderColor = palette.primary,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = palette.textPrimary,
                    unfocusedTextColor = palette.textPrimary
                )
            )

            // Status or error banner
            if (!addContactStatus.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .background(palette.primary.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Text(text = addContactStatus, color = palette.primary, fontSize = 13.sp)
                }
            }

            // Contacts List
            if (isLoading && contacts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = palette.primary)
                }
            } else if (filteredContacts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = strings.noContactsFound,
                            color = palette.textSecondary,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { showAddDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = palette.primary)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(strings.addContact)
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(filteredContacts, key = { it._id ?: it.phone ?: it.hashCode().toString() }) { contact ->
                        ContactRow(
                            contact = contact,
                            onClick = { onContactClick(contact) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(88.dp))
                    }
                }
            }
        }

        // Floating Action Button to Add Contact
        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = palette.primary,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 90.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Contact")
        }

        // Bottom Navigation
        TelegramBottomNav(
            selectedIndex = selectedBottomNavIndex,
            onSelect = onSelectBottomNav,
            currentUserAvatarUrl = currentUserAvatarUrl,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
        )

        // Add Contact Dialog
        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text(strings.addContact, color = palette.textPrimary) },
                text = {
                    Column {
                        Text("Enter phone number or user ID:", color = palette.textSecondary, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = newContactPhone,
                            onValueChange = { newContactPhone = it },
                            placeholder = { Text("+1234567890") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newContactPhone.isNotBlank()) {
                                onAddContact(newContactPhone.trim())
                                newContactPhone = ""
                                showAddDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = palette.primary)
                    ) {
                        Text("Add")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) {
                        Text("Cancel", color = palette.textSecondary)
                    }
                },
                containerColor = palette.surface
            )
        }

        // Create Group Dialog
        if (showGroupDialog) {
            AlertDialog(
                onDismissRequest = { showGroupDialog = false },
                title = { Text(if (groupType == "channel") strings.newChannel else strings.newGroup, color = palette.textPrimary) },
                text = {
                    Column {
                        OutlinedTextField(
                            value = groupName,
                            onValueChange = { groupName = it },
                            label = { Text("Title") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { groupType = "group" },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (groupType == "group") palette.primary else palette.surfaceVariant
                                )
                            ) {
                                Text("Group")
                            }
                            Button(
                                onClick = { groupType = "channel" },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (groupType == "channel") palette.primary else palette.surfaceVariant
                                )
                            ) {
                                Text("Channel")
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (groupName.isNotBlank()) {
                                onCreateGroup(groupName.trim(), groupType, null)
                                groupName = ""
                                showGroupDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = palette.primary)
                    ) {
                        Text("Create")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showGroupDialog = false }) {
                        Text("Cancel", color = palette.textSecondary)
                    }
                },
                containerColor = palette.surface
            )
        }
    }
}

@Composable
private fun ContactRow(
    contact: ApiContact,
    onClick: () -> Unit
) {
    val palette = appPalette
    val name = contact.name ?: contact.username ?: contact.phone ?: "User"
    val subtitle = if (!contact.phone.isNullOrBlank()) contact.phone else contact.username

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(palette.primary.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            if (!contact.avatar.isNullOrBlank()) {
                AsyncImage(
                    model = contact.avatar,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    text = name.take(1).uppercase(),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = palette.primary
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = palette.textPrimary
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = palette.textSecondary
                )
            }
        }
    }
}
