package com.example.novaplustrader.data.repository

import com.example.novaplustrader.ui.screens.admin.models.AdminAccessState
import com.example.novaplustrader.ui.screens.admin.models.AdminAuditLogModel
import com.example.novaplustrader.ui.screens.admin.models.AdminBotModel
import com.example.novaplustrader.ui.screens.admin.models.AdminMetricsModel
import com.example.novaplustrader.ui.screens.admin.models.AdminNotificationCategory
import com.example.novaplustrader.ui.screens.admin.models.AdminNotificationPriority
import com.example.novaplustrader.ui.screens.admin.models.AdminNotificationRequest
import com.example.novaplustrader.ui.screens.admin.models.AdminRole
import com.example.novaplustrader.ui.screens.admin.models.AdminSupportMessageModel
import com.example.novaplustrader.ui.screens.admin.models.AdminSupportTicketModel
import com.example.novaplustrader.ui.screens.admin.models.AdminSupportTicketPriority
import com.example.novaplustrader.ui.screens.admin.models.AdminSupportTicketStatus
import com.example.novaplustrader.ui.screens.admin.models.AdminSystemHealthModel
import com.example.novaplustrader.ui.screens.admin.models.AdminTradeModel
import com.example.novaplustrader.ui.screens.admin.models.AdminTransactionModel
import com.example.novaplustrader.ui.screens.admin.models.AdminUserModel
import com.example.novaplustrader.ui.screens.admin.models.AdminUserStatus
import com.example.novaplustrader.ui.screens.admin.models.AdminWalletModel
import com.example.novaplustrader.ui.screens.admin.models.HealthStatus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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
import java.util.UUID

