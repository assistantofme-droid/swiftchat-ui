package com.example.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SendOtpRequest(
    @Json(name = "phone") val phone: String,
    @Json(name = "forceSms") val forceSms: Boolean = false
)

@JsonClass(generateAdapter = true)
data class SendOtpResponse(
    @Json(name = "message") val message: String? = null,
    @Json(name = "phone") val phone: String? = null,
    @Json(name = "hash") val hash: String? = null
)

@JsonClass(generateAdapter = true)
data class VerifyOtpRequest(
    @Json(name = "phone") val phone: String,
    @Json(name = "code") val code: String
)

@JsonClass(generateAdapter = true)
data class VerifyOtpResponse(
    @Json(name = "token") val token: String? = null,
    @Json(name = "user") val user: ApiUser? = null,
    @Json(name = "message") val message: String? = null
)

@JsonClass(generateAdapter = true)
data class ApiUser(
    @Json(name = "_id") val _id: String? = null,
    @Json(name = "phone") val phone: String? = null,
    @Json(name = "name") val name: String? = null,
    @Json(name = "username") val username: String? = null,
    @Json(name = "avatar") val avatar: String? = null,
    @Json(name = "bio") val bio: String? = null,
    @Json(name = "isVerified") val isVerified: Boolean? = false,
    @Json(name = "isOwner") val isOwner: Boolean? = false,
    @Json(name = "isNewUser") val isNewUser: Boolean? = false
)

@JsonClass(generateAdapter = true)
data class ApiParticipant(
    @Json(name = "_id") val _id: String? = null,
    @Json(name = "user") val user: ApiUser? = null,
    @Json(name = "name") val name: String? = null,
    @Json(name = "avatar") val avatar: String? = null,
    @Json(name = "role") val role: String? = null
)

@JsonClass(generateAdapter = true)
data class ApiLastMessage(
    @Json(name = "_id") val _id: String? = null,
    @Json(name = "text") val text: String? = null,
    @Json(name = "createdAt") val createdAt: String? = null,
    @Json(name = "sender") val sender: ApiUser? = null
)

@JsonClass(generateAdapter = true)
data class ApiConversation(
    @Json(name = "_id") val _id: String,
    @Json(name = "type") val type: String? = "private",
    @Json(name = "name") val name: String? = null,
    @Json(name = "avatar") val avatar: String? = null,
    @Json(name = "participants") val participants: List<ApiParticipant>? = emptyList(),
    @Json(name = "lastMessage") val lastMessage: ApiLastMessage? = null,
    @Json(name = "unreadCount") val unreadCount: Int? = 0,
    @Json(name = "isMuted") val isMuted: Boolean? = false,
    @Json(name = "pinned") val pinned: Boolean? = false,
    @Json(name = "lastMessageAt") val lastMessageAt: String? = null,
    @Json(name = "handle") val handle: String? = null
)

@JsonClass(generateAdapter = true)
data class ApiReaction(
    @Json(name = "emoji") val emoji: String,
    @Json(name = "user") val user: String? = null
)

@JsonClass(generateAdapter = true)
data class ApiReplyTo(
    @Json(name = "_id") val _id: String? = null,
    @Json(name = "text") val text: String? = null
)

@JsonClass(generateAdapter = true)
data class ApiMessage(
    @Json(name = "_id") val _id: String,
    @Json(name = "conversationId") val conversationId: String? = null,
    @Json(name = "sender") val sender: ApiUser? = null,
    @Json(name = "text") val text: String? = null,
    @Json(name = "type") val type: String? = "text",
    @Json(name = "fileUrl") val fileUrl: String? = null,
    @Json(name = "videoThumbnailUrl") val videoThumbnailUrl: String? = null,
    @Json(name = "duration") val duration: Int? = null,
    @Json(name = "fileName") val fileName: String? = null,
    @Json(name = "createdAt") val createdAt: String? = null,
    @Json(name = "reactions") val reactions: List<ApiReaction>? = emptyList(),
    @Json(name = "readBy") val readBy: List<String>? = emptyList(),
    @Json(name = "replyTo") val replyTo: ApiReplyTo? = null,
    @Json(name = "isSilent") val isSilent: Boolean? = false
)

@JsonClass(generateAdapter = true)
data class SendMessageRequest(
    @Json(name = "conversationId") val conversationId: String? = null,
    @Json(name = "receiverId") val receiverId: String? = null,
    @Json(name = "text") val text: String? = null,
    @Json(name = "type") val type: String = "text",
    @Json(name = "fileUrl") val fileUrl: String? = null,
    @Json(name = "videoThumbnailUrl") val videoThumbnailUrl: String? = null,
    @Json(name = "duration") val duration: Int? = null,
    @Json(name = "fileName") val fileName: String? = null,
    @Json(name = "replyTo") val replyTo: String? = null,
    @Json(name = "isSilent") val isSilent: Boolean = false
)

@JsonClass(generateAdapter = true)
data class ReactionRequest(
    @Json(name = "emoji") val emoji: String
)

@JsonClass(generateAdapter = true)
data class ApiSticker(
    @Json(name = "_id") val _id: String,
    @Json(name = "name") val name: String? = null,
    @Json(name = "url") val url: String,
    @Json(name = "pack") val pack: String? = null,
    @Json(name = "isGlobal") val isGlobal: Boolean? = true
)

@JsonClass(generateAdapter = true)
data class ApiGif(
    @Json(name = "_id") val _id: String,
    @Json(name = "url") val url: String,
    @Json(name = "sourceUrl") val sourceUrl: String? = null,
    @Json(name = "width") val width: Int? = null,
    @Json(name = "height") val height: Int? = null,
    @Json(name = "isGlobal") val isGlobal: Boolean? = true
)

@JsonClass(generateAdapter = true)
data class UpdateProfileRequest(
    @Json(name = "name") val name: String? = null,
    @Json(name = "username") val username: String? = null,
    @Json(name = "bio") val bio: String? = null,
    @Json(name = "avatar") val avatar: String? = null
)

@JsonClass(generateAdapter = true)
data class InitiateCallRequest(
    @Json(name = "recipientId") val recipientId: String,
    @Json(name = "type") val type: String = "voice"
)

@JsonClass(generateAdapter = true)
data class CallResponse(
    @Json(name = "callId") val callId: String? = null,
    @Json(name = "status") val status: String? = null,
    @Json(name = "type") val type: String? = null
)
