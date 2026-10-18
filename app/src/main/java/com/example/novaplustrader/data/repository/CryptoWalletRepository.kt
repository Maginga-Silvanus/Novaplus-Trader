package com.example.novaplustrader.data.repository

import com.example.novaplustrader.ui.screens.trading.models.TradeDirection
import kotlinx.coroutines.flow.Flow

data class CryptoSubWallet(
    val id: String,
    val name: String,
    val purpose: String,
    val assetSymbol: String,
    val balance: Double,
    val receiveAddress: String
)

data class CryptoWalletSnapshot(
    val bitcoinWalletId: String,
    val marketingWalletId: String = "",
    val tradingWalletId: String = "",
    val assetBalances: Map<String, Double>,
    val mode: String,
    val paperTradeFeeBps: Int = 10,
    val minimumTradeUsdMinorUnits: Long = 100L,
    val subWallets: List<CryptoSubWallet> = emptyList()
) {
    val isPaperTrading: Boolean get() = mode == "paper"
}

data class SpotTradeRequest(
    val idempotencyKey: String,
    val asset: String,
    val pair: String,
    val direction: TradeDirection,
    val amount: Double,
    val amountDenomination: String,
    val quotePriceUsd: Double,
    val limitPriceUsd: Double,
    val quoteObservedAtMs: Long
)

data class SpotTradeResponse(
    val status: String,
    val message: String?,
    val receipt: SpotTradeReceipt? = null
)

data class SpotTradeReceipt(
    val idempotencyKey: String,
    val asset: String,
    val direction: String,
    val quantityAtomicUnits: Long,
    val executionPriceUsd: Double,
    val grossAmountMinorUnits: Long,
    val feeMinorUnits: Long,
    val netAmountMinorUnits: Long,
    val createdAtMs: Long,
    val status: String
)

data class SpotTradeRecord(
    val idempotencyKey: String,
    val asset: String,
    val direction: String,
    val amount: Double,
    val status: String,
    val message: String?,
    val quantity: Double = 0.0,
    val executionPriceUsd: Double? = null,
    val feeAmountUsd: Double = 0.0
)

interface CryptoWalletRepository {
    suspend fun ensureWallet(userId: String): Result<CryptoWalletSnapshot>
    fun observeWallet(userId: String): Flow<CryptoWalletSnapshot>
    fun observeTradeRequests(userId: String): Flow<List<SpotTradeRecord>>
    suspend fun submitSpotTradeRequest(request: SpotTradeRequest): Result<SpotTradeResponse>
}
