package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Holds the full color palette for the app, switching between dark and light
 * based on the user's theme preference. Provided by MyApplicationTheme and
 * accessed via [LocalAppPalette.current] from any composable — no need to
 * pass isDarkMode down through every screen.
 */
data class AppPalette(
    val isDark: Boolean,
    val bg: Color,
    val chatListBg: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val glassHeader: Color,
    val glassBorder: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val primary: Color,
    val accent: Color,
    val unreadBadge: Color,
    val unreadBadgeText: Color,
    val checkBlue: Color,
    val pinIcon: Color,
    val typingCyan: Color,
    val indicatorColor: Color,
    val menuBg: Color,
    val divider: Color,
    val incomingBubble: Color,
    val outgoingBubble: Color
)

val DarkPalette = AppPalette(
    isDark = true,
    bg = TelegramDarkBg,
    chatListBg = TelegramChatListBg,
    surface = TelegramSurface,
    surfaceVariant = TelegramSurfaceVariant,
    glassHeader = TelegramGlassHeader,
    glassBorder = TelegramGlassBorder,
    textPrimary = TelegramTextPrimary,
    textSecondary = TelegramTextSecondary,
    textMuted = TelegramTextMuted,
    primary = TelegramPrimary,
    accent = TelegramAccent,
    unreadBadge = TelegramUnreadBadge,
    unreadBadgeText = TelegramUnreadBadgeText,
    checkBlue = TelegramCheckBlue,
    pinIcon = TelegramPinIcon,
    typingCyan = TelegramTypingCyan,
    indicatorColor = Color(0xFF1E2D3D),
    menuBg = TelegramDarkBg,
    divider = Color.White.copy(alpha = 0.08f),
    incomingBubble = TelegramIncomingBubble,
    outgoingBubble = TelegramOutgoingBubble
)

val LightPalette = AppPalette(
    isDark = false,
    bg = LightBg,
    chatListBg = LightChatListBg,
    surface = LightSurface,
    surfaceVariant = LightSurfaceVariant,
    glassHeader = LightGlassHeader,
    glassBorder = LightGlassBorder,
    textPrimary = LightTextPrimary,
    textSecondary = LightTextSecondary,
    textMuted = LightTextMuted,
    primary = LightPrimary,
    accent = LightAccent,
    unreadBadge = LightUnreadBadge,
    unreadBadgeText = LightUnreadBadgeText,
    checkBlue = LightCheckBlue,
    pinIcon = LightPinIcon,
    typingCyan = LightTypingCyan,
    indicatorColor = Color(0xFFD6E9F7),
    menuBg = LightSurface,
    divider = Color.Black.copy(alpha = 0.08f),
    incomingBubble = LightIncomingBubble,
    outgoingBubble = LightOutgoingBubble
)

val LocalAppPalette = staticCompositionLocalOf { DarkPalette }

/**
 * Convenience accessor — use `appPalette.surface` etc. in any composable.
 */
val appPalette: AppPalette
    @Composable
    @ReadOnlyComposable
    get() = LocalAppPalette.current
