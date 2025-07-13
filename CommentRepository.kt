package com.example.social_rede_mobile.data

class CommentRepository(private val dao: CommentDao) {
    fun getCommentsForPost(postId: Int) = dao.getCommentsForPost(postId)
    suspend fun insert(comment: Comment) = dao.insert(comment)
}
