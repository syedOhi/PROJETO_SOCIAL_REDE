package com.example.social_rede_mobile.network.models

data class ChatMessageCreate(
    val sender: String,
    val receiver: String,
    val message: String,
    val timestamp: Long,
    val isVoice: Boolean = false,
    val emoji: String? = null
)

data class ChatMessageOut(
    val id: Int,
    val sender: String,
    val receiver: String,
    val message: String,
    val timestamp: Long,
    val isVoice: Boolean = false,
    val emoji: String? = null,
    val isRead: Boolean
)

data class UnreadCountResponse(
    val unread: Int
)

data class ReactResponse(
    val message: String,
    val id: Int,
    val emoji: String
)
data class ChatRequestOut(
    val id: Int,
    val sender: String,
    val receiver: String,
    val timestamp: Long
)

