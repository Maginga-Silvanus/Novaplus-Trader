package com.example.novaplustrader.data.repository

import com.example.novaplustrader.ui.screens.notifications.models.NotificationModel
import com.example.novaplustrader.ui.screens.notifications.models.NotificationPreferences
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    fun observeNotifications(): Flow<List<NotificationModel>>
    fun observeUnreadCount(): Flow<Int>
    fun observePreferences(): Flow<NotificationPreferences>

    suspend fun markAsRead(id: String)
    suspend fun markAllAsRead()
    suspend fun deleteNotification(id: String)
    suspend fun updatePreferences(preferences: NotificationPreferences)
    suspend fun saveNotification(notification: NotificationModel)
}
