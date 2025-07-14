// --- 3. Notification Repository ---
package com.example.social_rede_mobile.data

class NotificationRepository(private val dao: NotificationDao) {
    fun getAllNotifications() = dao.getAllNotifications()
    suspend fun insert(notification: Notification) = dao.insertNotification(notification)
    suspend fun markSeen(id: String) = dao.markAsSeen(id)
    suspend fun delete(notification: Notification) = dao.deleteNotification(notification)
}