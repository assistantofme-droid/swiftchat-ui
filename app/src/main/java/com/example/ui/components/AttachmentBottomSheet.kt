package com.example.ui.components

import android.Manifest
import android.content.ContentUris
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Poll
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.data.model.AttachmentTab
import com.example.data.model.MediaPickerItem
import com.example.ui.theme.TelegramAccent
import com.example.ui.theme.TelegramPrimary
import com.example.ui.theme.TelegramSheetBg
import com.example.ui.theme.TelegramTextPrimary
import com.example.ui.theme.TelegramTextSecondary

/**
 * The attachment bottom sheet that opens when the user taps the paperclip in
 * a chat. Shows:
 *
 *   - A "Recent Photos" grid of REAL device gallery thumbnails pulled from
 *     MediaStore.Images.Media (not drawable defaults). Tapping thumbnails
 *     selects them; the send FAB sends them as multipart POST /messages.
 *   - A "Device Gallery" pill at the top that opens the system photo picker
 *     (PickMultipleVisualMedia) for picking up to 10 photos/videos.
 *   - A bottom tab bar with: Gallery / File / Music / Location / Poll
 *     (Contact tab intentionally removed per spec — it was unused).
 *
 * The sheet requires the READ_MEDIA_IMAGES permission (or READ_EXTERNAL_STORAGE
 * on API ≤ 32) to populate the recent-photos grid. If the permission isn't
 * granted, the grid is empty and the user can still tap "Device Gallery" to
 * pick media via the system picker (which doesn't need any permission).
 */
