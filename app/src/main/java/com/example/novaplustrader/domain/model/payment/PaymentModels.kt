package com.example.novaplustrader.domain.model.payment

import com.example.novaplustrader.domain.model.ledger.FinancialTransactionStatus
import com.example.novaplustrader.ui.model.Currency

enum class PaymentProviderType(val displayName: String) {
    MPESA("M-Pesa Express"),
    BANK_WIRE("Bank Wire Transfer"),
    DEBIT_CREDIT_CARD("Debit / Credit Card"),
    CRYPTO("Crypto Vault Deposit")
}

enum class PaymentOperation {
    DEPOSIT,
    WITHDRAWAL
}

enum class PaymentProviderStatus {
    OPERATIONAL,
    DEGRADED,
    MAINTENANCE,
    OFFLINE
}

data class PaymentProviderModel(
    val providerId: String,
    val providerName: String,
    val type: PaymentProviderType,
    val supportedCurrencies: List<Currency>,
    val supportedOperations: List<PaymentOperation>,
    val status: PaymentProviderStatus = PaymentProviderStatus.OPERATIONAL
)

data class PaymentRequest(
    val requestId: String,
    val userId: String,
    val walletId: String,
    val providerType: PaymentProviderType,
    val operation: PaymentOperation,
    val amountMinorUnits: Long,
    val currency: Currency,
    val idempotencyKey: String,
    val destinationOrPhone: String,
    val status: FinancialTransactionStatus = FinancialTransactionStatus.CREATED,
    val createdAt: String,
    val updatedAt: String
)

enum class PaymentReconciliationStatus(val label: String) {
    MATCHED("Matched"),
    MISMATCH("Amount Mismatch"),
    PENDING_REVIEW("Pending Compliance Review"),
    RESOLVED("Reconciled")
}

data class PaymentLimitModel(
    val minDepositMinorUnits: Long = 1000L,       // KES 10 / $0.10
    val maxDepositMinorUnits: Long = 70000000L,   // KES 700,000 / $7,000
    val minWithdrawalMinorUnits: Long = 10000L,   // KES 100 / $1.00
    val maxWithdrawalMinorUnits: Long = 25000000L,// KES 250,000 / $2,500
    val dailyDepositLimitMinorUnits: Long = 100000000L, // KES 1,000,000
    val dailyWithdrawalLimitMinorUnits: Long = 50000000L // KES 500,000
) {
    val minDepositDouble: Double get() = minDepositMinorUnits / 100.0
    val maxDepositDouble: Double get() = maxDepositMinorUnits / 100.0
    val minWithdrawalDouble: Double get() = minWithdrawalMinorUnits / 100.0
    val maxWithdrawalDouble: Double get() = maxWithdrawalMinorUnits / 100.0
}

enum class PaymentError(val userMessage: String) {
    INVALID_AMOUNT("Invalid monetary amount entered."),
    INVALID_PHONE("Invalid Kenyan phone number format. Use 07XXXXXXXX, 01XXXXXXXX, or 254XXXXXXXXX."),
    LIMIT_EXCEEDED("Requested amount exceeds single transaction or daily limit."),
    INSUFFICIENT_FUNDS("Insufficient available wallet balance for this operation."),
    PROVIDER_UNAVAILABLE("The requested payment provider is currently under maintenance or offline."),
    PAYMENT_DECLINED("Payment declined by M-Pesa or issuing bank."),
    PAYMENT_TIMEOUT("Payment request timed out waiting for user confirmation."),
    DUPLICATE_REQUEST("Duplicate payment request detected."),
    TRANSACTION_NOT_FOUND("The specified transaction ID was not found."),
    VERIFICATION_FAILED("Security PIN verification failed."),
    UNKNOWN_ERROR("An unexpected payment error occurred. Please contact support.")
}
