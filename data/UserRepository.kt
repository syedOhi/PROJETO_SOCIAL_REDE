package com.example.social_rede_mobile.data

import androidx.lifecycle.LiveData
import kotlinx.coroutines.flow.Flow

class UserRepository(private val userDao: UserDao) {

    suspend fun register(user: User) = userDao.insertUser(user)

    suspend fun login(username: String, password: String): User? =
        userDao.login(username, password)

    suspend fun userExists(username: String): Boolean =
        userDao.getUser(username) != null

    fun getUserByUsername(username: String): LiveData<User> =
        userDao.getUserByUsername(username)

    suspend fun updateUserDetails(username: String, bio: String, dob: String, profileImageResId: Int) {
        userDao.updateUserDetails(username, bio, dob, profileImageResId)
    }

    suspend fun updateUserDetailsWithUri(username: String, bio: String, dob: String, profileImageUri: String) {
        userDao.updateUserDetailsWithUri(username, bio, dob, profileImageUri)
    }

    suspend fun updateUser(user: User) {
        userDao.updateUser(user)
    }

    suspend fun getUser(username: String): User? {
        return userDao.getUser(username)
    }

    fun getUserLiveData(username: String): LiveData<User?> {
        return userDao.getUserLive(username)
    }

    fun searchUsers(query: String): Flow<List<User>> {
        return userDao.searchUsersByUsername(query)
    }

    fun getAllUsers(): Flow<List<User>> = userDao.getAllUsers()

    suspend fun followUser(follow: Follow) = userDao.followUser(follow)

    suspend fun unfollowUser(follow: Follow) = userDao.unfollowUser(follow)

    fun getFollowerCount(username: String): Flow<Int> = userDao.getFollowerCount(username)

    fun getFollowedUsers(username: String): Flow<List<User>> {
        return userDao.getFollowedUsers(username)
    }

    fun getFollowingCount(username: String): Flow<Int> = userDao.getFollowingCount(username)
}
