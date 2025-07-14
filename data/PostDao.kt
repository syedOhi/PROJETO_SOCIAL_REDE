package com.example.social_rede_mobile.data

import androidx.lifecycle.LiveData
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PostDao {
    @Query("SELECT * FROM posts ORDER BY timestamp DESC")
    fun getAllPosts(): LiveData<List<Post>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: Post)

    @Delete
    suspend fun deletePost(post: Post)

    @Query("SELECT * FROM posts WHERE username = :username")
    fun getPostsByUsername(username: String): Flow<List<Post>>

    @Query("UPDATE posts SET isLiked = :liked, likeCount = likeCount + :delta WHERE id = :postId")
    suspend fun updateLikeStatus(postId: Int, liked: Boolean, delta: Int)
}
