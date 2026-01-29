package com.example.social_rede_mobile.network.models

data class PostCreate(
    val username: String,
    val caption: String,
    val imageUri: String?,
    val imageResId: Int? = null,
    val timestamp: Long
)
