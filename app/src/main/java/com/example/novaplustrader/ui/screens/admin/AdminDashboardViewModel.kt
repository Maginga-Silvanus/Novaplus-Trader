package com.example.novaplustrader.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.novaplustrader.data.repository.AdminRepository
import com.example.novaplustrader.data.repository.AdminRepositoryImpl
import com.example.novaplustrader.ui.screens.admin.models.AdminMetricsModel
import com.example.novaplustrader.ui.screens.admin.models.AdminRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminDashboardUiState(
    val metrics: AdminMetricsModel? = null,
    val isLoading: Boolean = false,
    val isUnavailable: Boolean = false,
    val currentRole: AdminRole = AdminRole.READ_ONLY,
    val errorMessage: String? = null
)

class AdminDashboardViewModel(
    private val repository: AdminRepository = AdminRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminDashboardUiState())
    val uiState: StateFlow<AdminDashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardMetrics()
        observeRole()
    }

    fun loadDashboardMetrics() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            repository.getDashboardMetrics().collect { m ->
                if (m == null) {
                    _uiState.update {
                        it.copy(
                            metrics = null,
                            isLoading = false,
                            isUnavailable = true
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            metrics = m,
                            isLoading = false,
                            isUnavailable = false
                        )
                    }
                }
            }
        }
    }

    private fun observeRole() {
        viewModelScope.launch {
            repository.getAdminRole().collect { role ->
                _uiState.update { it.copy(currentRole = role) }
            }
        }
    }
}
