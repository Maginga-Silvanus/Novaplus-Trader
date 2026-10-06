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

enum class Timeframe(val label: String) {
    M1("1m"),
    M5("5m"),
    M15("15m"),
    H1("1H"),
    H4("4H"),
    D1("1D")
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
    val chartDataMap: Map<Timeframe, List<Float>> = emptyMap()
)
