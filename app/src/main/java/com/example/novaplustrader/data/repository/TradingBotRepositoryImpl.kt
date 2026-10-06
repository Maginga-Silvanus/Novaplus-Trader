package com.example.novaplustrader.data.repository

import com.example.novaplustrader.ui.screens.bot.models.BotActiveTrade
import com.example.novaplustrader.ui.screens.bot.models.BotActivityEvent
import com.example.novaplustrader.ui.screens.bot.models.BotConfiguration
import com.example.novaplustrader.ui.screens.bot.models.BotPerformance
import com.example.novaplustrader.ui.screens.bot.models.BotStatus
import com.example.novaplustrader.ui.screens.trading.models.TradeDirection
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TradingBotRepositoryImpl : TradingBotRepository {

    private val _botStatus = MutableStateFlow(BotStatus.OFF)
    private val _botConfiguration = MutableStateFlow(BotConfiguration())
    private val _botPerformance = MutableStateFlow(BotPerformance())
    private val _activeTrades = MutableStateFlow<List<BotActiveTrade>>(emptyList())
    private val _historyEvents = MutableStateFlow<List<BotActivityEvent>>(
        listOf(
            BotActivityEvent(
                id = "e1",
                eventType = "SYSTEM",
                description = "Nova Bot initialization completed",
                timestamp = getCurrentTime(),
                status = "COMPLETED"
            )
        )
    )

    override fun observeBotStatus(): StateFlow<BotStatus> = _botStatus.asStateFlow()
    override fun observeBotConfiguration(): StateFlow<BotConfiguration> = _botConfiguration.asStateFlow()
    override fun observeActiveTrades(): StateFlow<List<BotActiveTrade>> = _activeTrades.asStateFlow()
    override fun observeBotPerformance(): StateFlow<BotPerformance> = _botPerformance.asStateFlow()
    override fun observeBotHistory(): StateFlow<List<BotActivityEvent>> = _historyEvents.asStateFlow()

    override fun updateConfiguration(configuration: BotConfiguration) {
        _botConfiguration.value = configuration
    }

    override suspend fun startBot(securityPinVerified: Boolean): Result<Boolean> {
        if (!securityPinVerified) {
            return Result.failure(SecurityException("Security verification required to activate trading bot"))
        }

        _botStatus.value = BotStatus.STARTING
        delay(800) // Simulate backend validation & engine connection

        _botStatus.value = BotStatus.ACTIVE

        val newEvent = BotActivityEvent(
            id = "e_${System.currentTimeMillis()}",
            eventType = "START",
            description = "Nova Bot started with ${_botConfiguration.value.strategy.title} strategy on ${_botConfiguration.value.asset.symbol}",
            timestamp = getCurrentTime(),
            status = "ACTIVE"
        )
        _historyEvents.update { listOf(newEvent) + it }

        // Populate sample mock active trades for UI visualization
        _activeTrades.value = listOf(
            BotActiveTrade(
                id = "bt1",
                assetSymbol = _botConfiguration.value.asset.symbol,
                direction = TradeDirection.BUY,
                entryPrice = _botConfiguration.value.asset.currentPrice - 120.0,
                currentPrice = _botConfiguration.value.asset.currentPrice,
                amount = _botConfiguration.value.allocatedAmount * 0.4,
                profitLoss = 1.34,
                status = "ACTIVE"
            )
        )

        return Result.success(true)
    }

    override suspend fun stopBot(): Result<Boolean> {
        _botStatus.value = BotStatus.STOPPED
        delay(600)

        val newEvent = BotActivityEvent(
            id = "e_${System.currentTimeMillis()}",
            eventType = "STOP",
            description = "Nova Bot stopped by user",
            timestamp = getCurrentTime(),
            status = "STOPPED"
        )
        _historyEvents.update { listOf(newEvent) + it }
        _activeTrades.value = emptyList()
        _botStatus.value = BotStatus.OFF

        return Result.success(true)
    }

    override suspend fun pauseBot(): Result<Boolean> {
        _botStatus.value = BotStatus.PAUSING
        delay(500)
        _botStatus.value = BotStatus.OFF

        val newEvent = BotActivityEvent(
            id = "e_${System.currentTimeMillis()}",
            eventType = "PAUSE",
            description = "Nova Bot paused",
            timestamp = getCurrentTime(),
            status = "PAUSED"
        )
        _historyEvents.update { listOf(newEvent) + it }
        return Result.success(true)
    }

    override suspend fun resumeBot(): Result<Boolean> {
        return startBot(securityPinVerified = true)
    }

    private fun getCurrentTime(): String {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.US)
        return sdf.format(Date())
    }
}
