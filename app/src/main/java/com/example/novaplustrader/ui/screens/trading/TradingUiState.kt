package com.example.novaplustrader.ui.screens.trading

import com.example.novaplustrader.ui.model.Currency
import com.example.novaplustrader.ui.screens.trading.models.Timeframe
import com.example.novaplustrader.ui.screens.trading.models.TradeDirection
import com.example.novaplustrader.ui.screens.trading.models.TradeDuration
import com.example.novaplustrader.ui.screens.trading.models.TradingAsset
import com.example.novaplustrader.domain.model.trading.MarketConnectionState
import com.example.novaplustrader.data.repository.SpotTradeRequest
import com.example.novaplustrader.data.repository.SpotTradeRecord
import com.example.novaplustrader.domain.model.trading.MarketSummary

data class TradingUiState(
    val isLoading: Boolean = false,
    val availableBalance: Double = 0.0,
    val paperTradeFeeBps: Int = 10,
    val minimumTradeUsdMinorUnits: Long = 100L,
    val bitcoinWalletId: String? = null,
    val assetBalances: Map<String, Double> = emptyMap(),
    val walletMode: String = "disabled",
    val tradeSuccessMessage: String? = null,
    val pendingTradeRequest: SpotTradeRequest? = null,
    val recentTradeRequests: List<SpotTradeRecord> = emptyList(),
    val userId: String? = null,
    val currency: Currency = Currency.USD,
    val selectedAsset: TradingAsset = defaultAssetList[0],
    val availableAssets: List<TradingAsset> = defaultAssetList,
    val marketConnectionState: MarketConnectionState = MarketConnectionState.CONNECTING,
    val marketDataError: String? = null,
    val marketSummaries: List<MarketSummary> = emptyList(),
    val marketIsLoading: Boolean = true,
    val marketDataTimestamp: String? = null,
    val isMarketPriceStale: Boolean = true,
    val selectedTimeframe: Timeframe = Timeframe.M1,
    val selectedDirection: TradeDirection = TradeDirection.BUY,
    val tradeAmountString: String = "100",
    val selectedPercentage: Int? = null,
    val selectedDuration: TradeDuration = TradeDuration.ONE_MIN,
    val validationError: String? = null,
    val isAssetPickerOpen: Boolean = false,
    val isConfirmSheetOpen: Boolean = false,
    val isSuccessDialogOpen: Boolean = false
)

val defaultAssetList = listOf(
    TradingAsset(
        symbol = "BTC/USDT",
        name = "Bitcoin",
        currentPrice = 0.0,
        change24hPercentage = 0.0,
        high24h = 0.0,
        low24h = 0.0,
        volume24h = "--",
        payoutPercentage = 88,
        candleDataMap = emptyMap()
    ),
    TradingAsset(
        symbol = "ETH/USDT",
        name = "Ethereum",
        currentPrice = 0.0,
        change24hPercentage = 0.0,
        high24h = 0.0,
        low24h = 0.0,
        volume24h = "--",
        payoutPercentage = 85,
        candleDataMap = emptyMap()
    ),
    TradingAsset(
        symbol = "SOL/USDT",
        name = "Solana",
        currentPrice = 0.0,
        change24hPercentage = 0.0,
        high24h = 0.0,
        low24h = 0.0,
        volume24h = "--",
        payoutPercentage = 87,
        candleDataMap = emptyMap()
    ),
    TradingAsset(
        symbol = "BNB/USDT",
        name = "BNB Chain",
        currentPrice = 0.0,
        change24hPercentage = 0.0,
        high24h = 0.0,
        low24h = 0.0,
        volume24h = "--",
        payoutPercentage = 84,
        candleDataMap = emptyMap()
    )
)
