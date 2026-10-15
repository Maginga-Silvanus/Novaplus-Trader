package com.example.novaplustrader.data.remote

import com.example.novaplustrader.domain.model.ledger.DepositRequest
import com.example.novaplustrader.domain.model.ledger.FeeModel
import com.example.novaplustrader.domain.model.ledger.FinancialTransaction
import com.example.novaplustrader.domain.model.ledger.FinancialTransactionStatus
import com.example.novaplustrader.domain.model.ledger.FinancialTransactionType
import com.example.novaplustrader.domain.model.ledger.RefundRequest
import com.example.novaplustrader.domain.model.ledger.ReversalRequest
import com.example.novaplustrader.domain.model.ledger.TransferRequest
import com.example.novaplustrader.domain.model.ledger.WalletBalanceModel
import com.example.novaplustrader.domain.model.ledger.WithdrawalRequest
import com.example.novaplustrader.ui.model.Currency
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FinancialBackendServiceImpl : FinancialBackendService {
    override suspend fun createDepositRequest(request: DepositRequest): Result<FinancialTransaction> = unavailable()
    override suspend fun createWithdrawalRequest(request: WithdrawalRequest): Result<FinancialTransaction> = unavailable()
    override suspend fun createTransferRequest(request: TransferRequest): Result<FinancialTransaction> = unavailable()
    override suspend fun getTransaction(transactionId: String): Result<FinancialTransaction?> = Result.success(null)
    override fun observeTransaction(transactionId: String): Flow<FinancialTransaction?> = flowOf(null)

    override suspend fun getWalletBalance(walletId: String): Result<WalletBalanceModel> =
        Result.success(emptyBalance(walletId))

    override fun observeWalletBalance(walletId: String): Flow<WalletBalanceModel> = flowOf(emptyBalance(walletId))

    override suspend fun getTransactionHistory(
        walletId: String,
        page: Int,
        pageSize: Int,
        type: FinancialTransactionType?,
        status: FinancialTransactionStatus?
    ): Result<List<FinancialTransaction>> = Result.success(emptyList())

    override fun observeTransactionHistory(walletId: String): Flow<List<FinancialTransaction>> = flowOf(emptyList())
    override suspend fun requestRefund(request: RefundRequest): Result<FinancialTransaction> = unavailable()
    override suspend fun requestReversal(request: ReversalRequest): Result<FinancialTransaction> = unavailable()

    override suspend fun calculateFee(
        type: FinancialTransactionType,
        amountMinorUnits: Long,
        currency: Currency
    ): Result<FeeModel> = Result.failure(backendUnavailable())

    private fun emptyBalance(walletId: String): WalletBalanceModel = WalletBalanceModel(
        walletId = walletId,
        userId = "",
        currency = if (walletId.endsWith("kes", ignoreCase = true)) Currency.KES else Currency.USD,
        availableBalanceMinorUnits = 0L,
        pendingBalanceMinorUnits = 0L,
        lockedBalanceMinorUnits = 0L
    )

    private fun backendUnavailable() = IllegalStateException(
        "The financial backend is not configured. No funds or transaction were changed."
    )

    private fun <T> unavailable(): Result<T> = Result.failure(backendUnavailable())
}
