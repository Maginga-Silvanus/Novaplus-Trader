package com.example.novaplustrader.ui.model

enum class TransactionType {
    PAYMENT,
    TRANSFER,
    DEPOSIT,
    WITHDRAWAL,
    CRYPTO_BUY,
    CRYPTO_SELL,
    TRADE,
    BET
}

enum class TransactionStatus {
    COMPLETED,
    SUBMITTED,
    PENDING,
    FAILED
}

data class TransactionUiModel(
    val id: String,
    val title: String,
    val subtitle: String,
    val type: TransactionType,
    val amount: Double,
    val currency: Currency,
    val isPositive: Boolean,
    val timestamp: String,
    val status: TransactionStatus = TransactionStatus.COMPLETED,
    val avatarUrl: String? = null,
    val categoryIconName: String? = null
)
