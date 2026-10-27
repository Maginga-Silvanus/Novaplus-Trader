package com.example.novaplustrader.data.remote

import com.example.novaplustrader.domain.model.bot.BotActivityLogModel
import com.example.novaplustrader.domain.model.bot.BotActivityType
import com.example.novaplustrader.domain.model.bot.BotPerformanceModel
import com.example.novaplustrader.domain.model.bot.BotSessionHistoryModel
import com.example.novaplustrader.domain.model.bot.TradingBotModel
import com.example.novaplustrader.domain.model.bot.TradingBotStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class BotExecutionServiceImpl : BotExecutionService {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

    private val _bot = MutableStateFlow(
        TradingBotModel(
            botId = "bot_active_01",
            userId = "",
            name = "Algorithmic Momentum Bot",
            status = TradingBotStatus.STOPPED,
            tradingPair = "BTC/USDT",
            allocationMinorUnits = 10_000L
        )
    )
    private val _performance = MutableStateFlow(
        BotPerformanceModel(
            totalTrades = 12,
            winningTrades = 9,
            losingTrades = 3,
            winRate = 75.0,
            realizedProfitLossMinorUnits = 4500L,
            runtimeFormatted = "1h 45m"
        )
    )
    private val _activityLogs = MutableStateFlow<List<BotActivityLogModel>>(
        listOf(
            BotActivityLogModel(
                activityId = "act_01",
                botId = "bot_active_01",
                timestamp = dateFormat.format(Date()),
                type = BotActivityType.SYSTEM,
                message = "Bot engine initialized and ready."
            )
        )
    )
    private val _sessionHistory = MutableStateFlow<List<BotSessionHistoryModel>>(emptyList())

    override fun observeBot(botId: String): StateFlow<TradingBotModel> = _bot.asStateFlow()
    override fun observeActivityLogs(botId: String): Flow<List<BotActivityLogModel>> = _activityLogs.asStateFlow()
    override fun observePerformance(botId: String): Flow<BotPerformanceModel> = _performance.asStateFlow()
    override fun observeSessionHistory(botId: String): Flow<List<BotSessionHistoryModel>> = _sessionHistory.asStateFlow()

    override suspend fun requestStartBot(
        botId: String,
        idempotencyKey: String,
        securityPin: String?
    ): Result<TradingBotModel> {
        val now = dateFormat.format(Date())
        val updatedBot = _bot.value.copy(
            botId = botId.ifBlank { "bot_active_01" },
            status = TradingBotStatus.RUNNING,
            startedAt = now,
            lastActivityAt = now,
            errorState = null
        )
        _bot.value = updatedBot

        val newLog = BotActivityLogModel(
            activityId = "act_${UUID.randomUUID().toString().take(8)}",
            botId = updatedBot.botId,
            timestamp = now,
            type = BotActivityType.STARTED,
            message = "Trading bot started successfully. Strategy: ${updatedBot.strategy.name}"
        )
        _activityLogs.value = listOf(newLog) + _activityLogs.value

        return Result.success(updatedBot)
    }

    override suspend fun requestStopBot(
        botId: String,
        idempotencyKey: String
    ): Result<TradingBotModel> {
        val now = dateFormat.format(Date())
        val updatedBot = _bot.value.copy(
            status = TradingBotStatus.STOPPED,
            stoppedAt = now,
            lastActivityAt = now
        )
        _bot.value = updatedBot

        val newLog = BotActivityLogModel(
            activityId = "act_${UUID.randomUUID().toString().take(8)}",
            botId = updatedBot.botId,
            timestamp = now,
            type = BotActivityType.STOPPED,
            message = "Trading bot execution stopped by user command."
        )
        _activityLogs.value = listOf(newLog) + _activityLogs.value

        return Result.success(updatedBot)
    }

    override suspend fun updateBotConfiguration(bot: TradingBotModel): Result<TradingBotModel> {
        _bot.value = bot
        return Result.success(bot)
    }
}
