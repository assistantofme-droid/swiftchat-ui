package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.TelegramGlassBorder
import com.example.ui.theme.TelegramGlassInput

@Composable
fun TelegramQuickReactionPopup(
    visible: Boolean,
    onSelectReaction: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val quickEmojis = listOf("❤️", "👍", "🔥", "😂", "👏", "🎉", "😮", "😍")

    AnimatedVisibility(
        visible = visible,
        enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(),
        exit = scaleOut() + fadeOut(),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .shadow(elevation = 12.dp, shape = CircleShape, spotColor = Color(0x66000000))
                .clip(CircleShape)
                .background(TelegramGlassInput)
                .border(1.dp, TelegramGlassBorder, CircleShape)
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                quickEmojis.forEach { emoji ->
                    ReactionEmojiItem(
                        emoji = emoji,
                        onClick = {
                            onSelectReaction(emoji)
                            onDismiss()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ReactionEmojiItem(
    emoji: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 1.35f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "emoji_scale"
    )

    Box(
        modifier = Modifier
            .scale(scale)
            .clip(CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 6.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = emoji, fontSize = 24.sp)
    }
}
