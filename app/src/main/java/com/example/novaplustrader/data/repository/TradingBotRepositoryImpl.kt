package com.example.novaplustrader.data.repository

import com.example.novaplustrader.data.remote.BotExecutionService
import com.example.novaplustrader.data.remote.BotExecutionServiceImpl
import com.example.novaplustrader.domain.model.bot.BotActivityLogModel
import com.example.novaplustrader.domain.model.bot.BotPerformanceModel
import com.example.novaplustrader.domain.model.bot.BotSessionHistoryModel
import com.example.novaplustrader.domain.model.bot.TradingBotModel
import com.example.novaplustrader.domain.model.bot.TradingBotStatus
import com.example.novaplustrader.ui.screens.bot.models.BotActiveTrade
import com.example.novaplustrader.ui.screens.bot.models.BotActivityEvent
import com.example.novaplustrader.ui.screens.bot.models.BotConfiguration
import com.example.novaplustrader.ui.screens.bot.models.BotPerformance
import com.example.novaplustrader.ui.screens.bot.models.BotStatus
import com.example.novaplustrader.ui.screens.notifications.models.NotificationCategory
import com.example.novaplustrader.ui.screens.notifications.models.NotificationModel
import com.example.novaplustrader.ui.screens.trading.models.TradeDirection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class TradingBotRepositoryImpl(
    private val executionService: BotExecutionService = BotExecutionServiceImpl(),
    private val notificationRepository: NotificationRepository = NotificationRepositoryImpl()
) : TradingBotRepository {

    private val scope = CoroutineScope(Dispatchers.Default)

    private val _botModel = MutableStateFlow(TradingBotModel())
    private val _legacyStatus = MutableStateFlow(BotStatus.OFF)
    private val _legacyConfig = MutableStateFlow(BotConfiguration())
    private val _legacyPerf = MutableStateFlow(BotPerformance())
    private val _legacyHistory = MutableStateFlow<List<BotActivityEvent>>(emptyList())
    private val _legacyActiveTrades = MutableStateFlow<List<BotActiveTrade>>(
        listOf(
            BotActiveTrade(
                id = "trd_bot_01",
                assetSymbol = "BTC/USDT",
                direction = TradeDirection.BUY,
                entryPrice = 88400.00,
                currentPrice = 88450.00,
                amount = 100.00,
                profitLoss = 12.50,
                status = "ACTIVE"
            )
        )
    )

    init {
        scope.launch {
            executionService.observeBot("bot_primary_01").collect { bot ->
                _botModel.value = bot
                _legacyStatus.value = when (bot.status) {
                    TradingBotStatus.RUNNING -> BotStatus.ACTIVE
                    TradingBotStatus.STARTING, TradingBotStatus.START_REQUESTED -> BotStatus.STARTING
                    TradingBotStatus.STOPPING, TradingBotStatus.STOP_REQUESTED -> BotStatus.PAUSING
                    TradingBotStatus.ERROR -> BotStatus.ERROR
                    else -> BotStatus.OFF
                }
            }
        }
    }

    override fun observeBot(): StateFlow<TradingBotModel> = _botModel.asStateFlow()
    override fun observeActivityLogs(): Flow<List<BotActivityLogModel>> = executionService.observeActivityLogs("bot_primary_01")
    override fun observePerformance(): Flow<BotPerformanceModel> = executionService.observePerformance("bot_primary_01")
    override fun observeSessionHistory(): Flow<List<BotSessionHistoryModel>> = executionService.observeSessionHistory("bot_primary_01")

    override fun observeBotStatus(): StateFlow<BotStatus> = _legacyStatus.asStateFlow()
    override fun observeBotConfiguration(): StateFlow<BotConfiguration> = _legacyConfig.asStateFlow()
    override fun observeActiveTrades(): StateFlow<List<BotActiveTrade>> = _legacyActiveTrades.asStateFlow()
    override fun observeBotPerformance(): StateFlow<BotPerformance> = _legacyPerf.asStateFlow()
    override fun observeBotHistory(): StateFlow<List<BotActivityEvent>> = _legacyHistory.asStateFlow()

    override fun updateConfiguration(configuration: BotConfiguration) {
        _legacyConfig.value = configuration
    }

    override suspend fun startBot(securityPinVerified: Boolean): Result<Boolean> {
        val key = "idemp_bot_start_${UUID.randomUUID().toString().take(8)}"
        val result = requestStartBot(key, null)
        return result.map { true }
    }

    override suspend fun stopBot(): Result<Boolean> {
        val key = "idemp_bot_stop_${UUID.randomUUID().toString().take(8)}"
        val result = requestStopBot(key)
        return result.map { true }
    }

    override suspend fun pauseBot(): Result<Boolean> {
        return stopBot()
    }

    override suspend fun resumeBot(): Result<Boolean> {
        return startBot(true)
    }

    override suspend fun requestStartBot(
        idempotencyKey: String,
        securityPin: String?
    ): Result<TradingBotModel> {
        val result = executionService.requestStartBot("bot_primary_01", idempotencyKey, securityPin)
        result.onSuccess {
            // Dispatch notification via notification repository
            notificationRepository.saveNotification(
                NotificationModel(
                    id = "notif_bot_start_${System.currentTimeMillis()}",
                    title = "Trading Bot Started",
                    body = "Nova Momentum Alpha is now active and monitoring market signals.",
                    category = NotificationCategory.BOT,
                    timestamp = "Just now",
                    deepLink = "trading_bot"
                )
            )
        }
        return result
    }

    override suspend fun requestStopBot(idempotencyKey: String): Result<TradingBotModel> {
        val result = executionService.requestStopBot("bot_primary_01", idempotencyKey)
        result.onSuccess {
            notificationRepository.saveNotification(
                NotificationModel(
                    id = "notif_bot_stop_${System.currentTimeMillis()}",
                    title = "Trading Bot Stopped",
                    body = "Nova Momentum Alpha has been stopped by user command.",
                    category = NotificationCategory.BOT,
                    timestamp = "Just now",
                    deepLink = "trading_bot"
                )
            )
        }
        return result
    }
}
