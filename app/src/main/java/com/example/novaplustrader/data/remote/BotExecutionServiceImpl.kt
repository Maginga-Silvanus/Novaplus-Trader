package com.example.novaplustrader.data.remote

import com.example.novaplustrader.domain.model.bot.BotActivityLogModel
import com.example.novaplustrader.domain.model.bot.BotPerformanceModel
import com.example.novaplustrader.domain.model.bot.BotSessionHistoryModel
import com.example.novaplustrader.domain.model.bot.TradingBotModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BotExecutionServiceImpl : BotExecutionService {

    private val _bot = MutableStateFlow(
        TradingBotModel(
            botId = "",
            userId = "",
            name = "Trading Bot",
            allocationMinorUnits = 0L
        )
    )
    private val _performance = MutableStateFlow(BotPerformanceModel())
    private val _activityLogs = MutableStateFlow<List<BotActivityLogModel>>(emptyList())
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
        return Result.failure(
            IllegalStateException("Trading bot execution backend is not configured. No bot was started.")
        )
    }

    override suspend fun requestStopBot(
        botId: String,
        idempotencyKey: String
    ): Result<TradingBotModel> {
        return Result.failure(
            IllegalStateException("Trading bot execution backend is not configured. No bot status was changed.")
        )
    }

    override suspend fun updateBotConfiguration(bot: TradingBotModel): Result<TradingBotModel> {
        return Result.failure(
            IllegalStateException("Trading bot configuration backend is not configured. Configuration was not saved.")
        )
    }
}
