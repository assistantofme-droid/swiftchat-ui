package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ============================================================
// Dark palette (existing)
// ============================================================
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

// ============================================================
// Light palette — uses the public Light* colors defined in Color.kt
// (no private re-declaration, which caused overload-resolution ambiguity)
// ============================================================
private val TelegramLightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = Color.White,
    secondary = LightAccent,
    onSecondary = Color.White,
    tertiary = Color(0xFF4FA6DB),
    background = LightChatListBg,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary
)

@Composable
fun MyApplicationTheme(
    isDarkMode: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (isDarkMode) TelegramDarkColorScheme else TelegramLightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
