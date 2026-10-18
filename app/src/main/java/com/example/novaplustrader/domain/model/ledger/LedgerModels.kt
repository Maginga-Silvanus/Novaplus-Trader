package com.example.novaplustrader.domain.model.ledger

import com.example.novaplustrader.ui.model.Currency

enum class LedgerDirection {
    DEBIT,
    CREDIT
}

enum class LedgerEntryType {
    DEPOSIT_CREDIT,
    WITHDRAWAL_DEBIT,
    TRANSFER_DEBIT,
    TRANSFER_CREDIT,
    FEE_DEBIT,
    TRADE_DEBIT,
    TRADE_CREDIT,
    REFUND_CREDIT,
    REVERSAL_COMPENSATING,
    ADJUSTMENT
}

enum class LedgerStatus {
    PENDING,
    COMMITTED,
    REVERSED
}

/**
 * Immutable Double-Entry Ledger Record.
 * All monetary amounts are represented in minor units (e.g. cents) as Long values
 * to prevent floating-point representation errors in authoritative calculations.
 */
data class FinancialLedgerEntry(
    val ledgerEntryId: String,
    val transactionId: String,
    val userId: String,
    val walletId: String,
    val entryType: LedgerEntryType,
    val currency: Currency,
    val amountMinorUnits: Long,
    val direction: LedgerDirection,
    val balanceBeforeMinorUnits: Long,
    val balanceAfterMinorUnits: Long,
    val status: LedgerStatus = LedgerStatus.PENDING,
    val reference: String,
    val createdAt: String,
    val processedAt: String? = null
) {
    val amountDouble: Double
        get() = amountMinorUnits / 100.0

    val balanceBeforeDouble: Double
        get() = balanceBeforeMinorUnits / 100.0

    val balanceAfterDouble: Double
        get() = balanceAfterMinorUnits / 100.0

    companion object {
        fun toMinorUnits(amountDouble: Double): Long {
            return Math.round(amountDouble * 100.0)
        }
    }
}

enum class FinancialTransactionType {
    DEPOSIT,
    WITHDRAWAL,
    TRANSFER,
    TRADE,
    FEE,
    REFUND,
    ADJUSTMENT
}

enum class FinancialTransactionStatus {
    CREATED,
    SUBMITTED,
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED,
    CANCELLED,
    REVERSED
}

/**
 * Authoritative Financial Transaction Domain Model.
 */
data class FinancialTransaction(
    val transactionId: String,
    val userId: String,
    val walletId: String,
    val type: FinancialTransactionType,
    val currency: Currency,
    val amountMinorUnits: Long,
    val feeMinorUnits: Long = 0L,
    val netAmountMinorUnits: Long = amountMinorUnits - feeMinorUnits,
    val status: FinancialTransactionStatus = FinancialTransactionStatus.CREATED,
    val idempotencyKey: String,
    val reference: String,
    val providerReference: String? = null,
    val description: String = "",
    val createdAt: String,
    val updatedAt: String,
    val completedAt: String? = null,
    val failureReason: String? = null
) {
    val amountDouble: Double
        get() = amountMinorUnits / 100.0

    val feeDouble: Double
        get() = feeMinorUnits / 100.0

    val netAmountDouble: Double
        get() = netAmountMinorUnits / 100.0
}

/**
 * Granular Wallet Balance Model.
 */
data class WalletBalanceModel(
    val walletId: String,
    val userId: String,
    val currency: Currency,
    val availableBalanceMinorUnits: Long,
    val pendingBalanceMinorUnits: Long = 0L,
    val lockedBalanceMinorUnits: Long = 0L
) {
    val totalBalanceMinorUnits: Long
        get() = availableBalanceMinorUnits + pendingBalanceMinorUnits + lockedBalanceMinorUnits

    val availableBalance: Double
        get() = availableBalanceMinorUnits / 100.0

    val pendingBalance: Double
        get() = pendingBalanceMinorUnits / 100.0

    val lockedBalance: Double
        get() = lockedBalanceMinorUnits / 100.0

    val totalBalance: Double
        get() = totalBalanceMinorUnits / 100.0
}

/**
 * Authoritative Fee Structure Model.
 */
data class FeeModel(
    val transactionType: FinancialTransactionType,
    val currency: Currency,
    val feeAmountMinorUnits: Long,
    val percentageFee: Double = 0.0,
    val minFeeMinorUnits: Long = 0L,
    val maxFeeMinorUnits: Long = Long.MAX_VALUE,
    val effectiveDate: String
) {
    val feeAmountDouble: Double
        get() = feeAmountMinorUnits / 100.0
}

/**
 * Immutable Financial Audit Trail Record.
 */
data class FinancialAuditRecord(
    val auditRecordId: String,
    val transactionId: String,
    val ledgerEntryIds: List<String>,
    val actorUid: String,
    val operation: String,
    val timestamp: String,
    val previousState: String,
    val newState: String,
    val backendReference: String
)
