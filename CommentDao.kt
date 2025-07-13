package com.example.social_rede_mobile.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
@Dao
interface CommentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(comment: Comment)

    @Query("SELECT * FROM comments WHERE postId = :postId ORDER BY timestamp DESC")
    fun getCommentsForPost(postId: Int): Flow<List<Comment>>
}