@Composable
fun AttachmentBottomSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    onSendMedia: (List<MediaPickerItem>) -> Unit,
    onSendLocation: ((latitude: Double, longitude: Double, title: String) -> Unit)? = null,
    onSendPoll: ((question: String, options: List<String>, isAnonymous: Boolean) -> Unit)? = null,
    onSendFile: ((fileName: String, fileUri: String) -> Unit)? = null,
    onSendMusic: ((title: String, audioUri: String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isLocationDialogOpen by remember { mutableStateOf(false) }
    var isPollDialogOpen by remember { mutableStateOf(false) }

    // ===== Device gallery thumbnails (read from MediaStore on first show) =====
    val deviceMediaItems = remember { mutableStateListOf<MediaPickerItem>() }
    val selectedIds = remember { mutableStateListOf<String>() }
    var hasMediaPermission by remember { mutableStateOf(false) }
    var permissionChecked by remember { mutableStateOf(false) }

    // Check if we already have permission to read images
    val readImagesPerm = if (android.os.Build.VERSION.SDK_INT >= 33) {
        Manifest.permission.READ_MEDIA_IMAGES
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasMediaPermission = granted
        permissionChecked = true
        if (granted) {
            loadDeviceGalleryThumbnails(context) { items ->
                deviceMediaItems.clear()
                deviceMediaItems.addAll(items)
            }
        }
    }

    // On first visibility, check permission and load thumbnails
    LaunchedEffect(visible) {
        if (visible && !permissionChecked) {
            val granted = ContextCompat.checkSelfPermission(context, readImagesPerm) ==
                PackageManager.PERMISSION_GRANTED
            hasMediaPermission = granted
            permissionChecked = true
            if (granted) {
                loadDeviceGalleryThumbnails(context) { items ->
                    deviceMediaItems.clear()
                    deviceMediaItems.addAll(items)
                }
            } else {
                permissionLauncher.launch(readImagesPerm)
            }
        }
    }

    // ===== System pickers (no permission required) =====
    // Pick multiple photos / videos from device gallery
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(10)
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            val items = uris.mapIndexed { index, uri ->
                MediaPickerItem(
                    id = "device_photo_$index",
                    uriString = uri.toString(),
                    isSelected = true
                )
            }
            onSendMedia(items)
            onDismiss()
        }
    }

    // Pick audio / music from device
    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val name = uri.lastPathSegment?.substringAfterLast("/") ?: "Music Track.mp3"
            onSendMusic?.invoke(name, uri.toString())
            onDismiss()
        }
    }

    // Pick file / document from device
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val name = uri.lastPathSegment?.substringAfterLast("/") ?: "Document"
            onSendFile?.invoke(name, uri.toString())
            onDismiss()
        }
    }

    var activeTab by remember { mutableStateOf(AttachmentTab.GALLERY) }
    val selectedCount = selectedIds.size

    // ===== Dialogs =====
    if (isLocationDialogOpen) {
        SendLocationDialog(
            visible = isLocationDialogOpen,
            onDismiss = { isLocationDialogOpen = false },
            onSendLocation = { lat, lng, title ->
                onSendLocation?.invoke(lat, lng, title)
                onDismiss()
            }
        )
    }

    if (isPollDialogOpen) {
        CreatePollDialog(
            visible = isPollDialogOpen,
            onDismiss = { isPollDialogOpen = false },
            onCreatePoll = { question, options, isAnon ->
                onSendPoll?.invoke(question, options, isAnon)
                onDismiss()
            }
        )
    }

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
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
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

                // Header: "Recent Photos" + "Device Gallery" pill
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Photos",
                        color = TelegramTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(TelegramPrimary.copy(alpha = 0.2f))
                            .clickable {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                                )
                            }
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = "Open Gallery",
                            tint = TelegramPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Device Gallery",
                            color = TelegramPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // ===== Media Grid — REAL device gallery thumbnails =====
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                ) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        contentPadding = PaddingValues(2.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Camera tile (always first)
                        item(key = "camera_tile") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1f)
                                    .background(Color(0xFF131D27))
                                    .clickable {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.PhotoCamera,
                                        contentDescription = "Camera",
                                        tint = Color.White.copy(alpha = 0.85f),
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Camera",
                                        color = Color.White.copy(alpha = 0.7f),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        // Real device photos
                        items(deviceMediaItems, key = { it.id }) { item ->
                            val isSelected = selectedIds.contains(item.id)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1f)
                                    .background(Color(0xFF1B242D))
                                    .clickable {
                                        if (isSelected) {
                                            selectedIds.remove(item.id)
                                        } else {
                                            selectedIds.add(item.id)
                                        }
                                    }
                            ) {
                                AsyncImage(
                                    model = item.uriString,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                // Selection radio circle in top right corner
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(6.dp)
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isSelected) TelegramPrimary else Color.Black.copy(alpha = 0.3f)
                                        )
                                        .border(
                                            width = 2.dp,
                                            color = if (isSelected) TelegramPrimary else Color.White.copy(alpha = 0.85f),
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
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

                        // If no permission granted yet, show a hint tile
                        if (!hasMediaPermission && deviceMediaItems.isEmpty()) {
                            item(key = "permission_hint") {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(1f)
                                        .background(Color(0xFF1B242D))
                                        .clickable {
                                            permissionLauncher.launch(readImagesPerm)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = Icons.Default.PhotoLibrary,
                                            contentDescription = null,
                                            tint = TelegramTextSecondary,
                                            modifier = Modifier.size(28.dp)
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "Tap to allow\n gallery access",
                                            color = TelegramTextSecondary,
                                            fontSize = 10.sp,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Floating send button when photos are selected
                    if (selectedCount > 0) {
                        FloatingActionButton(
                            onClick = {
                                val selected = deviceMediaItems.filter { selectedIds.contains(it.id) }
                                onSendMedia(selected)
                                selectedIds.clear()
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

                // ===== Bottom tab bar: Gallery / File / Music / Location / Poll =====
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
                        onClick = {
                            activeTab = AttachmentTab.GALLERY
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                            )
                        }
                    )
                    AttachmentTabItem(
                        label = "File",
                        icon = Icons.Default.Folder,
                        isSelected = activeTab == AttachmentTab.FILE,
                        onClick = {
                            activeTab = AttachmentTab.FILE
                            filePickerLauncher.launch("*/*")
                        }
                    )
                    AttachmentTabItem(
                        label = "Music",
                        icon = Icons.Default.Audiotrack,
                        isSelected = activeTab == AttachmentTab.MUSIC,
                        onClick = {
                            activeTab = AttachmentTab.MUSIC
                            audioPickerLauncher.launch("audio/*")
                        }
                    )
                    AttachmentTabItem(
                        label = "Location",
                        icon = Icons.Default.LocationOn,
                        isSelected = activeTab == AttachmentTab.LOCATION,
                        onClick = {
                            activeTab = AttachmentTab.LOCATION
                            isLocationDialogOpen = true
                        }
                    )
                    AttachmentTabItem(
                        label = "Poll",
                        icon = Icons.Default.Poll,
                        isSelected = activeTab == AttachmentTab.POLL,
                        onClick = {
                            activeTab = AttachmentTab.POLL
                            isPollDialogOpen = true
                        }
                    )
                }
            }
        }
    }
}

/**
 * Query MediaStore.Images.Media for the most recent images on the device,
 * returning a list of MediaPickerItem with their content:// uris. Runs on
 * a background thread (caller's responsibility). Caps at 30 items so we
 * don't blow up memory.
 */
private fun loadDeviceGalleryThumbnails(
    context: android.content.Context,
    onLoaded: (List<MediaPickerItem>) -> Unit
) {
    Thread {
        try {
            val items = mutableListOf<MediaPickerItem>()
            val projection = arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATE_ADDED
            )
            val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"
            val collection = if (android.os.Build.VERSION.SDK_INT >= 29) {
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
            } else {
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            }
            context.contentResolver.query(
                collection,
                projection,
                null,
                null,
                sortOrder
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                var count = 0
                while (cursor.moveToNext() && count < 30) {
                    val id = cursor.getLong(idColumn)
                    val name = cursor.getString(nameColumn)
                    val uri = ContentUris.withAppendedId(collection, id)
                    items.add(
                        MediaPickerItem(
                            id = "device_$id",
                            uriString = uri.toString(),
                            isSelected = false
                        )
                    )
                    count++
                }
            }
            android.os.Handler(android.os.Looper.getMainLooper()).post {
                onLoaded(items)
            }
        } catch (e: Exception) {
            android.os.Handler(android.os.Looper.getMainLooper()).post {
                onLoaded(emptyList())
            }
        }
    }.start()
}

@Composable
private fun AttachmentTabItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
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
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = tint,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}
