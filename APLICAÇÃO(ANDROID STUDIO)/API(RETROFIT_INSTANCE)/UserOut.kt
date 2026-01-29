package com.example.social_rede_mobile.network.models

data class UserOut(
    val id: Int,
    val username: String,
    val fullName: String,
    val password: String,
    val bio: String?,
    val dob: String?,
    val profileImageUri: String?,
    val profileImageResId: Int?,

    // ✅ new
    val created_at: String,
    val last_active: String
)