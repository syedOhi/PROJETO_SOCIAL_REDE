package com.example.social_rede_mobile.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sender: String,
    val receiver: String,
    val message: String,
    val timestamp: Long,
    val isVoice: Boolean = false,
    val emoji: String? = null,
    val isRead: Boolean = false // ✅ New field to track if message is seen
)
