package com.example.ui.components

import com.example.data.model.AvatarType

data class UserProfileData(
    val id: String,
    val name: String,
    val username: String? = null,
    val phone: String? = null,
    val bio: String? = null,
    val avatarUrl: String? = null,
    val avatarType: AvatarType = AvatarType.MOTORCYCLE,
    val isOnline: Boolean = false,
    val isSelf: Boolean = false,
    val birthday: String? = null
)
