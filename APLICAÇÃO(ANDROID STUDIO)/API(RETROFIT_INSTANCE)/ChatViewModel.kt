package com.example.social_rede_mobile.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.social_rede_mobile.firebase.FirestoreService
import com.example.social_rede_mobile.firestore.FirestoreMessage
import com.example.social_rede_mobile.network.RetrofitInstance
import com.example.social_rede_mobile.network.models.ChatMessageOut
import com.example.social_rede_mobile.network.models.UserOut
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import com.example.social_rede_mobile.network.models.ChatMessageCreate
import com.example.social_rede_mobile.network.models.ChatRequestOut
class ChatViewModel(application: Application) : AndroidViewModel(application) {

    /* -----------------------------
       CONVERSATION USERS (API)
    ------------------------------ */

    private val _conversationUsers = MutableStateFlow<List<UserOut>>(emptyList())
    val conversationUsers: StateFlow<List<UserOut>> = _conversationUsers
    private val _chatRequests = MutableStateFlow<List<ChatRequestOut>>(emptyList())
    val chatRequests: StateFlow<List<ChatRequestOut>> = _chatRequests

    fun loadConversationUsers(currentUsername: String) {
        viewModelScope.launch {
            try {
                // 1️⃣ users you chatted with
                val chatUsers =
                    RetrofitInstance.api.getConversations(currentUsername)

                // 2️⃣ users you follow
                val followingUsers =
                    RetrofitInstance.api.getFollowing(currentUsername)

                // 3️⃣ merge + remove duplicates + remove self
                val usernames = (chatUsers + followingUsers)
                    .distinct()
                    .filter { it != currentUsername }

                // 4️⃣ fetch full user profiles
                val users = usernames.mapNotNull { username ->
                    try {
                        RetrofitInstance.api.getUser(username)
                    } catch (e: Exception) {
                        null
                    }
                }

                _conversationUsers.value = users
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /* -----------------------------
       CHAT HISTORY (API)
    ------------------------------ */

    private val _messages = MutableStateFlow<List<ChatMessageOut>>(emptyList())
    val messages: StateFlow<List<ChatMessageOut>> = _messages

    fun loadConversation(currentUser: String, otherUser: String) {
        viewModelScope.launch {
            try {
                _messages.value =
                    RetrofitInstance.api.getConversation(currentUser, otherUser)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun sendMessage(
        sender: String,
        receiver: String,
        text: String,
        emoji: String? = null,
        isVoice: Boolean = false
    ) {
        viewModelScope.launch {
            try {
                RetrofitInstance.api.sendMessage(
                    ChatMessageCreate(
                        sender = sender,
                        receiver = receiver,
                        message = text,
                        timestamp = System.currentTimeMillis(),
                        isVoice = isVoice,
                        emoji = emoji
                    )
                )

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /* -----------------------------
       FIREBASE REALTIME (OPTIONAL)
    ------------------------------ */

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

    fun listenForRealtimeMessages(
        currentUser: String,
        otherUser: String,
        onMessage: (FirestoreMessage) -> Unit
    ) {
        FirestoreService.listenForMessages(currentUser, otherUser, onMessage)
    }

    /* -----------------------------
       READ + TYPING
    ------------------------------ */

    fun markMessagesAsRead(sender: String, receiver: String) {
        viewModelScope.launch {
            try {
                RetrofitInstance.api.markRead(sender, receiver)
                FirestoreService.markMessagesAsRead(sender, receiver)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateTyping(user: String, isTyping: Boolean) {
        FirestoreService.updateTypingStatus(user, isTyping)
    }

    fun observeTyping(otherUser: String, onTyping: (Boolean) -> Unit) {
        FirestoreService.listenTypingStatus(otherUser, onTyping)
    }

    // -----------------------------
// CHAT REQUESTS
// -----------------------------


    fun loadChatRequests(currentUsername: String) {
        viewModelScope.launch {
            try {
                _chatRequests.value =
                    RetrofitInstance.api.getChatRequests(currentUsername)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun acceptChatRequest(sender: String, receiver: String) {
        viewModelScope.launch {
            try {
                RetrofitInstance.api.acceptChatRequest(sender, receiver)

                // refresh both lists
                loadChatRequests(receiver)
                loadConversationUsers(receiver)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun ignoreChatRequest(sender: String, receiver: String) {
        viewModelScope.launch {
            try {
                RetrofitInstance.api.deleteChatRequest(sender, receiver)
                loadChatRequests(receiver)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

}
