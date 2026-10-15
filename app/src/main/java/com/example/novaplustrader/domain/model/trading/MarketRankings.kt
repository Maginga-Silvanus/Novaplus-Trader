package com.example.novaplustrader.domain.model.trading

enum class MarketRanking(val label: String) {
    HOT("Hot"),
    GAINERS("Gainers"),
    LOSERS("Losers"),
    NEW_LISTINGS("New"),
    VOLUME("24h Volume"),
    MARKET_CAP("Market Cap")
}

object MarketRankings {
    fun sort(markets: List<MarketSummary>, ranking: MarketRanking): List<MarketSummary> =
        when (ranking) {
            MarketRanking.HOT -> markets.sortedWith(
                compareByDescending<MarketSummary> { kotlin.math.abs(it.priceChangePercent24h) }
                    .thenByDescending { it.quoteVolume24h }
            )
            MarketRanking.GAINERS -> markets.sortedByDescending { it.priceChangePercent24h }
            MarketRanking.LOSERS -> markets.sortedBy { it.priceChangePercent24h }
            MarketRanking.NEW_LISTINGS -> markets.sortedByDescending { it.eventTimeMs }
            MarketRanking.VOLUME -> markets.sortedByDescending { it.quoteVolume24h }
            MarketRanking.MARKET_CAP -> markets.sortedByDescending { it.quoteVolume24h * 10 }
        }
}
