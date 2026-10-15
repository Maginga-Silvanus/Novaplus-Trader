package com.example.novaplustrader.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.novaplustrader.data.repository.MonitoringRepository
import com.example.novaplustrader.data.repository.MonitoringRepositoryImpl
import com.example.novaplustrader.domain.model.monitoring.IncidentModel
import com.example.novaplustrader.domain.model.monitoring.IncidentSeverity
import com.example.novaplustrader.domain.model.monitoring.IncidentStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class IncidentUiState(
    val incidents: List<IncidentModel> = emptyList(),
    val selectedIncident: IncidentModel? = null,
    val searchQuery: String = "",
    val serviceFilter: String? = null,
    val severityFilter: IncidentSeverity? = null,
    val statusFilter: IncidentStatus? = null,
    val currentPage: Int = 1,
    val pageSize: Int = 20,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val actionSuccessMessage: String? = null
)

class IncidentViewModel(
    private val repository: MonitoringRepository = MonitoringRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(IncidentUiState())
    val uiState: StateFlow<IncidentUiState> = _uiState.asStateFlow()

    init {
        loadIncidents()
    }

    fun loadIncidents() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val state = _uiState.value
            repository.getIncidents(
                searchQuery = state.searchQuery,
                serviceFilter = state.serviceFilter,
                severityFilter = state.severityFilter,
                statusFilter = state.statusFilter,
                page = state.currentPage,
                pageSize = state.pageSize
            ).collect { list ->
                _uiState.update {
                    it.copy(
                        incidents = list,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query, currentPage = 1) }
        loadIncidents()
    }

    fun setServiceFilter(service: String?) {
        _uiState.update { it.copy(serviceFilter = service, currentPage = 1) }
        loadIncidents()
    }

    fun setSeverityFilter(severity: IncidentSeverity?) {
        _uiState.update { it.copy(severityFilter = severity, currentPage = 1) }
        loadIncidents()
    }

    fun setStatusFilter(status: IncidentStatus?) {
        _uiState.update { it.copy(statusFilter = status, currentPage = 1) }
        loadIncidents()
    }

    fun setPage(page: Int) {
        if (page > 0) {
            _uiState.update { it.copy(currentPage = page) }
            loadIncidents()
        }
    }

    fun loadIncidentDetails(incidentId: String) {
        viewModelScope.launch {
            repository.getIncidentDetails(incidentId).collect { incident ->
                _uiState.update { it.copy(selectedIncident = incident) }
            }
        }
    }

    fun updateStatus(incidentId: String, newStatus: String, resolutionSummary: String?) {
        viewModelScope.launch {
            val result = repository.updateIncidentStatus(incidentId, newStatus, resolutionSummary)
            if (result.isSuccess) {
                _uiState.update { it.copy(actionSuccessMessage = "Incident status updated successfully.") }
                loadIncidents()
                loadIncidentDetails(incidentId)
            } else {
                _uiState.update { it.copy(errorMessage = result.exceptionOrNull()?.localizedMessage ?: "Failed to update incident.") }
            }
        }
    }

    fun createNewIncident(incident: IncidentModel) {
        viewModelScope.launch {
            val result = repository.createIncident(incident)
            if (result.isSuccess) {
                _uiState.update { it.copy(actionSuccessMessage = "New incident registered.") }
                loadIncidents()
            } else {
                _uiState.update { it.copy(errorMessage = result.exceptionOrNull()?.localizedMessage ?: "Failed to create incident.") }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, actionSuccessMessage = null) }
    }
}
