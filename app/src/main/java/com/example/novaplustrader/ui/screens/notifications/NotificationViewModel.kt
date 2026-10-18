package com.example.novaplustrader.ui.screens.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.novaplustrader.data.repository.NotificationRepository
import com.example.novaplustrader.data.repository.NotificationRepositoryImpl
import com.example.novaplustrader.ui.screens.notifications.models.NotificationCategory
import com.example.novaplustrader.ui.screens.notifications.models.NotificationPreferences
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NotificationViewModel(
    private val repository: NotificationRepository = NotificationRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationUiState())
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    init {
        observeNotifications()
    }

    private fun observeNotifications() {
        viewModelScope.launch {
            launch {
                repository.observeNotifications()
                    .catch { error -> reportError(error, "Unable to load notifications.") }
                    .collect { list -> _uiState.update { it.copy(notifications = list) } }
            }
            launch {
                repository.observeUnreadCount()
                    .catch { error -> reportError(error, "Unable to load unread notification count.") }
                    .collect { count -> _uiState.update { it.copy(unreadCount = count) } }
            }
            launch {
                repository.observePreferences()
                    .catch { error -> reportError(error, "Unable to load notification preferences.") }
                    .collect { prefs -> _uiState.update { it.copy(preferences = prefs) } }
            }
        }
    }

    fun selectCategory(category: NotificationCategory) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun markAsRead(id: String) {
        performAction("Unable to mark notification as read.") { repository.markAsRead(id) }
    }

    fun markAllAsRead() {
        performAction("Unable to mark notifications as read.") { repository.markAllAsRead() }
    }

    fun deleteNotification(id: String) {
        performAction("Unable to delete notification.") { repository.deleteNotification(id) }
    }

    fun updatePreferences(newPrefs: NotificationPreferences) {
        performAction("Unable to save notification preferences.") {
            repository.updatePreferences(newPrefs)
        }
    }

    private fun performAction(fallbackMessage: String, action: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                action()
                _uiState.update { it.copy(errorMessage = null) }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                reportError(error, fallbackMessage)
            }
        }
    }

    private fun reportError(error: Throwable, fallbackMessage: String) {
        _uiState.update {
            it.copy(errorMessage = error.localizedMessage ?: fallbackMessage)
        }
    }
}
