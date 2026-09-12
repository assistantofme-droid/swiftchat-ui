package com.example.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// =============================================================
// Authentication
// =============================================================

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
data class ApiProfileSong(
    @Json(name = "title") val title: String? = null,
    @Json(name = "url") val url: String? = null,
    @Json(name = "senderName") val senderName: String? = null,
    @Json(name = "duration") val duration: Int? = null,
    @Json(name = "coverUrl") val coverUrl: String? = null
)

@JsonClass(generateAdapter = true)
data class ApiSessionDevice(
    @Json(name = "platform") val platform: String? = null,
    @Json(name = "deviceName") val deviceName: String? = null,
    @Json(name = "appVersion") val appVersion: String? = null
)

@JsonClass(generateAdapter = true)
data class ApiSession(
    @Json(name = "_id") val _id: String? = null,
    @Json(name = "deviceInfo") val deviceInfo: ApiSessionDevice? = null,
    @Json(name = "ipAddress") val ipAddress: String? = null,
    @Json(name = "createdAt") val createdAt: String? = null,
    @Json(name = "lastActiveAt") val lastActiveAt: String? = null,
    @Json(name = "expiresAt") val expiresAt: String? = null,
    @Json(name = "current") val current: Boolean? = false
)

@JsonClass(generateAdapter = true)
data class ApiUser(
    @Json(name = "_id") val _id: String? = null,
    @Json(name = "phone") val phone: String? = null,
    @Json(name = "name") val name: String? = null,
    @Json(name = "username") val username: String? = null,
    @Json(name = "bio") val bio: String? = null,
    @Json(name = "about") val about: String? = null,
    @Json(name = "avatar") val avatar: String? = null,
    @Json(name = "avatars") val avatars: List<String>? = null,
    @Json(name = "isVerified") val isVerified: Boolean? = false,
    @Json(name = "isOwner") val isOwner: Boolean? = false,
    @Json(name = "isPhoneHidden") val isPhoneHidden: Boolean? = false,
    @Json(name = "isNewUser") val isNewUser: Boolean? = false,
    @Json(name = "isOnline") val isOnline: Boolean? = false,
    @Json(name = "lastSeen") val lastSeen: String? = null,
    @Json(name = "isBot") val isBot: Boolean? = false,
    @Json(name = "profileColor") val profileColor: String? = null,
    @Json(name = "birthday") val birthday: String? = null,
    @Json(name = "profileActiveSong") val profileActiveSong: ApiProfileSong? = null,
    @Json(name = "profileSongs") val profileSongs: List<ApiProfileSong>? = null
)

@JsonClass(generateAdapter = true)
data class UpdateProfileRequest(
    @Json(name = "name") val name: String? = null,
    @Json(name = "username") val username: String? = null,
    @Json(name = "bio") val bio: String? = null,
    @Json(name = "about") val about: String? = null,
    @Json(name = "avatar") val avatar: String? = null,
    @Json(name = "avatars") val avatars: List<String>? = null,
    @Json(name = "profileColor") val profileColor: String? = null,
    @Json(name = "birthday") val birthday: String? = null,
    @Json(name = "isPhoneHidden") val isPhoneHidden: Boolean? = null
)

@JsonClass(generateAdapter = true)
data class AddContactRequest(
    @Json(name = "identifier") val identifier: String? = null,
    @Json(name = "userId") val userId: String? = null
)

@JsonClass(generateAdapter = true)
data class ApiContact(
    @Json(name = "_id") val _id: String? = null,
    @Json(name = "name") val name: String? = null,
    @Json(name = "username") val username: String? = null,
    @Json(name = "avatar") val avatar: String? = null,
    @Json(name = "phone") val phone: String? = null,
    @Json(name = "isVerified") val isVerified: Boolean? = false,
    @Json(name = "isPhoneHidden") val isPhoneHidden: Boolean? = false
)

@JsonClass(generateAdapter = true)
data class CheckContactsRequest(
    @Json(name = "phones") val phones: List<String>
)

