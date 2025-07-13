package com.example.social_rede_mobile.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class PostViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val repository = PostRepository(db.postDao())

    // All posts observed as LiveData
    val posts: LiveData<List<Post>> = repository.allPosts

    // Insert a new post
    fun insert(post: Post) = viewModelScope.launch {
        repository.insert(post)
    }

    // Delete a post
    fun delete(post: Post) = viewModelScope.launch {
        repository.delete(post)
    }

    // Get posts by a specific user
    fun getPostsByUser(username: String): Flow<List<Post>> {
        return repository.getPostsByUsername(username)
    }

    fun updateLikeStatus(postId: Int, liked: Boolean, delta: Int) {
        viewModelScope.launch {
            repository.updateLikeStatus(postId, liked, delta)
        }
    }

}
