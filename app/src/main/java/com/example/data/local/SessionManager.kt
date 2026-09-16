package com.example.data.local

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("tg_session_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_TOKEN = "auth_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_PHONE = "user_phone"
        private const val KEY_NAME = "user_name"
        private const val KEY_USERNAME = "user_username"
        private const val KEY_AVATAR = "user_avatar"
        private const val KEY_BIO = "user_bio"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_IS_DARK_MODE = "is_dark_mode"
        private const val KEY_LANGUAGE = "language" // "fa" | "en"
        private const val KEY_MESSAGE_TEXT_SIZE = "message_text_size" // 12..22
        private const val KEY_MESSAGE_CORNER_RADIUS = "message_corner_radius" // 0..28
        private const val KEY_DOUBLE_TAP_EMOJI = "double_tap_emoji"
        private const val KEY_AUTO_DL_MOBILE = "auto_dl_mobile"
        private const val KEY_AUTO_DL_WIFI = "auto_dl_wifi"
        private const val KEY_AUTO_DL_ROAMING = "auto_dl_roaming"
        private const val KEY_SAVE_GALLERY_PRIVATE = "save_gallery_private"
        private const val KEY_SAVE_GALLERY_GROUPS = "save_gallery_groups"
        private const val KEY_SAVE_GALLERY_CHANNELS = "save_gallery_channels"
        private const val KEY_POWER_SAVING = "power_saving"
        private const val KEY_POWER_LOW_QUALITY = "power_low_quality"
        private const val KEY_POWER_DISABLE_ANIM = "power_disable_anim"
        private const val KEY_POWER_DISABLE_AUTOPLAY = "power_disable_autoplay"
        // Privacy
        private const val KEY_PRIVACY_LAST_SEEN = "privacy_last_seen"
        private const val KEY_PRIVACY_PHONE = "privacy_phone"
        private const val KEY_PRIVACY_FORWARDED = "privacy_forwarded"
        private const val KEY_PRIVACY_GROUPS = "privacy_groups"
        private const val KEY_IS_PHONE_HIDDEN = "is_phone_hidden"
    }

    var token: String?
        get() = prefs.getString(KEY_TOKEN, null)
        set(value) = prefs.edit().putString(KEY_TOKEN, value).apply()

    var userId: String?
        get() = prefs.getString(KEY_USER_ID, null)
        set(value) = prefs.edit().putString(KEY_USER_ID, value).apply()

    var phone: String?
        get() = prefs.getString(KEY_PHONE, null)
        set(value) = prefs.edit().putString(KEY_PHONE, value).apply()

    var name: String?
        get() = prefs.getString(KEY_NAME, null)
        set(value) = prefs.edit().putString(KEY_NAME, value).apply()

    var username: String?
        get() = prefs.getString(KEY_USERNAME, null)
        set(value) = prefs.edit().putString(KEY_USERNAME, value).apply()

    var avatar: String?
        get() = prefs.getString(KEY_AVATAR, null)
        set(value) = prefs.edit().putString(KEY_AVATAR, value).apply()

    var bio: String?
        get() = prefs.getString(KEY_BIO, "Hey there! I am using Telegram.")
        set(value) = prefs.edit().putString(KEY_BIO, value).apply()

    var isLoggedIn: Boolean
        get() = prefs.getBoolean(KEY_IS_LOGGED_IN, false) && !token.isNullOrBlank()
        set(value) = prefs.edit().putBoolean(KEY_IS_LOGGED_IN, value).apply()

    var isDarkMode: Boolean
        get() = prefs.getBoolean(KEY_IS_DARK_MODE, true)
        set(value) = prefs.edit().putBoolean(KEY_IS_DARK_MODE, value).apply()

    // ===== App preferences =====

    /** "fa" or "en" — defaults to "fa" */
    var language: String
        get() = prefs.getString(KEY_LANGUAGE, "fa") ?: "fa"
        set(value) = prefs.edit().putString(KEY_LANGUAGE, value).apply()

    /** Message text size in sp — range 12..22, default 16 */
    var messageTextSize: Int
        get() = prefs.getInt(KEY_MESSAGE_TEXT_SIZE, 16)
        set(value) = prefs.edit().putInt(KEY_MESSAGE_TEXT_SIZE, value.coerceIn(12, 22)).apply()

    /** Message bubble corner radius in dp — range 0..28, default 20 */
    var messageCornerRadius: Int
        get() = prefs.getInt(KEY_MESSAGE_CORNER_RADIUS, 20)
        set(value) = prefs.edit().putInt(KEY_MESSAGE_CORNER_RADIUS, value.coerceIn(0, 28)).apply()

    /** Emoji used when double-tapping a message — default "❤️" */
    var doubleTapEmoji: String
        get() = prefs.getString(KEY_DOUBLE_TAP_EMOJI, "❤️") ?: "❤️"
        set(value) = prefs.edit().putString(KEY_DOUBLE_TAP_EMOJI, value).apply()

    var autoDownloadMobile: Boolean
        get() = prefs.getBoolean(KEY_AUTO_DL_MOBILE, true)
        set(value) = prefs.edit().putBoolean(KEY_AUTO_DL_MOBILE, value).apply()

    var autoDownloadWifi: Boolean
        get() = prefs.getBoolean(KEY_AUTO_DL_WIFI, true)
        set(value) = prefs.edit().putBoolean(KEY_AUTO_DL_WIFI, value).apply()

    var autoDownloadRoaming: Boolean
        get() = prefs.getBoolean(KEY_AUTO_DL_ROAMING, false)
        set(value) = prefs.edit().putBoolean(KEY_AUTO_DL_ROAMING, value).apply()

    var saveGalleryPrivate: Boolean
        get() = prefs.getBoolean(KEY_SAVE_GALLERY_PRIVATE, false)
        set(value) = prefs.edit().putBoolean(KEY_SAVE_GALLERY_PRIVATE, value).apply()

    var saveGalleryGroups: Boolean
        get() = prefs.getBoolean(KEY_SAVE_GALLERY_GROUPS, false)
        set(value) = prefs.edit().putBoolean(KEY_SAVE_GALLERY_GROUPS, value).apply()

    var saveGalleryChannels: Boolean
        get() = prefs.getBoolean(KEY_SAVE_GALLERY_CHANNELS, false)
        set(value) = prefs.edit().putBoolean(KEY_SAVE_GALLERY_CHANNELS, value).apply()

    var powerSavingEnabled: Boolean
        get() = prefs.getBoolean(KEY_POWER_SAVING, false)
        set(value) = prefs.edit().putBoolean(KEY_POWER_SAVING, value).apply()

    var powerLowQuality: Boolean
        get() = prefs.getBoolean(KEY_POWER_LOW_QUALITY, false)
        set(value) = prefs.edit().putBoolean(KEY_POWER_LOW_QUALITY, value).apply()

    var powerDisableAnimations: Boolean
        get() = prefs.getBoolean(KEY_POWER_DISABLE_ANIM, false)
        set(value) = prefs.edit().putBoolean(KEY_POWER_DISABLE_ANIM, value).apply()

    var powerDisableAutoplay: Boolean
        get() = prefs.getBoolean(KEY_POWER_DISABLE_AUTOPLAY, false)
        set(value) = prefs.edit().putBoolean(KEY_POWER_DISABLE_AUTOPLAY, value).apply()

    // ===== Privacy settings =====
    // Values: "Everyone" | "Contacts" | "Nobody"

    var privacyLastSeen: String
        get() = prefs.getString(KEY_PRIVACY_LAST_SEEN, "Everyone") ?: "Everyone"
        set(value) = prefs.edit().putString(KEY_PRIVACY_LAST_SEEN, value).apply()

    var privacyPhoneNumber: String
        get() = prefs.getString(KEY_PRIVACY_PHONE, "Contacts") ?: "Contacts"
        set(value) = prefs.edit().putString(KEY_PRIVACY_PHONE, value).apply()

    var privacyForwarded: String
        get() = prefs.getString(KEY_PRIVACY_FORWARDED, "Everyone") ?: "Everyone"
        set(value) = prefs.edit().putString(KEY_PRIVACY_FORWARDED, value).apply()

    var privacyGroups: String
        get() = prefs.getString(KEY_PRIVACY_GROUPS, "Everyone") ?: "Everyone"
        set(value) = prefs.edit().putString(KEY_PRIVACY_GROUPS, value).apply()

    var dismissedNotificationId: String?
        get() = prefs.getString("dismissed_notification_id", null)
        set(value) = prefs.edit().putString("dismissed_notification_id", value).apply()
    var isPhoneHidden: Boolean
        get() = prefs.getBoolean(KEY_IS_PHONE_HIDDEN, false)
        set(value) = prefs.edit().putBoolean(KEY_IS_PHONE_HIDDEN, value).apply()

    fun saveSession(
        token: String,
        id: String? = null,
        phone: String? = null,
        name: String? = null,
        username: String? = null,
        avatar: String? = null,
        bio: String? = null
    ) {
        prefs.edit()
            .putString(KEY_TOKEN, token)
            .putString(KEY_USER_ID, id)
            .putString(KEY_PHONE, phone)
            .putString(KEY_NAME, name)
            .putString(KEY_USERNAME, username)
            .putString(KEY_AVATAR, avatar)
            .putString(KEY_BIO, bio ?: "Hey there! I am using Telegram.")
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .apply()
    }

    fun updateProfile(name: String?, username: String?, bio: String?) {
        prefs.edit()
            .apply {
                if (name != null) putString(KEY_NAME, name)
                if (username != null) putString(KEY_USERNAME, username)
                if (bio != null) putString(KEY_BIO, bio)
            }
            .apply()
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}
