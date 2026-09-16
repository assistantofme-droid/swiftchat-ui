package com.example.data.api

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    // =============================================================
    // Authentication — /auth/*
    // =============================================================

    @POST("auth/send-otp")
    suspend fun sendOtp(
        @Body request: SendOtpRequest
    ): Response<SendOtpResponse>

    @POST("auth/verify-otp")
    suspend fun verifyOtp(
        @Body request: VerifyOtpRequest
    ): Response<VerifyOtpResponse>

    @GET("auth/me")
    suspend fun getMe(): Response<ApiUser>

    @PUT("auth/profile")
    suspend fun updateProfile(
        @Body request: UpdateProfileRequest
    ): Response<ApiUser>

    /**
     * Upload an avatar / banner file as multipart/form-data.
     * Server stores under /uploads/ and returns the updated User.
     */
    @Multipart
    @PUT("auth/profile")
    suspend fun updateProfileAvatar(
        @Part file: MultipartBody.Part
    ): Response<ApiUser>

    @GET("auth/users/{userId}")
    suspend fun getUser(
        @Path("userId") userId: String
    ): Response<ApiUser>

    @GET("auth/getUserByUsername/{username}")
    suspend fun getUserByUsername(
        @Path("username") username: String
    ): Response<ApiUser>

    @GET("auth/contacts")
    suspend fun getContacts(): Response<List<ApiContact>>

    @POST("auth/contacts")
    suspend fun addContact(
        @Body request: AddContactRequest
    ): Response<ApiContact>

    @POST("auth/check-contacts")
    suspend fun checkContacts(
        @Body request: CheckContactsRequest
    ): Response<List<ApiContact>>

    @GET("auth/blocked/users")
    suspend fun getBlockedUsers(): Response<List<ApiUser>>

    @GET("auth/sessions")
    suspend fun getSessions(): Response<List<ApiSession>>

    /** POST /auth/logout-all — invalidates ALL sessions on the account (spec §1.9). */
    @POST("auth/logout-all")
    suspend fun logoutAllDevices(): Response<ResponseBody>

    /**
     * POST /messages/contacts — start a private chat with a user and save them
     * as a contact in one shot (spec §3 §messages/contacts).
     * Body: { identifier: "phone or @username" }
     */
    @POST("messages/contacts")
    suspend fun startPrivateChatWithContact(
        @Body body: AddContactRequest
    ): Response<ApiConversation>

    @POST("auth/block")
    suspend fun blockUser(
        @Body body: Map<String, String>
    ): Response<ResponseBody>

    @POST("auth/unblock")
    suspend fun unblockUser(
        @Body body: Map<String, String>
    ): Response<ResponseBody>

    // =============================================================
    // Conversations — /conversations/*
    // =============================================================

    @POST("conversations/group")
    suspend fun createGroup(
        @Body request: CreateConversationRequest
    ): Response<ApiConversation>

    @Multipart
    @POST("conversations/group")
    suspend fun createGroupWithAvatar(
        @Part file: MultipartBody.Part,
        @Part("name") name: RequestBody,
        @Part("type") type: RequestBody,
        @Part("description") description: RequestBody?,
        @Part("publicId") publicId: RequestBody?,
        @Part("isPrivateLink") isPrivateLink: RequestBody?,
        @Part("participants[]") participants: List<RequestBody>
    ): Response<ApiConversation>

    @GET("conversations")
    suspend fun getConversations(): Response<List<ApiConversation>>

    /**
     * GET /conversations/saved — fetch the user's Saved Messages conversation
     * (a private self-chat). Per spec §2.
     */
    @GET("conversations/saved")
    suspend fun getSavedMessagesConversation(): Response<ApiConversation>

    @GET("conversations/{id}")
    suspend fun getConversation(
        @Path("id") id: String
    ): Response<ApiConversation>

    @PUT("conversations/{id}")
    suspend fun updateConversation(
        @Path("id") id: String,
        @Body request: UpdateConversationRequest
    ): Response<ApiConversation>

    @Multipart
    @PUT("conversations/{id}/edit")
    suspend fun updateConversationWithAvatar(
        @Path("id") id: String,
        @Part file: MultipartBody.Part?,
        @Part("name") name: RequestBody?,
        @Part("description") description: RequestBody?
    ): Response<ApiConversation>

    @DELETE("conversations/{id}")
    suspend fun deleteConversation(
        @Path("id") id: String
    ): Response<ResponseBody>

    @PUT("conversations/{id}/mute")
    suspend fun toggleMute(
        @Path("id") id: String
    ): Response<MuteToggleResponse>

    @PUT("conversations/{id}/pin")
    suspend fun pinMessage(
        @Path("id") conversationId: String,
        @Body request: PinMessageRequest
    ): Response<ResponseBody>

    @GET("conversations/discover/search")
    suspend fun discoverSearch(
        @Query("query") query: String
    ): Response<List<DiscoverSearchResult>>

    @GET("conversations/invite/{code}")
    suspend fun getInviteInfo(
        @Path("code") code: String
    ): Response<ApiConversation>

    @POST("conversations/invite/{code}/join")
    suspend fun joinByInvite(
        @Path("code") code: String
    ): Response<ApiConversation>

    @POST("conversations/{id}/join")
    suspend fun joinConversation(
        @Path("id") id: String
    ): Response<ApiConversation>

    @POST("conversations/{id}/leave")
    suspend fun leaveConversation(
        @Path("id") id: String
    ): Response<ResponseBody>

    // =============================================================
    // Messages — /messages/*
    // =============================================================

    /**
     * Note: per spec, this is mounted at /messages/:conversationId
     * (NOT /conversations/:id/messages). The Express route file
     * /routes/messages.js mounts at /messages so the actual path is
     * GET /api/messages/:conversationId
     */
    @GET("messages/{conversationId}")
    suspend fun getMessages(
        @Path("conversationId") conversationId: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50,
        @Query("before") before: String? = null
    ): Response<List<ApiMessage>>

    @POST("messages")
    suspend fun sendMessage(
        @Body request: SendMessageRequest
    ): Response<ApiMessage>

    /**
     * Send media as multipart/form-data. The `file` part is the binary
     * payload; the other parts are metadata fields per the spec.
     */
    @Multipart
    @POST("messages")
    suspend fun sendMessageMedia(
        @Part file: MultipartBody.Part,
        @Part("conversationId") conversationId: RequestBody?,
        @Part("receiverId") receiverId: RequestBody?,
        @Part("type") type: RequestBody,
        @Part("text") text: RequestBody?,
        @Part("replyTo") replyTo: RequestBody?,
        @Part("isSilent") isSilent: RequestBody?,
        @Part("duration") duration: RequestBody?,
        @Part("audioMetadata") audioMetadata: RequestBody?,
        @Part("location") location: RequestBody?
    ): Response<ApiMessage>

    @PUT("messages/{messageId}")
    suspend fun editMessage(
        @Path("messageId") messageId: String,
        @Body request: EditMessageRequest
    ): Response<ApiMessage>

    @DELETE("messages/{messageId}")
    suspend fun deleteMessage(
        @Path("messageId") messageId: String
    ): Response<ResponseBody>

    @POST("messages/{messageId}/react")
    suspend fun reactMessage(
        @Path("messageId") messageId: String,
        @Body body: ReactionRequest
    ): Response<ResponseBody>

    @POST("messages/forward")
    suspend fun forwardMessage(
        @Body request: ForwardMessageRequest
    ): Response<ApiMessage>

    /**
     * GET /messages/{conversationId}/media — fetch the media-only feed
     * (images/videos/files) shared in a conversation. Per spec §3.
     */
    @GET("messages/{conversationId}/media")
    suspend fun getSharedMedia(
        @Path("conversationId") conversationId: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50
    ): Response<List<ApiMessage>>

    /**
     * POST /messages/{messageId}/view_once — register that a view-once
     * photo was opened. Per spec §3.
     */
    @POST("messages/{messageId}/view_once")
    suspend fun markViewOnceViewed(
        @Path("messageId") messageId: String
    ): Response<ResponseBody>

    /**
     * POST /messages/stop-generation/{messageId} — abort an AI bot's
     * streaming reply. Per spec §3.
     */
    @POST("messages/stop-generation/{messageId}")
    suspend fun stopGeneration(
        @Path("messageId") messageId: String
    ): Response<ResponseBody>

    /**
     * POST /messages/callback_query — simulate a tap on an inline bot
     * button (callback_data). Per spec §3.
     */
    @POST("messages/callback_query")
    suspend fun sendCallbackQuery(
        @Body body: Map<String, String>
    ): Response<ResponseBody>

    @POST("messages/{messageId}/poll/vote")
    suspend fun votePoll(
        @Path("messageId") messageId: String,
        @Body body: PollVoteRequest
    ): Response<ApiMessage>

    @POST("messages/{messageId}/poll/close")
    suspend fun closePoll(
        @Path("messageId") messageId: String
    ): Response<ApiMessage>

    @POST("messages/conversations/{conversationId}/read")
    suspend fun markConversationRead(
        @Path("conversationId") conversationId: String
    ): Response<ResponseBody>

    @GET("messages/conversations")
    suspend fun getConversationsAlt(): Response<List<ApiConversation>>

    @GET("messages/conversations/{userId}")
    suspend fun getPrivateConversation(
        @Path("userId") userId: String
    ): Response<ApiConversation>

    // =============================================================
    // Stickers & GIFs
    // =============================================================

    @GET("stickers")
    suspend fun getStickers(): Response<List<ApiSticker>>

    @GET("gifs")
    suspend fun getGifs(): Response<List<ApiGif>>

    @GET("gifs/global")
    suspend fun getGlobalGifs(): Response<List<ApiGif>>

    @POST("gifs/save")
    suspend fun saveGif(
        @Body body: Map<String, String>
    ): Response<ApiGif>

    @DELETE("gifs/{gifId}")
    suspend fun deleteGif(
        @Path("gifId") gifId: String
    ): Response<ResponseBody>

    // =============================================================
    // Stories
    // =============================================================

    @GET("stories")
    suspend fun getStories(): Response<List<ApiStoryGroup>>

    @GET("stories/me")
    suspend fun getMyStories(): Response<List<ApiStoryItem>>

    @Multipart
    @POST("stories")
    suspend fun createStory(
        @Part file: MultipartBody.Part,
        @Part("caption") caption: RequestBody?
    ): Response<ApiStoryItem>

    @POST("stories/{id}/view")
    suspend fun viewStory(
        @Path("id") id: String
    ): Response<ResponseBody>

    @DELETE("stories/{id}")
    suspend fun deleteStory(
        @Path("id") id: String
    ): Response<ResponseBody>

    // =============================================================
    // Gifts
    // =============================================================

    @GET("gifts/user/{userId}")
    suspend fun getUserGifts(
        @Path("userId") userId: String
    ): Response<List<ApiGift>>

    @GET("gifts/{giftId}")
    suspend fun getGift(
        @Path("giftId") giftId: String
    ): Response<ApiGift>

    @POST("gifts/transfer")
    suspend fun transferGift(
        @Body body: Map<String, Any?>
    ): Response<ResponseBody>

    // =============================================================
    // Voice Call (group) — REST endpoints
    // =============================================================

    @POST("conversations/{id}/voice-call/start")
    suspend fun startGroupCall(
        @Path("id") id: String
    ): Response<ApiConversation>

    @POST("conversations/{id}/voice-call/stop")
    suspend fun stopGroupCall(
        @Path("id") id: String
    ): Response<ApiConversation>

    @POST("conversations/{id}/voice-call/join")
    suspend fun joinGroupCall(
        @Path("id") id: String
    ): Response<ApiConversation>

    @POST("conversations/{id}/voice-call/leave")
    suspend fun leaveGroupCall(
        @Path("id") id: String
    ): Response<ApiConversation>

    @POST("conversations/{id}/voice-call/toggle-mute")
    suspend fun toggleGroupCallMute(
        @Path("id") id: String
    ): Response<ApiConversation>

    @POST("conversations/{id}/voice-call/admin-control")
    suspend fun groupCallAdminControl(
        @Path("id") id: String,
        @Body body: VoiceCallAdminControlRequest
    ): Response<ApiConversation>

    @GET("webrtc/ice-servers")
    suspend fun getIceServers(): Response<List<IceServer>>

    // =============================================================
    // P2P Calls — legacy REST endpoints kept for CallManager.kt
    // (The actual signaling happens over Socket.IO per spec §13.1,
    //  but the client still calls these for call logging / state.)
    // =============================================================

    @POST("calls/initiate")
    suspend fun initiateCall(
        @Body request: InitiateCallRequest
    ): Response<CallResponse>

    @POST("calls/{callId}/end")
    suspend fun endCall(
        @Path("callId") callId: String
    ): Response<ResponseBody>

    // =============================================================
    // Ads
    // =============================================================

    @GET("ads/public")
    suspend fun getPublicAds(): Response<List<ApiAd>>

    @GET("settings/public")
    suspend fun getPublicSettings(): Response<List<Any>>

    @GET("auth/startup-config")
    suspend fun getStartupConfig(): Response<Any?>
}
