package com.example.novaplustrader.ui.screens.trading

import com.example.novaplustrader.ui.model.Currency
import com.example.novaplustrader.ui.screens.trading.models.Timeframe
import com.example.novaplustrader.ui.screens.trading.models.TradeDirection
import com.example.novaplustrader.ui.screens.trading.models.TradeDuration
import com.example.novaplustrader.ui.screens.trading.models.TradingAsset

data class TradingUiState(
    val isLoading: Boolean = false,
    val availableBalance: Double = 6324.49,
    val currency: Currency = Currency.USD,
    val selectedAsset: TradingAsset = defaultAssetList[0],
    val availableAssets: List<TradingAsset> = defaultAssetList,
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
        currentPrice = 88450.00,
        change24hPercentage = 3.42,
        high24h = 89200.00,
        low24h = 86100.00,
        volume24h = "$2.8B",
        payoutPercentage = 88,
        chartDataMap = mapOf(
            Timeframe.M1 to listOf(88100f, 88250f, 88180f, 88300f, 88290f, 88400f, 88450f),
            Timeframe.M5 to listOf(87500f, 87800f, 88000f, 87900f, 88200f, 88450f),
            Timeframe.M15 to listOf(86800f, 87200f, 87600f, 88000f, 88450f),
            Timeframe.H1 to listOf(85000f, 86200f, 87000f, 87800f, 88450f),
            Timeframe.H4 to listOf(84000f, 85500f, 86800f, 88450f),
            Timeframe.D1 to listOf(81000f, 83500f, 86000f, 88450f)
        )
    ),
    TradingAsset(
        symbol = "ETH/USDT",
        name = "Ethereum",
        currentPrice = 3380.50,
        change24hPercentage = -1.85,
        high24h = 3490.00,
        low24h = 3320.00,
        volume24h = "$1.4B",
        payoutPercentage = 85,
        chartDataMap = mapOf(
            Timeframe.M1 to listOf(3410f, 3400f, 3390f, 3385f, 3380.5f),
            Timeframe.M5 to listOf(3450f, 3420f, 3400f, 3380.5f),
            Timeframe.M15 to listOf(3480f, 3440f, 3400f, 3380.5f),
            Timeframe.H1 to listOf(3500f, 3460f, 3410f, 3380.5f),
            Timeframe.H4 to listOf(3550f, 3480f, 3380.5f),
            Timeframe.D1 to listOf(3600f, 3500f, 3380.5f)
        )
    ),
    TradingAsset(
        symbol = "SOL/USDT",
        name = "Solana",
        currentPrice = 212.80,
        change24hPercentage = 6.15,
        high24h = 218.00,
        low24h = 198.50,
        volume24h = "$840M",
        payoutPercentage = 87,
        chartDataMap = mapOf(
            Timeframe.M1 to listOf(202f, 205f, 208f, 210f, 212.8f),
            Timeframe.M5 to listOf(199f, 203f, 207f, 212.8f),
            Timeframe.M15 to listOf(195f, 201f, 208f, 212.8f),
            Timeframe.H1 to listOf(190f, 198f, 206f, 212.8f),
            Timeframe.H4 to listOf(185f, 196f, 212.8f),
            Timeframe.D1 to listOf(175f, 190f, 212.8f)
        )
    ),
    TradingAsset(
        symbol = "BNB/USDT",
        name = "BNB Chain",
        currentPrice = 645.20,
        change24hPercentage = 0.95,
        high24h = 652.00,
        low24h = 638.00,
        volume24h = "$420M",
        payoutPercentage = 84,
        chartDataMap = mapOf(
            Timeframe.M1 to listOf(640f, 642f, 643f, 645.2f),
            Timeframe.M5 to listOf(638f, 641f, 645.2f),
            Timeframe.M15 to listOf(636f, 640f, 645.2f),
            Timeframe.H1 to listOf(632f, 638f, 645.2f),
            Timeframe.H4 to listOf(628f, 635f, 645.2f),
            Timeframe.D1 to listOf(620f, 632f, 645.2f)
        )
    )
)