// =============================================================
// Conversations
// =============================================================

@JsonClass(generateAdapter = true)
data class ApiLastMessage(
    @Json(name = "_id") val _id: String? = null,
    @Json(name = "text") val text: String? = null,
    @Json(name = "type") val type: String? = null,
    @Json(name = "createdAt") val createdAt: String? = null
    // NOTE: `sender` intentionally omitted — the server may return it as either
    // an ApiUser object (spec §8.3) or a bare String user-id (some implementations
    // of §3.4). Having a typed field here would cause Moshi to fail parsing the
    // entire conversation list when the shape doesn't match, resulting in an
    // empty chat list. We don't use lastMessage.sender for anything in the
    // chat list UI, so it's safer to just drop it.
)

/**
 * Conversation model. Per the actual server source code in
 * /src/controllers/messageController.ts → getConversation/getConversations,
 * `participants` is populated as a FLAT array of User objects (not wrapped
 * in a {user: ApiUser} sub-object). So we type it as List<ApiUser>.
 */
@JsonClass(generateAdapter = true)
data class ApiConversation(
    @Json(name = "_id") val _id: String,
    @Json(name = "type") val type: String? = "private",
    @Json(name = "name") val name: String? = null,
    @Json(name = "description") val description: String? = null,
    @Json(name = "avatar") val avatar: String? = null,
    @Json(name = "handle") val handle: String? = null,
    @Json(name = "participants") val participants: List<ApiUser>? = emptyList(),
    @Json(name = "owner") val owner: ApiUser? = null,
    @Json(name = "admins") val admins: List<ApiUser>? = emptyList(),
    @Json(name = "lastMessage") val lastMessage: ApiLastMessage? = null,
    // NOTE: unreadCount is intentionally OMITTED. The server stores it as a
    // Mongoose Map<string, number> which serializes to a JSON object.
    // @JsonClass(generateAdapter = true) cannot reliably handle Any? or
    // Map<String, Any?>? for this field — it causes silent parse failures
    // that make resp.body() return null, breaking ALL conversation endpoints.
    // Moshi simply skips undeclared JSON fields, so omitting it is the
    // safest approach. We default to 0 in the mapping layer.
    @Json(name = "isMuted") val isMuted: Boolean? = false,
    @Json(name = "pinned") val pinned: Boolean? = false,
    @Json(name = "lastMessageAt") val lastMessageAt: String? = null,
    @Json(name = "isChannel") val isChannel: Boolean? = false,
    @Json(name = "isVerified") val isVerified: Boolean? = false
)

@JsonClass(generateAdapter = true)
data class CreateConversationRequest(
    @Json(name = "name") val name: String,
    @Json(name = "type") val type: String = "group", // "group" | "channel"
    @Json(name = "description") val description: String? = null,
    @Json(name = "publicId") val publicId: String? = null,
    @Json(name = "isPrivateLink") val isPrivateLink: Boolean? = false,
    @Json(name = "participants") val participants: List<String> = emptyList()
)

@JsonClass(generateAdapter = true)
data class UpdateConversationRequest(
    @Json(name = "name") val name: String? = null,
    @Json(name = "description") val description: String? = null,
    @Json(name = "approveNewMembers") val approveNewMembers: Boolean? = null,
    @Json(name = "slowModeInterval") val slowModeInterval: Int? = null,
    @Json(name = "protectedContent") val protectedContent: Boolean? = null,
    @Json(name = "showSignatures") val showSignatures: Boolean? = null
)

@JsonClass(generateAdapter = true)
data class MuteToggleResponse(
    @Json(name = "message") val message: String? = null,
    @Json(name = "isMuted") val isMuted: Boolean? = null
)

@JsonClass(generateAdapter = true)
data class PinMessageRequest(
    @Json(name = "messageId") val messageId: String,
    @Json(name = "action") val action: String = "pin" // "pin" | "unpin"
)

