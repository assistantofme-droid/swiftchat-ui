package com.example.ui.call

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.data.api.ApiClient
import com.example.data.api.InitiateCallRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

data class CallState(
    val callId: String? = null,
    val contactName: String = "Telegram Contact",
    val contactAvatarUrl: String? = null,
    val isVideo: Boolean = false,
    val isMuted: Boolean = false,
    val isSpeakerOn: Boolean = false,
    val isCameraOff: Boolean = false,
    val isFrontCamera: Boolean = true,
    val isConnected: Boolean = false,
    val durationSeconds: Int = 0,
    val isFloating: Boolean = false,
    val isInPip: Boolean = false,
    val isActive: Boolean = false
)

object CallManager {
    private val scope = CoroutineScope(Dispatchers.Main)
    private val handler = Handler(Looper.getMainLooper())
    private var timerRunnable: Runnable? = null

    private val _callState = MutableStateFlow(CallState())
    val callState: StateFlow<CallState> = _callState.asStateFlow()

    fun startCall(
        context: Context,
        contactName: String,
        contactAvatarUrl: String? = null,
        isVideo: Boolean = false,
        recipientId: String = "recipient"
    ) {
        val tempCallId = UUID.randomUUID().toString()
        _callState.value = CallState(
            callId = tempCallId,
            contactName = contactName,
            contactAvatarUrl = contactAvatarUrl,
            isVideo = isVideo,
            isMuted = false,
            isSpeakerOn = isVideo, // Video calls default to speakerphone
            isCameraOff = false,
            isFrontCamera = true,
            isConnected = true,
            durationSeconds = 0,
            isFloating = false,
            isInPip = false,
            isActive = true
        )

        startCallTimer()

        // Call API
        scope.launch {
            try {
                ApiClient.service.initiateCall(
                    InitiateCallRequest(
                        recipientId = recipientId,
                        type = if (isVideo) "video" else "voice"
                    )
                )
            } catch (e: Exception) {
                Log.e("CallManager", "initiateCall error", e)
            }
        }
    }

    fun toggleMute() {
        _callState.value = _callState.value.copy(isMuted = !_callState.value.isMuted)
    }

    fun toggleSpeaker() {
        _callState.value = _callState.value.copy(isSpeakerOn = !_callState.value.isSpeakerOn)
    }

    fun toggleCamera() {
        _callState.value = _callState.value.copy(isCameraOff = !_callState.value.isCameraOff)
    }

    fun flipCamera() {
        _callState.value = _callState.value.copy(isFrontCamera = !_callState.value.isFrontCamera)
    }

    fun setFloating(floating: Boolean) {
        _callState.value = _callState.value.copy(isFloating = floating)
    }

    fun setInPictureInPicture(inPip: Boolean) {
        _callState.value = _callState.value.copy(
            isInPip = inPip,
            isFloating = if (inPip) false else _callState.value.isFloating
        )
    }

    fun endCall() {
        val currentCallId = _callState.value.callId
        stopCallTimer()
        _callState.value = CallState(isActive = false)

        if (currentCallId != null) {
            scope.launch {
                try {
                    ApiClient.service.endCall(currentCallId)
                } catch (e: Exception) {
                    Log.e("CallManager", "endCall error", e)
                }
            }
        }
    }

    private fun startCallTimer() {
        stopCallTimer()
        timerRunnable = object : Runnable {
            override fun run() {
                if (_callState.value.isActive) {
                    _callState.value = _callState.value.copy(
                        durationSeconds = _callState.value.durationSeconds + 1
                    )
                    handler.postDelayed(this, 1000)
                }
            }
        }
        handler.postDelayed(timerRunnable!!, 1000)
    }

    private fun stopCallTimer() {
        timerRunnable?.let { handler.removeCallbacks(it) }
        timerRunnable = null
    }

    fun formatDuration(seconds: Int): String {
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%02d:%02d", minutes, remainingSeconds)
    }
}
