package com.example.novaplustrader

import com.example.novaplustrader.domain.model.trading.MarketTick
import com.example.novaplustrader.domain.model.trading.TradeOrder
import com.example.novaplustrader.domain.model.trading.TradeOrderStatus
import com.example.novaplustrader.domain.model.trading.TradeResult
import com.example.novaplustrader.ui.model.Currency
import com.example.novaplustrader.ui.screens.trading.models.TradeDirection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TradingExecutionTest {

    @Test
    fun testMarketTickConversionAndPrecision() {
        val priceDouble = 88450.75
        val minorUnits = MarketTick.toMinorUnits(priceDouble)
        assertEquals(8845075L, minorUnits)

        val tick = MarketTick(
            asset = "BTC/USDT",
            bidMinorUnits = 8845000L,
            askMinorUnits = 8845100L,
            lastPriceMinorUnits = 8845075L,
            timestamp = "10:15:00",
            sequenceNumber = 1001L,
            isStale = false
        )

        assertEquals(88450.75, tick.lastPriceDouble, 0.001)
        assertEquals(88450.00, tick.bidDouble, 0.001)
        assertEquals(88451.00, tick.askDouble, 0.001)
    }

    @Test
    fun preciseProviderQuoteIsPreservedForPortfolioValuation() {
        val tick = MarketTick(
            asset = "SOL/USDT",
            bidMinorUnits = 15123L,
            askMinorUnits = 15124L,
            lastPriceMinorUnits = 15124L,
            timestamp = "10:15:00",
            preciseLastPriceUsd = 151.234567
        )

        assertEquals(151.234567, tick.lastPriceDouble, 0.0000001)
    }

    @Test
    fun testTradeOrderDomainModelAndCalculations() {
        val order = TradeOrder(
            orderId = "trd_test_101",
            userId = "usr_001",
            asset = "ETH/USDT",
            direction = TradeDirection.BUY,
            requestedAmountMinorUnits = 10000L, // $100.00
            currency = Currency.USD,
            durationSeconds = 60,
            requestTimestamp = "10:20:00",
            status = TradeOrderStatus.EXECUTED,
            idempotencyKey = "idemp_trd_test_key_101",
            executionPriceMinorUnits = 338050L, // $3,380.50
            executionTimestamp = "10:20:01",
            result = TradeResult.WIN,
            profitLossMinorUnits = 8500L // +$85.00
        )

        assertEquals(100.00, order.requestedAmountDouble, 0.001)
        assertEquals(3380.50, order.executionPriceDouble!!, 0.001)
        assertEquals(85.00, order.profitLossDouble, 0.001)
        assertEquals(TradeOrderStatus.EXECUTED, order.status)
        assertEquals(TradeResult.WIN, order.result)
        assertTrue(order.idempotencyKey.isNotBlank())
    }

    @Test
    fun testTradeOrderStatusLabels() {
        val status = TradeOrderStatus.REQUESTED
        assertEquals("Requested", status.label)
        assertNotNull(TradeOrderStatus.SETTLED.label)
    }
}
