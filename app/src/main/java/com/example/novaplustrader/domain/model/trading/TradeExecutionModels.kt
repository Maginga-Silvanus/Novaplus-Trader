package com.example.novaplustrader.domain.model.trading

import com.example.novaplustrader.ui.model.Currency
import com.example.novaplustrader.ui.screens.trading.models.TradeDirection

enum class TradeOrderStatus(val label: String) {
    REQUESTED("Requested"),
    VALIDATING("Validating"),
    ACCEPTED("Accepted"),
    EXECUTED("Executed"),
    OPEN("Open"),
    SETTLING("Settling"),
    SETTLED("Settled"),
    REJECTED("Rejected"),
    FAILED("Failed"),
    CANCELLED("Cancelled")
}

enum class TradeResult(val label: String) {
    WIN("Win"),
    LOSS("Loss"),
    TIE("Tie"),
    PENDING("Pending Settlement")
}

data class TradeOrder(
    val orderId: String,
    val userId: String,
    val asset: String,
    val direction: TradeDirection,
    val requestedAmountMinorUnits: Long,
    val currency: Currency = Currency.USD,
    val durationSeconds: Int = 60,
    val requestTimestamp: String,
    val status: TradeOrderStatus = TradeOrderStatus.REQUESTED,
    val idempotencyKey: String,
    val executionPriceMinorUnits: Long? = null,
    val executionTimestamp: String? = null,
    val settlementPriceMinorUnits: Long? = null,
    val settlementTimestamp: String? = null,
    val result: TradeResult = TradeResult.PENDING,
    val profitLossMinorUnits: Long = 0L,
    val feeMinorUnits: Long = 0L,
    val backendReference: String? = null
) {
    val requestedAmountDouble: Double
        get() = requestedAmountMinorUnits / 100.0

    val executionPriceDouble: Double?
        get() = executionPriceMinorUnits?.let { it / 100.0 }

    val settlementPriceDouble: Double?
        get() = settlementPriceMinorUnits?.let { it / 100.0 }

    val profitLossDouble: Double
        get() = profitLossMinorUnits / 100.0

    val feeDouble: Double
        get() = feeMinorUnits / 100.0
}
