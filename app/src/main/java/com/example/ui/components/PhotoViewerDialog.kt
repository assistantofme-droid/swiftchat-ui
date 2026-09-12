package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.api.ApiClient
import com.example.ui.theme.TelegramTextPrimary
import com.example.ui.theme.TelegramTextSecondary

@Composable
fun PhotoViewerDialog(
    photoUrl: String?,
    caption: String? = null,
    senderName: String? = null,
    time: String? = null,
    onDismiss: () -> Unit
) {
    if (photoUrl.isNullOrBlank()) return

    val resolvedUrl = ApiClient.resolveUrl(photoUrl)

    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.96f))
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        if (scale > 1f) {
                            scale = 1f
                            offset = Offset.Zero
                        } else {
                            scale = 2.5f
                        }
                    }
                )
            }
    ) {
        // Image with pinch-to-zoom and pan
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(1f, 5f)
                        if (scale > 1f) {
                            val maxOffset = (scale - 1f) * 600f
                            offset = Offset(
                                x = (offset.x + pan.x).coerceIn(-maxOffset, maxOffset),
                                y = (offset.y + pan.y).coerceIn(-maxOffset, maxOffset)
                            )
                        } else {
                            offset = Offset.Zero
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = resolvedUrl,
                contentDescription = "Zoomable photo",
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offset.x,
                        translationY = offset.y
                    ),
                contentScale = ContentScale.Fit
            )
        }

        // Top Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.5f))
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = senderName ?: "Photo",
                    color = TelegramTextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                if (!time.isNullOrBlank()) {
                    Text(
                        text = time,
                        color = TelegramTextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            // Reset zoom button
            if (scale > 1.05f) {
                IconButton(onClick = {
                    scale = 1f
                    offset = Offset.Zero
                }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset Zoom",
                        tint = Color.White
                    )
                }
            }

            IconButton(onClick = { /* Share */ }) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share",
                    tint = Color.White
                )
            }
        }

        // Bottom Caption Bar
        if (!caption.isNullOrBlank()) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.6f))
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = caption,
                    color = Color.White,
                    fontSize = 15.sp,
                    lineHeight = 20.sp
                )
            }
        }
    }
}
