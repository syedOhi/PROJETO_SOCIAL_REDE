package com.example.social_rede_mobile.data

class ChatRepository(private val dao: ChatDao) {
    suspend fun sendMessage(message: ChatMessageEntity) = dao.insertMessage(message)
    fun getConversation(currentUser: String, otherUser: String) =
        dao.getConversation(currentUser, otherUser)

    fun getChatUsers(currentUser: String) = dao.getChatUsers(currentUser)
    fun getUnreadCount(currentUser: String, otherUser: String) =
        dao.getUnreadCount(currentUser, otherUser)

    suspend fun markMessagesAsRead(sender: String, receiver: String) =
        dao.markMessagesAsRead(sender, receiver)


    suspend fun updateMessageEmoji(messageId: Int, emoji: String) =
        dao.updateMessageEmoji(messageId, emoji)

}

