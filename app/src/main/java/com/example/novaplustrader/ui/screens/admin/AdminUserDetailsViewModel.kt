package com.example.novaplustrader.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.novaplustrader.data.repository.AdminRepository
import com.example.novaplustrader.data.repository.AdminRepositoryImpl
import com.example.novaplustrader.ui.screens.admin.models.AdminUserModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class UserActionType {
    NONE,
    SUSPEND,
    RESTORE,
    LOCK,
    REQUIRE_REAUTH
}

data class AdminUserDetailsUiState(
    val user: AdminUserModel? = null,
    val isLoading: Boolean = false,
    val activeConfirmation: UserActionType = UserActionType.NONE,
    val actionReason: String = "",
    val isActionInProgress: Boolean = false,
    val actionSuccessMessage: String? = null,
    val errorMessage: String? = null
)

class AdminUserDetailsViewModel(
    private val repository: AdminRepository = AdminRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUserDetailsUiState())
    val uiState: StateFlow<AdminUserDetailsUiState> = _uiState.asStateFlow()

    fun loadUserDetails(userId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            repository.getUserDetails(userId).collect { userModel ->
                _uiState.update {
                    it.copy(
                        user = userModel,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun initiateAction(actionType: UserActionType) {
        _uiState.update {
            it.copy(
                activeConfirmation = actionType,
                actionReason = "",
                errorMessage = null
            )
        }
    }

    fun updateActionReason(reason: String) {
        _uiState.update { it.copy(actionReason = reason) }
    }

    fun dismissConfirmation() {
        _uiState.update {
            it.copy(
                activeConfirmation = UserActionType.NONE,
                actionReason = "",
                errorMessage = null
            )
        }
    }

    fun confirmAction() {
        val currentUser = _uiState.value.user ?: return
        val action = _uiState.value.activeConfirmation
        val reason = _uiState.value.actionReason.ifBlank { "Administrative action by Operator" }

        viewModelScope.launch {
            _uiState.update { it.copy(isActionInProgress = true) }
            val result = when (action) {
                UserActionType.SUSPEND -> repository.suspendUser(currentUser.uid, reason)
                UserActionType.RESTORE -> repository.restoreUser(currentUser.uid)
                UserActionType.LOCK -> repository.lockUser(currentUser.uid, reason)
                UserActionType.REQUIRE_REAUTH -> repository.requireReauthentication(currentUser.uid)
                UserActionType.NONE -> Result.success(true)
            }

            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        isActionInProgress = false,
                        activeConfirmation = UserActionType.NONE,
                        actionSuccessMessage = "Action ${action.name} executed successfully and logged.",
                        actionReason = ""
                    )
                }
                loadUserDetails(currentUser.uid)
            } else {
                _uiState.update {
                    it.copy(
                        isActionInProgress = false,
                        errorMessage = result.exceptionOrNull()?.message ?: "Operation failed authorization check."
                    )
                }
            }
        }
    }

    fun clearSuccessMessage() {
        _uiState.update { it.copy(actionSuccessMessage = null) }
    }
}
