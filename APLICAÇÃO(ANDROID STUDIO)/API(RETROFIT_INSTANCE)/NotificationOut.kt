package com.example.social_rede_mobile.network.models

data class NotificationOut(
    val id: String,
    val username: String,
    val message: String,
    val type: String,
    val targetUsername: String,
    val timestamp: Long,
    val seen: Boolean
)
