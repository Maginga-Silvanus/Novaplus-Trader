package com.example.novaplustrader.ui.screens.support

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.novaplustrader.data.repository.SupportRepository
import com.example.novaplustrader.data.repository.SupportRepositoryImpl
import com.example.novaplustrader.ui.screens.support.models.FaqCategory
import com.example.novaplustrader.ui.screens.support.models.SupportTicketModel
import com.example.novaplustrader.ui.screens.support.models.TicketCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SupportViewModel(
    private val repository: SupportRepository = SupportRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(SupportUiState())
    val uiState: StateFlow<SupportUiState> = _uiState.asStateFlow()

    init {
        observeRepositoryData()
    }

    private fun observeRepositoryData() {
        viewModelScope.launch {
            launch {
                repository.observeFaqs().collect { faqs ->
                    _uiState.update { it.copy(faqs = faqs) }
                }
            }
            launch {
                repository.observeTickets()
                    .catch { error -> reportError(error, "Unable to load support tickets.") }
                    .collect { tickets -> _uiState.update { it.copy(tickets = tickets) } }
            }
        }
    }

    fun updateFaqSearchQuery(query: String) {
        _uiState.update { it.copy(faqSearchQuery = query) }
    }

    fun selectFaqCategory(category: FaqCategory) {
        _uiState.update { it.copy(selectedFaqCategory = category) }
    }

    fun toggleFaqExpand(faqId: String) {
        val currentExpanded = _uiState.value.expandedFaqIds.toMutableSet()
        if (currentExpanded.contains(faqId)) {
            currentExpanded.remove(faqId)
        } else {
            currentExpanded.add(faqId)
        }
        _uiState.update { it.copy(expandedFaqIds = currentExpanded) }
    }

    fun selectTicketCategory(category: TicketCategory) {
        _uiState.update { it.copy(selectedTicketCategory = category) }
    }

    fun updateSubjectInput(subject: String) {
        _uiState.update { it.copy(subjectInput = subject, validationError = null) }
    }

    fun updateDescriptionInput(desc: String) {
        _uiState.update { it.copy(descriptionInput = desc, validationError = null) }
    }

    fun updateTransactionIdInput(txId: String) {
        _uiState.update { it.copy(transactionIdInput = txId) }
    }

    fun submitTicket() {
        val subject = _uiState.value.subjectInput.trim()
        val desc = _uiState.value.descriptionInput.trim()

        if (subject.isBlank()) {
            _uiState.update { it.copy(validationError = "Subject cannot be empty") }
            return
        }
        if (desc.isBlank()) {
            _uiState.update { it.copy(validationError = "Description cannot be empty") }
            return
        }

        _uiState.update { it.copy(isSubmitting = true, validationError = null) }

        viewModelScope.launch {
            val result = repository.submitSupportTicket(
                category = _uiState.value.selectedTicketCategory,
                subject = subject,
                description = desc,
                transactionId = _uiState.value.transactionIdInput.ifBlank { null }
            )

            _uiState.update { it.copy(isSubmitting = false) }

            result.onSuccess { ticket ->
                _uiState.update {
                    it.copy(
                        lastCreatedTicket = ticket,
                        subjectInput = "",
                        descriptionInput = "",
                        transactionIdInput = ""
                    )
                }
            }.onFailure { err ->
                _uiState.update { it.copy(errorMessage = err.message ?: "Failed to submit ticket") }
            }
        }
    }

    fun selectTicketDetails(ticket: SupportTicketModel?) {
        _uiState.update { it.copy(selectedTicket = ticket) }
        if (ticket != null) {
            viewModelScope.launch {
                repository.observeTicketMessages(ticket.id)
                    .catch { error -> reportError(error, "Unable to load support messages.") }
                    .collect { messages -> _uiState.update { it.copy(ticketMessages = messages) } }
            }
        }
    }

    fun updateReplyText(text: String) {
        _uiState.update { it.copy(replyText = text) }
    }

    fun sendReply() {
        val text = _uiState.value.replyText.trim()
        val ticket = _uiState.value.selectedTicket ?: return

        if (text.isBlank()) return

        viewModelScope.launch {
            repository.sendTicketReply(ticket.id, text)
                .onSuccess { _uiState.update { it.copy(replyText = "") } }
                .onFailure { error -> reportError(error, "Failed to send reply.") }
        }
    }

    fun clearLastCreatedTicket() {
        _uiState.update { it.copy(lastCreatedTicket = null) }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun reportError(error: Throwable, fallbackMessage: String) {
        _uiState.update {
            it.copy(errorMessage = error.localizedMessage ?: fallbackMessage)
        }
    }
}
