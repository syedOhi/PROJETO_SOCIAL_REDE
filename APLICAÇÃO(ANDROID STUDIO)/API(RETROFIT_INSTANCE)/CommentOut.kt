package com.example.social_rede_mobile.network.models

data class CommentOut(
    val id: Int,
    val postId: Int,
    val username: String,
    val text: String,
    val timestamp: Long
)
