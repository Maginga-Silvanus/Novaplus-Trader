package com.example.novaplustrader.data.repository

import com.example.novaplustrader.ui.screens.support.models.FaqModel
import com.example.novaplustrader.ui.screens.support.models.SupportTicketModel
import com.example.novaplustrader.ui.screens.support.models.TicketCategory
import com.example.novaplustrader.ui.screens.support.models.TicketMessageModel
import kotlinx.coroutines.flow.Flow

interface SupportRepository {
    fun observeFaqs(): Flow<List<FaqModel>>
    fun observeTickets(): Flow<List<SupportTicketModel>>
    fun observeTicketById(ticketId: String): Flow<SupportTicketModel?>
    fun observeTicketMessages(ticketId: String): Flow<List<TicketMessageModel>>

    suspend fun submitSupportTicket(
        category: TicketCategory,
        subject: String,
        description: String,
        transactionId: String?
    ): Result<SupportTicketModel>

    suspend fun sendTicketReply(
        ticketId: String,
        messageText: String
    ): Result<TicketMessageModel>

    suspend fun reportProblem(
        category: String,
        description: String,
        deviceMetadata: String
    ): Result<Boolean>
}
