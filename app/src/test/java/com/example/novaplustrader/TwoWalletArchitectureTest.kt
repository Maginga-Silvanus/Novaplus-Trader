package com.example.novaplustrader

import com.example.novaplustrader.data.repository.MainUserWalletSnapshot
import com.example.novaplustrader.domain.model.trading.MarketSummary
import com.example.novaplustrader.ui.screens.wallet.WalletUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TwoWalletArchitectureTest {
    @Test
    fun newMainWalletStartsWithFiveDollarBalanceUntilFirebaseInitializesIt() {
        val mainWallet = MainUserWalletSnapshot()

        assertEquals(500L, mainWallet.usdBalanceMinorUnits)
        assertEquals(0L, mainWallet.kesBalanceMinorUnits)
        assertEquals(5.0, mainWallet.usdBalance, 0.0)
        assertEquals(0.0, mainWallet.kesBalance, 0.0)
        assertEquals("testing", mainWallet.mode)
    }

    @Test
    fun cryptoHoldingsAndQuotesDoNotMutateMainWalletBalances() {
        val mainWallet = MainUserWalletSnapshot()
        val cryptoWallet = WalletUiState(
            cryptoBtcBalance = 0.25,
            cryptoPricesUsd = mapOf("BTC" to 60_000.0)
        )

        assertEquals(15_000.0, cryptoWallet.cryptoPortfolioValueUsd!!, 0.0)
        assertEquals(5.0, mainWallet.usdBalance, 0.0)
        assertEquals(0.0, mainWallet.kesBalance, 0.0)
    }

    @Test
    fun portfolioValueIsUnavailableWhenANonzeroHoldingHasNoVerifiedQuote() {
        val cryptoWallet = WalletUiState(
            cryptoBtcBalance = 0.25,
            cryptoUsdtBalance = 10.0,
            cryptoPricesUsd = mapOf("BTC" to 60_000.0)
        )

        assertNull(cryptoWallet.cryptoPortfolioValueUsd)
    }

    @Test
    fun liveMarketQuoteIsValuationOnlyAndDoesNotAlterEitherWallet() {
        val mainWallet = MainUserWalletSnapshot(usdBalanceMinorUnits = 10_000, kesBalanceMinorUnits = 20_000)
        val cryptoWallet = WalletUiState(
            cryptoBtcBalance = 0.1,
            cryptoPricesUsd = mapOf("BTC" to 60_000.0)
        )
        val quote = MarketSummary(
            symbol = "BTCUSDT",
            baseAsset = "BTC",
            quoteAsset = "USDT",
            name = "Bitcoin",
            lastPrice = 65_000.0,
            priceChangePercent24h = 3.0,
            highPrice24h = 66_000.0,
            lowPrice24h = 62_000.0,
            baseVolume24h = 1.0,
            quoteVolume24h = 65_000.0,
            eventTimeMs = 1_700_000_000_000L
        )

        val updatedCryptoWallet = cryptoWallet.copy(
            cryptoPricesUsd = cryptoWallet.cryptoPricesUsd + ("BTC" to quote.lastPrice)
        )

        assertEquals(6_500.0, updatedCryptoWallet.cryptoPortfolioValueUsd!!, 0.0)
        assertEquals(10_000L, mainWallet.usdBalanceMinorUnits)
        assertEquals(20_000L, mainWallet.kesBalanceMinorUnits)
        assertEquals(0.1, updatedCryptoWallet.cryptoBtcBalance, 0.0)
    }
}
