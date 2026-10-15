package com.example.novaplustrader.domain.model.trading

import com.example.novaplustrader.ui.screens.trading.models.Timeframe

enum class MarketStatus(val label: String) {
    OPEN("Market Open"),
    CLOSED("Market Closed"),
    SUSPENDED("Trading Suspended"),
    MAINTENANCE("System Maintenance"),
    DATA_UNAVAILABLE("Market Data Unavailable")
}

enum class MarketConnectionState(val label: String) {
    CONNECTED("Connected"),
    CONNECTING("Connecting..."),
    STALE("Data Stale"),
    DISCONNECTED("Disconnected")
}

/**
 * Immutable Market Tick Model.
 * Prices stored in minor units (e.g. cents) as Long values for authoritative calculation.
 */
data class MarketTick(
    val asset: String,
    val bidMinorUnits: Long,
    val askMinorUnits: Long,
    val lastPriceMinorUnits: Long,
    val timestamp: String,
    val source: String = "NovaBackendEngine",
    val sequenceNumber: Long = 0L,
    val isStale: Boolean = false,
    val change24hPercentage: Double? = null,
    val high24h: Double? = null,
    val low24h: Double? = null,
    val quoteVolume24h: Double? = null,
    val preciseLastPriceUsd: Double? = null,
    val baseVolume24h: Double? = null,
    val eventTimeMs: Long = 0L
) {
    val lastPriceDouble: Double
        get() = preciseLastPriceUsd ?: lastPriceMinorUnits / 100.0

    val bidDouble: Double
        get() = bidMinorUnits / 100.0

    val askDouble: Double
        get() = askMinorUnits / 100.0

    companion object {
        fun toMinorUnits(priceDouble: Double): Long {
            return Math.round(priceDouble * 100.0)
        }
    }
}

/**
 * Immutable OHLCV Market Candle Model.
 */
data class MarketCandle(
    val asset: String,
    val timeframe: Timeframe,
    val openPrice: Double,
    val highPrice: Double,
    val lowPrice: Double,
    val closePrice: Double,
    val baseVolume: Double,
    val quoteVolume: Double,
    val openTimeMs: Long,
    val closeTimeMs: Long,
    val isFinal: Boolean,
    val eventTimeMs: Long = closeTimeMs
) {
    val openDouble: Double get() = openPrice
    val highDouble: Double get() = highPrice
    val lowDouble: Double get() = lowPrice
    val closeDouble: Double get() = closePrice
}

data class MarketSummary(
    val symbol: String,
    val baseAsset: String,
    val quoteAsset: String,
    val name: String,
    val lastPrice: Double,
    val priceChangePercent24h: Double,
    val highPrice24h: Double,
    val lowPrice24h: Double,
    val baseVolume24h: Double,
    val quoteVolume24h: Double,
    val eventTimeMs: Long,
    val isStale: Boolean = false,
    val source: String = "Binance Spot"
) {
    val pair: String get() = "$baseAsset/$quoteAsset"
}
