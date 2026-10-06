package com.example.novaplustrader.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.novaplustrader.data.repository.AdminRepository
import com.example.novaplustrader.data.repository.AdminRepositoryImpl
import com.example.novaplustrader.ui.screens.admin.models.AdminAccessState
import com.example.novaplustrader.ui.screens.admin.models.AdminRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminAuthUiState(
    val accessState: AdminAccessState = AdminAccessState.LOADING,
    val role: AdminRole = AdminRole.READ_ONLY,
    val adminUid: String = "",
    val errorMessage: String? = null
)

class AdminAuthViewModel(
    private val repository: AdminRepository = AdminRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminAuthUiState())
    val uiState: StateFlow<AdminAuthUiState> = _uiState.asStateFlow()

    init {
        observeAuthState()
    }

    private fun observeAuthState() {
        viewModelScope.launch {
            repository.getAdminAccessState().collect { state ->
                _uiState.update { it.copy(accessState = state) }
            }
        }
        viewModelScope.launch {
            repository.getAdminRole().collect { role ->
                _uiState.update { it.copy(role = role) }
            }
        }
        _uiState.update { it.copy(adminUid = repository.getCurrentAdminUid()) }
    }

    fun verifySession() {
        viewModelScope.launch {
            _uiState.update { it.copy(accessState = AdminAccessState.LOADING) }
            val success = repository.verifyAdminSession()
            if (!success) {
                _uiState.update {
                    it.copy(
                        accessState = AdminAccessState.ACCESS_DENIED,
                        errorMessage = "Current account lacks administrative permissions."
                    )
                }
            }
        }
    }
}
