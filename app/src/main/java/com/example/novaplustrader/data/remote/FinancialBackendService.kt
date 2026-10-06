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

/**
 * Interface contract for authoritative backend financial processing.
 * Connects to Cloud Functions / Cloud Run / Payment Providers.
 */
interface FinancialBackendService {
    suspend fun createDepositRequest(request: DepositRequest): Result<FinancialTransaction>
    suspend fun createWithdrawalRequest(request: WithdrawalRequest): Result<FinancialTransaction>
    suspend fun createTransferRequest(request: TransferRequest): Result<FinancialTransaction>

    suspend fun getTransaction(transactionId: String): Result<FinancialTransaction?>
    fun observeTransaction(transactionId: String): Flow<FinancialTransaction?>

    suspend fun getWalletBalance(walletId: String): Result<WalletBalanceModel>
    fun observeWalletBalance(walletId: String): Flow<WalletBalanceModel>

    suspend fun getTransactionHistory(
        walletId: String,
        page: Int = 1,
        pageSize: Int = 20,
        type: FinancialTransactionType? = null,
        status: FinancialTransactionStatus? = null
    ): Result<List<FinancialTransaction>>

    fun observeTransactionHistory(walletId: String): Flow<List<FinancialTransaction>>

    suspend fun requestRefund(request: RefundRequest): Result<FinancialTransaction>
    suspend fun requestReversal(request: ReversalRequest): Result<FinancialTransaction>

    suspend fun calculateFee(
        type: FinancialTransactionType,
        amountMinorUnits: Long,
        currency: Currency
    ): Result<FeeModel>
}
