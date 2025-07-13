package com.example.social_rede_mobile.data
import kotlinx.coroutines.flow.Flow
class PostRepository(private val postDao: PostDao) {
    val allPosts = postDao.getAllPosts()

    suspend fun insert(post: Post) {
        postDao.insertPost(post)
    }

    suspend fun delete(post: Post) {
        postDao.deletePost(post)
    }
    fun getPostsByUsername(username: String): Flow<List<Post>> {
        return postDao.getPostsByUsername(username)
    }
    suspend fun updateLikeStatus(postId: Int, liked: Boolean, delta: Int) {
        postDao.updateLikeStatus(postId, liked, delta)
    }


}
