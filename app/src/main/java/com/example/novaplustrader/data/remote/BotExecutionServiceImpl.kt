package com.example.novaplustrader.data.remote

import com.example.novaplustrader.domain.model.bot.BotActivityLogModel
import com.example.novaplustrader.domain.model.bot.BotActivityType
import com.example.novaplustrader.domain.model.bot.BotPerformanceModel
import com.example.novaplustrader.domain.model.bot.BotSessionHistoryModel
import com.example.novaplustrader.domain.model.bot.TradingBotModel
import com.example.novaplustrader.domain.model.bot.TradingBotStatus
import com.example.novaplustrader.ui.screens.bot.models.BotStrategy
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BotExecutionServiceImpl : BotExecutionService {

    private val _bot = MutableStateFlow(TradingBotModel())
    private val _performance = MutableStateFlow(BotPerformanceModel())

    private val _activityLogs = MutableStateFlow<List<BotActivityLogModel>>(
        listOf(
            BotActivityLogModel(
                activityId = "act_101",
                botId = "bot_primary_01",
                timestamp = "10:14:02",
                type = BotActivityType.STARTED,
                message = "Bot worker initialized on backend execution cluster",
                status = "SUCCESS"
            ),
            BotActivityLogModel(
                activityId = "act_102",
                botId = "bot_primary_01",
                timestamp = "10:15:00",
                type = BotActivityType.SIGNAL,
                message = "Momentum signal detected on BTC/USDT (MACD Crossover)",
                asset = "BTC/USDT",
                signal = "MACD_BULLISH",
                status = "INFO"
            ),
            BotActivityLogModel(
                activityId = "act_103",
                botId = "bot_primary_01",
                timestamp = "10:15:01",
                type = BotActivityType.TRADE_EXECUTED,
                message = "Executed BUY order on BTC/USDT at $88,400.00",
                asset = "BTC/USDT",
                tradeId = "trd_8801",
                status = "SUCCESS"
            )
        )
    )

    private val _sessionHistory = MutableStateFlow<List<BotSessionHistoryModel>>(
        listOf(
            BotSessionHistoryModel(
                sessionId = "sess_901",
                botId = "bot_primary_01",
                strategy = BotStrategy.BALANCED,
                startTime = "Yesterday, 08:00",
                stopTime = "Yesterday, 22:15",
                durationFormatted = "14h 15m",
                totalTrades = 18,
                winRate = 77.8,
                realizedProfitLossMinorUnits = 4280L, // +$42.80
                reasonStopped = "User Requested Stop"
            ),
            BotSessionHistoryModel(
                sessionId = "sess_902",
                botId = "bot_primary_01",
                strategy = BotStrategy.CONSERVATIVE,
                startTime = "10 Oct, 09:00",
                stopTime = "10 Oct, 18:00",
                durationFormatted = "9h 00m",
                totalTrades = 10,
                winRate = 80.0,
                realizedProfitLossMinorUnits = 2850L, // +$28.50
                reasonStopped = "Session Max Duration Reached"
            )
        )
    )

    override fun observeBot(botId: String): StateFlow<TradingBotModel> = _bot.asStateFlow()
    override fun observeActivityLogs(botId: String): Flow<List<BotActivityLogModel>> = _activityLogs.asStateFlow()
    override fun observePerformance(botId: String): Flow<BotPerformanceModel> = _performance.asStateFlow()
    override fun observeSessionHistory(botId: String): Flow<List<BotSessionHistoryModel>> = _sessionHistory.asStateFlow()

    override suspend fun requestStartBot(
        botId: String,
        idempotencyKey: String,
        securityPin: String?
    ): Result<TradingBotModel> {
        delay(800) // Backend risk engine validation & worker cluster spawn

        val time = getFormattedTime()
        val updated = _bot.value.copy(
            status = TradingBotStatus.RUNNING,
            startedAt = time,
            lastActivityAt = time,
            errorState = null
        )

        _bot.value = updated

        val startLog = BotActivityLogModel(
            activityId = "act_${System.currentTimeMillis().toString().takeLast(6)}",
            botId = botId,
            timestamp = time,
            type = BotActivityType.STARTED,
            message = "Bot worker started successfully with allocation $${updated.allocationDouble}",
            status = "SUCCESS"
        )

        _activityLogs.update { listOf(startLog) + it }
        return Result.success(updated)
    }

    override suspend fun requestStopBot(
        botId: String,
        idempotencyKey: String
    ): Result<TradingBotModel> {
        delay(600) // Backend worker shutdown & trade settlement finalization

        val time = getFormattedTime()
        val updated = _bot.value.copy(
            status = TradingBotStatus.STOPPED,
            stoppedAt = time,
            lastActivityAt = time
        )

        _bot.value = updated

        val stopLog = BotActivityLogModel(
            activityId = "act_${System.currentTimeMillis().toString().takeLast(6)}",
            botId = botId,
            timestamp = time,
            type = BotActivityType.STOPPED,
            message = "Bot worker gracefully stopped by user command",
            status = "INFO"
        )

        _activityLogs.update { listOf(stopLog) + it }
        return Result.success(updated)
    }

    override suspend fun updateBotConfiguration(bot: TradingBotModel): Result<TradingBotModel> {
        delay(400)
        _bot.value = bot
        return Result.success(bot)
    }

    private fun getFormattedTime(): String {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.US)
        return sdf.format(Date())
    }
}
