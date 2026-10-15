package com.example.novaplustrader.data.repository

import com.example.novaplustrader.data.remote.TradingExecutionService
import com.example.novaplustrader.data.remote.TradingExecutionServiceImpl
import com.example.novaplustrader.domain.model.trading.TradeOrder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

class TradingRepositoryImpl(
    private val executionService: TradingExecutionService = TradingExecutionServiceImpl()
) : TradingRepository {

    override suspend fun submitTradeOrder(order: TradeOrder): Result<TradeOrder> {
        return executionService.submitTradeOrder(order)
    }

    override fun observeOpenTrades(): StateFlow<List<TradeOrder>> {
        return executionService.observeOpenTrades()
    }

    override fun observeSettledTrades(): StateFlow<List<TradeOrder>> {
        return executionService.observeSettledTrades()
    }

    override fun observeTradeById(orderId: String): Flow<TradeOrder?> {
        return executionService.observeTradeById(orderId)
    }
}
