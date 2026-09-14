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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.api.ApiSession
import com.example.data.api.ApiUser
import com.example.ui.components.TelegramBottomNav
import com.example.ui.locale.LocalAppStrings
import com.example.ui.theme.appPalette

@Composable
fun SettingsScreen(
    meUser: ApiUser?,
    currentUserAvatarUrl: String?,
    selectedBottomNavIndex: Int = 2,
    onSelectBottomNav: (Int) -> Unit,
    onLogout: () -> Unit,
    onOpenRoute: (String) -> Unit,
    onAskQuestion: () -> Unit
) {
    val palette = appPalette
    val strings = LocalAppStrings.current

    val name = meUser?.name ?: meUser?.username ?: "User"
    val phone = meUser?.phone ?: "No phone"
    val username = meUser?.username?.let { "@$it" } ?: ""

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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = strings.settingsTitle,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = palette.textPrimary
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                // User Card
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .background(palette.surface, RoundedCornerShape(16.dp))
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(palette.primary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!currentUserAvatarUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = currentUserAvatarUrl,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Text(
                                    text = name.take(1).uppercase(),
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = palette.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column {
                            Text(
                                text = name,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = palette.textPrimary
                            )
                            Text(
                                text = phone,
                                fontSize = 14.sp,
                                color = palette.textSecondary
                            )
                            if (username.isNotBlank()) {
                                Text(
                                    text = username,
                                    fontSize = 13.sp,
                                    color = palette.primary
                                )
                            }
                        }
                    }
                }

                // Settings Items
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .background(palette.surface, RoundedCornerShape(16.dp))
                    ) {
                        SettingsRow(
                            icon = Icons.Default.ChatBubbleOutline,
                            title = strings.chatSettings,
                            onClick = { onOpenRoute(SettingsRoute.CHAT) }
                        )
                        SettingsRow(
                            icon = Icons.Default.Lock,
                            title = strings.privacySecurity,
                            onClick = { onOpenRoute(SettingsRoute.PRIVACY) }
                        )
                        SettingsRow(
                            icon = Icons.Default.Storage,
                            title = strings.dataStorage,
                            onClick = { onOpenRoute(SettingsRoute.DATA) }
                        )
                        SettingsRow(
                            icon = Icons.Default.Folder,
                            title = strings.chatFolders,
                            onClick = { onOpenRoute(SettingsRoute.FOLDERS) }
                        )
                        SettingsRow(
                            icon = Icons.Default.Devices,
                            title = strings.devices,
                            onClick = { onOpenRoute(SettingsRoute.DEVICES) }
                        )
                        SettingsRow(
                            icon = Icons.Default.BatteryChargingFull,
                            title = strings.powerSaving,
                            onClick = { onOpenRoute(SettingsRoute.POWER) }
                        )
                        SettingsRow(
                            icon = Icons.Default.Language,
                            title = strings.language,
                            onClick = { onOpenRoute(SettingsRoute.LANGUAGE) }
                        )
                    }
                }

                // Help & Logout
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .background(palette.surface, RoundedCornerShape(16.dp))
                    ) {
                        SettingsRow(
                            icon = Icons.Default.HelpOutline,
                            title = strings.askQuestion,
                            onClick = onAskQuestion
                        )
                        SettingsRow(
                            icon = Icons.Default.Logout,
                            title = strings.logOut,
                            tint = Color(0xFFEF4444),
                            onClick = onLogout
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(88.dp))
                }
            }
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
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    tint: Color? = null,
    onClick: () -> Unit
) {
    val palette = appPalette
    val iconColor = tint ?: palette.primary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            fontSize = 15.sp,
            color = tint ?: palette.textPrimary,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
    }
}

// Sub-screen: Language
@Composable
fun LanguageScreen(
    selectedLanguage: String,
    onSelectLanguage: (String) -> Unit,
    onBack: () -> Unit
) {
    val palette = appPalette
    val languages = listOf(
        Pair("English", "en"),
        Pair("فارسی (Persian)", "fa")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.chatListBg)
            .statusBarsPadding()
    ) {
        SubScreenHeader(title = "Language / زبان", onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(palette.surface, RoundedCornerShape(16.dp))
        ) {
            languages.forEach { (name, code) ->
                val isSelected = selectedLanguage == code ||
                    (code == "fa" && (selectedLanguage == "persian" || selectedLanguage == "farsi")) ||
                    (code == "en" && selectedLanguage != "fa" && selectedLanguage != "persian" && selectedLanguage != "farsi")

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectLanguage(code) }
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = name,
                        fontSize = 16.sp,
                        color = if (isSelected) palette.primary else palette.textPrimary,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )

                    if (isSelected) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = palette.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

// Sub-screen: Privacy
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
    val palette = appPalette

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.chatListBg)
            .statusBarsPadding()
    ) {
        SubScreenHeader(title = "Privacy & Security", onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(palette.surface, RoundedCornerShape(16.dp))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Hide Phone Number", fontSize = 15.sp, color = palette.textPrimary)
                    Text("Never show your phone to strangers", fontSize = 12.sp, color = palette.textSecondary)
                }
                Switch(
                    checked = isPhoneHidden,
                    onCheckedChange = onSetPhoneHidden,
                    colors = SwitchDefaults.colors(checkedThumbColor = palette.primary)
                )
            }
        }
    }
}

