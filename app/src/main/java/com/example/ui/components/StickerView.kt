package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AvatarType
import com.example.data.model.ReactionItem

@Composable
fun StickerView(
    time: String,
    reactions: List<ReactionItem>,
    modifier: Modifier = Modifier,
    onReactionClick: (() -> Unit)? = null
) {
    Column(
        modifier = modifier.padding(vertical = 4.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Box(
            modifier = Modifier.padding(start = 8.dp)
        ) {
            // Big animated / stylized 3D thumbs up sticker
            Text(
                text = "👍",
                fontSize = 110.sp,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            // Time chip
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .background(Color(0x9917212B), RoundedCornerShape(12.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = time,
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Reaction pill below sticker
        if (reactions.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .padding(start = 12.dp, top = 2.dp)
                    .background(Color(0xCC17212B), CircleShape)
                    .clip(CircleShape)
                    .clickable { onReactionClick?.invoke() }
                    .padding(horizontal = 8.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                reactions.forEach { reaction ->
                    Text(text = reaction.emoji, fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    AvatarView(
                        avatarType = reaction.userAvatarType,
                        size = 16.dp
                    )
                }
            }
        }
    }
}
