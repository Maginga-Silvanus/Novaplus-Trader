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
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.Date

class AdminRepositoryImpl(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val functions: FirebaseFunctions = FirebaseFunctions.getInstance()
) : AdminRepository {

    private val _accessState = MutableStateFlow(AdminAccessState.LOADING)
    private val _currentRole = MutableStateFlow(AdminRole.READ_ONLY)
    private val _adminUid = MutableStateFlow("")

    private val _metrics = MutableStateFlow<AdminMetricsModel?>(null)
    private val _wallets = MutableStateFlow(emptyList<AdminWalletModel>())
    private val _bots = MutableStateFlow(emptyList<AdminBotModel>())
    private val _notifications = MutableStateFlow(emptyList<AdminNotificationRequest>())
    private val _supportTickets = MutableStateFlow(emptyList<AdminSupportTicketModel>())
    private val _auditLogs = MutableStateFlow(emptyList<AdminAuditLogModel>())
    private val _systemHealth = MutableStateFlow(emptyList<AdminSystemHealthModel>())

    override fun getAdminAccessState(): Flow<AdminAccessState> = _accessState.asStateFlow()

    override fun getAdminRole(): Flow<AdminRole> = _currentRole.asStateFlow()

    override fun getCurrentAdminUid(): String = _adminUid.value

    override suspend fun verifyAdminSession(): Boolean {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            _adminUid.value = ""
            _currentRole.value = AdminRole.READ_ONLY
            _accessState.value = AdminAccessState.UNAUTHENTICATED
            return false
        }
        return try {
            val claimRole = currentUser.getIdToken(true).await().claims["adminRole"] as? String
            val role = AdminRole.entries.firstOrNull { it.name == claimRole }
            if (role == null) {
                _adminUid.value = ""
                _currentRole.value = AdminRole.READ_ONLY
                _accessState.value = AdminAccessState.ACCESS_DENIED
                false
            } else {
                _adminUid.value = currentUser.uid
                _currentRole.value = role
                _accessState.value = AdminAccessState.AUTHORIZED
                true
            }
        } catch (error: CancellationException) {
            throw error
        } catch (_: Exception) {
            _adminUid.value = ""
            _currentRole.value = AdminRole.READ_ONLY
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
        return observeQuery(firestore.collection("users")).map { documents ->
            val list = documents.map { document -> document.toAdminUser() }
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
        if (userId.isBlank()) return kotlinx.coroutines.flow.flowOf(null)
        return callbackFlow {
            val registration = firestore.collection("users").document(userId)
                .addSnapshotListener { snapshot, error ->
                    when {
                        error != null -> close(error)
                        snapshot == null -> close(IllegalStateException("Unable to load user profile"))
                        !snapshot.exists() -> trySend(null)
                        else -> try {
                            trySend(snapshot.toAdminUser())
                        } catch (parseError: Exception) {
                            close(parseError)
                        }
                    }
                }
            awaitClose { registration.remove() }
        }
    }

    override suspend fun suspendUser(userId: String, reason: String): Result<Boolean> {
        return unavailableAdminOperation()
    }

    override suspend fun restoreUser(userId: String): Result<Boolean> {
        return unavailableAdminOperation()
    }

    override suspend fun lockUser(userId: String, reason: String): Result<Boolean> {
        return unavailableAdminOperation()
    }

    override suspend fun requireReauthentication(userId: String): Result<Boolean> {
        return unavailableAdminOperation()
    }

    override fun getWallets(
        searchQuery: String,
        currencyFilter: String?,
        statusFilter: String?,
        page: Int,
        pageSize: Int
    ): Flow<List<AdminWalletModel>> {
        return observeQuery(firestore.collectionGroup("wallets")).map { documents ->
            val list = documents.filter { it.id == "main" }.flatMap { wallet ->
                val userId = wallet.reference.path.userIdFromPath() ?: return@flatMap emptyList()
                listOf(
                    wallet.toAdminWallet(userId, "USD", "usdBalanceMinorUnits"),
                    wallet.toAdminWallet(userId, "KES", "kesBalanceMinorUnits")
                )
            }
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
        return unavailableAdminOperation()
    }

    override suspend fun requestLedgerAdjustment(
        walletId: String,
        amount: Double,
        currency: String,
        reason: String
    ): Result<Boolean> {
        return unavailableAdminOperation()
    }

    override fun getTransactions(
        searchQuery: String,
        typeFilter: String?,
        statusFilter: String?,
        page: Int,
        pageSize: Int
    ): Flow<List<AdminTransactionModel>> {
        return observeQuery(firestore.collectionGroup("transactions")).map { documents ->
            val list = documents.mapNotNull { document -> document.toAdminTransaction() }
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

    override suspend fun approveDeposit(
        userId: String,
        transactionId: String
    ): Result<Boolean> = try {
        val response = functions.getHttpsCallable("approvePaybillDeposit")
            .call(mapOf("userId" to userId, "transactionId" to transactionId))
            .await().data as? Map<*, *> ?: error("The approval service returned an invalid response")
        if (response["status"] != "APPROVED") error("The deposit was not approved")
        Result.success(true)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override fun getTrades(
        searchQuery: String,
        statusFilter: String?,
        pairFilter: String?,
        page: Int,
        pageSize: Int
    ): Flow<List<AdminTradeModel>> {
        return observeQuery(firestore.collectionGroup("tradeRequests")).map { documents ->
            val list = documents.mapNotNull { document -> document.toAdminTrade() }
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
        return unavailableAdminOperation()
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
        return unavailableAdminOperation()
    }

    override suspend fun sendNotificationRequest(request: AdminNotificationRequest): Result<Boolean> {
        return unavailableAdminOperation()
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
        return unavailableAdminOperation()
    }

    override suspend fun assignSupportTicket(ticketId: String, operatorName: String): Result<Boolean> {
        return unavailableAdminOperation()
    }

    override suspend fun addSupportReply(
        ticketId: String,
        message: String,
        isInternalNote: Boolean
    ): Result<Boolean> {
        return unavailableAdminOperation()
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

    private fun <T> unavailableAdminOperation(): Result<T> = Result.failure(
        IllegalStateException("Administrative actions require a configured trusted Firebase backend.")
    )

    private fun observeQuery(query: Query): Flow<List<DocumentSnapshot>> = callbackFlow {
        val registration = query.addSnapshotListener { snapshot, error ->
            when {
                error != null -> close(error)
                snapshot == null -> close(IllegalStateException("Unable to load administrative data"))
                else -> trySend(snapshot.documents)
            }
        }
        awaitClose { registration.remove() }
    }

    private fun DocumentSnapshot.toAdminUser(): AdminUserModel {
        val status = AdminUserStatus.entries.firstOrNull {
            it.name == getString("accountStatus")
        } ?: AdminUserStatus.PENDING
        return AdminUserModel(
            uid = id,
            fullName = getString("fullName").orEmpty(),
            email = getString("email").orEmpty(),
            role = AdminRole.READ_ONLY,
            accountStatus = status,
            verificationStatus = getString("verificationStatus") ?: "UNKNOWN",
            createdAt = getTimestamp("createdAt")?.toDate()?.toString().orEmpty(),
            lastActivity = "Unavailable"
        )
    }

    private fun DocumentSnapshot.toAdminWallet(
        userId: String,
        currencyCode: String,
        balanceField: String
    ): AdminWalletModel {
        val minorUnits = getLong(balanceField) ?: 0L
        return AdminWalletModel(
            walletId = "wallet-$userId-${currencyCode.lowercase()}",
            userId = userId,
            userName = userId,
            currency = currencyCode,
            availableBalance = minorUnits / 100.0,
            pendingBalance = 0.0,
            status = "ACTIVE",
            lastTransaction = "Unavailable",
            riskHold = false
        )
    }

    private fun DocumentSnapshot.toAdminTransaction(): AdminTransactionModel? {
        val parts = reference.path.split("/")
        val userIndex = parts.indexOf("users")
        val userId = parts.getOrNull(userIndex + 1) ?: return null
        val amountMinorUnits = getLong("amountMinorUnits") ?: getLong("grossAmountMinorUnits") ?: 0L
        val type = getString("operation")
            ?: getString("direction")?.let { "CRYPTO_$it" }
            ?: "TRANSACTION"
        return AdminTransactionModel(
            transactionId = id,
            userId = userId,
            userName = userId,
            type = type,
            status = getString("status") ?: "UNKNOWN",
            amount = amountMinorUnits / 100.0,
            currency = getString("currency") ?: "USD",
            timestamp = getTimestamp("createdAt")?.toDate()?.toString().orEmpty(),
            providerReference = getString("providerReference")
        )
    }

    private fun DocumentSnapshot.toAdminTrade(): AdminTradeModel? {
        val parts = reference.path.split("/")
        val userIndex = parts.indexOf("users")
        val userId = parts.getOrNull(userIndex + 1) ?: return null
        return AdminTradeModel(
            tradeId = getString("idempotencyKey") ?: id,
            userId = userId,
            assetPair = getString("pair") ?: "${getString("asset") ?: "UNKNOWN"}/USDT",
            direction = getString("direction") ?: "UNKNOWN",
            amount = (get("amount") as? Number)?.toDouble() ?: 0.0,
            duration = "Paper",
            status = getString("status") ?: "UNKNOWN",
            timestamp = getTimestamp("createdAt")?.toDate()?.toString().orEmpty()
        )
    }

    private fun String.userIdFromPath(): String? {
        val parts = split("/")
        val userIndex = parts.indexOf("users")
        return parts.getOrNull(userIndex + 1)
    }

}
