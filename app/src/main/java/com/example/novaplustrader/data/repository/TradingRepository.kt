package com.example.novaplustrader.data.repository

import com.example.novaplustrader.domain.model.trading.TradeOrder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface TradingRepository {
    suspend fun submitTradeOrder(order: TradeOrder): Result<TradeOrder>
    fun observeOpenTrades(): StateFlow<List<TradeOrder>>
    fun observeSettledTrades(): StateFlow<List<TradeOrder>>
    fun observeTradeById(orderId: String): Flow<TradeOrder?>
}
