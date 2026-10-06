package com.example.novaplustrader.data.repository

import com.example.novaplustrader.ui.model.Currency
import com.example.novaplustrader.ui.model.TransactionUiModel
import com.example.novaplustrader.ui.screens.wallet.models.DepositMethod
import com.example.novaplustrader.ui.screens.wallet.models.WithdrawMethod
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface WalletRepository {
    fun observeBalances(): StateFlow<Map<Currency, Double>>
    fun observeTransactions(): StateFlow<List<TransactionUiModel>>
    fun observeTransactionById(id: String): Flow<TransactionUiModel?>

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
        currency: Currency
    ): String
}
