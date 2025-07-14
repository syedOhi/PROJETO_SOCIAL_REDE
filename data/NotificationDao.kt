package com.example.social_rede_mobile.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: Notification)

    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<Notification>>

    @Query("UPDATE notifications SET seen = 1 WHERE id = :notificationId")
    suspend fun markAsSeen(notificationId: String)

    @Delete
    suspend fun deleteNotification(notification: Notification)
}