class AdminRepositoryImpl(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : AdminRepository {

    private val _accessState = MutableStateFlow(AdminAccessState.AUTHORIZED)
    private val _currentRole = MutableStateFlow(AdminRole.SUPER_ADMIN)
    private val _adminUid = MutableStateFlow("admin_op_9021")

    private val _metrics = MutableStateFlow<AdminMetricsModel?>(AdminMetricsModel())

    private val _users = MutableStateFlow(
        listOf(
            AdminUserModel(
                uid = "usr_9901",
                fullName = "Alexander Wright",
                email = "a.wright@novaplus.io",
                role = AdminRole.SUPER_ADMIN,
                accountStatus = AdminUserStatus.ACTIVE,
                verificationStatus = "VERIFIED",
                createdAt = "2024-01-15",
                lastActivity = "2 mins ago"
            ),
            AdminUserModel(
                uid = "usr_9902",
                fullName = "Sarah Jenkins",
                email = "s.jenkins@novaplus.io",
                role = AdminRole.OPERATIONS,
                accountStatus = AdminUserStatus.ACTIVE,
                verificationStatus = "VERIFIED",
                createdAt = "2024-02-01",
                lastActivity = "10 mins ago"
            ),
            AdminUserModel(
                uid = "usr_1042",
                fullName = "David Ochieng",
                email = "d.ochieng@example.com",
                role = AdminRole.READ_ONLY,
                accountStatus = AdminUserStatus.SUSPENDED,
                verificationStatus = "VERIFIED",
                createdAt = "2024-03-12",
                lastActivity = "2 days ago"
            ),
            AdminUserModel(
                uid = "usr_1043",
                fullName = "Elena Rostova",
                email = "elena.r@example.com",
                role = AdminRole.READ_ONLY,
                accountStatus = AdminUserStatus.LOCKED,
                verificationStatus = "PENDING",
                createdAt = "2024-03-20",
                lastActivity = "5 hours ago"
            ),
            AdminUserModel(
                uid = "usr_1044",
                fullName = "Marcus Vance",
                email = "m.vance@example.com",
                role = AdminRole.READ_ONLY,
                accountStatus = AdminUserStatus.PENDING,
                verificationStatus = "UNVERIFIED",
                createdAt = "2024-04-01",
                lastActivity = "Yesterday"
            )
        )
    )

    private val _wallets = MutableStateFlow(
        listOf(
            AdminWalletModel(
                walletId = "wlt_usd_8801",
                userId = "usr_9901",
                userName = "Alexander Wright",
                currency = "USD",
                availableBalance = 12450.00,
                pendingBalance = 250.00,
                status = "ACTIVE",
                lastTransaction = "2024-04-10 14:22 UTC",
                riskHold = false
            ),
            AdminWalletModel(
                walletId = "wlt_kes_8802",
                userId = "usr_1042",
                userName = "David Ochieng",
                currency = "KES",
                availableBalance = 850000.00,
                pendingBalance = 50000.00,
                status = "HOLD",
                lastTransaction = "2024-04-09 11:15 UTC",
                riskHold = true
            ),
            AdminWalletModel(
                walletId = "wlt_eur_8803",
                userId = "usr_1043",
                userName = "Elena Rostova",
                currency = "EUR",
                availableBalance = 3400.50,
                pendingBalance = 0.00,
                status = "LOCKED",
                lastTransaction = "2024-04-08 09:40 UTC",
                riskHold = true
            )
        )
    )

    private val _transactions = MutableStateFlow(
        listOf(
            AdminTransactionModel(
                transactionId = "tx_adm_9001",
                userId = "usr_9901",
                userName = "Alexander Wright",
                type = "DEPOSIT",
                status = "COMPLETED",
                amount = 2500.00,
                currency = "USD",
                timestamp = "2024-04-10 14:22:10 UTC"
            ),
            AdminTransactionModel(
                transactionId = "tx_adm_9002",
                userId = "usr_1042",
                userName = "David Ochieng",
                type = "WITHDRAWAL",
                status = "PENDING",
                amount = 50000.00,
                currency = "KES",
                timestamp = "2024-04-10 13:05:44 UTC"
            ),
            AdminTransactionModel(
                transactionId = "tx_adm_9003",
                userId = "usr_1043",
                userName = "Elena Rostova",
                type = "TRADE",
                status = "FAILED",
                amount = 1200.00,
                currency = "EUR",
                timestamp = "2024-04-10 10:12:00 UTC"
            )
        )
    )

    private val _trades = MutableStateFlow(
        listOf(
            AdminTradeModel(
                tradeId = "trd_7701",
                userId = "usr_9901",
                assetPair = "BTC/USD",
                direction = "BUY",
                amount = 5000.00,
                duration = "15m",
                status = "ACTIVE",
                timestamp = "2024-04-10 14:10 UTC"
            ),
            AdminTradeModel(
                tradeId = "trd_7702",
                userId = "usr_9902",
                assetPair = "ETH/USD",
                direction = "SELL",
                amount = 1500.00,
                duration = "1h",
                status = "COMPLETED",
                timestamp = "2024-04-10 12:00 UTC"
            )
        )
    )

    private val _bots = MutableStateFlow(
        listOf(
            AdminBotModel(
                botId = "bot_alpha_01",
                ownerUid = "usr_9901",
                ownerName = "Alexander Wright",
                pair = "BTC/USD",
                strategy = "Grid Trading Pro",
                riskLevel = "Moderate",
                allocation = 10000.0,
                status = "ACTIVE",
                startedTime = "2024-04-01",
                lastActivity = "1 min ago",
                errorState = null
            ),
            AdminBotModel(
                botId = "bot_beta_02",
                ownerUid = "usr_1042",
                ownerName = "David Ochieng",
                pair = "ETH/USD",
                strategy = "Arbitrage Alpha",
                riskLevel = "High",
                allocation = 250000.0,
                status = "ERROR",
                startedTime = "2024-04-05",
                lastActivity = "3 hours ago",
                errorState = "API rate limit exceeded on exchange stream"
            )
        )
    )

    private val _notifications = MutableStateFlow<List<AdminNotificationRequest>>(emptyList())

    private val _supportTickets = MutableStateFlow(
        listOf(
            AdminSupportTicketModel(
                ticketId = "tkt_881",
                userId = "usr_1042",
                userName = "David Ochieng",
                subject = "Withdrawal hold query",
                category = "Wallet & Verification",
                priority = AdminSupportTicketPriority.HIGH,
                status = AdminSupportTicketStatus.OPEN,
                assignedOperator = "Unassigned",
                lastUpdate = "10 mins ago",
                messages = listOf(
                    AdminSupportMessageModel(
                        id = "msg_1",
                        authorUid = "usr_1042",
                        authorName = "David Ochieng",
                        message = "My withdrawal of KES 50,000 shows pending for 2 hours.",
                        timestamp = "10 mins ago"
                    )
                )
            )
        )
    )

    private val _auditLogs = MutableStateFlow(
        listOf(
            AdminAuditLogModel(
                auditId = "aud_1001",
                actorUid = "admin_op_9021",
                actorRole = "SUPER_ADMIN",
                action = "ADMIN_LOGIN",
                targetType = "SESSION",
                targetId = "sess_9021",
                timestamp = "2024-04-10 14:00:00 UTC",
                result = "SUCCESS",
                metadata = "IP: 192.168.1.1, Device: Admin Portal Mobile"
            )
        )
    )

    private val _systemHealth = MutableStateFlow(
        listOf(
            AdminSystemHealthModel("Authentication Service", HealthStatus.OPERATIONAL, 42, "Just now"),
            AdminSystemHealthModel("Firestore Database", HealthStatus.OPERATIONAL, 35, "Just now"),
            AdminSystemHealthModel("Notification Service", HealthStatus.OPERATIONAL, 18, "Just now"),
            AdminSystemHealthModel("Trading Settlement Engine", HealthStatus.OPERATIONAL, 52, "Just now"),
            AdminSystemHealthModel("Wallet Ledger Backend", HealthStatus.OPERATIONAL, 60, "Just now"),
            AdminSystemHealthModel("Bot Execution Cluster", HealthStatus.DEGRADED, 140, "Just now"),
            AdminSystemHealthModel("Support Desk API", HealthStatus.OPERATIONAL, 28, "Just now")
        )
    )

    override fun getAdminAccessState(): Flow<AdminAccessState> = _accessState.asStateFlow()

    override fun getAdminRole(): Flow<AdminRole> = _currentRole.asStateFlow()

    override fun getCurrentAdminUid(): String = _adminUid.value

    override suspend fun verifyAdminSession(): Boolean {
        delay(300)
        val currentUser = auth.currentUser
        return if (currentUser != null || _accessState.value == AdminAccessState.AUTHORIZED) {
            _accessState.value = AdminAccessState.AUTHORIZED
            true
        } else {
            _accessState.value = AdminAccessState.ACCESS_DENIED
            false
        }
    }

    override fun getDashboardMetrics(): Flow<AdminMetricsModel?> = _metrics.asStateFlow()

    override fun getUsers(
        searchQuery: String,
        statusFilter: AdminUserStatus?,
        verificationFilter: String?,
        sortBy: String,
        page: Int,
        pageSize: Int
    ): Flow<List<AdminUserModel>> {
        return _users.map { list ->
            list.filter { user ->
                val matchesQuery = searchQuery.isBlank() ||
                        user.fullName.contains(searchQuery, ignoreCase = true) ||
                        user.email.contains(searchQuery, ignoreCase = true) ||
                        user.uid.contains(searchQuery, ignoreCase = true)
                val matchesStatus = statusFilter == null || user.accountStatus == statusFilter
                val matchesVerif = verificationFilter == null || user.verificationStatus.equals(verificationFilter, ignoreCase = true)
                matchesQuery && matchesStatus && matchesVerif
            }.sortedWith { u1, u2 ->
                when (sortBy) {
                    "NAME" -> u1.fullName.compareTo(u2.fullName)
                    "CREATED" -> u2.createdAt.compareTo(u1.createdAt)
                    "ACTIVITY" -> u2.lastActivity.compareTo(u1.lastActivity)
                    else -> u1.fullName.compareTo(u2.fullName)
                }
            }
        }
    }

    override fun getUserDetails(userId: String): Flow<AdminUserModel?> {
        return _users.map { list -> list.find { it.uid == userId } }
    }

    override suspend fun suspendUser(userId: String, reason: String): Result<Boolean> {
        delay(500)
        _users.update { list ->
            list.map { if (it.uid == userId) it.copy(accountStatus = AdminUserStatus.SUSPENDED) else it }
        }
        logAuditEvent("SUSPEND_USER", "USER", userId, "Reason: $reason")
        return Result.success(true)
    }

    override suspend fun restoreUser(userId: String): Result<Boolean> {
        delay(500)
        _users.update { list ->
            list.map { if (it.uid == userId) it.copy(accountStatus = AdminUserStatus.ACTIVE) else it }
        }
        logAuditEvent("RESTORE_USER", "USER", userId, "User account restored to ACTIVE")
        return Result.success(true)
    }

    override suspend fun lockUser(userId: String, reason: String): Result<Boolean> {
        delay(500)
        _users.update { list ->
            list.map { if (it.uid == userId) it.copy(accountStatus = AdminUserStatus.LOCKED) else it }
        }
        logAuditEvent("LOCK_USER", "USER", userId, "Reason: $reason")
        return Result.success(true)
    }

    override suspend fun requireReauthentication(userId: String): Result<Boolean> {
        delay(400)
        logAuditEvent("REQUIRE_REAUTH", "USER", userId, "Invalidated session tokens")
        return Result.success(true)
    }

    override fun getWallets(
        searchQuery: String,
        currencyFilter: String?,
        statusFilter: String?,
        page: Int,
        pageSize: Int
    ): Flow<List<AdminWalletModel>> {
        return _wallets.map { list ->
            list.filter { w ->
                val matchesQuery = searchQuery.isBlank() ||
                        w.walletId.contains(searchQuery, ignoreCase = true) ||
                        w.userId.contains(searchQuery, ignoreCase = true) ||
                        w.userName.contains(searchQuery, ignoreCase = true)
                val matchesCurrency = currencyFilter.isNullOrBlank() || w.currency.equals(currencyFilter, ignoreCase = true)
                val matchesStatus = statusFilter.isNullOrBlank() || w.status.equals(statusFilter, ignoreCase = true)
                matchesQuery && matchesCurrency && matchesStatus
            }
        }
    }

    override suspend fun requestWalletRiskHold(walletId: String, riskHold: Boolean, reason: String): Result<Boolean> {
        delay(500)
        _wallets.update { list ->
            list.map { if (it.walletId == walletId) it.copy(riskHold = riskHold, status = if (riskHold) "HOLD" else "ACTIVE") else it }
        }
        logAuditEvent(if (riskHold) "SET_RISK_HOLD" else "REMOVE_RISK_HOLD", "WALLET", walletId, "Reason: $reason")
        return Result.success(true)
    }

    override suspend fun requestLedgerAdjustment(
        walletId: String,
        amount: Double,
        currency: String,
        reason: String
    ): Result<Boolean> {
        delay(600)
        logAuditEvent("REQUEST_LEDGER_ADJUSTMENT", "WALLET", walletId, "Amount: $amount $currency, Reason: $reason")
        return Result.success(true)
    }

    override fun getTransactions(
        searchQuery: String,
        typeFilter: String?,
        statusFilter: String?,
        page: Int,
        pageSize: Int
    ): Flow<List<AdminTransactionModel>> {
        return _transactions.map { list ->
            list.filter { tx ->
                val matchesQuery = searchQuery.isBlank() ||
                        tx.transactionId.contains(searchQuery, ignoreCase = true) ||
                        tx.userId.contains(searchQuery, ignoreCase = true) ||
                        tx.userName.contains(searchQuery, ignoreCase = true)
                val matchesType = typeFilter.isNullOrBlank() || tx.type.equals(typeFilter, ignoreCase = true)
                val matchesStatus = statusFilter.isNullOrBlank() || tx.status.equals(statusFilter, ignoreCase = true)
                matchesQuery && matchesType && matchesStatus
            }
        }
    }

    override fun getTrades(
        searchQuery: String,
        statusFilter: String?,
        pairFilter: String?,
        page: Int,
        pageSize: Int
    ): Flow<List<AdminTradeModel>> {
        return _trades.map { list ->
            list.filter { trd ->
                val matchesQuery = searchQuery.isBlank() ||
                        trd.tradeId.contains(searchQuery, ignoreCase = true) ||
                        trd.userId.contains(searchQuery, ignoreCase = true)
                val matchesStatus = statusFilter.isNullOrBlank() || trd.status.equals(statusFilter, ignoreCase = true)
                val matchesPair = pairFilter.isNullOrBlank() || trd.assetPair.equals(pairFilter, ignoreCase = true)
                matchesQuery && matchesStatus && matchesPair
            }
        }
    }

    override suspend fun requestTradeCancellation(tradeId: String, reason: String): Result<Boolean> {
        delay(500)
        _trades.update { list ->
            list.map { if (it.tradeId == tradeId) it.copy(status = "CANCELLED") else it }
        }
        logAuditEvent("CANCEL_TRADE_REQUEST", "TRADE", tradeId, "Reason: $reason")
        return Result.success(true)
    }

    override fun getBots(
        searchQuery: String,
        statusFilter: String?,
        page: Int,
        pageSize: Int
    ): Flow<List<AdminBotModel>> {
        return _bots.map { list ->
            list.filter { bot ->
                val matchesQuery = searchQuery.isBlank() ||
                        bot.botId.contains(searchQuery, ignoreCase = true) ||
                        bot.ownerName.contains(searchQuery, ignoreCase = true) ||
                        bot.strategy.contains(searchQuery, ignoreCase = true)
                val matchesStatus = statusFilter.isNullOrBlank() || bot.status.equals(statusFilter, ignoreCase = true)
                matchesQuery && matchesStatus
            }
        }
    }

    override suspend fun sendBotCommand(botId: String, command: String): Result<Boolean> {
        delay(500)
        _bots.update { list ->
            list.map {
                if (it.botId == botId) {
                    when (command.uppercase()) {
                        "STOP" -> it.copy(status = "STOPPED")
                        "PAUSE" -> it.copy(status = "PAUSED")
                        "RESUME" -> it.copy(status = "ACTIVE")
                        else -> it
                    }
                } else it
            }
        }
        logAuditEvent("BOT_COMMAND", "BOT", botId, "Command: $command")
        return Result.success(true)
    }

    override suspend fun sendNotificationRequest(request: AdminNotificationRequest): Result<Boolean> {
        delay(600)
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss 'UTC'", Locale.US).format(Date())
        val reqWithMeta = request.copy(
            requestId = "req_${UUID.randomUUID().toString().take(8)}",
            createdBy = _adminUid.value,
            timestamp = timestamp
        )
        _notifications.update { listOf(reqWithMeta) + it }
        logAuditEvent("SEND_NOTIFICATION_REQUEST", "NOTIFICATION", reqWithMeta.requestId, "Audience: ${request.audience.label}, Title: ${request.title}")
        return Result.success(true)
    }

    override fun getSupportTickets(
        searchQuery: String,
        statusFilter: String?,
        priorityFilter: String?,
        page: Int,
        pageSize: Int
    ): Flow<List<AdminSupportTicketModel>> {
        return _supportTickets.map { list ->
            list.filter { t ->
                val matchesQuery = searchQuery.isBlank() ||
                        t.ticketId.contains(searchQuery, ignoreCase = true) ||
                        t.userName.contains(searchQuery, ignoreCase = true) ||
                        t.subject.contains(searchQuery, ignoreCase = true)
                val matchesStatus = statusFilter.isNullOrBlank() || t.status.name.equals(statusFilter, ignoreCase = true)
                val matchesPriority = priorityFilter.isNullOrBlank() || t.priority.name.equals(priorityFilter, ignoreCase = true)
                matchesQuery && matchesStatus && matchesPriority
            }
        }
    }

    override suspend fun updateSupportTicketStatus(ticketId: String, newStatus: String): Result<Boolean> {
        delay(400)
        _supportTickets.update { list ->
            list.map {
                if (it.ticketId == ticketId) {
                    val statusEnum = try { AdminSupportTicketStatus.valueOf(newStatus) } catch (e: Exception) { it.status }
                    it.copy(status = statusEnum, lastUpdate = "Just now")
                } else it
            }
        }
        logAuditEvent("UPDATE_SUPPORT_TICKET_STATUS", "TICKET", ticketId, "New Status: $newStatus")
        return Result.success(true)
    }

    override suspend fun assignSupportTicket(ticketId: String, operatorName: String): Result<Boolean> {
        delay(400)
        _supportTickets.update { list ->
            list.map { if (it.ticketId == ticketId) it.copy(assignedOperator = operatorName, lastUpdate = "Just now") else it }
        }
        logAuditEvent("ASSIGN_SUPPORT_TICKET", "TICKET", ticketId, "Assigned to: $operatorName")
        return Result.success(true)
    }

    override suspend fun addSupportReply(
        ticketId: String,
        message: String,
        isInternalNote: Boolean
    ): Result<Boolean> {
        delay(400)
        val timestamp = SimpleDateFormat("HH:mm", Locale.US).format(Date())
        _supportTickets.update { list ->
            list.map { t ->
                if (t.ticketId == ticketId) {
                    val newMsg = AdminSupportMessageModel(
                        id = "msg_${UUID.randomUUID().toString().take(6)}",
                        authorUid = _adminUid.value,
                        authorName = "Operator",
                        message = message,
                        timestamp = timestamp,
                        isInternalNote = isInternalNote
                    )
                    t.copy(messages = t.messages + newMsg, lastUpdate = "Just now")
                } else t
            }
        }
        logAuditEvent(
            if (isInternalNote) "ADD_INTERNAL_NOTE" else "ADD_SUPPORT_REPLY",
            "TICKET",
            ticketId,
            "Message length: ${message.length}"
        )
        return Result.success(true)
    }

    override fun getAuditLogs(
        searchQuery: String,
        actionFilter: String?,
        actorFilter: String?,
        page: Int,
        pageSize: Int
    ): Flow<List<AdminAuditLogModel>> {
        return _auditLogs.map { list ->
            list.filter { log ->
                val matchesQuery = searchQuery.isBlank() ||
                        log.auditId.contains(searchQuery, ignoreCase = true) ||
                        log.action.contains(searchQuery, ignoreCase = true) ||
                        log.targetId.contains(searchQuery, ignoreCase = true)
                val matchesAction = actionFilter.isNullOrBlank() || log.action.equals(actionFilter, ignoreCase = true)
                val matchesActor = actorFilter.isNullOrBlank() || log.actorUid.equals(actorFilter, ignoreCase = true)
                matchesQuery && matchesAction && matchesActor
            }
        }
    }

    override fun getSystemHealth(): Flow<List<AdminSystemHealthModel>> = _systemHealth.asStateFlow()

    private fun logAuditEvent(action: String, targetType: String, targetId: String, metadata: String) {
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss 'UTC'", Locale.US).format(Date())
        val log = AdminAuditLogModel(
            auditId = "aud_${UUID.randomUUID().toString().take(8)}",
            actorUid = _adminUid.value,
            actorRole = _currentRole.value.name,
            action = action,
            targetType = targetType,
            targetId = targetId,
            timestamp = timestamp,
            result = "SUCCESS",
            metadata = metadata
        )
        _auditLogs.update { listOf(log) + it }
    }
}
