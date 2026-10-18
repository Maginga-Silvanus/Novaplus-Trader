package com.example.novaplustrader

import com.example.novaplustrader.data.remote.BinanceMarketParser
import com.example.novaplustrader.data.remote.BinanceRetryPolicy
import com.example.novaplustrader.data.remote.MarketReconnectTracker
import com.example.novaplustrader.domain.model.trading.MarketCandle
import com.example.novaplustrader.domain.model.trading.MarketRanking
import com.example.novaplustrader.domain.model.trading.MarketRankings
import com.example.novaplustrader.domain.model.trading.MarketSummary
import com.example.novaplustrader.ui.screens.trading.models.Timeframe
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class BinanceMarketParserTest {
    @Test
    fun parsesSupportedTradingSpotMetadataAndRejectsUnsupportedOrInactivePairs() {
        val response = JSONObject().put(
            "symbols",
            JSONArray()
                .put(symbolInfo("BTCUSDT", true))
                .put(symbolInfo("ETHUSDT", true))
                .put(symbolInfo("SOLUSDT", false))
                .put(JSONObject().put("symbol", "BTCBUSD").put("status", "TRADING")
                    .put("quoteAsset", "BUSD").put("isSpotTradingAllowed", true))
        )

        assertEquals(setOf("BTCUSDT", "ETHUSDT"), BinanceMarketParser.parseExchangeInfo(response.toString()))
    }

    @Test
    fun exposesEightSupportedSpotPairsAndFiltersExchangeMetadataAgainstThatSet() {
        val expected = setOf(
            "BTCUSDT", "ETHUSDT", "SOLUSDT", "BNBUSDT",
            "XRPUSDT", "ADAUSDT", "DOGEUSDT", "LTCUSDT"
        )
        val response = JSONObject().put(
            "symbols",
            JSONArray().apply {
                expected.forEach { put(symbolInfo(it, true)) }
                put(symbolInfo("SHIBUSDT", true))
            }
        )

        assertEquals(expected, BinanceMarketParser.supportedSymbols)
        assertEquals(expected, BinanceMarketParser.parseExchangeInfo(response.toString()))
        assertEquals(expected.size, BinanceMarketParser.supportedAssets.size)
    }

    @Test
    fun parsesRestTickerAndWebSocketTickerIntoProviderValues() {
        val restTicker = JSONObject()
            .put("symbol", "BTCUSDT")
            .put("lastPrice", "60123.456789")
            .put("bidPrice", "60123.45")
            .put("askPrice", "60123.46")
            .put("priceChangePercent", "2.50")
            .put("highPrice", "62000")
            .put("lowPrice", "59000")
            .put("volume", "100.25")
            .put("quoteVolume", "6100000.50")
            .put("closeTime", 1_700_000_000_000L)
        val tick = BinanceMarketParser.parseTicker(restTicker.toString(), "BTCUSDT")

        assertEquals("BTC/USDT", tick.asset)
        assertEquals(60123.456789, tick.lastPriceDouble, 0.0000001)
        assertEquals(2.5, tick.change24hPercentage!!, 0.0)
        assertEquals(100.25, tick.baseVolume24h!!, 0.0)
        assertEquals(6_100_000.50, tick.quoteVolume24h!!, 0.0)
        assertEquals(1_700_000_000_000L, tick.eventTimeMs)

        val websocketTicker = JSONObject()
            .put("s", "ETHUSDT")
            .put("c", "3200.125")
            .put("b", "3200")
            .put("a", "3200.25")
            .put("P", "-1.25")
            .put("h", "3300")
            .put("l", "3100")
            .put("v", "50")
            .put("q", "160000")
            .put("E", 1_700_000_001_000L)
        assertEquals(3200.125, BinanceMarketParser.parseTicker(websocketTicker.toString(), "ETHUSDT").lastPriceDouble, 0.0)
    }

    @Test
    fun parsesTickerArraysAndRejectsMissingOrMalformedProviderData() {
        val ticker = JSONObject()
            .put("symbol", "SOLUSDT")
            .put("lastPrice", "150")
            .put("priceChangePercent", "4")
            .put("highPrice", "155")
            .put("lowPrice", "140")
            .put("volume", "2000")
            .put("quoteVolume", "300000")
            .put("closeTime", 1234)
        val markets = BinanceMarketParser.parseTickerArray(JSONArray().put(ticker).toString())
        assertEquals(1, markets.size)
        assertEquals("Solana", markets.single().name)
        assertEquals("SOL/USDT", markets.single().pair)

        assertTrue(BinanceMarketParser.parseTickerArray("[]").isEmpty())
        assertThrows(Exception::class.java) {
            BinanceMarketParser.parseTickerArray("[{\"symbol\":\"BTCUSDT\"}]")
        }
        assertThrows(Exception::class.java) {
            BinanceMarketParser.parseTicker("{", "BTCUSDT")
        }
        assertThrows(IllegalArgumentException::class.java) {
            BinanceMarketParser.parseTicker(ticker.toString(), "BTCUSDT")
        }
    }

    @Test
    fun parsesOhlcvWithUtcCandleBoundariesAndFinalization() {
        val open = 1_700_000_040_000L
        val response = JSONArray()
            .put(kline(open, open + Timeframe.M1.intervalMs - 1, "10", "13", "9", "12", "5.25", "60.5"))
            .put(kline(open + Timeframe.M1.intervalMs, open + 2 * Timeframe.M1.intervalMs - 1, "12", "14", "11", "13", "7", "90"))
        val candles = BinanceMarketParser.parseRestCandles(
            response.toString(),
            "BTC/USDT",
            Timeframe.M1,
            open + Timeframe.M1.intervalMs + 5_000L
        )

        assertEquals(listOf(open, open + Timeframe.M1.intervalMs), candles.map { it.openTimeMs })
        assertEquals(10.0, candles.first().openPrice, 0.0)
        assertEquals(13.0, candles.first().highPrice, 0.0)
        assertEquals(9.0, candles.first().lowPrice, 0.0)
        assertEquals(12.0, candles.first().closePrice, 0.0)
        assertEquals(5.25, candles.first().baseVolume, 0.0)
        assertEquals(60.5, candles.first().quoteVolume, 0.0)
        assertTrue(candles.first().isFinal)
        assertFalse(candles.last().isFinal)
    }

    @Test
    fun rejectsEmptyMalformedMisalignedAndInconsistentCandles() {
        assertEquals(emptyList<MarketCandle>(), BinanceMarketParser.parseRestCandles("[]", "BTC/USDT", Timeframe.M1, 1))
        assertThrows(Exception::class.java) {
            BinanceMarketParser.parseRestCandles("[[1,\"2\"]]", "BTC/USDT", Timeframe.M1, 1)
        }
        assertThrows(IllegalArgumentException::class.java) {
            BinanceMarketParser.parseRestCandles(
                JSONArray().put(kline(1, 60_000, "10", "12", "9", "11", "1", "10")).toString(),
                "BTC/USDT",
                Timeframe.M1,
                100_000
            )
        }
        assertThrows(IllegalArgumentException::class.java) {
            BinanceMarketParser.parseRestCandles(
                JSONArray().put(kline(0, 59_999, "10", "9", "8", "11", "1", "10")).toString(),
                "BTC/USDT",
                Timeframe.M1,
                100_000
            )
        }
    }

    @Test
    fun allSupportedIntervalsUseExactUtcCandleBoundaries() {
        Timeframe.entries.forEach { timeframe ->
            val openTime = 0L
            val closeTime = timeframe.intervalMs - 1
            val parsed = BinanceMarketParser.parseRestCandles(
                JSONArray().put(kline(openTime, closeTime, "1", "2", "0.5", "1.5", "3", "4")).toString(),
                "BTC/USDT",
                timeframe,
                closeTime + 1
            )
            assertEquals(timeframe, parsed.single().timeframe)
            assertTrue(parsed.single().isFinal)
        }
    }

    @Test
    fun websocketKlinesPreserveOhlcAndMergeDuplicateOutOfOrderEvents() {
        val first = websocketKline(60_000, 119_999, 1_001, "10", "14", "9", "12", "2", "24", false)
        val duplicateLater = websocketKline(60_000, 119_999, 1_002, "10", "15", "8", "13", "3", "39", false)
        val duplicateOlder = websocketKline(60_000, 119_999, 1_000, "10", "99", "1", "99", "99", "99", false)
        val next = websocketKline(120_000, 179_999, 1_003, "13", "16", "12", "15", "4", "60", true)

        var candles = BinanceMarketParser.mergeCandles(emptyList(), BinanceMarketParser.parseWebSocketCandle(first, "BTC/USDT", Timeframe.M1))
        candles = BinanceMarketParser.mergeCandles(candles, BinanceMarketParser.parseWebSocketCandle(duplicateLater, "BTC/USDT", Timeframe.M1))
        candles = BinanceMarketParser.mergeCandles(candles, BinanceMarketParser.parseWebSocketCandle(duplicateOlder, "BTC/USDT", Timeframe.M1))
        candles = BinanceMarketParser.mergeCandles(candles, BinanceMarketParser.parseWebSocketCandle(next, "BTC/USDT", Timeframe.M1))

        assertEquals(2, candles.size)
        assertEquals(listOf(60_000L, 120_000L), candles.map { it.openTimeMs })
        assertEquals(15.0, candles.first().highPrice, 0.0)
        assertEquals(8.0, candles.first().lowPrice, 0.0)
        assertEquals(13.0, candles.first().closePrice, 0.0)
        assertEquals(3.0, candles.first().baseVolume, 0.0)
        assertTrue(candles.last().isFinal)
    }

    @Test
    fun ranksOnlyByProviderChangeAndComparableQuoteVolume() {
        val markets = listOf(
            market("BTC", change = 2.0, volume = 1_000.0),
            market("ETH", change = -6.0, volume = 50.0),
            market("SOL", change = 4.0, volume = 2_000.0)
        )
        assertEquals(listOf("ETH", "SOL", "BTC"), MarketRankings.sort(markets, MarketRanking.HOT).map { it.baseAsset })
        assertEquals(listOf("SOL", "BTC", "ETH"), MarketRankings.sort(markets, MarketRanking.GAINERS).map { it.baseAsset })
        assertEquals(listOf("ETH", "BTC", "SOL"), MarketRankings.sort(markets, MarketRanking.LOSERS).map { it.baseAsset })
        assertEquals(listOf("SOL", "BTC", "ETH"), MarketRankings.sort(markets, MarketRanking.VOLUME).map { it.baseAsset })
    }

    @Test
    fun retryPolicyUsesBoundedBackoffAndHonorsBinanceRetryAfter() {
        assertEquals(2_000L, BinanceRetryPolicy.nextBackoffMillis(1_000L))
        assertEquals(30_000L, BinanceRetryPolicy.nextBackoffMillis(30_000L))
        assertEquals(60_000L, BinanceRetryPolicy.delayMillis(2_000L, 60_000L))
        assertEquals(300_000L, BinanceRetryPolicy.delayMillis(2_000L, 900_000L))
        assertEquals(4_000L, BinanceRetryPolicy.delayMillis(4_000L))
    }

    @Test
    fun websocketDisconnectRetainsStaleStateAndReconnectResumesFreshData() {
        val reconnect = MarketReconnectTracker()

        assertEquals(com.example.novaplustrader.domain.model.trading.MarketConnectionState.DISCONNECTED, reconnect.onDisconnected())
        assertEquals(1_000L, reconnect.nextRetryDelay())
        assertEquals(2_000L, reconnect.nextRetryDelay())

        reconnect.onRestDataReceived()
        assertEquals(com.example.novaplustrader.domain.model.trading.MarketConnectionState.STALE, reconnect.onDisconnected())
        assertEquals(com.example.novaplustrader.domain.model.trading.MarketConnectionState.CONNECTED, reconnect.onConnected())
        assertEquals(1_000L, reconnect.nextRetryDelay())
    }

    @Test
    fun staleMarketSnapshotsRetainLastVerifiedQuoteForClearlyLabeledDisplay() {
        val fresh = market("BTC", change = 2.0, volume = 1_000.0)
        val stale = fresh.copy(isStale = true)

        assertTrue(stale.isStale)
        assertEquals(fresh.lastPrice, stale.lastPrice, 0.0)
        assertEquals(fresh.eventTimeMs, stale.eventTimeMs)
        assertEquals(fresh.quoteVolume24h, stale.quoteVolume24h, 0.0)
    }

    private fun symbolInfo(symbol: String, allowed: Boolean) = JSONObject()
        .put("symbol", symbol)
        .put("status", if (allowed) "TRADING" else "BREAK")
        .put("quoteAsset", "USDT")
        .put("isSpotTradingAllowed", allowed)

    private fun kline(
        openTime: Long,
        closeTime: Long,
        open: String,
        high: String,
        low: String,
        close: String,
        baseVolume: String,
        quoteVolume: String
    ) = JSONArray()
        .put(openTime).put(open).put(high).put(low).put(close)
        .put(baseVolume).put(closeTime).put(quoteVolume).put(0).put(0).put(0).put(0)

    private fun websocketKline(
        openTime: Long,
        closeTime: Long,
        eventTime: Long,
        open: String,
        high: String,
        low: String,
        close: String,
        baseVolume: String,
        quoteVolume: String,
        isFinal: Boolean
    ) = JSONObject()
        .put("e", "kline")
        .put("E", eventTime)
        .put("s", "BTCUSDT")
        .put(
            "k",
            JSONObject()
                .put("t", openTime)
                .put("T", closeTime)
                .put("s", "BTCUSDT")
                .put("i", "1m")
                .put("o", open)
                .put("h", high)
                .put("l", low)
                .put("c", close)
                .put("v", baseVolume)
                .put("q", quoteVolume)
                .put("x", isFinal)
        )
        .toString()

    private fun market(base: String, change: Double, volume: Double) = MarketSummary(
        symbol = "${base}USDT",
        baseAsset = base,
        quoteAsset = "USDT",
        name = base,
        lastPrice = 1.0,
        priceChangePercent24h = change,
        highPrice24h = 2.0,
        lowPrice24h = 0.5,
        baseVolume24h = volume,
        quoteVolume24h = volume,
        eventTimeMs = 1L
    )
}
