package com.example.ui.media

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.ui.call.CallManager

class MediaReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_PLAY_PAUSE = "com.example.ACTION_PLAY_PAUSE"
        const val ACTION_NEXT = "com.example.ACTION_NEXT"
        const val ACTION_PREVIOUS = "com.example.ACTION_PREVIOUS"
        const val ACTION_STOP = "com.example.ACTION_STOP"
        const val ACTION_CALL_MUTE = "com.example.ACTION_CALL_MUTE"
        const val ACTION_CALL_END = "com.example.ACTION_CALL_END"
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_PLAY_PAUSE -> {
                AudioPlayerManager.togglePlayPause()
            }
            ACTION_NEXT -> {
                AudioPlayerManager.skipNext()
            }
            ACTION_PREVIOUS -> {
                AudioPlayerManager.skipPrevious()
            }
            ACTION_STOP -> {
                AudioPlayerManager.stop(context)
            }
            ACTION_CALL_MUTE -> {
                CallManager.toggleMute()
            }
            ACTION_CALL_END -> {
                CallManager.endCall()
            }
        }
    }
}
