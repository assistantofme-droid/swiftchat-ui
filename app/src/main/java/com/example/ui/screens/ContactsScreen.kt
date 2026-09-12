package com.example.ui.screens

import com.example.ui.theme.appPalette
import android.Manifest
import android.content.pm.PackageManager
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.api.ApiContact
import com.example.ui.components.AvatarView
import com.example.ui.components.TelegramBottomNav

/**
 * Contacts screen — shows REAL device contacts filtered down to only
 * registered 7eve9Chat users. Per the user's spec:
 *
 *   - Search bar at the top
 *   - New Group button
 *   - New Channel button
 *   - Below: list of device contacts who are registered users
 *     (POST /auth/check-contacts returns only registered users)
 *
 * Tapping a contact opens a private chat with them via
 * GET /messages/conversations/{userId}.
 */
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
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var query by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var showCreateGroupDialog by remember { mutableStateOf(false) }
    var showCreateChannelDialog by remember { mutableStateOf(false) }
    var hasContactsPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) ==
                PackageManager.PERMISSION_GRANTED
        )
    }

    val contactsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasContactsPermission = granted
        if (granted) onLaunchLoad()
    }

    // First time the screen is shown: ask for contacts permission + load
    LaunchedEffect(Unit) {
        if (hasContactsPermission) {
            onLaunchLoad()
        } else {
            contactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        }
    }

    // Show add-contact feedback as a snackbar
    LaunchedEffect(addContactStatus) {
        if (!addContactStatus.isNullOrBlank()) {
            snackbarHostState.showSnackbar(addContactStatus)
            onClearAddStatus()
        }
    }

    val filtered = remember(contacts, query) {
        if (query.isBlank()) contacts
        else contacts.filter {
            (it.name?.contains(query, ignoreCase = true) == true) ||
                    (it.username?.contains(query, ignoreCase = true) == true) ||
                    (it.phone?.contains(query) == true)
        }
    }

    Scaffold(
        containerColor = appPalette.chatListBg,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { ContactsHeader() },
        bottomBar = {
            TelegramBottomNav(
                selectedIndex = selectedBottomNavIndex,
                onSelect = onSelectBottomNav,
                currentUserAvatarUrl = currentUserAvatarUrl,
                modifier = Modifier.fillMaxWidth()
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = appPalette.primary,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = "Add contact")
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                SearchBar(query = query, onQueryChange = { query = it })

                // === New Group / New Channel buttons ===
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ActionChip(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Group,
                        label = "New Group",
                        onClick = { showCreateGroupDialog = true }
                    )
                    ActionChip(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Campaign,
                        label = "New Channel",
                        onClick = { showCreateChannelDialog = true }
                    )
                }

                when {
                    !hasContactsPermission -> {
                        PermissionPrompt(
                            onAllow = { contactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS) }
                        )
                    }
                    isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = appPalette.primary)
                        }
                    }
                    errorMessage != null && contacts.isEmpty() -> {
                        ErrorState(
                            message = errorMessage,
                            onRetry = { onLaunchLoad() }
                        )
                    }
                    filtered.isEmpty() && contacts.isNotEmpty() -> {
                        EmptyState(message = "No contacts match \"$query\"")
                    }
                    filtered.isEmpty() -> {
                        EmptyState(
                            message = "None of your device contacts are on 7eve9Chat yet. " +
                                "Tap + to invite someone by username or phone."
                        )
                    }
                    else -> {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(filtered, key = { it._id ?: it.username ?: it.phone ?: it.name ?: "" }) { contact ->
                                ContactRow(contact = contact, onClick = { onContactClick(contact) })
                            }
                            item { Spacer(modifier = Modifier.height(80.dp)) }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddContactDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { identifier ->
                onAddContact(identifier)
                showAddDialog = false
            }
        )
    }

    if (showCreateGroupDialog) {
        CreateConversationDialog(
            title = "New Group",
            type = "group",
            onDismiss = { showCreateGroupDialog = false },
            onCreate = { name, type, description ->
                onCreateGroup(name, type, description)
                showCreateGroupDialog = false
            }
        )
    }

    if (showCreateChannelDialog) {
        CreateConversationDialog(
            title = "New Channel",
            type = "channel",
            onDismiss = { showCreateChannelDialog = false },
            onCreate = { name, type, description ->
                onCreateGroup(name, type, description)
                showCreateChannelDialog = false
            }
        )
    }
}

