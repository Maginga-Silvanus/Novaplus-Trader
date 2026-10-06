package com.example.novaplustrader.ui.screens.support

import com.example.novaplustrader.ui.screens.support.models.FaqCategory
import com.example.novaplustrader.ui.screens.support.models.FaqModel
import com.example.novaplustrader.ui.screens.support.models.SupportTicketModel
import com.example.novaplustrader.ui.screens.support.models.TicketCategory
import com.example.novaplustrader.ui.screens.support.models.TicketMessageModel

data class SupportUiState(
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val validationError: String? = null,
    val faqs: List<FaqModel> = emptyList(),
    val faqSearchQuery: String = "",
    val selectedFaqCategory: FaqCategory = FaqCategory.ALL,
    val expandedFaqIds: Set<String> = emptySet(),

    // Tickets
    val tickets: List<SupportTicketModel> = emptyList(),
    val selectedTicket: SupportTicketModel? = null,
    val ticketMessages: List<TicketMessageModel> = emptyList(),
    val replyText: String = "",
    val lastCreatedTicket: SupportTicketModel? = null,

    // Ticket Creation Form
    val selectedTicketCategory: TicketCategory = TicketCategory.ACCOUNT,
    val subjectInput: String = "",
    val descriptionInput: String = "",
    val transactionIdInput: String = ""
)
