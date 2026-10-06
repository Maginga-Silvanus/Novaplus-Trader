package com.example.novaplustrader.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.novaplustrader.data.repository.AdminRepository
import com.example.novaplustrader.data.repository.AdminRepositoryImpl
import com.example.novaplustrader.ui.screens.admin.models.AdminNotificationAudience
import com.example.novaplustrader.ui.screens.admin.models.AdminNotificationCategory
import com.example.novaplustrader.ui.screens.admin.models.AdminNotificationPriority
import com.example.novaplustrader.ui.screens.admin.models.AdminNotificationRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminNotificationsUiState(
    val title: String = "",
    val message: String = "",
    val category: AdminNotificationCategory = AdminNotificationCategory.SYSTEM,
    val audience: AdminNotificationAudience = AdminNotificationAudience.ALL_USERS,
    val targetUserId: String = "",
    val scheduleTime: String = "IMMEDIATE",
    val priority: AdminNotificationPriority = AdminNotificationPriority.MEDIUM,
    val deepLinkRoute: String = "",
    val isConfirmationShowing: Boolean = false,
    val isSending: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
)

class AdminNotificationsViewModel(
    private val repository: AdminRepository = AdminRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminNotificationsUiState())
    val uiState: StateFlow<AdminNotificationsUiState> = _uiState.asStateFlow()

    fun updateTitle(title: String) {
        _uiState.update { it.copy(title = title, errorMessage = null) }
    }

    fun updateMessage(message: String) {
        _uiState.update { it.copy(message = message, errorMessage = null) }
    }

    fun updateCategory(category: AdminNotificationCategory) {
        _uiState.update { it.copy(category = category) }
    }

    fun updateAudience(audience: AdminNotificationAudience) {
        _uiState.update { it.copy(audience = audience) }
    }

    fun updateTargetUserId(userId: String) {
        _uiState.update { it.copy(targetUserId = userId) }
    }

    fun updateScheduleTime(schedule: String) {
        _uiState.update { it.copy(scheduleTime = schedule) }
    }

    fun updatePriority(priority: AdminNotificationPriority) {
        _uiState.update { it.copy(priority = priority) }
    }

    fun updateDeepLinkRoute(route: String) {
        _uiState.update { it.copy(deepLinkRoute = route) }
    }

    fun initiateSendConfirmation() {
        if (_uiState.value.title.isBlank() || _uiState.value.message.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Notification Title and Message cannot be empty.") }
            return
        }
        if (_uiState.value.audience == AdminNotificationAudience.SPECIFIC_USER && _uiState.value.targetUserId.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Target User ID is required when sending to a specific user.") }
            return
        }
        _uiState.update { it.copy(isConfirmationShowing = true) }
    }

    fun dismissConfirmation() {
        _uiState.update { it.copy(isConfirmationShowing = false) }
    }

    fun confirmSendNotification() {
        val state = _uiState.value
        val request = AdminNotificationRequest(
            title = state.title,
            message = state.message,
            category = state.category,
            audience = state.audience,
            targetUserId = if (state.audience == AdminNotificationAudience.SPECIFIC_USER) state.targetUserId else null,
            scheduleTime = state.scheduleTime,
            priority = state.priority,
            deepLinkRoute = state.deepLinkRoute
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isSending = true) }
            val result = repository.sendNotificationRequest(request)
            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        isSending = false,
                        isConfirmationShowing = false,
                        title = "",
                        message = "",
                        targetUserId = "",
                        deepLinkRoute = "",
                        successMessage = "Operational notification request dispatched to backend notification service."
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isSending = false,
                        errorMessage = "Failed to dispatch notification request to server."
                    )
                }
            }
        }
    }

    fun clearSuccessMessage() {
        _uiState.update { it.copy(successMessage = null) }
    }
}
