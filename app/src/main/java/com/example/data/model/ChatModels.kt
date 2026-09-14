package com.example.data.model

enum class AvatarType {
    MOTORCYCLE,
    GALAXY,
    CAR,
    DUCK,
    ANIME_GIRL,
    SAMURAI,
    HUG,
    TEXT_LOGO,
    SKULL,
    PIXEL_ART
}

enum class MessageType {
    TEXT,
    BIG_STICKER,
    IMAGE_COLLAGE,
    PHOTO,
    VIDEO,
    AUDIO,
    GIF,
    LOCATION,
    POLL,
    FILE
}

data class ReactionItem(
    val emoji: String,
    val userAvatarType: AvatarType = AvatarType.MOTORCYCLE,
    val count: Int = 1
)

data class MessageItem(
    val id: String,
    val text: String? = null,
    val time: String,
    val isOutgoing: Boolean,
    val type: MessageType = MessageType.TEXT,
    val photoResId: Int? = null,
    val mediaUrl: String? = null,
    val videoThumbnailUrl: String? = null,
    val duration: Int? = null, // Duration in seconds for audio/video
    val fileName: String? = null,
    val senderName: String? = null,
    val senderAvatarUrl: String? = null,
    val reactions: List<ReactionItem> = emptyList(),
    val isRead: Boolean = true,
    val isPending: Boolean = false,
    val hasSingleCheck: Boolean = false,
    val dateHeader: String? = null
)

data class StickerItem(
    val id: String,
    val name: String? = null,
    val url: String,
    val pack: String? = null
)

data class GifItem(
    val id: String,
    val url: String,
    val sourceUrl: String? = null,
    val width: Int = 240,
    val height: Int = 240
)

data class ChatItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val time: String,
    val isPinned: Boolean = false,
    val unreadCount: Int = 0,
    val hasMention: Boolean = false,
    val isMuted: Boolean = false,
    val hasDoubleCheck: Boolean = false,
    val hasSingleCheck: Boolean = false,
    val isTyping: Boolean = false,
    val typingUser: String? = null,
    val avatarType: AvatarType = AvatarType.MOTORCYCLE,
    val avatarResId: Int? = null,
    val avatarUrl: String? = null,
    val isGroup: Boolean = false,
    val isChannel: Boolean = false,
    val memberCount: Int = 0,
    val isOnline: Boolean = false
) {
    val name: String get() = title
}

data class MediaPickerItem(
    val id: String,
    val isCamera: Boolean = false,
    val drawableResId: Int? = null,
    val uriString: String? = null,
    val isSelected: Boolean = false,
    val selectionIndex: Int = 0
)

enum class AttachmentTab(val label: String) {
    GALLERY("Gallery"),
    FILE("File"),
    MUSIC("Music"),
    LOCATION("Location"),
    POLL("Poll"),
    CONTACT("Contact")
}
