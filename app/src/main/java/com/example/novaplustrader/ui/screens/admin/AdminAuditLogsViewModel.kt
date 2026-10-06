package com.example.novaplustrader.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.novaplustrader.data.repository.AdminRepository
import com.example.novaplustrader.data.repository.AdminRepositoryImpl
import com.example.novaplustrader.ui.screens.admin.models.AdminAuditLogModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminAuditLogsUiState(
    val auditLogs: List<AdminAuditLogModel> = emptyList(),
    val searchQuery: String = "",
    val actionFilter: String = "ALL",
    val actorFilter: String = "ALL",
    val selectedLogDetails: AdminAuditLogModel? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class AdminAuditLogsViewModel(
    private val repository: AdminRepository = AdminRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminAuditLogsUiState())
    val uiState: StateFlow<AdminAuditLogsUiState> = _uiState.asStateFlow()

    init {
        loadAuditLogs()
    }

    fun loadAuditLogs() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val actFilter = if (_uiState.value.actionFilter == "ALL") null else _uiState.value.actionFilter
            val actrFilter = if (_uiState.value.actorFilter == "ALL") null else _uiState.value.actorFilter

            repository.getAuditLogs(
                searchQuery = _uiState.value.searchQuery,
                actionFilter = actFilter,
                actorFilter = actrFilter
            ).collect { list ->
                _uiState.update {
                    it.copy(
                        auditLogs = list,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        loadAuditLogs()
    }

    fun onActionFilterSelected(action: String) {
        _uiState.update { it.copy(actionFilter = action) }
        loadAuditLogs()
    }

    fun onActorFilterSelected(actor: String) {
        _uiState.update { it.copy(actorFilter = actor) }
        loadAuditLogs()
    }

    fun selectLogDetails(log: AdminAuditLogModel?) {
        _uiState.update { it.copy(selectedLogDetails = log) }
    }
}
