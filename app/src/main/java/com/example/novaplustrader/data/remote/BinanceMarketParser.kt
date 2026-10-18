package com.example.novaplustrader.data.remote

import com.example.novaplustrader.domain.model.trading.MarketCandle
import com.example.novaplustrader.domain.model.trading.MarketSummary
import com.example.novaplustrader.domain.model.trading.MarketTick
import com.example.novaplustrader.ui.screens.trading.models.Timeframe
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object BinanceMarketParser {
    val supportedAssets = linkedMapOf(
        "BTC" to "Bitcoin",
        "ETH" to "Ethereum",
        "SOL" to "Solana",
        "BNB" to "BNB",
        "XRP" to "XRP",
        "ADA" to "Cardano",
        "DOGE" to "Dogecoin",
        "LTC" to "Litecoin"
    )

    val supportedSymbols = supportedAssets.keys.map { "${it}USDT" }.toSet()

    fun parseExchangeInfo(json: String): Set<String> {
        val symbols = JSONObject(json).getJSONArray("symbols")
        return buildSet {
            for (index in 0 until symbols.length()) {
                val symbol = symbols.getJSONObject(index)
                val name = symbol.getString("symbol")
                if (name in supportedSymbols &&
                    symbol.getString("status") == "TRADING" &&
                    symbol.getString("quoteAsset") == "USDT" &&
                    symbol.optBoolean("isSpotTradingAllowed", false)
                ) add(name)
            }
        }
    }

    fun parseTicker(json: String, expectedSymbol: String): MarketTick {
        val ticker = JSONObject(json)
        require(ticker.string("s", "symbol") == expectedSymbol) { "Ticker symbol did not match the requested pair" }
        val baseAsset = baseAsset(expectedSymbol)
        val last = positive(ticker.string("c", "lastPrice"), "last price")
        val bid = positive(ticker.string("b", "bidPrice"), "bid price")
        val ask = positive(ticker.string("a", "askPrice"), "ask price")
        val eventTime = ticker.long("E", "closeTime").takeIf { it > 0 } ?: System.currentTimeMillis()
        return MarketTick(
            asset = "$baseAsset/USDT",
            bidMinorUnits = MarketTick.toMinorUnits(bid),
            askMinorUnits = MarketTick.toMinorUnits(ask),
            lastPriceMinorUnits = MarketTick.toMinorUnits(last),
            timestamp = formatTimestamp(eventTime),
            source = "Binance Spot market data",
            change24hPercentage = finite(ticker.string("P", "priceChangePercent"), "24-hour change"),
            high24h = positive(ticker.string("h", "highPrice"), "24-hour high"),
            low24h = positive(ticker.string("l", "lowPrice"), "24-hour low"),
            quoteVolume24h = positiveOrZero(ticker.string("q", "quoteVolume"), "quote volume"),
            preciseLastPriceUsd = last,
            baseVolume24h = positiveOrZero(ticker.string("v", "volume"), "base volume"),
            eventTimeMs = eventTime
        )
    }

    fun parseTickerArray(json: String, allowedSymbols: Set<String> = supportedSymbols): List<MarketSummary> {
        val tickers = JSONArray(json)
        val results = mutableListOf<MarketSummary>()
        for (index in 0 until tickers.length()) {
            val ticker = tickers.getJSONObject(index)
            val symbol = ticker.string("s", "symbol")
            if (symbol !in allowedSymbols) continue
            val base = baseAsset(symbol)
            results += MarketSummary(
                symbol = symbol,
                baseAsset = base,
                quoteAsset = "USDT",
                name = supportedAssets[base] ?: base,
                lastPrice = positive(ticker.string("c", "lastPrice"), "last price"),
                priceChangePercent24h = finite(ticker.string("P", "priceChangePercent"), "24-hour change"),
                highPrice24h = positive(ticker.string("h", "highPrice"), "24-hour high"),
                lowPrice24h = positive(ticker.string("l", "lowPrice"), "24-hour low"),
                baseVolume24h = positiveOrZero(ticker.string("v", "volume"), "base volume"),
                quoteVolume24h = positiveOrZero(ticker.string("q", "quoteVolume"), "quote volume"),
                eventTimeMs = ticker.long("E", "closeTime").takeIf { it > 0 } ?: System.currentTimeMillis()
            )
        }
        return results
    }

    fun parseRestCandles(
        json: String,
        asset: String,
        timeframe: Timeframe,
        nowMs: Long
    ): List<MarketCandle> {
        val klines = JSONArray(json)
        val candles = mutableMapOf<Long, MarketCandle>()
        for (index in 0 until klines.length()) {
            val kline = klines.getJSONArray(index)
            require(kline.length() >= 8) { "Kline response is missing OHLCV fields" }
            val openTime = kline.getLong(0)
            val closeTime = kline.getLong(6)
            validateCandleBoundary(openTime, closeTime, timeframe)
            candles[openTime] = MarketCandle(
                asset = asset,
                timeframe = timeframe,
                openPrice = positive(kline.getString(1), "open"),
                highPrice = positive(kline.getString(2), "high"),
                lowPrice = positive(kline.getString(3), "low"),
                closePrice = positive(kline.getString(4), "close"),
                baseVolume = positiveOrZero(kline.getString(5), "base volume"),
                quoteVolume = positiveOrZero(kline.getString(7), "quote volume"),
                openTimeMs = openTime,
                closeTimeMs = closeTime,
                isFinal = closeTime < nowMs,
                eventTimeMs = closeTime
            ).also(::validateOhlc)
        }
        return candles.values.sortedBy { it.openTimeMs }
    }

    fun parseWebSocketCandle(
        json: String,
        expectedAsset: String,
        timeframe: Timeframe
    ): MarketCandle {
        val event = JSONObject(json)
        val kline = event.getJSONObject("k")
        val symbol = kline.getString("s")
        require(symbol == exchangeSymbol(expectedAsset)) { "Kline symbol did not match the requested pair" }
        val openTime = kline.getLong("t")
        val closeTime = kline.getLong("T")
        validateCandleBoundary(openTime, closeTime, timeframe)
        return MarketCandle(
            asset = expectedAsset,
            timeframe = timeframe,
            openPrice = positive(kline.getString("o"), "open"),
            highPrice = positive(kline.getString("h"), "high"),
            lowPrice = positive(kline.getString("l"), "low"),
            closePrice = positive(kline.getString("c"), "close"),
            baseVolume = positiveOrZero(kline.getString("v"), "base volume"),
            quoteVolume = positiveOrZero(kline.getString("q"), "quote volume"),
            openTimeMs = openTime,
            closeTimeMs = closeTime,
            isFinal = kline.getBoolean("x"),
            eventTimeMs = event.optLong("E", openTime)
        ).also(::validateOhlc)
    }

    fun mergeCandles(
        existing: List<MarketCandle>,
        incoming: MarketCandle,
        maximumCandles: Int = 100
    ): List<MarketCandle> {
        require(maximumCandles > 0)
        val candles = existing.associateBy { it.openTimeMs }.toMutableMap()
        val previous = candles[incoming.openTimeMs]
        if (previous == null || incoming.eventTimeMs >= previous.eventTimeMs) {
            candles[incoming.openTimeMs] = incoming
        }
        return candles.values.sortedBy { it.openTimeMs }.takeLast(maximumCandles)
    }

    fun exchangeSymbol(asset: String): String {
        val base = asset.substringBefore("/").uppercase(Locale.US)
        require(base in supportedAssets) { "Unsupported market asset: $asset" }
        return "${base}USDT"
    }

    private fun baseAsset(symbol: String): String {
        require(symbol.endsWith("USDT") && symbol in supportedSymbols) { "Unsupported Binance symbol: $symbol" }
        return symbol.removeSuffix("USDT")
    }

    private fun validateCandleBoundary(openTime: Long, closeTime: Long, timeframe: Timeframe) {
        require(openTime >= 0 && openTime % timeframe.intervalMs == 0L) {
            "Kline open time does not align with the ${timeframe.interval} boundary"
        }
        require(closeTime == openTime + timeframe.intervalMs - 1L) {
            "Kline close time does not match the ${timeframe.interval} interval"
        }
    }

    private fun validateOhlc(candle: MarketCandle) {
        require(candle.highPrice >= maxOf(candle.openPrice, candle.closePrice, candle.lowPrice)) {
            "Kline high is inconsistent with its OHLC values"
        }
        require(candle.lowPrice <= minOf(candle.openPrice, candle.closePrice, candle.highPrice)) {
            "Kline low is inconsistent with its OHLC values"
        }
    }

    private fun positive(value: String, label: String): Double =
        finite(value, label).also { require(it > 0.0) { "$label must be positive" } }

    private fun positiveOrZero(value: String, label: String): Double =
        finite(value, label).also { require(it >= 0.0) { "$label cannot be negative" } }

    private fun finite(value: String, label: String): Double =
        value.toDouble().also { require(it.isFinite()) { "$label must be finite" } }

    private fun formatTimestamp(timestampMs: Long): String =
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss 'UTC'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(Date(timestampMs))

    private fun JSONObject.string(vararg keys: String): String {
        keys.forEach { key ->
            if (has(key) && !isNull(key)) return get(key).toString()
        }
        throw IllegalArgumentException("Ticker is missing ${keys.joinToString("/")}")
    }

    private fun JSONObject.long(vararg keys: String): Long {
        keys.forEach { key ->
            if (has(key) && !isNull(key)) return getLong(key)
        }
        return 0L
    }
}
