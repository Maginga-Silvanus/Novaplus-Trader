package com.example.novaplustrader.data.repository

import com.example.novaplustrader.ui.screens.notifications.models.NotificationModel
import com.example.novaplustrader.ui.screens.notifications.models.NotificationPreferences
import kotlinx.coroutines.flow.StateFlow

interface NotificationRepository {
    fun observeNotifications(): StateFlow<List<NotificationModel>>
    fun observeUnreadCount(): StateFlow<Int>
    fun observePreferences(): StateFlow<NotificationPreferences>

    suspend fun markAsRead(id: String)
    suspend fun markAllAsRead()
    suspend fun deleteNotification(id: String)
    suspend fun updatePreferences(preferences: NotificationPreferences)
    suspend fun saveNotification(notification: NotificationModel)
}
