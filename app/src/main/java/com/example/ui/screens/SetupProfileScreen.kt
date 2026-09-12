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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.components.AvatarView
import com.example.ui.theme.TelegramDarkBg
import com.example.ui.theme.TelegramPrimary
import com.example.ui.theme.TelegramSurface
import com.example.ui.theme.TelegramSurfaceVariant
import com.example.ui.theme.TelegramTextMuted
import com.example.ui.theme.TelegramTextPrimary
import com.example.ui.theme.TelegramTextSecondary

/**
 * First-time setup screen — shown when isNewUser=true OR when the user has no
 * name/username set. The user must enter:
 *   - Name (required, can't be empty)
 *   - Username (required, only letters/numbers/underscore, 5+ chars)
 *   - Avatar (optional — launches image picker)
 *
 * On submit, calls PUT /auth/profile with { name, username, avatar }.
 * If avatar was picked, also calls multipart PUT /auth/profile with the file.
 */
@Composable
fun SetupProfileScreen(
    initialName: String? = null,
    initialUsername: String? = null,
    initialAvatarUrl: String? = null,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    prefillPhone: String? = null,
    onSubmit: (name: String, username: String, avatarUri: String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf(initialName.orEmpty()) }
    var username by remember { mutableStateOf(initialUsername.orEmpty()) }
    var avatarUri by remember { mutableStateOf<String?>(initialAvatarUrl) }
    val keyboard = LocalSoftwareKeyboardController.current

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        avatarUri = uri?.toString()
    }

    // Username: only letters, numbers, underscore. Min 5 chars.
    val usernameRegex = remember { Regex("^[a-zA-Z0-9_]{5,32}$") }
    val isUsernameValid = usernameRegex.matches(username)
    val isNameValid = name.trim().length >= 2
    val canSubmit = isNameValid && isUsernameValid && !isLoading

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TelegramDarkBg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title
            Text(
                text = "Set Up Your Profile",
                color = TelegramTextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Enter your name and choose a username so others can find you on 7eve9Chat.",
                color = TelegramTextSecondary,
                fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Avatar picker (optional)
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(TelegramSurfaceVariant)
                    .border(2.dp, TelegramPrimary.copy(alpha = 0.5f), CircleShape)
                    .clickable { imagePicker.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                when {
                    !avatarUri.isNullOrBlank() && avatarUri!!.startsWith("content://") -> {
                        AsyncImage(
                            model = Uri.parse(avatarUri),
                            contentDescription = "Avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    !avatarUri.isNullOrBlank() -> {
                        AsyncImage(
                            model = avatarUri,
                            contentDescription = "Avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    else -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Pick avatar",
                                tint = TelegramTextMuted,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Photo (optional)",
                                color = TelegramTextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Name field
            OutlinedTextField(
                value = name,
                onValueChange = { name = it.take(50) },
                label = { Text("Name", color = TelegramTextSecondary) },
                singleLine = true,
                isError = name.isNotEmpty() && !isNameValid,
                supportingText = {
                    if (name.isNotEmpty() && !isNameValid) {
                        Text("Name must be at least 2 characters", color = Color(0xFFFF5252), fontSize = 12.sp)
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TelegramPrimary,
                    unfocusedBorderColor = TelegramSurfaceVariant,
                    focusedTextColor = TelegramTextPrimary,
                    unfocusedTextColor = TelegramTextPrimary,
                    cursorColor = TelegramPrimary
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Username field
            OutlinedTextField(
                value = username,
                onValueChange = { newValue ->
                    // Strip invalid characters as user types
                    val filtered = newValue.filter { it.isLetterOrDigit() || it == '_' }.take(32)
                    username = filtered
                },
                label = { Text("Username", color = TelegramTextSecondary) },
                prefix = { Text("@", color = TelegramTextMuted) },
                singleLine = true,
                isError = username.isNotEmpty() && !isUsernameValid,
                supportingText = {
                    when {
                        username.isEmpty() -> {
                            Text("5-32 characters. Letters, numbers, underscore only.", color = TelegramTextMuted, fontSize = 12.sp)
                        }
                        !isUsernameValid -> {
                            Text("Must be 5-32 chars, letters/numbers/_ only", color = Color(0xFFFF5252), fontSize = 12.sp)
                        }
                        else -> {
                            Text("✓ Available", color = Color(0xFF4CAF50), fontSize = 12.sp)
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Ascii,
                    imeAction = ImeAction.Done
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TelegramPrimary,
                    unfocusedBorderColor = TelegramSurfaceVariant,
                    focusedTextColor = TelegramTextPrimary,
                    unfocusedTextColor = TelegramTextPrimary,
                    cursorColor = TelegramPrimary
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )

            // Phone info (read-only)
            if (!prefillPhone.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Phone: +$prefillPhone",
                    color = TelegramTextMuted,
                    fontSize = 12.sp
                )
            }

            // Error message
            if (!errorMessage.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = errorMessage,
                    color = Color(0xFFFF5252),
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Submit button
            Button(
                onClick = {
                    keyboard?.hide()
                    if (canSubmit) {
                        onSubmit(name.trim(), username.trim(), avatarUri)
                    }
                },
                enabled = canSubmit,
                colors = ButtonDefaults.buttonColors(
                    containerColor = TelegramPrimary,
                    disabledContainerColor = TelegramPrimary.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(22.dp)
                    )
                } else {
                    Text(
                        text = "Continue",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
