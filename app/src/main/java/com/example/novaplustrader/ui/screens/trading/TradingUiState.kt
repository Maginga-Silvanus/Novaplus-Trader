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
    val walletMode: String = "live",
    val tradeSuccessMessage: String? = null,
    val pendingTradeRequest: SpotTradeRequest? = null,
    val recentTradeRequests: List<SpotTradeRecord> = emptyList(),
    val userId: String? = null,
    val currency: Currency = Currency.USD,
    val selectedAsset: TradingAsset = defaultAssetList[0],
    val availableAssets: List<TradingAsset> = defaultAssetList,
    val marketConnectionState: MarketConnectionState = MarketConnectionState.CONNECTED,
    val marketDataError: String? = null,
    val marketSummaries: List<MarketSummary> = emptyList(),
    val marketIsLoading: Boolean = false, // Silent background refresh
    val marketDataTimestamp: String? = null,
    val isMarketPriceStale: Boolean = false,
    val selectedTimeframe: Timeframe = Timeframe.M1,
    val selectedDirection: TradeDirection = TradeDirection.BUY,
    val tradeAmountString: String = "10.00",
    val selectedPercentage: Int? = null,
    val selectedDuration: TradeDuration = TradeDuration.ONE_MIN,
    val selectedChartType: String = "Candlestick",
    val validationError: String? = null,
    val isAssetPickerOpen: Boolean = false,
    val isConfirmSheetOpen: Boolean = false,
    val isSuccessDialogOpen: Boolean = false
)

val defaultAssetList = listOf(
    TradingAsset(
        symbol = "BTC/USDT",
        name = "Bitcoin",
        currentPrice = 65000.0,
        change24hPercentage = 2.45,
        high24h = 66200.0,
        low24h = 64100.0,
        volume24h = "1.2B",
        payoutPercentage = 88,
        candleDataMap = emptyMap()
    ),
    TradingAsset(
        symbol = "ETH/USDT",
        name = "Ethereum",
        currentPrice = 3500.0,
        change24hPercentage = 1.82,
        high24h = 3580.0,
        low24h = 3420.0,
        volume24h = "850M",
        payoutPercentage = 85,
        candleDataMap = emptyMap()
    ),
    TradingAsset(
        symbol = "SOL/USDT",
        name = "Solana",
        currentPrice = 145.0,
        change24hPercentage = 5.12,
        high24h = 150.0,
        low24h = 138.0,
        volume24h = "420M",
        payoutPercentage = 87,
        candleDataMap = emptyMap()
    ),
    TradingAsset(
        symbol = "BNB/USDT",
        name = "BNB Chain",
        currentPrice = 580.0,
        change24hPercentage = -0.45,
        high24h = 590.0,
        low24h = 575.0,
        volume24h = "210M",
        payoutPercentage = 84,
        candleDataMap = emptyMap()
    ),
    TradingAsset(
        symbol = "XRP/USDT",
        name = "Ripple",
        currentPrice = 0.58,
        change24hPercentage = 1.20,
        high24h = 0.60,
        low24h = 0.57,
        volume24h = "180M",
        payoutPercentage = 82,
        candleDataMap = emptyMap()
    ),
    TradingAsset(
        symbol = "ADA/USDT",
        name = "Cardano",
        currentPrice = 0.45,
        change24hPercentage = -1.10,
        high24h = 0.47,
        low24h = 0.44,
        volume24h = "95M",
        payoutPercentage = 80,
        candleDataMap = emptyMap()
    ),
    TradingAsset(
        symbol = "DOGE/USDT",
        name = "Dogecoin",
        currentPrice = 0.12,
        change24hPercentage = 8.40,
        high24h = 0.13,
        low24h = 0.11,
        volume24h = "550M",
        payoutPercentage = 85,
        candleDataMap = emptyMap()
    ),
    TradingAsset(
        symbol = "DOT/USDT",
        name = "Polkadot",
        currentPrice = 6.80,
        change24hPercentage = 0.90,
        high24h = 7.00,
        low24h = 6.70,
        volume24h = "75M",
        payoutPercentage = 81,
        candleDataMap = emptyMap()
    )
)
