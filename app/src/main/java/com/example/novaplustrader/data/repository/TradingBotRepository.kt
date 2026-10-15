package com.example.novaplustrader.data.repository

import com.example.novaplustrader.domain.model.bot.BotActivityLogModel
import com.example.novaplustrader.domain.model.bot.BotPerformanceModel
import com.example.novaplustrader.domain.model.bot.BotSessionHistoryModel
import com.example.novaplustrader.domain.model.bot.TradingBotModel
import com.example.novaplustrader.ui.screens.bot.models.BotActiveTrade
import com.example.novaplustrader.ui.screens.bot.models.BotActivityEvent
import com.example.novaplustrader.ui.screens.bot.models.BotConfiguration
import com.example.novaplustrader.ui.screens.bot.models.BotPerformance
import com.example.novaplustrader.ui.screens.bot.models.BotStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface TradingBotRepository {
    fun observeBot(): StateFlow<TradingBotModel>
    fun observeActivityLogs(): Flow<List<BotActivityLogModel>>
    fun observePerformance(): Flow<BotPerformanceModel>
    fun observeSessionHistory(): Flow<List<BotSessionHistoryModel>>

    // Legacy interface support for backward compatibility with UI components
    fun observeBotStatus(): StateFlow<BotStatus>
    fun observeBotConfiguration(): StateFlow<BotConfiguration>
    fun observeActiveTrades(): StateFlow<List<BotActiveTrade>>
    fun observeBotPerformance(): StateFlow<BotPerformance>
    fun observeBotHistory(): StateFlow<List<BotActivityEvent>>

    fun updateConfiguration(configuration: BotConfiguration)
    suspend fun startBot(securityPinVerified: Boolean): Result<Boolean>
    suspend fun stopBot(): Result<Boolean>
    suspend fun pauseBot(): Result<Boolean>
    suspend fun resumeBot(): Result<Boolean>

    // Authoritative execution commands
    suspend fun requestStartBot(idempotencyKey: String, securityPin: String?): Result<TradingBotModel>
    suspend fun requestStopBot(idempotencyKey: String): Result<TradingBotModel>
}
