package com.example.novaplustrader.ui.screens.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.novaplustrader.data.repository.NotificationRepository
import com.example.novaplustrader.data.repository.NotificationRepositoryImpl
import com.example.novaplustrader.ui.screens.notifications.models.NotificationCategory
import com.example.novaplustrader.ui.screens.notifications.models.NotificationPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
                repository.observeNotifications().collect { list ->
                    _uiState.update { it.copy(notifications = list) }
                }
            }
            launch {
                repository.observeUnreadCount().collect { count ->
                    _uiState.update { it.copy(unreadCount = count) }
                }
            }
            launch {
                repository.observePreferences().collect { prefs ->
                    _uiState.update { it.copy(preferences = prefs) }
                }
            }
        }
    }

    fun selectCategory(category: NotificationCategory) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun markAsRead(id: String) {
        viewModelScope.launch {
            repository.markAsRead(id)
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            repository.markAllAsRead()
        }
    }

    fun deleteNotification(id: String) {
        viewModelScope.launch {
            repository.deleteNotification(id)
        }
    }

    fun updatePreferences(newPrefs: NotificationPreferences) {
        viewModelScope.launch {
            repository.updatePreferences(newPrefs)
        }
    }
}
