package com.example.novaplustrader

import com.example.novaplustrader.domain.model.ledger.FeeModel
import com.example.novaplustrader.domain.model.ledger.FinancialLedgerEntry
import com.example.novaplustrader.domain.model.ledger.FinancialTransaction
import com.example.novaplustrader.domain.model.ledger.FinancialTransactionStatus
import com.example.novaplustrader.domain.model.ledger.FinancialTransactionType
import com.example.novaplustrader.domain.model.ledger.LedgerDirection
import com.example.novaplustrader.domain.model.ledger.LedgerEntryType
import com.example.novaplustrader.domain.model.ledger.LedgerStatus
import com.example.novaplustrader.domain.model.ledger.WalletBalanceModel
import com.example.novaplustrader.ui.model.Currency
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FinancialTransactionTest {

    @Test
    fun testMinorUnitConversionAndPrecision() {
        val amountDouble = 124.50
        val minorUnits = FinancialLedgerEntry.toMinorUnits(amountDouble)
        assertEquals(12450L, minorUnits)

        val entry = FinancialLedgerEntry(
            ledgerEntryId = "le_101",
            transactionId = "tx_101",
            userId = "usr_001",
            walletId = "w_usd",
            entryType = LedgerEntryType.DEPOSIT_CREDIT,
            currency = Currency.USD,
            amountMinorUnits = minorUnits,
            direction = LedgerDirection.CREDIT,
            balanceBeforeMinorUnits = 100000L,
            balanceAfterMinorUnits = 112450L,
            status = LedgerStatus.COMMITTED,
            reference = "DEP-REF-101",
            createdAt = "2024-11-01T10:00:00Z"
        )

        assertEquals(124.50, entry.amountDouble, 0.001)
        assertEquals(1000.00, entry.balanceBeforeDouble, 0.001)
        assertEquals(1124.50, entry.balanceAfterDouble, 0.001)
    }

    @Test
    fun testFinancialTransactionNetAmountAndState() {
        val transaction = FinancialTransaction(
            transactionId = "tx_202",
            userId = "usr_001",
            walletId = "w_kes",
            type = FinancialTransactionType.WITHDRAWAL,
            currency = Currency.KES,
            amountMinorUnits = 500000L, // KES 5,000.00
            feeMinorUnits = 3000L, // KES 30.00 fee
            status = FinancialTransactionStatus.PENDING,
            idempotencyKey = "idemp_wd_test_key_123",
            reference = "WD-REF-202",
            createdAt = "2024-11-01T11:00:00Z",
            updatedAt = "2024-11-01T11:00:00Z"
        )

        assertEquals(497000L, transaction.netAmountMinorUnits)
        assertEquals(5000.00, transaction.amountDouble, 0.001)
        assertEquals(30.00, transaction.feeDouble, 0.001)
        assertEquals(4970.00, transaction.netAmountDouble, 0.001)
        assertEquals(FinancialTransactionStatus.PENDING, transaction.status)
        assertTrue(transaction.idempotencyKey.isNotBlank())
    }

    @Test
    fun testWalletBalanceModelGranularity() {
        val balance = WalletBalanceModel(
            walletId = "w_usd",
            userId = "usr_001",
            currency = Currency.USD,
            availableBalanceMinorUnits = 632449L, // $6,324.49
            pendingBalanceMinorUnits = 10000L,   // $100.00
            lockedBalanceMinorUnits = 5000L       // $50.00
        )

        assertEquals(647449L, balance.totalBalanceMinorUnits)
        assertEquals(6324.49, balance.availableBalance, 0.001)
        assertEquals(100.00, balance.pendingBalance, 0.001)
        assertEquals(50.00, balance.lockedBalance, 0.001)
        assertEquals(6474.49, balance.totalBalance, 0.001)
    }

    @Test
    fun testFeeModelCalculation() {
        val feeModel = FeeModel(
            transactionType = FinancialTransactionType.WITHDRAWAL,
            currency = Currency.USD,
            feeAmountMinorUnits = 250L, // $2.50
            percentageFee = 0.01, // 1%
            minFeeMinorUnits = 100L,
            maxFeeMinorUnits = 1000L,
            effectiveDate = "2024-11-01"
        )

        assertEquals(2.50, feeModel.feeAmountDouble, 0.001)
        assertNotNull(feeModel.effectiveDate)
    }
}
