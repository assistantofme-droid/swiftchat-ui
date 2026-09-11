package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.AttachmentTab
import com.example.data.model.MediaPickerItem
import com.example.ui.theme.TelegramAccent
import com.example.ui.theme.TelegramPrimary
import com.example.ui.theme.TelegramSheetBg
import com.example.ui.theme.TelegramSheetBorder
import com.example.ui.theme.TelegramTextPrimary
import com.example.ui.theme.TelegramTextSecondary

@Composable
fun AttachmentBottomSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    onSendMedia: (List<MediaPickerItem>) -> Unit,
    modifier: Modifier = Modifier
) {
    // Initial mock media items matching the screenshot
    var mediaItems by remember {
        mutableStateOf(
            listOf(
                MediaPickerItem(id = "cam", isCamera = true),
                MediaPickerItem(id = "m1", drawableResId = R.drawable.img_retro_guy_collage),
                MediaPickerItem(id = "m2", drawableResId = R.drawable.img_avatar_motorcycle),
                MediaPickerItem(id = "m3", drawableResId = R.drawable.img_chat_wallpaper),
                MediaPickerItem(id = "m4", drawableResId = R.drawable.img_app_icon),
                MediaPickerItem(id = "m5", drawableResId = R.drawable.img_retro_guy_collage),
                MediaPickerItem(id = "m6", drawableResId = R.drawable.img_avatar_motorcycle),
                MediaPickerItem(id = "m7", drawableResId = R.drawable.img_chat_wallpaper),
                MediaPickerItem(id = "m8", drawableResId = R.drawable.img_app_icon)
            )
        )
    }

    var activeTab by remember { mutableStateOf(AttachmentTab.GALLERY) }
    val selectedCount = mediaItems.count { it.isSelected }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically(
            animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow),
            initialOffsetY = { it }
        ),
        exit = fadeOut() + slideOutVertically(
            animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium),
            targetOffsetY = { it }
        ),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable { onDismiss() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .background(TelegramSheetBg)
                    .clickable(enabled = false) {}
                    .draggable(
                        orientation = Orientation.Vertical,
                        state = rememberDraggableState { delta ->
                            if (delta > 30f) onDismiss()
                        }
                    )
                    .navigationBarsPadding()
            ) {
                // Drag handle bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, bottom = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .width(38.dp)
                            .height(4.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.35f))
                    )
                }

                // Main Media Grid (3 Columns)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                ) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        contentPadding = PaddingValues(2.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(mediaItems, key = { it.id }) { item ->
                            if (item.isCamera) {
                                // Camera tile
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(1f)
                                        .background(Color(0xFF131D27))
                                        .clickable {
                                            // Handle camera click
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PhotoCamera,
                                        contentDescription = "Camera",
                                        tint = Color.White.copy(alpha = 0.85f),
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                            } else {
                                // Media thumbnail tile
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(1f)
                                        .background(Color(0xFF1B242D))
                                        .clickable {
                                            mediaItems = mediaItems.map {
                                                if (it.id == item.id) it.copy(isSelected = !it.isSelected)
                                                else it
                                            }
                                        }
                                ) {
                                    if (item.drawableResId != null) {
                                        Image(
                                            painter = painterResource(id = item.drawableResId),
                                            contentDescription = null,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(
                                                    Brush.linearGradient(
                                                        listOf(Color(0xFF2C3E50), Color(0xFF1A252F))
                                                    )
                                                )
                                        )
                                    }

                                    // Selection radio circle in top right corner
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(6.dp)
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (item.isSelected) TelegramPrimary else Color.Transparent
                                            )
                                            .border(
                                                width = 2.dp,
                                                color = if (item.isSelected) TelegramPrimary else Color.White.copy(alpha = 0.85f),
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (item.isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Floating send button when photos are selected
                    if (selectedCount > 0) {
                        FloatingActionButton(
                            onClick = {
                                val selected = mediaItems.filter { it.isSelected }
                                onSendMedia(selected)
                                mediaItems = mediaItems.map { it.copy(isSelected = false) }
                                onDismiss()
                            },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(16.dp)
                                .size(56.dp),
                            containerColor = TelegramPrimary,
                            contentColor = Color.White,
                            elevation = FloatingActionButtonDefaults.elevation(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Send $selectedCount photo(s)"
                            )
                        }
                    }
                }

                // Bottom Tab Bar inside Sheet
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(TelegramSheetBg)
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AttachmentTabItem(
                        label = "Gallery",
                        icon = Icons.Default.PhotoLibrary,
                        isSelected = activeTab == AttachmentTab.GALLERY,
                        onClick = { activeTab = AttachmentTab.GALLERY }
                    )
                    AttachmentTabItem(
                        label = "File",
                        icon = Icons.Default.Folder,
                        isSelected = activeTab == AttachmentTab.FILE,
                        onClick = { activeTab = AttachmentTab.FILE }
                    )
                    AttachmentTabItem(
                        label = "Location",
                        icon = Icons.Default.LocationOn,
                        isSelected = activeTab == AttachmentTab.LOCATION,
                        onClick = { activeTab = AttachmentTab.LOCATION }
                    )
                    AttachmentTabItem(
                        label = "Article",
                        icon = Icons.Default.Description,
                        hasStar = true,
                        isSelected = activeTab == AttachmentTab.ARTICLE,
                        onClick = { activeTab = AttachmentTab.ARTICLE }
                    )
                    AttachmentTabItem(
                        label = "Checklist",
                        icon = Icons.Default.CheckCircle,
                        hasStar = true,
                        isSelected = activeTab == AttachmentTab.CHECKLIST,
                        onClick = { activeTab = AttachmentTab.CHECKLIST }
                    )
                    AttachmentTabItem(
                        label = "Contact",
                        icon = Icons.Default.Person,
                        isSelected = activeTab == AttachmentTab.CONTACT,
                        onClick = { activeTab = AttachmentTab.CONTACT }
                    )
                }
            }
        }
    }
}

@Composable
private fun AttachmentTabItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    hasStar: Boolean = false,
    onClick: () -> Unit
) {
    val bg = if (isSelected) Color(0xFF243B55) else Color.Transparent
    val tint = if (isSelected) TelegramAccent else TelegramTextSecondary

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(24.dp)
            )
            if (hasStar) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(TelegramAccent),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(8.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = tint,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}
