package com.example.novaplustrader.ui.screens.trading.models

enum class TradeDirection(val label: String) {
    BUY("BUY"),
    SELL("SELL")
}

enum class TradeDuration(val label: String, val seconds: Int) {
    THIRTY_SEC("30s", 30),
    ONE_MIN("1m", 60),
    FIVE_MIN("5m", 300),
    FIFTEEN_MIN("15m", 900),
    THIRTY_MIN("30m", 1800)
}

enum class Timeframe(val label: String, val interval: String, val intervalMs: Long) {
    M1("1m", "1m", 60_000L),
    M5("5m", "5m", 300_000L),
    M15("15m", "15m", 900_000L),
    H1("1H", "1h", 3_600_000L),
    H4("4H", "4h", 14_400_000L),
    D1("1D", "1d", 86_400_000L)
}

data class ChartPoint(
    val index: Int,
    val price: Float,
    val timeLabel: String = ""
)

data class TradingAsset(
    val symbol: String,
    val name: String,
    val currentPrice: Double,
    val change24hPercentage: Double,
    val high24h: Double,
    val low24h: Double,
    val volume24h: String,
    val payoutPercentage: Int = 85,
    val candleDataMap: Map<Timeframe, List<com.example.novaplustrader.domain.model.trading.MarketCandle>> = emptyMap()
)
