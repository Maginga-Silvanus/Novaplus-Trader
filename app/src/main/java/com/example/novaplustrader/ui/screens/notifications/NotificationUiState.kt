package com.example.novaplustrader.ui.screens.notifications

import com.example.novaplustrader.ui.screens.notifications.models.NotificationCategory
import com.example.novaplustrader.ui.screens.notifications.models.NotificationModel
import com.example.novaplustrader.ui.screens.notifications.models.NotificationPreferences

data class NotificationUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val notifications: List<NotificationModel> = emptyList(),
    val unreadCount: Int = 0,
    val selectedCategory: NotificationCategory = NotificationCategory.ALL,
    val preferences: NotificationPreferences = NotificationPreferences()
)
