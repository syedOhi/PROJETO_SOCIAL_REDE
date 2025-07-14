package com.example.social_rede_mobile.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,  // ✅ Now Room will assign ID automatically
    val username: String,
    val fullName: String,
    val password: String,
    val bio: String = "",
    val dob: String = "",
    val profileImageUri: String? = null,

    val profileImageResId: Int? = null
)

