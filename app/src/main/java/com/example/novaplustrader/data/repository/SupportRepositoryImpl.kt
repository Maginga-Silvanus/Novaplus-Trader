package com.example.novaplustrader.data.repository

import com.example.novaplustrader.ui.screens.support.models.FaqModel
import com.example.novaplustrader.ui.screens.support.models.SenderType
import com.example.novaplustrader.ui.screens.support.models.SupportTicketModel
import com.example.novaplustrader.ui.screens.support.models.TicketCategory
import com.example.novaplustrader.ui.screens.support.models.TicketMessageModel
import com.example.novaplustrader.ui.screens.support.models.TicketStatus
import com.example.novaplustrader.ui.screens.support.models.defaultFaqList
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SupportRepositoryImpl : SupportRepository {

    private val _faqs = MutableStateFlow(defaultFaqList)

    private val _tickets = MutableStateFlow<List<SupportTicketModel>>(
        listOf(
            SupportTicketModel(
                id = "st_901",
                subject = "Deposit confirmation inquiry for M-Pesa",
                category = TicketCategory.DEPOSIT,
                status = TicketStatus.RESOLVED,
                createdDate = "12 Oct, 14:00",
                lastUpdated = "12 Oct, 14:25",
                latestResponse = "Your deposit was confirmed and credited to your KES wallet."
            ),
            SupportTicketModel(
                id = "st_902",
                subject = "Question regarding Bot custom stop-loss settings",
                category = TicketCategory.BOT,
                status = TicketStatus.OPEN,
                createdDate = "Yesterday, 16:30",
                lastUpdated = "Yesterday, 16:30",
                latestResponse = "Our technical desk is reviewing your bot parameter setup."
            )
        )
    )

    private val _messagesMap = MutableStateFlow<Map<String, List<TicketMessageModel>>>(
        mapOf(
            "st_901" to listOf(
                TicketMessageModel(
                    id = "m1",
                    ticketId = "st_901",
                    senderType = SenderType.USER,
                    senderName = "Leonard Lopez",
                    messageText = "Hi, I initiated an M-Pesa deposit of KES 5,000. Just wanted to check confirmation.",
                    timestamp = "12 Oct, 14:00"
                ),
                TicketMessageModel(
                    id = "m2",
                    ticketId = "st_901",
                    senderType = SenderType.SUPPORT_AGENT,
                    senderName = "Nova Support Desk",
                    messageText = "Hello Leonard! Your deposit was confirmed by the payment gateway and credited to your wallet.",
                    timestamp = "12 Oct, 14:25"
                )
            ),
            "st_902" to listOf(
                TicketMessageModel(
                    id = "m3",
                    ticketId = "st_902",
                    senderType = SenderType.USER,
                    senderName = "Leonard Lopez",
                    messageText = "Does the custom strategy allow stop-loss percentages below 0.5%?",
                    timestamp = "Yesterday, 16:30"
                )
            )
        )
    )

    override fun observeFaqs(): StateFlow<List<FaqModel>> = _faqs.asStateFlow()
    override fun observeTickets(): StateFlow<List<SupportTicketModel>> = _tickets.asStateFlow()

    override fun observeTicketById(ticketId: String): Flow<SupportTicketModel?> {
        return _tickets.map { list -> list.find { it.id == ticketId } }
    }

    override fun observeTicketMessages(ticketId: String): Flow<List<TicketMessageModel>> {
        return _messagesMap.map { map -> map[ticketId] ?: emptyList() }
    }

    override suspend fun submitSupportTicket(
        category: TicketCategory,
        subject: String,
        description: String,
        transactionId: String?
    ): Result<SupportTicketModel> {
        delay(900) // Backend ticket creation verification

        val newTicketId = "st_${System.currentTimeMillis().toString().takeLast(5)}"
        val time = getFormattedTime()

        val newTicket = SupportTicketModel(
            id = newTicketId,
            subject = subject,
            category = category,
            status = TicketStatus.OPEN,
            createdDate = time,
            lastUpdated = time,
            latestResponse = "Ticket received. Support team will respond shortly.",
            transactionId = transactionId
        )

        val firstMessage = TicketMessageModel(
            id = "m_${System.currentTimeMillis()}",
            ticketId = newTicketId,
            senderType = SenderType.USER,
            senderName = "Leonard Lopez",
            messageText = description,
            timestamp = time
        )

        _tickets.update { listOf(newTicket) + it }
        _messagesMap.update { current ->
            current + (newTicketId to listOf(firstMessage))
        }

        return Result.success(newTicket)
    }

    override suspend fun sendTicketReply(
        ticketId: String,
        messageText: String
    ): Result<TicketMessageModel> {
        delay(600)
        val time = getFormattedTime()

        val newMessage = TicketMessageModel(
            id = "m_${System.currentTimeMillis()}",
            ticketId = ticketId,
            senderType = SenderType.USER,
            senderName = "Leonard Lopez",
            messageText = messageText,
            timestamp = time
        )

        _messagesMap.update { current ->
            val existing = current[ticketId] ?: emptyList()
            current + (ticketId to (existing + newMessage))
        }

        // Update ticket last updated timestamp
        _tickets.update { list ->
            list.map { if (it.id == ticketId) it.copy(lastUpdated = time, status = TicketStatus.OPEN) else it }
        }

        return Result.success(newMessage)
    }

    override suspend fun reportProblem(
        category: String,
        description: String,
        deviceMetadata: String
    ): Result<Boolean> {
        delay(800)
        return Result.success(true)
    }

    private fun getFormattedTime(): String {
        val sdf = SimpleDateFormat("dd MMM, HH:mm", Locale.US)
        return sdf.format(Date())
    }
}
