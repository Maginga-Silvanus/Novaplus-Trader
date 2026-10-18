package com.example.novaplustrader

import com.example.novaplustrader.data.remote.FinancialBackendServiceImpl
import com.example.novaplustrader.data.remote.TradingExecutionServiceImpl
import com.example.novaplustrader.domain.model.ledger.DepositRequest
import com.example.novaplustrader.domain.model.trading.TradeOrder
import com.example.novaplustrader.domain.model.trading.TradeOrderStatus
import com.example.novaplustrader.ui.model.Currency
import com.example.novaplustrader.ui.screens.trading.models.TradeDirection
import com.example.novaplustrader.ui.screens.wallet.models.DepositMethod
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SecureBackendFallbackTest {
    @Test
    fun unconfiguredFinancialBackendHasNoSeededFundsOrCompletedDeposits() = runBlocking {
        val backend = FinancialBackendServiceImpl()
        assertTrue(backend.getWalletBalance("wallet-test-user-usd").isFailure)

        val result = backend.createDepositRequest(
            DepositRequest(
                idempotencyKey = "deposit-request-test-01",
                userId = "authenticated-test-user",
                walletId = "w_usd",
                method = DepositMethod.MPESA,
                amountMinorUnits = 10_000,
                currency = Currency.USD
            )
        )
        assertTrue(result.isFailure)
        assertTrue(backend.getTransactionHistory("wallet-test-user-usd").isFailure)
    }

    @Test
    fun unconfiguredTradeExecutionCannotCreateAnOpenOrFilledOrder() = runBlocking {
        val execution = TradingExecutionServiceImpl()
        val order = TradeOrder(
            orderId = "request-test",
            userId = "authenticated-test-user",
            asset = "BTC/USDT",
            direction = TradeDirection.BUY,
            requestedAmountMinorUnits = 10_000,
            durationSeconds = 60,
            requestTimestamp = "now",
            idempotencyKey = "trade-request-test-01"
        )

        val result = execution.submitTradeOrder(order)
        assertTrue(result.isFailure)
        assertFalse(execution.observeOpenTrades().value.any { it.status == TradeOrderStatus.OPEN })
        assertTrue(execution.observeSettledTrades().value.isEmpty())
    }
}
