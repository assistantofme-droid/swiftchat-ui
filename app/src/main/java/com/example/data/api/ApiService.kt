package com.example.data.api

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

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

    @GET("messages/conversations")
    suspend fun getConversations(): Response<List<ApiConversation>>

    @GET("conversations")
    suspend fun getConversationsAlt(): Response<List<ApiConversation>>

    @GET("messages/{conversationId}")
    suspend fun getMessages(
        @Path("conversationId") conversationId: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50
    ): Response<List<ApiMessage>>

    @POST("messages")
    suspend fun sendMessage(
        @Body request: SendMessageRequest
    ): Response<ApiMessage>

    @POST("messages/{messageId}/react")
    suspend fun reactMessage(
        @Path("messageId") messageId: String,
        @Body body: ReactionRequest
    ): Response<ResponseBody>

    @POST("messages/conversations/{conversationId}/read")
    suspend fun markConversationRead(
        @Path("conversationId") conversationId: String
    ): Response<ResponseBody>

    @GET("stickers")
    suspend fun getStickers(): Response<List<ApiSticker>>

    @GET("gifs")
    suspend fun getGifs(): Response<List<ApiGif>>

    @GET("gifs/global")
    suspend fun getGlobalGifs(): Response<List<ApiGif>>

    @retrofit2.http.PUT("auth/profile")
    suspend fun updateProfile(
        @Body request: UpdateProfileRequest
    ): Response<ApiUser>
}
