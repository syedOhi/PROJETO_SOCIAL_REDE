package com.example.social_rede_mobile.network.models

data class NotificationCreate(
    val id: String,
    val username: String,
    val message: String,
    val type: String,
    val targetUsername: String,
    val timestamp: Long,
    val seen: Boolean = false        // <-- THIS MUST BE ADDED

)
