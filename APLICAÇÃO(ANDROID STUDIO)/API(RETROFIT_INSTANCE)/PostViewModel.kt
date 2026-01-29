package com.example.social_rede_mobile.data

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.social_rede_mobile.network.RetrofitInstance
import com.example.social_rede_mobile.network.models.PostCreate
import com.example.social_rede_mobile.network.models.PostOut
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PostViewModel(application: Application) : AndroidViewModel(application) {

    // ------------------------------
    // ROOMDB SUPPORT (OLD SYSTEM)
    // ------------------------------

    private val db = AppDatabase.getDatabase(application)
    private val repository = PostRepository(db.postDao())

    val posts: LiveData<List<Post>> = repository.allPosts

    fun insert(post: Post) = viewModelScope.launch {
        repository.insert(post)
    }

    fun delete(post: Post) = viewModelScope.launch {
        repository.delete(post)
    }

    fun getPostsByUser(username: String): Flow<List<Post>> {
        return repository.getPostsByUsername(username)
    }

    fun updateLikeStatus(postId: Int, liked: Boolean, delta: Int) {
        viewModelScope.launch {
            repository.updateLikeStatus(postId, liked, delta)
        }
    }

    // ------------------------------
    // API: FEED POSTS
    // ------------------------------

    private val _apiPosts = MutableStateFlow<List<PostOut>>(emptyList())
    val apiPosts = _apiPosts.asStateFlow()

    fun loadPostsFromApi(username: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val posts = RetrofitInstance.api.getAllPosts(username)
                _apiPosts.value = posts
            } catch (e: Exception) {
                Log.e("PostViewModel", "Error loading posts: ${e.message}")
            }
        }
    }

    fun toggleLike(postId: Int, username: String, liked: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (liked) {
                    RetrofitInstance.api.likePost(postId, username)
                } else {
                    RetrofitInstance.api.unlikePost(postId, username)
                }

                // Refresh feed after like/unlike
                loadPostsFromApi(username)

            } catch (e: Exception) {
                Log.e("PostViewModel", "Like error: ${e.message}")
            }
        }
    }


    // ------------------------------
    // API: CREATE POST
    // ------------------------------

    fun createPostApi(username: String, caption: String, imageUri: String, onComplete: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val body = PostCreate(
                    username = username,
                    caption = caption,
                    imageUri = imageUri,
                    imageResId = null,
                    timestamp = System.currentTimeMillis()
                )

                RetrofitInstance.api.createPost(body)

                withContext(Dispatchers.Main) { onComplete() }

            } catch (e: Exception) {
                Log.e("PostViewModel", "Error creating post: ${e.message}")
            }
        }
    }


    // ------------------------------
    // API: USER POSTS (PROFILE SCREEN)
    // ------------------------------

    private val _userApiPosts = MutableStateFlow<List<PostOut>>(emptyList())
    val userApiPosts = _userApiPosts.asStateFlow()

    fun loadUserPosts(username: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _userApiPosts.value = RetrofitInstance.api.getUserPosts(username)
            } catch (e: Exception) {
                Log.e("PostViewModel", "User posts error: ${e.message}")
            }
        }
    }


    // ------------------------------
    // API: USER POSTS (SEARCH DETAILS SCREEN)
    // ------------------------------

    fun loadPostsByUserApi(username: String): StateFlow<List<PostOut>> {
        return flow {
            emit(RetrofitInstance.api.getUserPosts(username))
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            emptyList()
        )
    }
    // ------------------------------
// API: DELETE POST
// ------------------------------
    fun deletePostApi(postId: Int, username: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                RetrofitInstance.api.deletePost(postId, username)
                loadPostsFromApi(username)
            } catch (e: Exception) {
                Log.e("PostViewModel", "Delete post error: ${e.message}")
            }
        }
    }

    fun reportPostApi(postId: Int, reportedBy: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                RetrofitInstance.api.reportPost(postId, reportedBy)
                // optional refresh (doesn't hurt)
                loadPostsFromApi(reportedBy)
            } catch (e: Exception) {
                Log.e("PostViewModel", "Report post error: ${e.message}")
            }
        }
    }

}
