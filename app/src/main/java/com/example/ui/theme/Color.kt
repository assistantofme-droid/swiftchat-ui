package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Telegram Dark Mode Palette
val TelegramDarkBg = Color(0xFF0E1621)
val TelegramChatListBg = Color(0xFF131D27)
val TelegramSurface = Color(0xFF17212B)
val TelegramSurfaceVariant = Color(0xFF242D39)
val TelegramPrimary = Color(0xFF50A7EA)
val TelegramPrimaryVariant = Color(0xFF2481CC)
val TelegramAccent = Color(0xFF52B8FF)

// Glassmorphism & Liquid Glass Palette
val TelegramGlassBg = Color(0xD90E1621)
val TelegramGlassHeader = Color(0xD90E1621)
val TelegramGlassInput = Color(0xDE182430)
val TelegramGlassBorder = Color(0x33FFFFFF)
val TelegramGlassHighlight = Color(0x1FFFFFFF)
val TelegramGlassPill = Color(0xB316222D)

// Message Bubbles (Authentic Bubbly & Glassy Gradients)
val TelegramOutgoingBubble = Color(0xFF2B5278) // Authentic Telegram Dark Blue/Teal
val TelegramOutgoingBubbleEnd = Color(0xFF213F5E)
val TelegramIncomingBubble = Color(0xFF1E2C3A) // Dark slate bubble
val TelegramBubbleDatePill = Color(0x99111922)

// Outgoing Glass Bubble Gradient
val TelegramOutgoingGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xEE2A5A84),
        Color(0xDF1F4364)
    )
)

// Incoming Glass Bubble Gradient
val TelegramIncomingGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xF01E2B38),
        Color(0xE017232E)
    )
)

// Send FAB Gradient
val TelegramSendFabGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF5AB4F8),
        Color(0xFF2180CB)
    )
)

// Badges & Accents
val TelegramUnreadBadge = Color(0xFF4A84BA)
val TelegramUnreadBadgeText = Color(0xFFFFFFFF)
val TelegramTypingCyan = Color(0xFF52B8FF)
val TelegramCheckBlue = Color(0xFF4FC3F7)
val TelegramPinIcon = Color(0xFF6C7A89)

// Text Colors
val TelegramTextPrimary = Color(0xFFF5F5F5)
val TelegramTextSecondary = Color(0xFF7F8C99)
val TelegramTextMuted = Color(0xFF5E6D7E)

// Attachment Sheet Colors
val TelegramSheetBg = Color(0xF2141D26)
val TelegramSheetItemBg = Color(0xFF202C3A)
val TelegramSheetBorder = Color(0x2EFFFFFF)
