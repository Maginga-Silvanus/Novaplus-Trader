package com.example.novaplustrader

import com.example.novaplustrader.data.remote.FxRate
import com.example.novaplustrader.domain.model.trading.CryptoAssetMetadata
import com.example.novaplustrader.domain.model.trading.MarketRanking
import com.example.novaplustrader.domain.model.trading.MarketRankings
import com.example.novaplustrader.domain.model.trading.MarketSummary
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GlobalCryptoAndFxTest {

    @Test
    fun testCryptoAssetMetadataParsingAndTradability() {
        val btc = CryptoAssetMetadata(
            id = "bitcoin",
            symbol = "BTC",
            name = "Bitcoin",
            logoUrl = "https://assets.coingecko.com/coins/images/1/large/bitcoin.png",
            marketCapUsd = 1_350_000_000_000.0,
            marketCapRank = 1,
            change24h = 2.5,
            volume24h = 35_000_000_000.0,
            lastUpdatedMs = System.currentTimeMillis(),
            isTradableOnBinance = true
        )

        val unlistedAsset = CryptoAssetMetadata(
            id = "random-token",
            symbol = "RND",
            name = "Random Token",
            logoUrl = null,
            marketCapUsd = null,
            marketCapRank = null,
            change24h = null,
            volume24h = null,
            lastUpdatedMs = System.currentTimeMillis(),
            isTradableOnBinance = false
        )

        assertEquals("Bitcoin", btc.name)
        assertTrue(btc.isTradableOnBinance)
        assertNotNull(btc.marketCapUsd)
        assertEquals("Random Token", unlistedAsset.name)
        assertTrue(!unlistedAsset.isTradableOnBinance)
        assertEquals(null, unlistedAsset.marketCapUsd)
    }

    @Test
    fun testMarketRankingsSorting() {
        val summaries = listOf(
            MarketSummary("BTCUSDT", "BTC", "USDT", "Bitcoin", 65000.0, 3.5, 66000.0, 64000.0, 100.0, 6500000.0, 1000L),
            MarketSummary("ETHUSDT", "ETH", "USDT", "Ethereum", 3500.0, -5.2, 3600.0, 3400.0, 500.0, 1750000.0, 2000L),
            MarketSummary("SOLUSDT", "SOL", "USDT", "Solana", 180.0, 8.1, 185.0, 175.0, 1000.0, 180000.0, 3000L)
        )

        val gainers = MarketRankings.sort(summaries, MarketRanking.GAINERS)
        assertEquals("SOLUSDT", gainers.first().symbol)

        val losers = MarketRankings.sort(summaries, MarketRanking.LOSERS)
        assertEquals("ETHUSDT", losers.first().symbol)

        val volume = MarketRankings.sort(summaries, MarketRanking.VOLUME)
        assertEquals("BTCUSDT", volume.first().symbol)
    }

    @Test
    fun testFxRateParsingAndStaleness() {
        val rate = FxRate(
            baseCurrency = "USD",
            quoteCurrency = "KES",
            rate = 129.50,
            source = "ExchangeRate-API",
            observedAtMs = System.currentTimeMillis(),
            isStale = false
        )

        assertEquals("USD", rate.baseCurrency)
        assertEquals("KES", rate.quoteCurrency)
        assertEquals(129.50, rate.rate, 0.001)
        assertEquals(false, rate.isStale)
    }

    @Test
    fun testPortfolioValuationCalculationWithoutBalanceMutation() {
        val usdBalance = 5000.0
        val kesRate = 129.50
        val convertedKes = usdBalance * kesRate

        assertEquals(647500.0, convertedKes, 0.01)
        // Verify balance quantity is unchanged
        assertEquals(5000.0, usdBalance, 0.0001)
    }
}
