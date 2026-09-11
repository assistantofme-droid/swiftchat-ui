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
