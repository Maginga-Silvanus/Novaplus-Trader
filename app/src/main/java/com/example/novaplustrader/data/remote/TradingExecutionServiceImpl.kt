package com.example.novaplustrader.data.remote

import com.example.novaplustrader.domain.model.trading.TradeOrder
import com.example.novaplustrader.domain.model.trading.TradeOrderStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf

class TradingExecutionServiceImpl : TradingExecutionService {
    private val openTrades = MutableStateFlow<List<TradeOrder>>(emptyList())
    private val settledTrades = MutableStateFlow<List<TradeOrder>>(emptyList())

    override fun observeOpenTrades(): StateFlow<List<TradeOrder>> = openTrades.asStateFlow()
    override fun observeSettledTrades(): StateFlow<List<TradeOrder>> = settledTrades.asStateFlow()
    override fun observeTradeById(orderId: String): Flow<TradeOrder?> = flowOf(null)

    override suspend fun submitTradeOrder(order: TradeOrder): Result<TradeOrder> =
        Result.failure(
            IllegalStateException(
                "Trade execution is not configured. No order was submitted or filled (${TradeOrderStatus.REJECTED.label})."
            )
        )
}
