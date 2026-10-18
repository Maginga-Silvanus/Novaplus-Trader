package com.example.novaplustrader.data.repository

import com.example.novaplustrader.domain.model.ledger.DepositRequest
import com.example.novaplustrader.domain.model.ledger.FeeModel
import com.example.novaplustrader.domain.model.ledger.FinancialTransaction
import com.example.novaplustrader.domain.model.ledger.FinancialTransactionType
import com.example.novaplustrader.domain.model.ledger.RefundRequest
import com.example.novaplustrader.domain.model.ledger.ReversalRequest
import com.example.novaplustrader.domain.model.ledger.TransferRequest
import com.example.novaplustrader.domain.model.ledger.WalletBalanceModel
import com.example.novaplustrader.domain.model.ledger.WithdrawalRequest
import com.example.novaplustrader.ui.model.Currency
import com.example.novaplustrader.ui.model.TransactionUiModel
import com.example.novaplustrader.ui.screens.wallet.models.DepositMethod
import com.example.novaplustrader.ui.screens.wallet.models.WithdrawMethod
import kotlinx.coroutines.flow.Flow

interface WalletRepository {
    fun observeBalances(): Flow<Map<Currency, Double>>
    fun observeTransactions(): Flow<List<TransactionUiModel>>
    fun observeTransactionById(id: String): Flow<TransactionUiModel?>

    fun observeWalletBalanceModel(walletId: String): Flow<WalletBalanceModel>
    fun observeFinancialTransactions(): Flow<List<FinancialTransaction>>
    fun observeFinancialTransactionById(id: String): Flow<FinancialTransaction?>

    suspend fun createDepositRequest(request: DepositRequest): Result<FinancialTransaction>
    suspend fun createWithdrawalRequest(request: WithdrawalRequest): Result<FinancialTransaction>
    suspend fun createTransferRequest(request: TransferRequest): Result<FinancialTransaction>
    suspend fun requestRefund(request: RefundRequest): Result<FinancialTransaction>
    suspend fun requestReversal(request: ReversalRequest): Result<FinancialTransaction>
    suspend fun getFeeEstimate(type: FinancialTransactionType, amountMinorUnits: Long, currency: Currency): Result<FeeModel>

    suspend fun initiateDeposit(
        method: DepositMethod,
        amount: Double,
        currency: Currency
    ): Result<TransactionUiModel>

    suspend fun initiateWithdrawal(
        method: WithdrawMethod,
        destination: String,
        amount: Double,
        currency: Currency,
        securityPin: String
    ): Result<TransactionUiModel>

    suspend fun sendMoney(
        recipientId: String,
        amount: Double,
        currency: Currency,
        note: String
    ): Result<TransactionUiModel>

    fun generateReceiveRequest(
        requestedAmount: Double?,
        currency: Currency,
        walletId: String,
        userName: String
    ): String
}
