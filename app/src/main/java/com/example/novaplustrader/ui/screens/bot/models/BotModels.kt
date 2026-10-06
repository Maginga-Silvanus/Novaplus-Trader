package com.example.novaplustrader.ui.screens.bot.models

import com.example.novaplustrader.ui.screens.trading.models.TradeDirection
import com.example.novaplustrader.ui.screens.trading.models.TradingAsset
import com.example.novaplustrader.ui.screens.trading.TradingUiState
import com.example.novaplustrader.ui.screens.trading.defaultAssetList

enum class BotStatus(val label: String) {
    OFF("Bot Offline"),
    STARTING("Starting..."),
    ACTIVE("Bot Active"),
    PAUSING("Pausing..."),
    STOPPED("Bot Stopped"),
    ERROR("Connection Error")
}

enum class BotStrategy(
    val title: String,
    val description: String
) {
    CONSERVATIVE(
        title = "Conservative",
        description = "Lower-risk configuration intended for reduced market exposure."
    ),
    BALANCED(
        title = "Balanced",
        description = "Moderate exposure balancing trade frequency and risk parameters."
    ),
    MOMENTUM(
        title = "Momentum",
        description = "Designed around momentum-based algorithmic market indicators."
    ),
    CUSTOM(
        title = "Custom",
        description = "Allows manual configuration of entry, stop-loss, and exit thresholds."
    )
}

enum class BotRiskLevel(
    val title: String,
    val exposureText: String
) {
    LOW(title = "LOW", exposureText = "Lower exposure"),
    MEDIUM(title = "MEDIUM", exposureText = "Moderate exposure"),
    HIGH(title = "HIGH", exposureText = "Higher exposure")
}

data class BotConfiguration(
    val asset: TradingAsset = defaultAssetListFallback,
    val strategy: BotStrategy = BotStrategy.BALANCED,
    val riskLevel: BotRiskLevel = BotRiskLevel.MEDIUM,
    val allocatedAmount: Double = 500.00,
    val entryThresholdPercent: Double = 1.5,
    val stopLossPercent: Double = 2.0,
    val takeProfitPercent: Double = 4.0,
    val maxOpenTrades: Int = 3,
    val cooldownPeriodSeconds: Int = 60,
    val maxSessionDurationHours: Int = 24
)

private val defaultAssetListFallback = defaultAssetList[0]

data class BotPerformance(
    val sessionProfitLoss: Double = 42.80,
    val totalTrades: Int = 18,
    val winningTrades: Int = 14,
    val losingTrades: Int = 4,
    val winRatePercentage: Double = 77.8,
    val currentExposure: Double = 350.00,
    val isMockData: Boolean = true
)

data class BotActiveTrade(
    val id: String,
    val assetSymbol: String,
    val direction: TradeDirection,
    val entryPrice: Double,
    val currentPrice: Double,
    val amount: Double,
    val profitLoss: Double,
    val status: String = "ACTIVE"
)

data class BotActivityEvent(
    val id: String,
    val eventType: String,
    val description: String,
    val timestamp: String,
    val status: String = "INFO"
)
