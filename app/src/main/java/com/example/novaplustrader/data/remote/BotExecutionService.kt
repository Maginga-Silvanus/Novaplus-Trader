package com.example.novaplustrader.data.remote

import com.example.novaplustrader.domain.model.bot.BotActivityLogModel
import com.example.novaplustrader.domain.model.bot.BotPerformanceModel
import com.example.novaplustrader.domain.model.bot.BotSessionHistoryModel
import com.example.novaplustrader.domain.model.bot.TradingBotModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface BotExecutionService {
    fun observeBot(botId: String): StateFlow<TradingBotModel>
    fun observeActivityLogs(botId: String): Flow<List<BotActivityLogModel>>
    fun observePerformance(botId: String): Flow<BotPerformanceModel>
    fun observeSessionHistory(botId: String): Flow<List<BotSessionHistoryModel>>

    suspend fun requestStartBot(
        botId: String,
        idempotencyKey: String,
        securityPin: String?
    ): Result<TradingBotModel>

    suspend fun requestStopBot(
        botId: String,
        idempotencyKey: String
    ): Result<TradingBotModel>

    suspend fun updateBotConfiguration(
        bot: TradingBotModel
    ): Result<TradingBotModel>
}
