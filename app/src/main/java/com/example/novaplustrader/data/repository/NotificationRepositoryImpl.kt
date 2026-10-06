package com.example.novaplustrader.data.repository

import com.example.novaplustrader.ui.screens.notifications.models.NotificationCategory
import com.example.novaplustrader.ui.screens.notifications.models.NotificationModel
import com.example.novaplustrader.ui.screens.notifications.models.NotificationPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class NotificationRepositoryImpl : NotificationRepository {

    private val _notifications = MutableStateFlow<List<NotificationModel>>(
        listOf(
            NotificationModel(
                id = "notif_1",
                title = "Deposit Confirmed",
                body = "Your deposit of $2,400.00 USD has been credited to your primary wallet.",
                category = NotificationCategory.WALLET,
                timestamp = "10 min ago",
                isRead = false,
                deepLink = "transaction_history"
            ),
            NotificationModel(
                id = "notif_2",
                title = "Nova Bot Strategy Triggered",
                body = "Nova Bot opened a BUY position on BTC/USDT at $88,450.00.",
                category = NotificationCategory.BOT,
                timestamp = "1 hour ago",
                isRead = false,
                deepLink = "trading_bot"
            ),
            NotificationModel(
                id = "notif_3",
                title = "New Login Detected",
                body = "Authorized sign-in from Pixel 9 Pro in Nairobi, Kenya.",
                category = NotificationCategory.SECURITY,
                timestamp = "Yesterday",
                isRead = true,
                deepLink = "profile"
            ),
            NotificationModel(
                id = "notif_4",
                title = "Binary Option Trade Settlement",
                body = "Your 1m UP position on ETH/USDT was settled successfully.",
                category = NotificationCategory.TRADING,
                timestamp = "2 days ago",
                isRead = true,
                deepLink = "trade"
            )
        )
    )

    private val _unreadCount = MutableStateFlow(2)
    private val _preferences = MutableStateFlow(NotificationPreferences())

    override fun observeNotifications(): StateFlow<List<NotificationModel>> = _notifications.asStateFlow()
    override fun observeUnreadCount(): StateFlow<Int> = _unreadCount.asStateFlow()
    override fun observePreferences(): StateFlow<NotificationPreferences> = _preferences.asStateFlow()

    override suspend fun markAsRead(id: String) {
        _notifications.update { list ->
            list.map { if (it.id == id) it.copy(isRead = true) else it }
        }
        recalculateUnread()
    }

    override suspend fun markAllAsRead() {
        _notifications.update { list ->
            list.map { it.copy(isRead = true) }
        }
        recalculateUnread()
    }

    override suspend fun deleteNotification(id: String) {
        _notifications.update { list ->
            list.filterNot { it.id == id }
        }
        recalculateUnread()
    }

    override suspend fun updatePreferences(preferences: NotificationPreferences) {
        _preferences.value = preferences
    }

    override suspend fun saveNotification(notification: NotificationModel) {
        _notifications.update { listOf(notification) + it }
        recalculateUnread()
    }

    private fun recalculateUnread() {
        _unreadCount.value = _notifications.value.count { !it.isRead }
    }
}
