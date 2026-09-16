package com.example.ui.screens

data class ChatFolder(
    val id: String,
    val name: String
)

object SettingsRoute {
    const val PRIVACY = "privacy"
    const val CHAT = "chat"
    const val DATA = "data"
    const val FOLDERS = "folders"
    const val DEVICES = "devices"
    const val POWER = "power"
    const val LANGUAGE = "language"
    const val NOTIFICATIONS = "notifications"
}
