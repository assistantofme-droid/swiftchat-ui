package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SportsKabaddi
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.api.ApiClient
import com.example.data.model.AvatarType

@Composable
fun AvatarView(
    avatarType: AvatarType = AvatarType.MOTORCYCLE,
    modifier: Modifier = Modifier,
    size: Dp = 54.dp,
    avatarResId: Int? = null,
    avatarUrl: String? = null,
    title: String? = null
) {
    val resolvedUrl = ApiClient.resolveUrl(avatarUrl)

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape),
        contentAlignment = Alignment.Center
    ) {
        when {
            !resolvedUrl.isNullOrBlank() -> {
                AsyncImage(
                    model = resolvedUrl,
                    contentDescription = title ?: "Avatar",
                    modifier = Modifier.matchParentSize(),
                    contentScale = ContentScale.Crop
                )
            }
            avatarResId != null -> {
                Image(
                    painter = painterResource(id = avatarResId),
                    contentDescription = null,
                    modifier = Modifier.matchParentSize(),
                    contentScale = ContentScale.Crop
                )
            }
            !title.isNullOrBlank() && avatarType == AvatarType.MOTORCYCLE -> {
                // Generate consistent colored avatar with initials
                val hash = kotlin.math.abs(title.hashCode())
                val palettes = listOf(
                    listOf(Color(0xFFE53935), Color(0xFFD81B60)),
                    listOf(Color(0xFF8E24AA), Color(0xFF5E35B1)),
                    listOf(Color(0xFF1E88E5), Color(0xFF00ACC1)),
                    listOf(Color(0xFF43A047), Color(0xFF7CB342)),
                    listOf(Color(0xFFFB8C00), Color(0xFFF4511E)),
                    listOf(Color(0xFF00897B), Color(0xFF00B0FF))
                )
                val palette = palettes[hash % palettes.size]
                val initials = title.trim().split(" ")
                    .filter { it.isNotBlank() }
                    .take(2)
                    .map { it.first() }
                    .joinToString("")
                    .uppercase()

                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Brush.linearGradient(palette)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (initials.isNotBlank()) initials else "T",
                        color = Color.White,
                        fontSize = (size.value * 0.38f).sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            avatarType == AvatarType.MOTORCYCLE -> {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF2A5298), Color(0xFF1E3C72))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsCar,
                        contentDescription = "Avatar",
                        tint = Color.White,
                        modifier = Modifier.size(size * 0.55f)
                    )
                }
            }
            avatarType == AvatarType.GALAXY -> {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(Color(0xFFBA68C8), Color(0xFF4A148C), Color(0xFF0D0221))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🌙", fontSize = (size.value * 0.45f).sp)
                }
            }
            avatarType == AvatarType.CAR -> {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF263238), Color(0xFF10171D), Color(0xFF000000))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsCar,
                        contentDescription = null,
                        tint = Color(0xFF64B5F6),
                        modifier = Modifier.size(size * 0.55f)
                    )
                }
            }
            avatarType == AvatarType.DUCK -> {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF4DD0E1), Color(0xFF00838F))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🐥", fontSize = (size.value * 0.5f).sp)
                }
            }
            avatarType == AvatarType.ANIME_GIRL -> {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFFF8BBD0), Color(0xFF6A1B9A))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Face,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(size * 0.6f)
                    )
                }
            }
            avatarType == AvatarType.SAMURAI -> {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF00E5FF), Color(0xFF1565C0), Color(0xFF0A1931))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SportsKabaddi,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(size * 0.58f)
                    )
                }
            }
            avatarType == AvatarType.HUG -> {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF546E7A), Color(0xFF263238))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🤍", fontSize = (size.value * 0.42f).sp)
                }
            }
            avatarType == AvatarType.TEXT_LOGO -> {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color(0xFF111111)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "مای انیمه",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            avatarType == AvatarType.SKULL -> {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(Color(0xFFD32F2F), Color(0xFF4A0000), Color(0xFF1A0000))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = Color(0xFFFFCDD2),
                        modifier = Modifier.size(size * 0.55f)
                    )
                }
            }
            avatarType == AvatarType.PIXEL_ART -> {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.sweepGradient(
                                colors = listOf(
                                    Color(0xFF00E676),
                                    Color(0xFF2979FF),
                                    Color(0xFFFF1744),
                                    Color(0xFFFFEA00),
                                    Color(0xFF00E676)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🎨", fontSize = (size.value * 0.45f).sp)
                }
            }
        }
    }
}
