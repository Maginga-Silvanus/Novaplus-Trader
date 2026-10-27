package com.example.novaplustrader

import com.example.novaplustrader.data.remote.BotExecutionServiceImpl
import com.example.novaplustrader.domain.model.bot.BotActivityLogModel
import com.example.novaplustrader.domain.model.bot.BotActivityType
import com.example.novaplustrader.domain.model.bot.BotPerformanceModel
import com.example.novaplustrader.domain.model.bot.TradingBotModel
import com.example.novaplustrader.domain.model.bot.TradingBotStatus
import com.example.novaplustrader.ui.model.Currency
import com.example.novaplustrader.ui.screens.bot.models.BotRiskLevel
import com.example.novaplustrader.ui.screens.bot.models.BotStrategy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first

class BotExecutionTest {

    @Test
    fun testTradingBotModelAllocationPrecision() {
        val bot = TradingBotModel(
            botId = "bot_test_01",
            userId = "usr_001",
            name = "Nova Test Bot",
            status = TradingBotStatus.RUNNING,
            strategy = BotStrategy.MOMENTUM,
            tradingPair = "BTC/USDT",
            allocationMinorUnits = 50000L, // $500.00
            currency = Currency.USD,
            riskLevel = BotRiskLevel.HIGH,
            maxOpenTrades = 5,
            cooldownSeconds = 30
        )

        assertEquals(500.00, bot.allocationDouble, 0.001)
        assertEquals(TradingBotStatus.RUNNING, bot.status)
        assertEquals("BTC/USDT", bot.tradingPair)
        assertEquals(BotStrategy.MOMENTUM, bot.strategy)
    }

    @Test
    fun testBotPerformanceCalculations() {
        val perf = BotPerformanceModel(
            totalTrades = 20,
            winningTrades = 15,
            losingTrades = 5,
            winRate = 75.0,
            realizedProfitLossMinorUnits = 5250L, // +$52.50
            unrealizedProfitLossMinorUnits = 0L,
            totalFeesMinorUnits = 150L,
            currentExposureMinorUnits = 20000L // $200.00
        )

        assertEquals(52.50, perf.realizedProfitLossDouble, 0.001)
        assertEquals(1.50, perf.totalFeesDouble, 0.001)
        assertEquals(200.00, perf.currentExposureDouble, 0.001)
        assertEquals(75.0, perf.winRate, 0.001)
    }

    @Test
    fun testBotActivityLogAndStatusLabels() {
        val log = BotActivityLogModel(
            activityId = "act_test_101",
            botId = "bot_test_01",
            timestamp = "12:00:00",
            type = BotActivityType.STARTED,
            message = "Bot worker started",
            status = "SUCCESS"
        )

        assertEquals(BotActivityType.STARTED, log.type)
        assertEquals("Bot Started", log.type.label)
        assertNotNull(log.timestamp)
        assertTrue(log.message.contains("started"))
    }

    @Test
    fun botServiceStartsAndStopsSuccessfully() = runBlocking {
        val service = BotExecutionServiceImpl()

        val start = service.requestStartBot("bot_test", "idempotency-key", null)
        assertTrue(start.isSuccess)
        assertEquals(TradingBotStatus.RUNNING, service.observeBot("bot_test").value.status)

        val stop = service.requestStopBot("bot_test", "idempotency-key")
        assertTrue(stop.isSuccess)
        assertEquals(TradingBotStatus.STOPPED, service.observeBot("bot_test").value.status)
        assertTrue(service.observeActivityLogs("bot_test").first().isNotEmpty())
    }
}
