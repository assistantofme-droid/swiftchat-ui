package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Telegram Palette Colors
val TelegramPrimary = Color(0xFF2AABEE)
val TelegramDarkBg = Color(0xFF0F172A)
val TelegramSheetBg = Color(0xFF1E293B)
val TelegramTextPrimary = Color(0xFFFFFFFF)
val TelegramTextSecondary = Color(0xFF94A3B8)
val TelegramTextMuted = Color(0xFF64748B)
val TelegramCheckBlue = Color(0xFF40B7FE)
val TelegramBubbleDatePill = Color(0x66000000)
val TelegramGlassBorder = Color(0x26FFFFFF)
val TelegramGlassHighlight = Color(0x1AFFFFFF)
// Additional colors used by old components
val TelegramGlassInput = Color(0xDE182430)
val TelegramChatListBg = Color(0xFF0F172A)
val TelegramSurface = Color(0xFF1E293B)
val TelegramSurfaceVariant = Color(0xFF334155)
val TelegramUnreadBadge = Color(0xFF4A84BA)
val TelegramPinIcon = Color(0xFF6C7A89)
val TelegramTypingCyan = Color(0xFF52B8FF)
val TelegramIncomingBubble = Color(0xFF1E2C3A)
val TelegramOutgoingBubble = Color(0xFF2B5278)
val TelegramSheetItemBg = Color(0xFF202C3A)
val TelegramSheetBorder = Color(0x2EFFFFFF)
val TelegramAccent = Color(0xFF38BDF8)

val TelegramIncomingGradient = Brush.verticalGradient(
    listOf(Color(0xFF1E293B), Color(0xFF0F172A))
)

val TelegramOutgoingGradient = Brush.verticalGradient(
    listOf(Color(0xFF2563EB), Color(0xFF1D4ED8))
)

val TelegramSendFabGradient = Brush.verticalGradient(
    listOf(Color(0xFF38BDF8), Color(0xFF0284C7))
)

data class AppPalette(
    val isDark: Boolean,
    val primary: Color = TelegramPrimary,
    val accent: Color = TelegramAccent,
    val chatListBg: Color = if (isDark) Color(0xFF0F172A) else Color(0xFFF1F5F9),
    val surface: Color = if (isDark) Color(0xFF1E293B) else Color(0xFFFFFFFF),
    val surfaceVariant: Color = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0),
    val textPrimary: Color = if (isDark) Color(0xFFFFFFFF) else Color(0xFF0F172A),
    val textSecondary: Color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
    val textMuted: Color = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8),
    val glassHeader: Color = if (isDark) Color(0xFF1E293B) else Color(0xFFFFFFFF),
    val glassBorder: Color = if (isDark) Color(0x26FFFFFF) else Color(0x1A000000),
    val tabActive: Color = TelegramPrimary,
    val tabActiveText: Color = Color.White,
    val tabInactive: Color = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8),
    val badgeBg: Color = TelegramPrimary,
    val badgeText: Color = Color.White
)

val LocalAppPalette = compositionLocalOf { AppPalette(isDark = true) }

val appPalette: AppPalette
    @Composable
    get() = LocalAppPalette.current

private val DarkColorScheme = darkColorScheme(
    primary = TelegramPrimary,
    secondary = TelegramAccent,
    background = Color(0xFF0F172A),
    surface = Color(0xFF1E293B),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = TelegramPrimary,
    secondary = TelegramAccent,
    background = Color(0xFFF1F5F9),
    surface = Color(0xFFFFFFFF),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF0F172A),
    onSurface = Color(0xFF0F172A)
)

@Composable
fun MyApplicationTheme(
    isDarkMode: Boolean = true,
    isPersian: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (isDarkMode) DarkColorScheme else LightColorScheme
    val palette = AppPalette(isDark = isDarkMode)
    val strings = com.example.ui.locale.AppStrings(isPersian = isPersian)

    CompositionLocalProvider(
        LocalAppPalette provides palette,
        com.example.ui.locale.LocalAppStrings provides strings
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }
}