// Sub-screen: Chat Settings
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
    val palette = appPalette
    var sliderValue by remember { mutableFloatStateOf(messageTextSize.toFloat()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.chatListBg)
            .statusBarsPadding()
    ) {
        SubScreenHeader(title = "Chat Settings", onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(palette.surface, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Dark Mode", fontSize = 15.sp, color = palette.textPrimary)
                Switch(
                    checked = isDarkMode,
                    onCheckedChange = { onToggleDarkMode() },
                    colors = SwitchDefaults.colors(checkedThumbColor = palette.primary)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Text Size: ${sliderValue.toInt()} sp", fontSize = 14.sp, color = palette.textPrimary)
            Slider(
                value = sliderValue,
                onValueChange = {
                    sliderValue = it
                    onSetTextSize(it.toInt())
                },
                valueRange = 12f..24f,
                steps = 6
            )
        }
    }
}

// Sub-screen: Data & Storage
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
    val palette = appPalette

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.chatListBg)
            .statusBarsPadding()
    ) {
        SubScreenHeader(title = "Data & Storage", onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(palette.surface, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Button(
                onClick = onClearCache,
                colors = ButtonDefaults.buttonColors(containerColor = palette.primary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Clear Cache")
            }
        }
    }
}

// Sub-screen: Folders
@Composable
fun ChatFoldersScreen(
    folders: List<ChatFolder>,
    showFolderTags: Boolean,
    onAddFolder: (String) -> Unit,
    onDeleteFolder: (String) -> Unit,
    onToggleFolderTags: (Boolean) -> Unit,
    onBack: () -> Unit
) {
    val palette = appPalette

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.chatListBg)
            .statusBarsPadding()
    ) {
        SubScreenHeader(title = "Chat Folders", onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(palette.surface, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Text("Organize your chats into custom folders", color = palette.textSecondary, fontSize = 14.sp)
        }
    }
}

// Sub-screen: Devices
@Composable
fun DevicesScreen(
    sessions: List<ApiSession>,
    isLoading: Boolean,
    errorMessage: String?,
    onLaunchLoad: () -> Unit,
    onLogoutAllOthers: () -> Unit,
    onBack: () -> Unit
) {
    val palette = appPalette

    LaunchedEffect(Unit) {
        onLaunchLoad()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.chatListBg)
            .statusBarsPadding()
    ) {
        SubScreenHeader(title = "Devices", onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(palette.surface, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = palette.primary)
            } else {
                Text("Active Sessions (${sessions.size})", color = palette.textPrimary, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onLogoutAllOthers,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Terminate All Other Sessions")
                }
            }
        }
    }
}

// Sub-screen: Power Saving
@Composable
fun PowerSavingScreen(
    powerSavingEnabled: Boolean,
    powerLowQuality: Boolean,
    powerDisableAnimations: Boolean,
    powerDisableAutoplay: Boolean,
    onSetPowerSaving: (Boolean) -> Unit,
    onSetLowQuality: (Boolean) -> Unit,
    onSetDisableAnimations: (Boolean) -> Unit,
    onSetDisableAutoplay: (Boolean) -> Unit,
    onBack: () -> Unit
) {
    val palette = appPalette

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.chatListBg)
            .statusBarsPadding()
    ) {
        SubScreenHeader(title = "Power Saving", onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(palette.surface, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Enable Power Saving Mode", fontSize = 15.sp, color = palette.textPrimary)
                Switch(
                    checked = powerSavingEnabled,
                    onCheckedChange = onSetPowerSaving,
                    colors = SwitchDefaults.colors(checkedThumbColor = palette.primary)
                )
            }
        }
    }
}

@Composable
private fun SubScreenHeader(
    title: String,
    onBack: () -> Unit
) {
    val palette = appPalette

    Surface(
        color = palette.glassHeader,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = palette.textPrimary
                )
            }
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = palette.textPrimary,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}
