package com.example.novaplustrader.ui.screens.security

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.novaplustrader.data.repository.SecurityRepository
import com.example.novaplustrader.data.repository.SecurityRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SecurityViewModel(
    private val repository: SecurityRepository = SecurityRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(SecurityUiState())
    val uiState: StateFlow<SecurityUiState> = _uiState.asStateFlow()

    init {
        observeSecurityData()
    }

    private fun observeSecurityData() {
        viewModelScope.launch {
            launch {
                repository.observeBiometricEnabled().collect { enabled ->
                    _uiState.update { it.copy(isBiometricEnabled = enabled) }
                }
            }
            launch {
                repository.observeActiveSessions().collect { sessions ->
                    _uiState.update { it.copy(activeSessions = sessions) }
                }
            }
            launch {
                repository.observeSecurityActivity().collect { activity ->
                    _uiState.update { it.copy(securityActivity = activity) }
                }
            }
        }
    }

    fun toggleBiometric(enabled: Boolean) {
        viewModelScope.launch {
            repository.setBiometricEnabled(enabled)
        }
    }

    fun openSignOutOthersConfirmSheet() {
        _uiState.update { it.copy(isSignOutOthersConfirmOpen = true) }
    }

    fun closeSignOutOthersConfirmSheet() {
        _uiState.update { it.copy(isSignOutOthersConfirmOpen = false) }
    }

    fun confirmSignOutOthers() {
        _uiState.update { it.copy(isSignOutOthersConfirmOpen = false, isLoading = true) }
        viewModelScope.launch {
            val result = repository.signOutOtherSessions()
            _uiState.update {
                it.copy(
                    isLoading = false,
                    successMessage = if (result.isSuccess) "Signed out from all other active devices" else null
                )
            }
        }
    }

    fun openDeleteAccountConfirmSheet() {
        _uiState.update { it.copy(isDeleteAccountConfirmOpen = true) }
    }

    fun closeDeleteAccountConfirmSheet() {
        _uiState.update { it.copy(isDeleteAccountConfirmOpen = false) }
    }

    fun confirmDeleteAccountRequest() {
        _uiState.update { it.copy(isDeleteAccountConfirmOpen = false, isLoading = true) }
        viewModelScope.launch {
            val result = repository.requestAccountDeletion()
            _uiState.update {
                it.copy(
                    isLoading = false,
                    successMessage = if (result.isSuccess) "Account deletion request submitted for backend compliance processing" else null
                )
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