@Composable
private fun CreateConversationDialog(
    title: String,
    type: String,
    onDismiss: () -> Unit,
    onCreate: (name: String, type: String, description: String?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = appPalette.surface,
        titleContentColor = appPalette.textPrimary,
        title = { Text(title, color = appPalette.textPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it.take(64) },
                    label = { Text("Name", color = appPalette.textSecondary) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = appPalette.primary,
                        unfocusedBorderColor = appPalette.surfaceVariant,
                        focusedTextColor = appPalette.textPrimary,
                        unfocusedTextColor = appPalette.textPrimary,
                        cursorColor = appPalette.primary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it.take(255) },
                    label = { Text("Description (optional)", color = appPalette.textSecondary) },
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = appPalette.primary,
                        unfocusedBorderColor = appPalette.surfaceVariant,
                        focusedTextColor = appPalette.textPrimary,
                        unfocusedTextColor = appPalette.textPrimary,
                        cursorColor = appPalette.primary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) {
                        onCreate(
                            name.trim(),
                            type,
                            description.takeIf { it.isNotBlank() }
                        )
                    }
                }
            ) { Text("Create", color = appPalette.primary, fontWeight = FontWeight.SemiBold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = appPalette.textSecondary)
            }
        }
    )
}

@Composable
private fun ContactsHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(appPalette.chatListBg)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Contacts",
            color = appPalette.textPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = { /* future: sort options */ }) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Sort,
                contentDescription = "Sort",
                tint = appPalette.textSecondary
            )
        }
    }
}

@Composable
private fun SearchBar(query: String, onQueryChange: (String) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        TextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = { Text("Search Contacts", color = appPalette.textSecondary) },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, tint = appPalette.textSecondary)
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = appPalette.textSecondary)
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = appPalette.surface,
                unfocusedContainerColor = appPalette.surface,
                cursorColor = appPalette.primary,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor = appPalette.textPrimary,
                unfocusedTextColor = appPalette.textPrimary
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun ActionChip(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        color = appPalette.surface,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .height(52.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = appPalette.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                color = appPalette.textPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ContactRow(contact: ApiContact, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AvatarView(
            avatarUrl = contact.avatar,
            title = contact.name ?: contact.username,
            size = 48.dp
        )
        Spacer(modifier = Modifier.size(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = contact.name ?: contact.username ?: "Unknown",
                color = appPalette.textPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = contact.username?.let { "@$it" } ?: contact.phone ?: "last seen recently",
                color = appPalette.textSecondary,
                fontSize = 14.sp
            )
        }
        if (contact.isVerified == true) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .background(appPalette.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("✓", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun AddContactDialog(
    onDismiss: () -> Unit,
    onAdd: (String) -> Unit
) {
    var identifier by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = appPalette.surface,
        titleContentColor = appPalette.textPrimary,
        title = { Text("Add Contact", color = appPalette.textPrimary) },
        text = {
            Column {
                Text(
                    text = "Enter phone number or @username",
                    color = appPalette.textSecondary,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                OutlinedTextField(
                    value = identifier,
                    onValueChange = { identifier = it },
                    placeholder = { Text("989123456789 or @alice", color = appPalette.textMuted) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = appPalette.primary,
                        unfocusedBorderColor = appPalette.surfaceVariant,
                        focusedTextColor = appPalette.textPrimary,
                        unfocusedTextColor = appPalette.textPrimary,
                        cursorColor = appPalette.primary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (identifier.isNotBlank()) onAdd(identifier.trim())
                }
            ) { Text("Add", color = appPalette.primary, fontWeight = FontWeight.SemiBold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = appPalette.textSecondary)
            }
        }
    )
}

@Composable
private fun EmptyState(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Call,
                contentDescription = null,
                tint = appPalette.textMuted,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = message,
                color = appPalette.textSecondary,
                fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
        }
    }
}

@Composable
private fun PermissionPrompt(onAllow: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.PersonAdd,
                contentDescription = null,
                tint = appPalette.textMuted,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Allow access to your contacts to see who's on 7eve9Chat",
                color = appPalette.textSecondary,
                fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = onAllow) {
                Text("Allow Contacts", color = appPalette.primary, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = message,
                color = appPalette.textSecondary,
                fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            TextButton(onClick = onRetry) {
                Text("Retry", color = appPalette.primary, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
