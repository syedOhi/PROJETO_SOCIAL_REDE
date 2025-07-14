package com.example.social_rede_mobile.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity)

    @Query("""
        SELECT * FROM messages 
        WHERE (sender = :currentUser AND receiver = :otherUser) 
           OR (sender = :otherUser AND receiver = :currentUser)
        ORDER BY timestamp ASC
    """)
    fun getConversation(currentUser: String, otherUser: String): Flow<List<ChatMessageEntity>>

    // ✅ New: Get all distinct usernames you’ve chatted with
    @Query("""
        SELECT DISTINCT 
            CASE 
                WHEN sender = :currentUser THEN receiver 
                ELSE sender 
            END AS otherUser 
        FROM messages 
        WHERE sender = :currentUser OR receiver = :currentUser
    """)
    fun getChatUsers(currentUser: String): Flow<List<String>>
    @Query("""
    SELECT COUNT(*) FROM messages
    WHERE receiver = :currentUser AND sender = :otherUser AND isRead = 0
""")
    fun getUnreadCount(currentUser: String, otherUser: String): Flow<Int>
    @Query("""
    UPDATE messages 
    SET isRead = 1 
    WHERE sender = :sender AND receiver = :receiver AND isRead = 0
""")
    suspend fun markMessagesAsRead(sender: String, receiver: String)
    @Query("UPDATE messages SET emoji = :emoji WHERE id = :messageId")
    suspend fun updateMessageEmoji(messageId: Int, emoji: String)

}
