package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val TelegramDarkColorScheme = darkColorScheme(
    primary = TelegramPrimary,
    onPrimary = TelegramTextPrimary,
    secondary = TelegramAccent,
    onSecondary = TelegramTextPrimary,
    tertiary = TelegramOutgoingBubble,
    background = TelegramDarkBg,
    onBackground = TelegramTextPrimary,
    surface = TelegramSurface,
    onSurface = TelegramTextPrimary,
    surfaceVariant = TelegramSurfaceVariant,
    onSurfaceVariant = TelegramTextSecondary
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = TelegramDarkColorScheme,
        typography = Typography,
        content = content
    )
}
