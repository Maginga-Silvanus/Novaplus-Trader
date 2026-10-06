package com.example.novaplustrader.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.novaplustrader.data.repository.AdminRepository
import com.example.novaplustrader.data.repository.AdminRepositoryImpl
import com.example.novaplustrader.ui.screens.admin.models.AdminSupportTicketModel
import com.example.novaplustrader.ui.screens.admin.models.AdminSupportTicketStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminSupportUiState(
    val tickets: List<AdminSupportTicketModel> = emptyList(),
    val searchQuery: String = "",
    val selectedStatusFilter: String = "ALL", // ALL, OPEN, IN_PROGRESS, PENDING_USER, RESOLVED, CLOSED
    val selectedPriorityFilter: String = "ALL", // ALL, CRITICAL, HIGH, MEDIUM, LOW
    val selectedTicketDetails: AdminSupportTicketModel? = null,
    val replyText: String = "",
    val isInternalNote: Boolean = false,
    val isSubmittingReply: Boolean = false,
    val isLoading: Boolean = false,
    val actionSuccessMessage: String? = null,
    val errorMessage: String? = null
)

class AdminSupportViewModel(
    private val repository: AdminRepository = AdminRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminSupportUiState())
    val uiState: StateFlow<AdminSupportUiState> = _uiState.asStateFlow()

    init {
        loadTickets()
    }

    fun loadTickets() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val statusFilter = if (_uiState.value.selectedStatusFilter == "ALL") null else _uiState.value.selectedStatusFilter
            val priorityFilter = if (_uiState.value.selectedPriorityFilter == "ALL") null else _uiState.value.selectedPriorityFilter

            repository.getSupportTickets(
                searchQuery = _uiState.value.searchQuery,
                statusFilter = statusFilter,
                priorityFilter = priorityFilter
            ).collect { list ->
                _uiState.update {
                    it.copy(
                        tickets = list,
                        isLoading = false
                    )
                }
                // Refresh active selected ticket details if open
                val currentTicketId = _uiState.value.selectedTicketDetails?.ticketId
                if (currentTicketId != null) {
                    val updatedTicket = list.find { it.ticketId == currentTicketId }
                    _uiState.update { it.copy(selectedTicketDetails = updatedTicket) }
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        loadTickets()
    }

    fun onStatusFilterSelected(status: String) {
        _uiState.update { it.copy(selectedStatusFilter = status) }
        loadTickets()
    }

    fun onPriorityFilterSelected(priority: String) {
        _uiState.update { it.copy(selectedPriorityFilter = priority) }
        loadTickets()
    }

    fun selectTicket(ticket: AdminSupportTicketModel?) {
        _uiState.update {
            it.copy(
                selectedTicketDetails = ticket,
                replyText = "",
                isInternalNote = false
            )
        }
    }

    fun updateReplyText(text: String) {
        _uiState.update { it.copy(replyText = text) }
    }

    fun toggleInternalNote(isNote: Boolean) {
        _uiState.update { it.copy(isInternalNote = isNote) }
    }

    fun submitReply() {
        val ticket = _uiState.value.selectedTicketDetails ?: return
        val message = _uiState.value.replyText.trim()
        if (message.isBlank()) return

        val isNote = _uiState.value.isInternalNote

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmittingReply = true) }
            val result = repository.addSupportReply(ticket.ticketId, message, isNote)
            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        isSubmittingReply = false,
                        replyText = "",
                        actionSuccessMessage = if (isNote) "Internal note added." else "Reply sent to user."
                    )
                }
                loadTickets()
            } else {
                _uiState.update {
                    it.copy(
                        isSubmittingReply = false,
                        errorMessage = "Failed to add support response."
                    )
                }
            }
        }
    }

    fun updateTicketStatus(newStatus: AdminSupportTicketStatus) {
        val ticket = _uiState.value.selectedTicketDetails ?: return
        viewModelScope.launch {
            val result = repository.updateSupportTicketStatus(ticket.ticketId, newStatus.name)
            if (result.isSuccess) {
                _uiState.update { it.copy(actionSuccessMessage = "Ticket status updated to ${newStatus.label}.") }
                loadTickets()
            }
        }
    }

    fun assignToMe(operatorName: String) {
        val ticket = _uiState.value.selectedTicketDetails ?: return
        viewModelScope.launch {
            val result = repository.assignSupportTicket(ticket.ticketId, operatorName)
            if (result.isSuccess) {
                _uiState.update { it.copy(actionSuccessMessage = "Ticket assigned to $operatorName.") }
                loadTickets()
            }
        }
    }

    fun clearSuccessMessage() {
        _uiState.update { it.copy(actionSuccessMessage = null) }
    }
}