@JsonClass(generateAdapter = true)
data class DiscoverSearchResult(
    @Json(name = "_id") val _id: String,
    @Json(name = "type") val type: String? = null,
    @Json(name = "name") val name: String? = null,
    @Json(name = "avatar") val avatar: String? = null,
    @Json(name = "handle") val handle: String? = null,
    @Json(name = "description") val description: String? = null,
    @Json(name = "participantsCount") val participantsCount: Int? = 0,
    @Json(name = "isVerified") val isVerified: Boolean? = false
)

// =============================================================
// Messages
// =============================================================

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
data class ApiPollOption(
    @Json(name = "_id") val _id: String? = null,
    @Json(name = "text") val text: String? = null,
    @Json(name = "voters") val voters: List<String>? = null
)

@JsonClass(generateAdapter = true)
data class ApiPoll(
    @Json(name = "question") val question: String? = null,
    @Json(name = "options") val options: List<ApiPollOption>? = null,
    @Json(name = "anonymous") val anonymous: Boolean? = false,
    @Json(name = "multiSelect") val multiSelect: Boolean? = false,
    @Json(name = "allowChangeVote") val allowChangeVote: Boolean? = false,
    @Json(name = "closedAt") val closedAt: String? = null
)

@JsonClass(generateAdapter = true)
data class ApiLocation(
    @Json(name = "lat") val lat: Double? = null,
    @Json(name = "lng") val lng: Double? = null,
    @Json(name = "name") val name: String? = null
)

@JsonClass(generateAdapter = true)
data class ApiAudioMetadata(
    @Json(name = "duration") val duration: Int? = null,
    @Json(name = "waveform") val waveform: List<Int>? = null
)

@JsonClass(generateAdapter = true)
data class ApiMessage(
    @Json(name = "_id") val _id: String,
    @Json(name = "conversationId") val conversationId: String? = null,
    @Json(name = "sender") val sender: ApiUser? = null,
    @Json(name = "authorSignature") val authorSignature: String? = null,
    @Json(name = "text") val text: String? = null,
    @Json(name = "type") val type: String? = "text",
    @Json(name = "fileUrl") val fileUrl: String? = null,
    @Json(name = "videoThumbnailUrl") val videoThumbnailUrl: String? = null,
    @Json(name = "fileName") val fileName: String? = null,
    @Json(name = "fileSize") val fileSize: Long? = null,
    @Json(name = "duration") val duration: Int? = null,
    @Json(name = "audioMetadata") val audioMetadata: ApiAudioMetadata? = null,
    @Json(name = "width") val width: Int? = null,
    @Json(name = "height") val height: Int? = null,
    @Json(name = "mimeType") val mimeType: String? = null,
    @Json(name = "location") val location: ApiLocation? = null,
    @Json(name = "replyTo") val replyTo: ApiMessage? = null,
    @Json(name = "forwardFrom") val forwardFrom: String? = null,
    @Json(name = "forwardedFromUser") val forwardedFromUser: ApiUser? = null,
    @Json(name = "reactions") val reactions: List<ApiReaction>? = emptyList(),
    @Json(name = "readBy") val readBy: List<String>? = emptyList(),
    @Json(name = "deliveredTo") val deliveredTo: List<String>? = emptyList(),
    @Json(name = "poll") val poll: ApiPoll? = null,
    @Json(name = "albumId") val albumId: String? = null,
    @Json(name = "albumOrder") val albumOrder: Int? = null,
    @Json(name = "isSilent") val isSilent: Boolean? = false,
    @Json(name = "isEdited") val isEdited: Boolean? = false,
    @Json(name = "editedAt") val editedAt: String? = null,
    @Json(name = "createdAt") val createdAt: String? = null,
    @Json(name = "updatedAt") val updatedAt: String? = null
)

