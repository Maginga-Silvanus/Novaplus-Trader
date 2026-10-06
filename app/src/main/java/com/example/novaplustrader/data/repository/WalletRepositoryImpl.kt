package com.example.novaplustrader.data.repository

import com.example.novaplustrader.ui.model.Currency
import com.example.novaplustrader.ui.model.TransactionStatus
import com.example.novaplustrader.ui.model.TransactionType
import com.example.novaplustrader.ui.model.TransactionUiModel
import com.example.novaplustrader.ui.screens.wallet.models.DepositMethod
import com.example.novaplustrader.ui.screens.wallet.models.WithdrawMethod
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WalletRepositoryImpl : WalletRepository {

    private val _balances = MutableStateFlow(
        mapOf(
            Currency.USD to 6324.49,
            Currency.KES to 824000.00
        )
    )

    private val _transactions = MutableStateFlow<List<TransactionUiModel>>(
        listOf(
            TransactionUiModel(
                id = "tx_101",
                title = "Bolt Food",
                subtitle = "Payment • Visa Card",
                type = TransactionType.PAYMENT,
                amount = 26.81,
                currency = Currency.USD,
                isPositive = false,
                timestamp = "Today, 1:45 PM",
                status = TransactionStatus.COMPLETED
            ),
            TransactionUiModel(
                id = "tx_102",
                title = "Maria Wanjiku",
                subtitle = "Peer Transfer",
                type = TransactionType.TRANSFER,
                amount = 100.00,
                currency = Currency.USD,
                isPositive = false,
                timestamp = "Today, 10:12 AM",
                status = TransactionStatus.COMPLETED
            ),
            TransactionUiModel(
                id = "tx_103",
                title = "Salary Deposit",
                subtitle = "Bank Wire • Chase Bank",
                type = TransactionType.DEPOSIT,
                amount = 2400.00,
                currency = Currency.USD,
                isPositive = true,
                timestamp = "Yesterday, 4:30 PM",
                status = TransactionStatus.COMPLETED
            ),
            TransactionUiModel(
                id = "tx_104",
                title = "M-Pesa Cashout",
                subtitle = "Mobile Withdrawal",
                type = TransactionType.WITHDRAWAL,
                amount = 15000.00,
                currency = Currency.KES,
                isPositive = false,
                timestamp = "14 Oct, 2:10 PM",
                status = TransactionStatus.COMPLETED
            )
        )
    )

    override fun observeBalances(): StateFlow<Map<Currency, Double>> = _balances.asStateFlow()
    override fun observeTransactions(): StateFlow<List<TransactionUiModel>> = _transactions.asStateFlow()

    override fun observeTransactionById(id: String): Flow<TransactionUiModel?> {
        return _transactions.map { list -> list.find { it.id == id } }
    }

    override suspend fun initiateDeposit(
        method: DepositMethod,
        amount: Double,
        currency: Currency
    ): Result<TransactionUiModel> {
        delay(900) // Simulate network authorization / STK Push prompt

        val newTx = TransactionUiModel(
            id = "tx_dep_${System.currentTimeMillis()}",
            title = "${method.title} Deposit",
            subtitle = method.description,
            type = TransactionType.DEPOSIT,
            amount = amount,
            currency = currency,
            isPositive = true,
            timestamp = "Just now (${getFormattedTime()})",
            status = TransactionStatus.COMPLETED
        )

        // Update ledger state safely
        _balances.update { currentMap ->
            val existing = currentMap[currency] ?: 0.0
            currentMap + (currency to (existing + amount))
        }

        _transactions.update { listOf(newTx) + it }
        return Result.success(newTx)
    }

    override suspend fun initiateWithdrawal(
        method: WithdrawMethod,
        destination: String,
        amount: Double,
        currency: Currency,
        securityPin: String
    ): Result<TransactionUiModel> {
        val currentBalance = _balances.value[currency] ?: 0.0
        if (amount > currentBalance) {
            return Result.failure(IllegalArgumentException("Insufficient wallet balance for withdrawal"))
        }

        delay(900) // Simulate secure ledger transaction processing

        val newTx = TransactionUiModel(
            id = "tx_wd_${System.currentTimeMillis()}",
            title = "${method.title} Withdrawal",
            subtitle = "To: $destination",
            type = TransactionType.WITHDRAWAL,
            amount = amount,
            currency = currency,
            isPositive = false,
            timestamp = "Just now (${getFormattedTime()})",
            status = TransactionStatus.COMPLETED
        )

        // Deduct from wallet ledger
        _balances.update { currentMap ->
            val existing = currentMap[currency] ?: 0.0
            currentMap + (currency to (existing - amount).coerceAtLeast(0.0))
        }

        _transactions.update { listOf(newTx) + it }
        return Result.success(newTx)
    }

    override suspend fun sendMoney(
        recipientId: String,
        amount: Double,
        currency: Currency,
        note: String
    ): Result<TransactionUiModel> {
        val currentBalance = _balances.value[currency] ?: 0.0
        if (amount > currentBalance) {
            return Result.failure(IllegalArgumentException("Insufficient funds for peer transfer"))
        }

        delay(800)

        val newTx = TransactionUiModel(
            id = "tx_send_${System.currentTimeMillis()}",
            title = "Transfer to $recipientId",
            subtitle = if (note.isBlank()) "Peer Transfer" else note,
            type = TransactionType.TRANSFER,
            amount = amount,
            currency = currency,
            isPositive = false,
            timestamp = "Just now (${getFormattedTime()})",
            status = TransactionStatus.COMPLETED
        )

        _balances.update { currentMap ->
            val existing = currentMap[currency] ?: 0.0
            currentMap + (currency to (existing - amount).coerceAtLeast(0.0))
        }

        _transactions.update { listOf(newTx) + it }
        return Result.success(newTx)
    }

    override fun generateReceiveRequest(
        requestedAmount: Double?,
        currency: Currency
    ): String {
        val amtPart = if (requestedAmount != null && requestedAmount > 0) "&amount=$requestedAmount" else ""
        return "novapay://transfer?recipient=#NV-92841&currency=${currency.code}$amtPart"
    }

    private fun getFormattedTime(): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.US)
        return sdf.format(Date())
    }
}
