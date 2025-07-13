package com.example.social_rede_mobile.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.social_rede_mobile.firebase.FirestoreService
import com.example.social_rede_mobile.firestore.FirestoreMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.UUID

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).chatDao()
    private val repository = ChatRepository(dao)
    private val db = AppDatabase.getDatabase(application)

    fun getMessages(currentUser: String, otherUser: String): Flow<List<ChatMessageEntity>> {
        return repository.getConversation(currentUser, otherUser)
    }

    fun getUnreadCount(currentUser: String, otherUser: String): Flow<Int> {
        return repository.getUnreadCount(currentUser, otherUser)
    }

    fun sendMessage(sender: String, receiver: String, text: String, emoji: String? = null, isVoice: Boolean = false) {
        val message = ChatMessageEntity(
            sender = sender,
            receiver = receiver,
            message = text,
            timestamp = System.currentTimeMillis(),
            emoji = emoji,
            isVoice = isVoice
        )
        viewModelScope.launch {
            repository.sendMessage(message)
        }
    }

    fun getChatUsers(currentUser: String): Flow<List<String>> {
        return repository.getChatUsers(currentUser)
    }

    fun markMessagesAsRead(sender: String, receiver: String) {
        viewModelScope.launch {
            repository.markMessagesAsRead(sender, receiver)
            FirestoreService.markMessagesAsRead(sender, receiver)
        }
    }

    fun reactToMessage(messageId: Int, emoji: String) {
        viewModelScope.launch {
            repository.updateMessageEmoji(messageId, emoji)
            // Optional: sync to Firestore if needed
        }
    }

    // ✅ SEND TO FIRESTORE
    fun sendFirestoreMessage(
        sender: String,
        receiver: String,
        text: String,
        emoji: String? = null,
        isVoice: Boolean = false
    ) {
        viewModelScope.launch {
            val message = FirestoreMessage(
                id = UUID.randomUUID().toString(),
                sender = sender,
                receiver = receiver,
                message = text,
                timestamp = System.currentTimeMillis(),
                isVoice = isVoice,
                emoji = emoji,
                isRead = false
            )
            FirestoreService.sendMessage(message)
        }
    }

    // ✅ LISTEN FOR REALTIME MESSAGES FROM FIRESTORE
    fun syncMessagesWithFirestore(currentUser: String, otherUser: String) {
        FirestoreService.listenForMessages(currentUser, otherUser) { msg ->
            viewModelScope.launch {
                dao.insertMessage(
                    ChatMessageEntity(
                        id = msg.id.hashCode(),
                        sender = msg.sender,
                        receiver = msg.receiver,
                        message = msg.message,
                        timestamp = msg.timestamp,
                        emoji = msg.emoji,
                        isVoice = msg.isVoice,
                        isRead = msg.isRead
                    )
                )
            }
        }
    }

    // ✅ TYPING STATUS SUPPORT
    fun updateTyping(user: String, isTyping: Boolean) {
        FirestoreService.updateTypingStatus(user, isTyping)
    }

    fun observeTyping(otherUser: String, onTyping: (Boolean) -> Unit) {
        FirestoreService.listenTypingStatus(otherUser, onTyping)
    }
}
