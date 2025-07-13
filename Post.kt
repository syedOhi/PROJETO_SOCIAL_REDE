package com.example.social_rede_mobile.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "posts")
data class Post(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val caption: String,
    val imageResId: Int? = null,        // For static image support (optional)
    val imageUri: String? = null,       // For dynamic image picker
    val isLiked: Boolean = false,
    val likeCount: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)
