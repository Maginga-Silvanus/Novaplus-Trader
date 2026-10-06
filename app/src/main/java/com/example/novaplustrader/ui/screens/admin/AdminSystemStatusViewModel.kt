package com.example.novaplustrader.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.novaplustrader.data.repository.AdminRepository
import com.example.novaplustrader.data.repository.AdminRepositoryImpl
import com.example.novaplustrader.ui.screens.admin.models.AdminSystemHealthModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminSystemStatusUiState(
    val services: List<AdminSystemHealthModel> = emptyList(),
    val isRefreshing: Boolean = false,
    val lastRefreshedTime: String = "Just now",
    val errorMessage: String? = null
)

class AdminSystemStatusViewModel(
    private val repository: AdminRepository = AdminRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminSystemStatusUiState())
    val uiState: StateFlow<AdminSystemStatusUiState> = _uiState.asStateFlow()

    init {
        loadSystemHealth()
    }

    fun loadSystemHealth() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            repository.getSystemHealth().collect { healthList ->
                _uiState.update {
                    it.copy(
                        services = healthList,
                        isRefreshing = false,
                        lastRefreshedTime = "Just now"
                    )
                }
            }
        }
    }
}
