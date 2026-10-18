package com.example.novaplustrader.domain.model.bot

import com.example.novaplustrader.ui.model.Currency
import com.example.novaplustrader.ui.screens.bot.models.BotRiskLevel
import com.example.novaplustrader.ui.screens.bot.models.BotStrategy

enum class TradingBotStatus(val label: String) {
    CREATED("Created"),
    CONFIGURED("Configured"),
    START_REQUESTED("Start Requested"),
    STARTING("Starting Execution..."),
    RUNNING("Running Active"),
    STOP_REQUESTED("Stop Requested"),
    STOPPING("Stopping Execution..."),
    STOPPED("Stopped"),
    ERROR("Execution Error")
}

enum class BotActivityType(val label: String) {
    STARTED("Bot Started"),
    STOPPED("Bot Stopped"),
    SIGNAL("Signal Detected"),
    TRADE_REQUEST("Trade Requested"),
    TRADE_EXECUTED("Trade Executed"),
    TRADE_REJECTED("Trade Rejected"),
    RISK_BLOCK("Risk Limit Blocked"),
    ERROR("Error Event"),
    SYSTEM("System Notification")
}

data class TradingBotModel(
    val botId: String = "",
    val userId: String = "",
    val name: String = "Trading Bot",
    val status: TradingBotStatus = TradingBotStatus.STOPPED,
    val strategy: BotStrategy = BotStrategy.BALANCED,
    val tradingPair: String = "BTC/USDT",
    val allocationMinorUnits: Long = 0L,
    val currency: Currency = Currency.USD,
    val riskLevel: BotRiskLevel = BotRiskLevel.MEDIUM,
    val maxOpenTrades: Int = 3,
    val cooldownSeconds: Int = 60,
    val stopLossPercentage: Double = 2.0,
    val takeProfitPercentage: Double = 4.0,
    val entryThresholdPercentage: Double = 1.5,
    val maxSessionDurationHours: Int = 24,
    val createdAt: String = "",
    val updatedAt: String = "",
    val startedAt: String? = null,
    val stoppedAt: String? = null,
    val lastActivityAt: String? = null,
    val errorState: String? = null
) {
    val allocationDouble: Double
        get() = allocationMinorUnits / 100.0
}

data class BotActivityLogModel(
    val activityId: String,
    val botId: String,
    val timestamp: String,
    val type: BotActivityType,
    val message: String,
    val asset: String? = null,
    val signal: String? = null,
    val decision: String? = null,
    val tradeId: String? = null,
    val status: String = "INFO"
)

data class BotPerformanceModel(
    val totalTrades: Int = 0,
    val winningTrades: Int = 0,
    val losingTrades: Int = 0,
    val winRate: Double = 0.0,
    val realizedProfitLossMinorUnits: Long = 0L,
    val unrealizedProfitLossMinorUnits: Long = 0L,
    val totalFeesMinorUnits: Long = 0L,
    val currentExposureMinorUnits: Long = 0L,
    val runtimeFormatted: String = "0m"
) {
    val realizedProfitLossDouble: Double
        get() = realizedProfitLossMinorUnits / 100.0

    val unrealizedProfitLossDouble: Double
        get() = unrealizedProfitLossMinorUnits / 100.0

    val totalFeesDouble: Double
        get() = totalFeesMinorUnits / 100.0

    val currentExposureDouble: Double
        get() = currentExposureMinorUnits / 100.0
}

data class BotSessionHistoryModel(
    val sessionId: String,
    val botId: String,
    val strategy: BotStrategy,
    val startTime: String,
    val stopTime: String,
    val durationFormatted: String,
    val totalTrades: Int,
    val winRate: Double,
    val realizedProfitLossMinorUnits: Long,
    val reasonStopped: String
) {
    val realizedProfitLossDouble: Double
        get() = realizedProfitLossMinorUnits / 100.0
}
