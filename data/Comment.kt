package com.example.social_rede_mobile.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "comments")
data class Comment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val postId: Int,
    val username: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)
