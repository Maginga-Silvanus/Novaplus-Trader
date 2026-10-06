package com.example.novaplustrader.data.repository

import com.example.novaplustrader.ui.screens.admin.models.AdminAccessState
import com.example.novaplustrader.ui.screens.admin.models.AdminAuditLogModel
import com.example.novaplustrader.ui.screens.admin.models.AdminBotModel
import com.example.novaplustrader.ui.screens.admin.models.AdminMetricsModel
import com.example.novaplustrader.ui.screens.admin.models.AdminNotificationRequest
import com.example.novaplustrader.ui.screens.admin.models.AdminRole
import com.example.novaplustrader.ui.screens.admin.models.AdminSupportTicketModel
import com.example.novaplustrader.ui.screens.admin.models.AdminSystemHealthModel
import com.example.novaplustrader.ui.screens.admin.models.AdminTradeModel
import com.example.novaplustrader.ui.screens.admin.models.AdminTransactionModel
import com.example.novaplustrader.ui.screens.admin.models.AdminUserModel
import com.example.novaplustrader.ui.screens.admin.models.AdminUserStatus
import com.example.novaplustrader.ui.screens.admin.models.AdminWalletModel
import kotlinx.coroutines.flow.Flow

interface AdminRepository {
    fun getAdminAccessState(): Flow<AdminAccessState>
    fun getAdminRole(): Flow<AdminRole>
    fun getCurrentAdminUid(): String
    suspend fun verifyAdminSession(): Boolean

    fun getDashboardMetrics(): Flow<AdminMetricsModel?>

    fun getUsers(
        searchQuery: String = "",
        statusFilter: AdminUserStatus? = null,
        verificationFilter: String? = null,
        sortBy: String = "NAME",
        page: Int = 1,
        pageSize: Int = 20
    ): Flow<List<AdminUserModel>>

    fun getUserDetails(userId: String): Flow<AdminUserModel?>

    suspend fun suspendUser(userId: String, reason: String): Result<Boolean>
    suspend fun restoreUser(userId: String): Result<Boolean>
    suspend fun lockUser(userId: String, reason: String): Result<Boolean>
    suspend fun requireReauthentication(userId: String): Result<Boolean>

    fun getWallets(
        searchQuery: String = "",
        currencyFilter: String? = null,
        statusFilter: String? = null,
        page: Int = 1,
        pageSize: Int = 20
    ): Flow<List<AdminWalletModel>>

    suspend fun requestWalletRiskHold(walletId: String, riskHold: Boolean, reason: String): Result<Boolean>
    suspend fun requestLedgerAdjustment(walletId: String, amount: Double, currency: String, reason: String): Result<Boolean>

    fun getTransactions(
        searchQuery: String = "",
        typeFilter: String? = null,
        statusFilter: String? = null,
        page: Int = 1,
        pageSize: Int = 20
    ): Flow<List<AdminTransactionModel>>

    fun getTrades(
        searchQuery: String = "",
        statusFilter: String? = null,
        pairFilter: String? = null,
        page: Int = 1,
        pageSize: Int = 20
    ): Flow<List<AdminTradeModel>>

    suspend fun requestTradeCancellation(tradeId: String, reason: String): Result<Boolean>

    fun getBots(
        searchQuery: String = "",
        statusFilter: String? = null,
        page: Int = 1,
        pageSize: Int = 20
    ): Flow<List<AdminBotModel>>

    suspend fun sendBotCommand(botId: String, command: String): Result<Boolean>

    suspend fun sendNotificationRequest(request: AdminNotificationRequest): Result<Boolean>

    fun getSupportTickets(
        searchQuery: String = "",
        statusFilter: String? = null,
        priorityFilter: String? = null,
        page: Int = 1,
        pageSize: Int = 20
    ): Flow<List<AdminSupportTicketModel>>

    suspend fun updateSupportTicketStatus(ticketId: String, newStatus: String): Result<Boolean>
    suspend fun assignSupportTicket(ticketId: String, operatorName: String): Result<Boolean>
    suspend fun addSupportReply(ticketId: String, message: String, isInternalNote: Boolean): Result<Boolean>

    fun getAuditLogs(
        searchQuery: String = "",
        actionFilter: String? = null,
        actorFilter: String? = null,
        page: Int = 1,
        pageSize: Int = 20
    ): Flow<List<AdminAuditLogModel>>

    fun getSystemHealth(): Flow<List<AdminSystemHealthModel>>
}
