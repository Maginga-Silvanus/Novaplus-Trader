package com.example.novaplustrader.data.repository

import android.net.Uri
import com.example.novaplustrader.data.remote.FinancialBackendService
import com.example.novaplustrader.data.remote.FinancialBackendServiceImpl
import com.google.firebase.auth.FirebaseAuth
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
import com.example.novaplustrader.ui.model.TransactionStatus
import com.example.novaplustrader.ui.model.TransactionType
import com.example.novaplustrader.ui.model.TransactionUiModel
import com.example.novaplustrader.ui.screens.wallet.models.DepositMethod
import com.example.novaplustrader.ui.screens.wallet.models.WithdrawMethod
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.UUID

class WalletRepositoryImpl(
    private val backendService: FinancialBackendService = FinancialBackendServiceImpl(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : WalletRepository {

    private val scope = CoroutineScope(Dispatchers.Default)

    private val _balances = MutableStateFlow(
        mapOf(
            Currency.USD to 0.0,
            Currency.KES to 0.0
        )
    )

    private val _uiTransactions = MutableStateFlow<List<TransactionUiModel>>(emptyList())

    init {
        // Synchronize legacy UI flow streams with backend authoritative state
        scope.launch {
            backendService.observeWalletBalance("w_usd").collect { usdModel ->
                _balances.value = _balances.value + (Currency.USD to usdModel.availableBalance)
            }
        }
        scope.launch {
            backendService.observeWalletBalance("w_kes").collect { kesModel ->
                _balances.value = _balances.value + (Currency.KES to kesModel.availableBalance)
            }
        }
        scope.launch {
            backendService.observeTransactionHistory("").collect { list ->
                _uiTransactions.value = list.map { mapToUiModel(it) }
            }
        }
    }

    override fun observeBalances(): StateFlow<Map<Currency, Double>> = _balances.asStateFlow()

    override fun observeTransactions(): StateFlow<List<TransactionUiModel>> = _uiTransactions.asStateFlow()

    override fun observeTransactionById(id: String): Flow<TransactionUiModel?> {
        return backendService.observeTransaction(id).map { it?.let { mapToUiModel(it) } }
    }

    override fun observeWalletBalanceModel(walletId: String): Flow<WalletBalanceModel> {
        return backendService.observeWalletBalance(walletId)
    }

    override fun observeFinancialTransactions(): Flow<List<FinancialTransaction>> {
        return backendService.observeTransactionHistory("")
    }

    override fun observeFinancialTransactionById(id: String): Flow<FinancialTransaction?> {
        return backendService.observeTransaction(id)
    }

    override suspend fun createDepositRequest(request: DepositRequest): Result<FinancialTransaction> {
        return backendService.createDepositRequest(request)
    }

    override suspend fun createWithdrawalRequest(request: WithdrawalRequest): Result<FinancialTransaction> {
        return backendService.createWithdrawalRequest(request)
    }

    override suspend fun createTransferRequest(request: TransferRequest): Result<FinancialTransaction> {
        return backendService.createTransferRequest(request)
    }

    override suspend fun requestRefund(request: RefundRequest): Result<FinancialTransaction> {
        return backendService.requestRefund(request)
    }

    override suspend fun requestReversal(request: ReversalRequest): Result<FinancialTransaction> {
        return backendService.requestReversal(request)
    }

    override suspend fun getFeeEstimate(
        type: FinancialTransactionType,
        amountMinorUnits: Long,
        currency: Currency
    ): Result<FeeModel> {
        return backendService.calculateFee(type, amountMinorUnits, currency)
    }

    override suspend fun initiateDeposit(
        method: DepositMethod,
        amount: Double,
        currency: Currency
    ): Result<TransactionUiModel> {
        val userId = auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("Sign in before requesting a deposit"))
        val walletId = if (currency == Currency.USD) "w_usd" else "w_kes"
        val req = DepositRequest(
            idempotencyKey = "idemp_dep_${UUID.randomUUID().toString().take(10)}",
            userId = userId,
            walletId = walletId,
            method = method,
            amountMinorUnits = Math.round(amount * 100.0),
            currency = currency
        )

        val result = backendService.createDepositRequest(req)
        return result.map { mapToUiModel(it) }
    }

    override suspend fun initiateWithdrawal(
        method: WithdrawMethod,
        destination: String,
        amount: Double,
        currency: Currency,
        securityPin: String
    ): Result<TransactionUiModel> {
        val userId = auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("Sign in before requesting a withdrawal"))
        val walletId = if (currency == Currency.USD) "w_usd" else "w_kes"
        val req = WithdrawalRequest(
            idempotencyKey = "idemp_wd_${UUID.randomUUID().toString().take(10)}",
            userId = userId,
            walletId = walletId,
            method = method,
            destination = destination,
            amountMinorUnits = Math.round(amount * 100.0),
            currency = currency,
            securityPin = securityPin
        )

        val result = backendService.createWithdrawalRequest(req)
        return result.map { mapToUiModel(it) }
    }

    override suspend fun sendMoney(
        recipientId: String,
        amount: Double,
        currency: Currency,
        note: String
    ): Result<TransactionUiModel> {
        val userId = auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("Sign in before requesting a transfer"))
        val walletId = if (currency == Currency.USD) "w_usd" else "w_kes"
        val req = TransferRequest(
            idempotencyKey = "idemp_trf_${UUID.randomUUID().toString().take(10)}",
            senderUserId = userId,
            senderWalletId = walletId,
            recipientId = recipientId,
            amountMinorUnits = Math.round(amount * 100.0),
            currency = currency,
            note = note
        )

        val result = backendService.createTransferRequest(req)
        return result.map { mapToUiModel(it) }
    }

    override fun generateReceiveRequest(
        requestedAmount: Double?,
        currency: Currency,
        walletId: String,
        userName: String
    ): String {
        if (walletId.isBlank()) return ""
        val builder = Uri.Builder()
            .scheme("novapay")
            .authority("transfer")
            .appendQueryParameter("recipient", walletId)
            .appendQueryParameter("name", userName)
            .appendQueryParameter("currency", currency.code)
        if (requestedAmount != null && requestedAmount > 0) {
            builder.appendQueryParameter("amount", requestedAmount.toString())
        }
        return builder.build().toString()
    }

    private fun mapToUiModel(tx: FinancialTransaction): TransactionUiModel {
        val uiType = when (tx.type) {
            FinancialTransactionType.DEPOSIT -> TransactionType.DEPOSIT
            FinancialTransactionType.WITHDRAWAL -> TransactionType.WITHDRAWAL
            FinancialTransactionType.TRANSFER -> TransactionType.TRANSFER
            FinancialTransactionType.TRADE -> TransactionType.TRADE
            else -> TransactionType.PAYMENT
        }

        val uiStatus = when (tx.status) {
            FinancialTransactionStatus.COMPLETED -> TransactionStatus.COMPLETED
            FinancialTransactionStatus.FAILED, FinancialTransactionStatus.CANCELLED, FinancialTransactionStatus.REVERSED -> TransactionStatus.FAILED
            else -> TransactionStatus.PENDING
        }

        val isPos = tx.type == FinancialTransactionType.DEPOSIT || tx.type == FinancialTransactionType.REFUND

        return TransactionUiModel(
            id = tx.transactionId,
            title = tx.description.ifBlank { "${tx.type.name} Transaction" },
            subtitle = "Ref: ${tx.reference}",
            type = uiType,
            amount = tx.amountDouble,
            currency = tx.currency,
            isPositive = isPos,
            timestamp = tx.createdAt,
            status = uiStatus
        )
    }
}
