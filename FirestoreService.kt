package com.example.social_rede_mobile.firebase

import com.example.social_rede_mobile.firestore.FirestoreMessage
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import android.util.Log

object FirestoreService {
    private val db = Firebase.firestore
    private val messages = db.collection("messages")
    private val typingCollection = db.collection("typing")

    suspend fun sendMessage(message: FirestoreMessage) {
        try {
            messages.document(message.id).set(message).await()
            Log.d("FirestoreService", "Message sent: ${message.message}")
        } catch (e: Exception) {
            Log.e("FirestoreService", "Error sending message", e)
        }
    }

    fun listenForMessages(
        currentUser: String,
        otherUser: String,
        onMessage: (FirestoreMessage) -> Unit
    ) {
        messages
            .whereIn("sender", listOf(currentUser, otherUser))
            .whereIn("receiver", listOf(currentUser, otherUser))
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FirestoreService", "Error listening for messages", error)
                    return@addSnapshotListener
                }
                val list = snapshot?.toObjects(FirestoreMessage::class.java)
                list?.forEach(onMessage)
            }
    }

    fun markMessagesAsRead(sender: String, receiver: String) {
        messages
            .whereEqualTo("sender", sender)
            .whereEqualTo("receiver", receiver)
            .whereEqualTo("isRead", false)
            .get()
            .addOnSuccessListener { result ->
                for (doc in result) {
                    messages.document(doc.id).update("isRead", true)
                    Log.d("FirestoreService", "Marked message ${doc.id} as read")
                }
            }
            .addOnFailureListener {
                Log.e("FirestoreService", "Failed to mark messages as read", it)
            }
    }

    fun updateTypingStatus(user: String, isTyping: Boolean) {
        Log.d("TypingStatus", "[$user] typing: $isTyping")
        typingCollection.document(user)
            .set(mapOf("isTyping" to isTyping))
            .addOnSuccessListener {
                Log.d("TypingStatus", "Updated typing to $isTyping for $user")
            }
            .addOnFailureListener {
                Log.e("TypingStatus", "Failed to update typing for $user", it)
            }
    }

    fun listenTypingStatus(user: String, onTyping: (Boolean) -> Unit) {
        typingCollection.document(user)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("TypingStatus", "Error listening to typing status", error)
                    return@addSnapshotListener
                }
                val typing = snapshot?.getBoolean("isTyping") ?: false
                Log.d("TypingStatus", "[$user] isTyping: $typing")
                onTyping(typing)
            }
    }
}
