package com.example.novaplustrader.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.novaplustrader.data.repository.FeatureAccessRepository
import com.example.novaplustrader.data.repository.FeatureAccessRepositoryImpl
import com.example.novaplustrader.domain.model.access.FeatureConfiguration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminFeatureAccessUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val configurations: List<FeatureConfiguration> = emptyList()
)

class AdminFeatureAccessViewModel(
    private val repository: FeatureAccessRepository = FeatureAccessRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminFeatureAccessUiState())
    val uiState: StateFlow<AdminFeatureAccessUiState> = _uiState.asStateFlow()

    init {
        observeConfigurations()
    }

    private fun observeConfigurations() {
        viewModelScope.launch {
            repository.observeFeatureConfigurations().collect { configs ->
                _uiState.update { it.copy(configurations = configs) }
            }
        }
    }

    fun toggleFeatureEnablement(config: FeatureConfiguration) {
        val updated = config.copy(enabled = !config.enabled, updatedAt = "Just now", updatedBy = "Admin_Operator")
        updateConfig(updated)
    }

    fun toggleMaintenanceMode(config: FeatureConfiguration) {
        val updated = config.copy(maintenanceMode = !config.maintenanceMode, updatedAt = "Just now", updatedBy = "Admin_Operator")
        updateConfig(updated)
    }

    private fun updateConfig(config: FeatureConfiguration) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = repository.updateFeatureConfiguration(config, "admin_uid_901")
            _uiState.update {
                it.copy(
                    isLoading = false,
                    successMessage = if (result.isSuccess) "Configuration updated for ${config.feature.displayName}" else null,
                    errorMessage = result.exceptionOrNull()?.message
                )
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
