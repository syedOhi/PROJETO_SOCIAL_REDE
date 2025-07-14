package com.example.social_rede_mobile.data

import androidx.room.Entity
@Entity(
    tableName = "follows",
    primaryKeys = ["followerUsername", "followedUsername"]
)
data class Follow(
    val followerUsername: String,
    val followedUsername: String
)
