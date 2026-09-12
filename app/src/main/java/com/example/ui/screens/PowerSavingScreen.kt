package com.example.ui.screens

import com.example.ui.theme.appPalette
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
    Scaffold(
        containerColor = appPalette.chatListBg,
        topBar = { SettingsTopBar(title = "Power Saving", onBack = onBack) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 12.dp)
        ) {
            // === Master toggle ===
            item {
                SettingsCard {
                    SectionHeader("Power Saving Mode")
                    ToggleRow(
                        icon = Icons.Default.BatteryFull,
                        iconColor = Color(0xFF4CAF50),
                        title = "Enable Power Saving",
                        subtitle = "Reduces background activity to extend battery life",
                        checked = powerSavingEnabled,
                        onCheckedChange = onSetPowerSaving
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }

            // === Individual options ===
            item {
                SettingsCard {
                    SectionHeader("Reduction Options")
                    ToggleRow(
                        icon = Icons.Default.HighQuality,
                        iconColor = Color(0xFF5DADE2),
                        title = "Lower Media Quality",
                        subtitle = "Download compressed images and videos",
                        checked = powerLowQuality,
                        onCheckedChange = onSetLowQuality
                    )
                    ToggleRow(
                        icon = Icons.Default.Animation,
                        iconColor = Color(0xFFF39C12),
                        title = "Disable Animations",
                        subtitle = "Turn off bubble, transition and sticker animations",
                        checked = powerDisableAnimations,
                        onCheckedChange = onSetDisableAnimations
                    )
                    ToggleRow(
                        icon = Icons.Default.PlayCircle,
                        iconColor = Color(0xFFEC7063),
                        title = "Disable Auto-Play",
                        subtitle = "Stop GIFs and videos from playing automatically",
                        checked = powerDisableAutoplay,
                        onCheckedChange = onSetDisableAutoplay
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }

            // === Info card ===
            item {
                SettingsCard {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Column {
                            Text(
                                text = "💡 How Power Saving Works",
                                color = appPalette.primary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "When enabled, 7eve9Chat will reduce network polling, " +
                                    "lower media quality, and skip animations to save battery. " +
                                    "Messages may take a few seconds longer to arrive.",
                                color = appPalette.textSecondary,
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
