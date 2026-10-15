package com.example.novaplustrader.data.repository

import com.example.novaplustrader.data.remote.MarketDataService
import com.example.novaplustrader.data.remote.MarketDataServiceImpl
import com.example.novaplustrader.domain.model.trading.MarketCandle
import com.example.novaplustrader.domain.model.trading.MarketConnectionState
import com.example.novaplustrader.domain.model.trading.MarketStatus
import com.example.novaplustrader.domain.model.trading.MarketSummary
import com.example.novaplustrader.domain.model.trading.MarketTick
import com.example.novaplustrader.ui.screens.trading.models.Timeframe
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

class MarketDataRepositoryImpl(
    private val marketDataService: MarketDataService = MarketDataServiceImpl()
) : MarketDataRepository {

    override fun observeMarketTicks(asset: String): Flow<MarketTick> {
        return marketDataService.observeMarketTicks(asset)
    }

    override fun observeMarkets(): Flow<List<MarketSummary>> = marketDataService.observeMarkets()

    override fun observeMarketCandles(asset: String, timeframe: Timeframe): Flow<List<MarketCandle>> {
        return marketDataService.observeMarketCandles(asset, timeframe)
    }

    override fun observeMarketStatus(asset: String): StateFlow<MarketStatus> {
        return marketDataService.observeMarketStatus(asset)
    }

    override fun observeConnectionState(): StateFlow<MarketConnectionState> {
        return marketDataService.observeConnectionState()
    }
}
