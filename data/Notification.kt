package com.example.social_rede_mobile.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "notifications")
data class Notification(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val username: String,           // The actor, e.g. "alice"
    val message: String,            // e.g. "started following you."
    val timestamp: Long = System.currentTimeMillis(),
    val seen: Boolean = false,
    val type: String,
    val targetUsername: String      // 🚨 New: who this notification is meant for
)
