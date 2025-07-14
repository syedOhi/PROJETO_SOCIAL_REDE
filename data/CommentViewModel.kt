package com.example.social_rede_mobile.data
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.social_rede_mobile.data.AppDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import com.example.social_rede_mobile.data.Comment
import com.example.social_rede_mobile.data.CommentRepository



class CommentViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = CommentRepository(AppDatabase.getDatabase(application).commentDao())

    fun getComments(postId: Int): Flow<List<Comment>> = repository.getCommentsForPost(postId)

    fun insert(comment: Comment) = viewModelScope.launch {
        repository.insert(comment)
    }

}
