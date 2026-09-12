package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Gif
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.SentimentSatisfiedAlt
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.api.ApiClient
import com.example.data.model.GifItem
import com.example.data.model.StickerItem
import com.example.ui.theme.TelegramDarkBg
import com.example.ui.theme.TelegramPrimary
import com.example.ui.theme.TelegramTextMuted
import com.example.ui.theme.TelegramTextPrimary
import com.example.ui.theme.TelegramTextSecondary

@Composable
fun StickerGifPickerSheet(
    visible: Boolean,
    stickers: List<StickerItem>,
    gifs: List<GifItem>,
    isLoading: Boolean = false,
    onDismiss: () -> Unit,
    onSelectSticker: (StickerItem) -> Unit,
    onSelectGif: (GifItem) -> Unit,
    onSelectEmoji: (String) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Stickers, 1: GIFs, 2: Emoji

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it })
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 320.dp, max = 400.dp)
                .background(
                    color = Color(0xFF1E2833),
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                )
                .navigationBarsPadding()
        ) {
            // Drag handle and close button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(36.dp)
                        .height(4.dp)
                        .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(2.dp))
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 8.dp)
                        .size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TelegramTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Tab bar
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = TelegramPrimary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = TelegramPrimary,
                        height = 2.dp
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.SentimentSatisfiedAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Stickers", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        }
                    },
                    selectedContentColor = TelegramPrimary,
                    unselectedContentColor = TelegramTextSecondary
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Gif, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("GIFs", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        }
                    },
                    selectedContentColor = TelegramPrimary,
                    unselectedContentColor = TelegramTextSecondary
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Mood, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Emoji", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        }
                    },
                    selectedContentColor = TelegramPrimary,
                    unselectedContentColor = TelegramTextSecondary
                )
            }

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = TelegramPrimary, modifier = Modifier.size(32.dp))
                }
            } else {
                when (selectedTab) {
                    0 -> {
                        // Stickers Grid
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(4),
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(horizontal = 8.dp),
                            contentPadding = PaddingValues(vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(stickers, key = { it.id }) { sticker ->
                                Box(
                                    modifier = Modifier
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { onSelectSticker(sticker) }
                                        .padding(4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    AsyncImage(
                                        model = ApiClient.resolveUrl(sticker.url),
                                        contentDescription = sticker.name ?: "Sticker",
                                        modifier = Modifier.size(72.dp),
                                        contentScale = ContentScale.Fit
                                    )
                                }
                            }
                        }
                    }
                    1 -> {
                        // GIFs Grid
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(horizontal = 8.dp),
                            contentPadding = PaddingValues(vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(gifs, key = { it.id }) { gif ->
                                Box(
                                    modifier = Modifier
                                        .aspectRatio(1.2f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF141C24))
                                        .clickable { onSelectGif(gif) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    AsyncImage(
                                        model = ApiClient.resolveUrl(gif.url),
                                        contentDescription = "GIF",
                                        modifier = Modifier.fillMaxWidth(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        }
                    }
                    2 -> {
                        // Standard Telegram Emojis Grid
                        val emojis = listOf(
                            "❤️", "🔥", "👍", "👏", "😁", "😍", "🎉", "😱",
                            "🙏", "👌", "🤩", "🚀", "😎", "💯", "🤣", "💔",
                            "🤔", "🥳", "🥺", "✨", "🥰", "💪", "⚡", "😴",
                            "🫡", "👀", "🙌", "🤍", "🕊️", "🌹", "💎", "🌟"
                        )
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(7),
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(horizontal = 8.dp),
                            contentPadding = PaddingValues(vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(emojis) { emoji ->
                                Box(
                                    modifier = Modifier
                                        .aspectRatio(1f)
                                        .clip(CircleShape)
                                        .clickable { onSelectEmoji(emoji) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = emoji, fontSize = 28.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
