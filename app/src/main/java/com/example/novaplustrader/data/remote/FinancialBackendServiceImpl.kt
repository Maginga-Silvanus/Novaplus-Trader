package com.example.novaplustrader.data.remote

import com.example.novaplustrader.data.repository.NotificationRepository
import com.example.novaplustrader.data.repository.NotificationRepositoryImpl
import com.example.novaplustrader.domain.model.ledger.DepositRequest
import com.example.novaplustrader.domain.model.ledger.FeeModel
import com.example.novaplustrader.domain.model.ledger.FinancialAuditRecord
import com.example.novaplustrader.domain.model.ledger.FinancialLedgerEntry
import com.example.novaplustrader.domain.model.ledger.FinancialTransaction
import com.example.novaplustrader.domain.model.ledger.FinancialTransactionStatus
import com.example.novaplustrader.domain.model.ledger.FinancialTransactionType
import com.example.novaplustrader.domain.model.ledger.LedgerDirection
import com.example.novaplustrader.domain.model.ledger.LedgerEntryType
import com.example.novaplustrader.domain.model.ledger.LedgerStatus
import com.example.novaplustrader.domain.model.ledger.RefundRequest
import com.example.novaplustrader.domain.model.ledger.ReversalRequest
import com.example.novaplustrader.domain.model.ledger.TransferRequest
import com.example.novaplustrader.domain.model.ledger.WalletBalanceModel
import com.example.novaplustrader.domain.model.ledger.WithdrawalRequest
import com.example.novaplustrader.ui.model.Currency
import com.example.novaplustrader.ui.screens.notifications.models.NotificationCategory
import com.example.novaplustrader.ui.screens.notifications.models.NotificationModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class FinancialBackendServiceImpl(
    private val notificationRepository: NotificationRepository = NotificationRepositoryImpl()
) : FinancialBackendService {

    // Idempotency cache: idempotencyKey -> FinancialTransaction
    private val idempotencyCache = mutableMapOf<String, FinancialTransaction>()

    // Master double-entry ledger entries
    private val _ledgerEntries = MutableStateFlow<List<FinancialLedgerEntry>>(emptyList())

    // Master transactions store
    private val _transactions = MutableStateFlow<List<FinancialTransaction>>(
        listOf(
            FinancialTransaction(
                transactionId = "tx_sys_1001",
                userId = "usr_9901",
                walletId = "w_usd",
                type = FinancialTransactionType.DEPOSIT,
                currency = Currency.USD,
                amountMinorUnits = 632449L,
                feeMinorUnits = 0L,
                status = FinancialTransactionStatus.COMPLETED,
                idempotencyKey = "idemp_sys_1001",
                reference = "REF-SYS-INIT-USD",
                description = "Initial USD Vault Deposit",
                createdAt = "2024-04-01 10:00:00 UTC",
                updatedAt = "2024-04-01 10:00:00 UTC",
                completedAt = "2024-04-01 10:00:01 UTC"
            ),
            FinancialTransaction(
                transactionId = "tx_sys_1002",
                userId = "usr_9901",
                walletId = "w_kes",
                type = FinancialTransactionType.DEPOSIT,
                currency = Currency.KES,
                amountMinorUnits = 82400000L,
                feeMinorUnits = 0L,
                status = FinancialTransactionStatus.COMPLETED,
                idempotencyKey = "idemp_sys_1002",
                reference = "REF-SYS-INIT-KES",
                description = "Initial KES M-Pesa Vault Balance",
                createdAt = "2024-04-01 10:00:00 UTC",
                updatedAt = "2024-04-01 10:00:00 UTC",
                completedAt = "2024-04-01 10:00:01 UTC"
            )
        )
    )

    // Wallet balances state map: walletId -> WalletBalanceModel
    private val _balances = MutableStateFlow(
        mapOf(
            "w_usd" to WalletBalanceModel(
                walletId = "w_usd",
                userId = "usr_9901",
                currency = Currency.USD,
                availableBalanceMinorUnits = 632449L,
                pendingBalanceMinorUnits = 0L,
                lockedBalanceMinorUnits = 0L
            ),
            "w_kes" to WalletBalanceModel(
                walletId = "w_kes",
                userId = "usr_9901",
                currency = Currency.KES,
                availableBalanceMinorUnits = 82400000L,
                pendingBalanceMinorUnits = 0L,
                lockedBalanceMinorUnits = 0L
            )
        )
    )

    private val auditRecords = mutableListOf<FinancialAuditRecord>()

    override suspend fun createDepositRequest(request: DepositRequest): Result<FinancialTransaction> {
        // Idempotency check
        idempotencyCache[request.idempotencyKey]?.let {
            return Result.success(it)
        }

        delay(300) // Simulate backend network roundtrip

        val feeRes = calculateFee(FinancialTransactionType.DEPOSIT, request.amountMinorUnits, request.currency).getOrDefault(
            FeeModel(FinancialTransactionType.DEPOSIT, request.currency, 0L, effectiveDate = "2024-01-01")
        )

        val txId = "tx_dep_${UUID.randomUUID().toString().take(8)}"
        val now = getCurrentTimestamp()

        val pendingTx = FinancialTransaction(
            transactionId = txId,
            userId = request.userId,
            walletId = request.walletId,
            type = FinancialTransactionType.DEPOSIT,
            currency = request.currency,
            amountMinorUnits = request.amountMinorUnits,
            feeMinorUnits = feeRes.feeAmountMinorUnits,
            status = FinancialTransactionStatus.PENDING,
            idempotencyKey = request.idempotencyKey,
            reference = "DEP-${request.method.name}-${txId.takeLast(6).uppercase()}",
            providerReference = request.providerReference ?: "PROV-${UUID.randomUUID().toString().take(6)}",
            description = "Deposit via ${request.method.title}",
            createdAt = now,
            updatedAt = now
        )

        // Store transaction in PENDING state (Wallet balance not modified yet!)
        _transactions.update { listOf(pendingTx) + it }
        idempotencyCache[request.idempotencyKey] = pendingTx

        // Update pending balance in wallet model
        updateWalletBalances(request.walletId) { current ->
            current.copy(pendingBalanceMinorUnits = current.pendingBalanceMinorUnits + request.amountMinorUnits)
        }

        sendNotification(
            title = "Deposit Pending",
            body = "${request.method.title} deposit of ${request.currency.code} ${(request.amountMinorUnits / 100.0)} received.",
            categoryStr = "WALLET"
        )

        // Transition: PENDING -> PROCESSING -> COMPLETED
        delay(600)
        val completedTx = commitDepositTransaction(pendingTx)

        return Result.success(completedTx)
    }

    private suspend fun commitDepositTransaction(pendingTx: FinancialTransaction): FinancialTransaction {
        val now = getCurrentTimestamp()
        val currentWallet = _balances.value[pendingTx.walletId] ?: WalletBalanceModel(
            walletId = pendingTx.walletId,
            userId = pendingTx.userId,
            currency = pendingTx.currency,
            availableBalanceMinorUnits = 0L
        )

        val beforeAvail = currentWallet.availableBalanceMinorUnits
        val netCredit = pendingTx.netAmountMinorUnits
        val afterAvail = beforeAvail + netCredit

        // Create Double-Entry Ledger Entry
        val ledgerEntry = FinancialLedgerEntry(
            ledgerEntryId = "ldg_${UUID.randomUUID().toString().take(8)}",
            transactionId = pendingTx.transactionId,
            userId = pendingTx.userId,
            walletId = pendingTx.walletId,
            entryType = LedgerEntryType.DEPOSIT_CREDIT,
            currency = pendingTx.currency,
            amountMinorUnits = netCredit,
            direction = LedgerDirection.CREDIT,
            balanceBeforeMinorUnits = beforeAvail,
            balanceAfterMinorUnits = afterAvail,
            status = LedgerStatus.COMMITTED,
            reference = pendingTx.reference,
            createdAt = now,
            processedAt = now
        )

        _ledgerEntries.update { listOf(ledgerEntry) + it }

        val completedTx = pendingTx.copy(
            status = FinancialTransactionStatus.COMPLETED,
            updatedAt = now,
            completedAt = now
        )

        _transactions.update { list ->
            list.map { if (it.transactionId == pendingTx.transactionId) completedTx else it }
        }
        idempotencyCache[pendingTx.idempotencyKey] = completedTx

        // Commit balance: Move pending balance -> available balance
        updateWalletBalances(pendingTx.walletId) { current ->
            current.copy(
                availableBalanceMinorUnits = current.availableBalanceMinorUnits + netCredit,
                pendingBalanceMinorUnits = (current.pendingBalanceMinorUnits - pendingTx.amountMinorUnits).coerceAtLeast(0L)
            )
        }

        logAuditRecord(
            txId = completedTx.transactionId,
            ledgerIds = listOf(ledgerEntry.ledgerEntryId),
            actorUid = pendingTx.userId,
            operation = "DEPOSIT_COMMIT",
            prevState = "PENDING",
            newState = "COMPLETED"
        )

        sendNotification(
            title = "Deposit Completed",
            body = "${pendingTx.currency.code} ${(pendingTx.amountMinorUnits / 100.0)} deposited to wallet successfully.",
            categoryStr = "WALLET"
        )

        return completedTx
    }

    override suspend fun createWithdrawalRequest(request: WithdrawalRequest): Result<FinancialTransaction> {
        idempotencyCache[request.idempotencyKey]?.let {
            return Result.success(it)
        }

        val currentWallet = _balances.value[request.walletId] ?: return Result.failure(IllegalArgumentException("Wallet account not found"))

        if (request.amountMinorUnits > currentWallet.availableBalanceMinorUnits) {
            return Result.failure(IllegalArgumentException("Insufficient available balance for withdrawal"))
        }

        delay(300)

        val feeRes = calculateFee(FinancialTransactionType.WITHDRAWAL, request.amountMinorUnits, request.currency).getOrDefault(
            FeeModel(FinancialTransactionType.WITHDRAWAL, request.currency, 50L, effectiveDate = "2024-01-01")
        )

        val totalDebit = request.amountMinorUnits + feeRes.feeAmountMinorUnits
        if (totalDebit > currentWallet.availableBalanceMinorUnits) {
            return Result.failure(IllegalArgumentException("Insufficient available balance including withdrawal fee"))
        }

        val txId = "tx_wd_${UUID.randomUUID().toString().take(8)}"
        val now = getCurrentTimestamp()

        val pendingTx = FinancialTransaction(
            transactionId = txId,
            userId = request.userId,
            walletId = request.walletId,
            type = FinancialTransactionType.WITHDRAWAL,
            currency = request.currency,
            amountMinorUnits = request.amountMinorUnits,
            feeMinorUnits = feeRes.feeAmountMinorUnits,
            status = FinancialTransactionStatus.PENDING,
            idempotencyKey = request.idempotencyKey,
            reference = "WD-${request.method.name}-${txId.takeLast(6).uppercase()}",
            description = "Withdrawal to ${request.destination}",
            createdAt = now,
            updatedAt = now
        )

        _transactions.update { listOf(pendingTx) + it }
        idempotencyCache[request.idempotencyKey] = pendingTx

        // Lock funds on backend during PROCESSING
        updateWalletBalances(request.walletId) { current ->
            current.copy(
                availableBalanceMinorUnits = current.availableBalanceMinorUnits - totalDebit,
                lockedBalanceMinorUnits = current.lockedBalanceMinorUnits + totalDebit
            )
        }

        sendNotification(
            title = "Withdrawal Initiated",
            body = "Withdrawal request of ${request.currency.code} ${(request.amountMinorUnits / 100.0)} queued.",
            categoryStr = "WALLET"
        )

        delay(600)
        val completedTx = commitWithdrawalTransaction(pendingTx, totalDebit)

        return Result.success(completedTx)
    }

    private suspend fun commitWithdrawalTransaction(pendingTx: FinancialTransaction, totalDebit: Long): FinancialTransaction {
        val now = getCurrentTimestamp()
        val currentWallet = _balances.value[pendingTx.walletId]!!

        val beforeAvail = currentWallet.availableBalanceMinorUnits + totalDebit
        val afterAvail = currentWallet.availableBalanceMinorUnits

        val ledgerEntry = FinancialLedgerEntry(
            ledgerEntryId = "ldg_${UUID.randomUUID().toString().take(8)}",
            transactionId = pendingTx.transactionId,
            userId = pendingTx.userId,
            walletId = pendingTx.walletId,
            entryType = LedgerEntryType.WITHDRAWAL_DEBIT,
            currency = pendingTx.currency,
            amountMinorUnits = totalDebit,
            direction = LedgerDirection.DEBIT,
            balanceBeforeMinorUnits = beforeAvail,
            balanceAfterMinorUnits = afterAvail,
            status = LedgerStatus.COMMITTED,
            reference = pendingTx.reference,
            createdAt = now,
            processedAt = now
        )

        _ledgerEntries.update { listOf(ledgerEntry) + it }

        val completedTx = pendingTx.copy(
            status = FinancialTransactionStatus.COMPLETED,
            updatedAt = now,
            completedAt = now
        )

        _transactions.update { list ->
            list.map { if (it.transactionId == pendingTx.transactionId) completedTx else it }
        }
        idempotencyCache[pendingTx.idempotencyKey] = completedTx

        // Release locked funds after final ledger debit commit
        updateWalletBalances(pendingTx.walletId) { current ->
            current.copy(
                lockedBalanceMinorUnits = (current.lockedBalanceMinorUnits - totalDebit).coerceAtLeast(0L)
            )
        }

        logAuditRecord(
            txId = completedTx.transactionId,
            ledgerIds = listOf(ledgerEntry.ledgerEntryId),
            actorUid = pendingTx.userId,
            operation = "WITHDRAWAL_COMMIT",
            prevState = "PENDING",
            newState = "COMPLETED"
        )

        sendNotification(
            title = "Withdrawal Completed",
            body = "Withdrawal of ${pendingTx.currency.code} ${(pendingTx.amountMinorUnits / 100.0)} confirmed by payout provider.",
            categoryStr = "WALLET"
        )

        return completedTx
    }

    override suspend fun createTransferRequest(request: TransferRequest): Result<FinancialTransaction> {
        idempotencyCache[request.idempotencyKey]?.let {
            return Result.success(it)
        }

        val senderWallet = _balances.value[request.senderWalletId] ?: return Result.failure(IllegalArgumentException("Sender wallet not found"))

        if (request.amountMinorUnits > senderWallet.availableBalanceMinorUnits) {
            return Result.failure(IllegalArgumentException("Insufficient available balance for internal transfer"))
        }

        delay(300)

        val txId = "tx_send_${UUID.randomUUID().toString().take(8)}"
        val now = getCurrentTimestamp()

        val completedTx = FinancialTransaction(
            transactionId = txId,
            userId = request.senderUserId,
            walletId = request.senderWalletId,
            type = FinancialTransactionType.TRANSFER,
            currency = request.currency,
            amountMinorUnits = request.amountMinorUnits,
            feeMinorUnits = 0L,
            status = FinancialTransactionStatus.COMPLETED,
            idempotencyKey = request.idempotencyKey,
            reference = "TRF-${txId.takeLast(6).uppercase()}",
            description = "Transfer to ${request.recipientId} (${request.note})",
            createdAt = now,
            updatedAt = now,
            completedAt = now
        )

        // Debit Sender Wallet
        val beforeSenderAvail = senderWallet.availableBalanceMinorUnits
        val afterSenderAvail = beforeSenderAvail - request.amountMinorUnits

        val senderLedger = FinancialLedgerEntry(
            ledgerEntryId = "ldg_${UUID.randomUUID().toString().take(8)}",
            transactionId = txId,
            userId = request.senderUserId,
            walletId = request.senderWalletId,
            entryType = LedgerEntryType.TRANSFER_DEBIT,
            currency = request.currency,
            amountMinorUnits = request.amountMinorUnits,
            direction = LedgerDirection.DEBIT,
            balanceBeforeMinorUnits = beforeSenderAvail,
            balanceAfterMinorUnits = afterSenderAvail,
            status = LedgerStatus.COMMITTED,
            reference = completedTx.reference,
            createdAt = now,
            processedAt = now
        )

        _ledgerEntries.update { listOf(senderLedger) + it }
        _transactions.update { listOf(completedTx) + it }
        idempotencyCache[request.idempotencyKey] = completedTx

        updateWalletBalances(request.senderWalletId) { current ->
            current.copy(availableBalanceMinorUnits = afterSenderAvail)
        }

        logAuditRecord(
            txId = txId,
            ledgerIds = listOf(senderLedger.ledgerEntryId),
            actorUid = request.senderUserId,
            operation = "TRANSFER_EXECUTE",
            prevState = "CREATED",
            newState = "COMPLETED"
        )

        sendNotification(
            title = "Transfer Sent",
            body = "Transferred ${request.currency.code} ${(request.amountMinorUnits / 100.0)} to ${request.recipientId}.",
            categoryStr = "WALLET"
        )

        return Result.success(completedTx)
    }

    override suspend fun getTransaction(transactionId: String): Result<FinancialTransaction?> {
        val tx = _transactions.value.find { it.transactionId == transactionId }
        return Result.success(tx)
    }

    override fun observeTransaction(transactionId: String): Flow<FinancialTransaction?> {
        return _transactions.map { list -> list.find { it.transactionId == transactionId } }
    }

    override suspend fun getWalletBalance(walletId: String): Result<WalletBalanceModel> {
        val bal = _balances.value[walletId] ?: WalletBalanceModel(walletId, "usr_9901", Currency.USD, 0L)
        return Result.success(bal)
    }

    override fun observeWalletBalance(walletId: String): Flow<WalletBalanceModel> {
        return _balances.map { map ->
            map[walletId] ?: WalletBalanceModel(walletId, "usr_9901", Currency.USD, 0L)
        }
    }

    override suspend fun getTransactionHistory(
        walletId: String,
        page: Int,
        pageSize: Int,
        type: FinancialTransactionType?,
        status: FinancialTransactionStatus?
    ): Result<List<FinancialTransaction>> {
        val filtered = _transactions.value.filter { tx ->
            val matchesWallet = tx.walletId == walletId || walletId.isBlank()
            val matchesType = type == null || tx.type == type
            val matchesStatus = status == null || tx.status == status
            matchesWallet && matchesType && matchesStatus
        }
        return Result.success(filtered)
    }

    override fun observeTransactionHistory(walletId: String): Flow<List<FinancialTransaction>> {
        return _transactions.map { list ->
            list.filter { it.walletId == walletId || walletId.isBlank() }
        }
    }

    override suspend fun requestRefund(request: RefundRequest): Result<FinancialTransaction> {
        idempotencyCache[request.idempotencyKey]?.let { return Result.success(it) }

        val origTx = _transactions.value.find { it.transactionId == request.originalTransactionId }
            ?: return Result.failure(IllegalArgumentException("Original transaction not found"))

        if (origTx.status != FinancialTransactionStatus.COMPLETED) {
            return Result.failure(IllegalStateException("Only completed transactions can be refunded"))
        }

        val now = getCurrentTimestamp()
        val refundTxId = "tx_rfnd_${UUID.randomUUID().toString().take(8)}"

        val refundTx = FinancialTransaction(
            transactionId = refundTxId,
            userId = origTx.userId,
            walletId = origTx.walletId,
            type = FinancialTransactionType.REFUND,
            currency = origTx.currency,
            amountMinorUnits = origTx.amountMinorUnits,
            feeMinorUnits = 0L,
            status = FinancialTransactionStatus.COMPLETED,
            idempotencyKey = request.idempotencyKey,
            reference = "RFND-${origTx.reference}",
            description = "Refund for ${origTx.transactionId}: ${request.reason}",
            createdAt = now,
            updatedAt = now,
            completedAt = now
        )

        _transactions.update { listOf(refundTx) + it }
        idempotencyCache[request.idempotencyKey] = refundTx

        // Credit wallet back with refund amount
        updateWalletBalances(origTx.walletId) { current ->
            current.copy(availableBalanceMinorUnits = current.availableBalanceMinorUnits + origTx.amountMinorUnits)
        }

        logAuditRecord(
            txId = refundTxId,
            ledgerIds = emptyList(),
            actorUid = request.requestedByUid,
            operation = "REFUND_EXECUTE",
            prevState = origTx.status.name,
            newState = "REFUNDED"
        )

        sendNotification(
            title = "Refund Processed",
            body = "Refund of ${origTx.currency.code} ${(origTx.amountMinorUnits / 100.0)} credited.",
            categoryStr = "WALLET"
        )

        return Result.success(refundTx)
    }

    override suspend fun requestReversal(request: ReversalRequest): Result<FinancialTransaction> {
        idempotencyCache[request.idempotencyKey]?.let { return Result.success(it) }

        val origTx = _transactions.value.find { it.transactionId == request.originalTransactionId }
            ?: return Result.failure(IllegalArgumentException("Original transaction not found"))

        val now = getCurrentTimestamp()
        val reversedOrigTx = origTx.copy(
            status = FinancialTransactionStatus.REVERSED,
            updatedAt = now
        )

        _transactions.update { list ->
            list.map { if (it.transactionId == origTx.transactionId) reversedOrigTx else it }
        }

        val reversalTxId = "tx_rev_${UUID.randomUUID().toString().take(8)}"
        val reversalTx = FinancialTransaction(
            transactionId = reversalTxId,
            userId = origTx.userId,
            walletId = origTx.walletId,
            type = FinancialTransactionType.ADJUSTMENT,
            currency = origTx.currency,
            amountMinorUnits = origTx.amountMinorUnits,
            feeMinorUnits = 0L,
            status = FinancialTransactionStatus.COMPLETED,
            idempotencyKey = request.idempotencyKey,
            reference = "REV-${origTx.reference}",
            description = "Reversal compensating record for ${origTx.transactionId}: ${request.reason}",
            createdAt = now,
            updatedAt = now,
            completedAt = now
        )

        _transactions.update { listOf(reversalTx) + it }
        idempotencyCache[request.idempotencyKey] = reversalTx

        logAuditRecord(
            txId = reversalTxId,
            ledgerIds = emptyList(),
            actorUid = request.requestedByUid,
            operation = "REVERSAL_EXECUTE",
            prevState = origTx.status.name,
            newState = "REVERSED"
        )

        sendNotification(
            title = "Transaction Reversed",
            body = "Reversal executed for ${origTx.transactionId}.",
            categoryStr = "SECURITY"
        )

        return Result.success(reversalTx)
    }

    override suspend fun calculateFee(
        type: FinancialTransactionType,
        amountMinorUnits: Long,
        currency: Currency
    ): Result<FeeModel> {
        val feeMinorUnits = when (type) {
            FinancialTransactionType.DEPOSIT -> 0L
            FinancialTransactionType.WITHDRAWAL -> Math.round(amountMinorUnits * 0.01).coerceAtLeast(30L)
            FinancialTransactionType.TRANSFER -> 0L
            FinancialTransactionType.TRADE -> Math.round(amountMinorUnits * 0.001)
            else -> 0L
        }

        return Result.success(
            FeeModel(
                transactionType = type,
                currency = currency,
                feeAmountMinorUnits = feeMinorUnits,
                percentageFee = if (type == FinancialTransactionType.WITHDRAWAL) 1.0 else 0.0,
                effectiveDate = getCurrentTimestamp()
            )
        )
    }

    private suspend fun sendNotification(title: String, body: String, categoryStr: String) {
        val category = try { NotificationCategory.valueOf(categoryStr) } catch (_: Exception) { NotificationCategory.WALLET }
        val notif = NotificationModel(
            id = "notif_${UUID.randomUUID().toString().take(8)}",
            title = title,
            body = body,
            category = category,
            timestamp = "Just now",
            isRead = false,
            deepLink = "transaction_history"
        )
        notificationRepository.saveNotification(notif)
    }

    private fun updateWalletBalances(walletId: String, transform: (WalletBalanceModel) -> WalletBalanceModel) {
        _balances.update { currentMap ->
            val existing = currentMap[walletId] ?: WalletBalanceModel(walletId, "usr_9901", Currency.USD, 0L)
            val updated = transform(existing)
            currentMap + (walletId to updated)
        }
    }

    private fun logAuditRecord(txId: String, ledgerIds: List<String>, actorUid: String, operation: String, prevState: String, newState: String) {
        val record = FinancialAuditRecord(
            auditRecordId = "aud_fin_${UUID.randomUUID().toString().take(8)}",
            transactionId = txId,
            ledgerEntryIds = ledgerIds,
            actorUid = actorUid,
            operation = operation,
            timestamp = getCurrentTimestamp(),
            previousState = prevState,
            newState = newState,
            backendReference = "BACKEND-LEDGER-V1"
        )
        auditRecords.add(record)
    }

    private fun getCurrentTimestamp(): String {
        return SimpleDateFormat("yyyy-MM-dd HH:mm:ss 'UTC'", Locale.US).format(Date())
    }
}