@JsonClass(generateAdapter = true)
data class SendMessageRequest(
    @Json(name = "conversationId") val conversationId: String? = null,
    @Json(name = "receiverId") val receiverId: String? = null,
    @Json(name = "text") val text: String? = null,
    @Json(name = "type") val type: String = "text",
    @Json(name = "fileUrl") val fileUrl: String? = null,
    @Json(name = "videoThumbnailUrl") val videoThumbnailUrl: String? = null,
    @Json(name = "fileName") val fileName: String? = null,
    @Json(name = "duration") val duration: Int? = null,
    @Json(name = "audioMetadata") val audioMetadata: ApiAudioMetadata? = null,
    @Json(name = "location") val location: ApiLocation? = null,
    @Json(name = "replyTo") val replyTo: String? = null,
    @Json(name = "poll") val poll: ApiPoll? = null,
    @Json(name = "isSilent") val isSilent: Boolean = false
)

@JsonClass(generateAdapter = true)
data class ReactionRequest(
    @Json(name = "emoji") val emoji: String
)

@JsonClass(generateAdapter = true)
data class ForwardMessageRequest(
    @Json(name = "conversationId") val conversationId: String,
    @Json(name = "messageId") val messageId: String
)

@JsonClass(generateAdapter = true)
data class PollVoteRequest(
    @Json(name = "optionId") val optionId: String
)

@JsonClass(generateAdapter = true)
data class EditMessageRequest(
    @Json(name = "text") val text: String
)

// =============================================================
// Stickers & GIFs
// =============================================================

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

// =============================================================
// Stories
// =============================================================

@JsonClass(generateAdapter = true)
data class ApiStoryItem(
    @Json(name = "_id") val _id: String? = null,
    @Json(name = "mediaUrl") val mediaUrl: String? = null,
    @Json(name = "mediaType") val mediaType: String? = "image",
    @Json(name = "caption") val caption: String? = null,
    @Json(name = "createdAt") val createdAt: String? = null
)

@JsonClass(generateAdapter = true)
data class ApiStoryGroup(
    @Json(name = "user") val user: ApiUser? = null,
    @Json(name = "stories") val stories: List<ApiStoryItem>? = null
)

// =============================================================
// Gifts
// =============================================================

@JsonClass(generateAdapter = true)
data class ApiGift(
    @Json(name = "_id") val _id: String? = null,
    @Json(name = "name") val name: String? = null,
    @Json(name = "description") val description: String? = null,
    @Json(name = "imageUrl") val imageUrl: String? = null,
    @Json(name = "animationUrl") val animationUrl: String? = null,
    @Json(name = "displayValue") val displayValue: String? = null,
    @Json(name = "isTransferable") val isTransferable: Boolean? = false,
    @Json(name = "sender") val sender: ApiUser? = null,
    @Json(name = "senderName") val senderName: String? = null,
    @Json(name = "isAnonymous") val isAnonymous: Boolean? = false,
    @Json(name = "caption") val caption: String? = null,
    @Json(name = "createdAt") val createdAt: String? = null
)

// =============================================================
// Voice Call (group) — REST endpoints
// =============================================================

@JsonClass(generateAdapter = true)
data class VoiceCallAdminControlRequest(
    @Json(name = "targetUserId") val targetUserId: String,
    @Json(name = "action") val action: String // "mute" | "kick" | "unmute"
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

// =============================================================
// WebRTC ICE servers
// =============================================================

@JsonClass(generateAdapter = true)
data class IceServer(
    @Json(name = "urls") val urls: String? = null,
    @Json(name = "username") val username: String? = null,
    @Json(name = "credential") val credential: String? = null
)

// =============================================================
// Ads
// =============================================================

@JsonClass(generateAdapter = true)
data class ApiAd(
    @Json(name = "_id") val _id: String? = null,
    @Json(name = "title") val title: String? = null,
    @Json(name = "imageUrl") val imageUrl: String? = null,
    @Json(name = "targetUrl") val targetUrl: String? = null,
    @Json(name = "impressions") val impressions: Int? = 0,
    @Json(name = "clicks") val clicks: Int? = 0
)
