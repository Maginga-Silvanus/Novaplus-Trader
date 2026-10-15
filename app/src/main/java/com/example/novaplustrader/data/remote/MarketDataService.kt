package com.example.novaplustrader.data.remote

import com.example.novaplustrader.domain.model.trading.MarketCandle
import com.example.novaplustrader.domain.model.trading.MarketConnectionState
import com.example.novaplustrader.domain.model.trading.MarketStatus
import com.example.novaplustrader.domain.model.trading.MarketSummary
import com.example.novaplustrader.domain.model.trading.MarketTick
import com.example.novaplustrader.ui.screens.trading.models.Timeframe
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface MarketDataService {
    fun observeMarketTicks(asset: String): Flow<MarketTick>
    fun observeMarkets(): Flow<List<MarketSummary>>
    fun observeMarketCandles(asset: String, timeframe: Timeframe): Flow<List<MarketCandle>>
    fun observeMarketStatus(asset: String): StateFlow<MarketStatus>
    fun observeConnectionState(): StateFlow<MarketConnectionState>
}
