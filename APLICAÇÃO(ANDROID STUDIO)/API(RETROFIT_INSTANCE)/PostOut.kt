package com.example.social_rede_mobile.network.models

data class PostOut(
    val id: Int,
    val username: String,
    val caption: String,
    val imageUri: String?,
    val imageResId: Int?,
    val likeCount: Int,
    val isLiked: Boolean,
    val timestamp: Long,
    val commentCount: Int   // ✅ ADD THIS
)

