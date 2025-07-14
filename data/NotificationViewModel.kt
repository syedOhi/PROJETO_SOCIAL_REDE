package com.example.social_rede_mobile.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


class NotificationViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).NotificationDao()
    private val repository = NotificationRepository(dao)

    val notifications = repository.getAllNotifications()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun insertNotification(notification: Notification) = viewModelScope.launch {
        repository.insert(notification)
    }

    fun markAsSeen(id: String) = viewModelScope.launch {
        repository.markSeen(id)
    }

    fun deleteNotification(notification: Notification) = viewModelScope.launch {
        repository.delete(notification)
    }
}
