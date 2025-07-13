package com.example.social_rede_mobile.firestore

data class FirestoreMessage(
    val id: String = "",
    val sender: String = "",
    val receiver: String = "",
    val message: String = "",
    val timestamp: Long = 0,
    val emoji: String? = null,
    val isVoice: Boolean = false,
    val isRead: Boolean = false
)
