package com.example.social_rede_mobile.data

import androidx.lifecycle.LiveData
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    // --- Registration & Authentication ---
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: User)

    @Query("SELECT * FROM users WHERE username = :username AND password = :password")
    suspend fun login(username: String, password: String): User?

    @Query("SELECT * FROM users WHERE username = :username")
    suspend fun getUser(username: String): User?

    // --- Profile Detail Updates ---
    @Query("UPDATE users SET bio = :bio, dob = :dob, profileImageResId = :profileImageResId WHERE username = :username")
    suspend fun updateUserDetails(username: String, bio: String, dob: String, profileImageResId: Int)

    @Query("UPDATE users SET bio = :bio, dob = :dob, profileImageUri = :profileImageUri WHERE username = :username")
    suspend fun updateUserDetailsWithUri(username: String, bio: String, dob: String, profileImageUri: String)

    @Update
    suspend fun updateUser(user: User)

    // --- User Retrieval ---
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    fun getUserByUsername(username: String): LiveData<User>

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    fun getUserByUsernameDirect(username: String): User?

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    fun getUserLive(username: String): LiveData<User?>

    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<User>>

    @Query("SELECT * FROM users WHERE username LIKE '%' || :query || '%'")
    fun searchUsersByUsername(query: String): Flow<List<User>>

    // --- Follow/Unfollow Logic ---
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun followUser(follow: Follow)

    @Delete
    suspend fun unfollowUser(follow: Follow)

    @Query("SELECT COUNT(*) FROM follows WHERE followedUsername = :username")
    fun getFollowerCount(username: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM follows WHERE followerUsername = :username")
    fun getFollowingCount(username: String): Flow<Int>

    @Query("SELECT EXISTS(SELECT 1 FROM follows WHERE followerUsername = :follower AND followedUsername = :followed)")
    fun isFollowing(follower: String, followed: String): Flow<Boolean>

    @Query("""
        SELECT * FROM users WHERE username IN (
            SELECT followedUsername FROM follows WHERE followerUsername = :username
        )
    """)
    fun getFollowedUsers(username: String): Flow<List<User>>
}
