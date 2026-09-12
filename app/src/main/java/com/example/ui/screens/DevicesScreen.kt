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
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.api.ApiSession

@Composable
fun DevicesScreen(
    sessions: List<ApiSession>,
    isLoading: Boolean,
    errorMessage: String?,
    onLaunchLoad: () -> Unit,
    onLogoutAllOthers: () -> Unit,
    onBack: () -> Unit
) {
    var showLogoutAllDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = appPalette.chatListBg,
        topBar = { SettingsTopBar(title = "Devices", onBack = onBack) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 12.dp)
        ) {
            // === Current session ===
            item {
                SettingsCard {
                    SectionHeader("This Device")
                    val currentSession = sessions.firstOrNull { it.current == true }
                    SessionRow(
                        session = currentSession ?: ApiSession(
                            _id = "current",
                            deviceInfo = com.example.data.api.ApiSessionDevice(
                                platform = "android",
                                deviceName = android.os.Build.MODEL,
                                appVersion = "1.0"
                            ),
                            ipAddress = null,
                            lastActiveAt = null,
                            current = true
                        ),
                        isCurrent = true
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }

            // === Active sessions on other devices ===
            item {
                SettingsCard {
                    SectionHeader("Active Sessions")
                    when {
                        isLoading && sessions.isEmpty() -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = appPalette.primary)
                            }
                        }
                        errorMessage != null && sessions.isEmpty() -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = errorMessage,
                                    color = appPalette.textSecondary,
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                TextButton(onClick = onLaunchLoad) {
                                    Text("Retry", color = appPalette.primary)
                                }
                            }
                        }
                        sessions.none { it.current != true } -> {
                            Text(
                                text = "No other active sessions.",
                                color = appPalette.textSecondary,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
                            )
                        }
                        else -> {
                            sessions.filter { it.current != true }.forEach { session ->
                                SessionRow(session = session, isCurrent = false)
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }

            // === Logout all others ===
            item {
                SettingsCard {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showLogoutAllDialog = true }
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = null,
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "Log Out All Other Devices",
                            color = Color(0xFFEF4444),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }

    if (showLogoutAllDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutAllDialog = false },
            containerColor = appPalette.surface,
            titleContentColor = appPalette.textPrimary,
            title = { Text("Log out all other devices?", color = appPalette.textPrimary) },
            text = {
                Text(
                    text = "This will end all other active sessions on your account. " +
                        "You'll stay logged in on this device.",
                    color = appPalette.textSecondary,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onLogoutAllOthers()
                        showLogoutAllDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFEF4444)
                    )
                ) {
                    Text("Log Out All", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutAllDialog = false }) {
                    Text("Cancel", color = appPalette.textSecondary)
                }
            }
        )
    }
}

@Composable
private fun SessionRow(session: ApiSession, isCurrent: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val icon = when (session.deviceInfo?.platform?.lowercase()) {
            "ios" -> Icons.Default.PhoneAndroid
            "android" -> Icons.Default.PhoneAndroid
            "web" -> Icons.Default.Computer
            else -> Icons.Default.Devices
        }
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(appPalette.primary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = appPalette.primary,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = session.deviceInfo?.deviceName ?: "Unknown device",
                color = appPalette.textPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(2.dp))
            val subtitle = buildString {
                session.deviceInfo?.platform?.let { append(it.replaceFirstChar { c -> c.uppercase() }) }
                session.deviceInfo?.appVersion?.let { if (isNotEmpty()) append(" • ") ; append("v$it") }
                session.ipAddress?.let { if (isNotEmpty()) append(" • ") ; append(it) }
                if (isEmpty()) append(if (isCurrent) "Current session" else "Last active recently")
            }
            Text(
                text = subtitle,
                color = appPalette.textSecondary,
                fontSize = 13.sp
            )
        }
        if (isCurrent) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(appPalette.primary.copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "CURRENT",
                    color = appPalette.primary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
