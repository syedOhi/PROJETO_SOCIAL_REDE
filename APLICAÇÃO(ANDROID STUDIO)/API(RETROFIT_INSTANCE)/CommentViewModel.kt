package com.example.social_rede_mobile.data
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.social_rede_mobile.data.AppDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import com.example.social_rede_mobile.data.Comment
import com.example.social_rede_mobile.data.CommentRepository
import kotlinx.coroutines.Dispatchers
import com.example.social_rede_mobile.network.RetrofitInstance
import com.example.social_rede_mobile.network.models.CommentCreate


class CommentViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = CommentRepository(AppDatabase.getDatabase(application).commentDao())

    fun getComments(postId: Int): Flow<List<Comment>> = repository.getCommentsForPost(postId)

    fun insert(comment: Comment) = viewModelScope.launch {
        repository.insert(comment)
    }
    fun addComment(postId: Int, username: String, text: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val comment = CommentCreate(
                postId = postId,
                username = username,
                text = text,
                timestamp = System.currentTimeMillis()
            )

            RetrofitInstance.api.createComment(comment)
        }
    }


}
