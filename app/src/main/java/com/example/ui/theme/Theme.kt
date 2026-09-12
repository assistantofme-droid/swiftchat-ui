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
// Light palette
// ============================================================
private val LightBackground = Color(0xFFF0F2F5)
private val LightSurface = Color(0xFFFFFFFF)
private val LightSurfaceVariant = Color(0xFFE9EBEE)
private val LightTextPrimary = Color(0xFF1B1D22)
private val LightTextSecondary = Color(0xFF65758A)
private val LightPrimary = Color(0xFF2A9CE0)
private val LightAccent = Color(0xFF36A8F2)

private val TelegramLightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = Color.White,
    secondary = LightAccent,
    onSecondary = Color.White,
    tertiary = Color(0xFF4FA6DB),
    background = LightBackground,
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
