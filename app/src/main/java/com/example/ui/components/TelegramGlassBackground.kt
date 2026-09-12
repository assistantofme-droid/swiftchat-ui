package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.R
import com.example.ui.theme.TelegramDarkBg

/**
 * High-end Glassmorphic Telegram Chat Canvas Background
 * Combines ambient glowing radial light orbs with wallpaper pattern overlay
 */
@Composable
fun TelegramGlassBackground(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TelegramDarkBg)
    ) {
        // Dynamic ambient glowing orbs Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Cyan ambient glow (top right)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x2E2481CC),
                        Color(0x0F195E96),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.85f, h * 0.18f),
                    radius = w * 0.75f
                ),
                center = Offset(w * 0.85f, h * 0.18f),
                radius = w * 0.75f
            )

            // Indigo/Purple ambient glow (mid left)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x284B308C),
                        Color(0x0C2E1D59),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.12f, h * 0.55f),
                    radius = w * 0.65f
                ),
                center = Offset(w * 0.12f, h * 0.55f),
                radius = w * 0.65f
            )

            // Deep blue glow (bottom center)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x3317426A),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.5f, h * 0.9f),
                    radius = w * 0.8f
                ),
                center = Offset(w * 0.5f, h * 0.9f),
                radius = w * 0.8f
            )
        }

        // Overlay Telegram Doodle Wallpaper
        Image(
            painter = painterResource(id = R.drawable.img_chat_wallpaper),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.45f
        )
    }
}
