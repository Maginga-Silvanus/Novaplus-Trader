package com.example.novaplustrader.ui.screens.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.novaplustrader.data.repository.WalletRepository
import com.example.novaplustrader.data.repository.WalletRepositoryImpl
import com.example.novaplustrader.domain.model.ledger.DepositRequest
import com.example.novaplustrader.domain.model.ledger.TransferRequest
import com.example.novaplustrader.domain.model.ledger.WithdrawalRequest
import com.example.novaplustrader.ui.model.Currency
import com.example.novaplustrader.ui.model.TransactionUiModel
import com.example.novaplustrader.ui.model.WalletUiModel
import com.example.novaplustrader.ui.screens.wallet.models.DepositMethod
import com.example.novaplustrader.ui.screens.wallet.models.WithdrawMethod
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.UUID

class WalletViewModel(
    private val repository: WalletRepository = WalletRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(WalletUiState())
    val uiState: StateFlow<WalletUiState> = _uiState.asStateFlow()

    init {
        observeWalletRepository()
    }

    private fun observeWalletRepository() {
        viewModelScope.launch {
            launch {
                repository.observeWalletBalanceModel("w_usd").collect { usdModel ->
                    _uiState.update { current ->
                        val sampleWallets = listOf(
                            WalletUiModel(
                                id = "w_usd",
                                walletId = "#NV-92841",
                                userName = current.userName,
                                currency = Currency.USD,
                                balanceAmount = usdModel.availableBalance,
                                cardNumberPlaceholder = "•••• •••• •••• 8842",
                                isPrimary = true
                            ),
                            WalletUiModel(
                                id = "w_kes",
                                walletId = "#NV-92842",
                                userName = current.userName,
                                currency = Currency.KES,
                                balanceAmount = current.kesBalance,
                                cardNumberPlaceholder = "•••• •••• •••• 4109",
                                isPrimary = false
                            )
                        )
                        current.copy(
                            usdBalance = usdModel.availableBalance,
                            usdPendingBalance = usdModel.pendingBalance,
                            usdLockedBalance = usdModel.lockedBalance,
                            wallets = sampleWallets
                        )
                    }
                }
            }

            launch {
                repository.observeWalletBalanceModel("w_kes").collect { kesModel ->
                    _uiState.update { current ->
                        current.copy(
                            kesBalance = kesModel.availableBalance,
                            kesPendingBalance = kesModel.pendingBalance,
                            kesLockedBalance = kesModel.lockedBalance
                        )
                    }
                }
            }

            launch {
                repository.observeTransactions().collect { txs ->
                    _uiState.update { it.copy(transactions = txs) }
                }
            }

            launch {
                repository.observeFinancialTransactions().collect { finTxs ->
                    _uiState.update { it.copy(financialTransactions = finTxs) }
                }
            }
        }
    }

    fun selectCurrency(currency: Currency) {
        _uiState.update { it.copy(selectedCurrency = currency) }
    }

    fun setTransactionFilter(filter: String) {
        _uiState.update { it.copy(selectedTransactionFilter = filter) }
    }

    fun selectTransactionDetails(transaction: TransactionUiModel?) {
        _uiState.update { it.copy(selectedTransaction = transaction) }
    }

    // --- DEPOSIT FLOW ---
    fun selectDepositMethod(method: DepositMethod) {
        viewModelScope.launch {
            val amountMinor = Math.round((_uiState.value.depositAmountString.toDoubleOrNull() ?: 100.0) * 100.0)
            val feeRes = repository.getFeeEstimate(
                com.example.novaplustrader.domain.model.ledger.FinancialTransactionType.DEPOSIT,
                amountMinor,
                _uiState.value.selectedCurrency
            ).getOrNull()

            val fee = feeRes?.feeAmountDouble ?: when (method) {
                DepositMethod.BANK -> 1.2
                DepositMethod.CARD -> 1.5
                else -> 0.0
            }
            _uiState.update { it.copy(selectedDepositMethod = method, depositFee = fee) }
        }
    }

    fun updateDepositAmount(amountStr: String) {
        val cleanStr = amountStr.filter { (it.isDigit()) || (it == '.') }
        _uiState.update { it.copy(depositAmountString = cleanStr, validationError = null) }
    }

    fun openDepositConfirmSheet() {
        val amount = _uiState.value.depositAmountString.toDoubleOrNull() ?: 0.0
        if (amount <= 0) {
            _uiState.update { it.copy(validationError = "Please enter a valid deposit amount") }
            return
        }
        _uiState.update { it.copy(validationError = null, isDepositConfirmOpen = true) }
    }

    fun closeDepositConfirmSheet() {
        _uiState.update { it.copy(isDepositConfirmOpen = false) }
    }

    fun executeDeposit() {
        _uiState.update { it.copy(isDepositConfirmOpen = false, isLoading = true) }
        viewModelScope.launch {
            val amount = _uiState.value.depositAmountString.toDoubleOrNull() ?: 0.0
            val walletId = if (_uiState.value.selectedCurrency == Currency.USD) "w_usd" else "w_kes"

            val req = DepositRequest(
                idempotencyKey = "idemp_dep_${UUID.randomUUID().toString().take(12)}",
                userId = "usr_9901",
                walletId = walletId,
                method = _uiState.value.selectedDepositMethod,
                amountMinorUnits = Math.round(amount * 100.0),
                currency = _uiState.value.selectedCurrency
            )

            val result = repository.createDepositRequest(req)
            _uiState.update { it.copy(isLoading = false) }

            result.onSuccess { finTx ->
                val uiTx = TransactionUiModel(
                    id = finTx.transactionId,
                    title = finTx.description,
                    subtitle = "Ref: ${finTx.reference}",
                    type = com.example.novaplustrader.ui.model.TransactionType.DEPOSIT,
                    amount = finTx.amountDouble,
                    currency = finTx.currency,
                    isPositive = true,
                    timestamp = finTx.createdAt,
                    status = if (finTx.status == com.example.novaplustrader.domain.model.ledger.FinancialTransactionStatus.COMPLETED) com.example.novaplustrader.ui.model.TransactionStatus.COMPLETED else com.example.novaplustrader.ui.model.TransactionStatus.PENDING
                )
                _uiState.update { it.copy(lastCompletedTransaction = uiTx) }
            }.onFailure { err ->
                _uiState.update { it.copy(errorMessage = err.message ?: "Deposit request failed") }
            }
        }
    }

    // --- WITHDRAW FLOW ---
    fun selectWithdrawMethod(method: WithdrawMethod) {
        viewModelScope.launch {
            val amountMinor = Math.round((_uiState.value.withdrawAmountString.toDoubleOrNull() ?: 50.0) * 100.0)
            val feeRes = repository.getFeeEstimate(
                com.example.novaplustrader.domain.model.ledger.FinancialTransactionType.WITHDRAWAL,
                amountMinor,
                _uiState.value.selectedCurrency
            ).getOrNull()

            val fee = feeRes?.feeAmountDouble ?: when (method) {
                WithdrawMethod.BANK -> 2.50
                WithdrawMethod.MPESA -> 0.30
                WithdrawMethod.CRYPTO_WALLET -> 1.00
            }
            _uiState.update { it.copy(selectedWithdrawMethod = method, withdrawFee = fee) }
        }
    }

    fun updateWithdrawDestination(dest: String) {
        _uiState.update { it.copy(withdrawDestination = dest, validationError = null) }
    }

    fun updateWithdrawAmount(amountStr: String) {
        val cleanStr = amountStr.filter { (it.isDigit()) || (it == '.') }
        val amount = cleanStr.toDoubleOrNull() ?: 0.0
        val currentBal = if (_uiState.value.selectedCurrency == Currency.USD) _uiState.value.usdBalance else _uiState.value.kesBalance

        val error = if (amount > currentBal) "Amount exceeds available balance" else null
        _uiState.update { it.copy(withdrawAmountString = cleanStr, selectedPercentage = null, validationError = error) }
    }

    fun applyWithdrawPercentageShortcut(percentage: Int) {
        val currentBal = if (_uiState.value.selectedCurrency == Currency.USD) _uiState.value.usdBalance else _uiState.value.kesBalance
        val calculatedAmount = (currentBal * (percentage / 100.0))
        val formattedStr = String.format(Locale.US, "%.2f", calculatedAmount)

        _uiState.update {
            it.copy(
                withdrawAmountString = formattedStr,
                selectedPercentage = percentage,
                validationError = null
            )
        }
    }

    fun openWithdrawConfirmSheet() {
        val amount = _uiState.value.withdrawAmountString.toDoubleOrNull() ?: 0.0
        val currentBal = if (_uiState.value.selectedCurrency == Currency.USD) _uiState.value.usdBalance else _uiState.value.kesBalance

        if (amount <= 0) {
            _uiState.update { it.copy(validationError = "Please enter a valid withdrawal amount") }
            return
        }
        if (amount > currentBal) {
            _uiState.update { it.copy(validationError = "Amount exceeds available balance") }
            return
        }
        if (_uiState.value.withdrawDestination.isBlank()) {
            _uiState.update { it.copy(validationError = "Destination address/number is required") }
            return
        }

        _uiState.update { it.copy(validationError = null, isWithdrawConfirmOpen = true) }
    }

    fun closeWithdrawConfirmSheet() {
        _uiState.update { it.copy(isWithdrawConfirmOpen = false) }
    }

    fun openPinVerification() {
        _uiState.update { it.copy(isWithdrawConfirmOpen = false, isPinVerificationOpen = true, securityPin = "") }
    }

    fun closePinVerification() {
        _uiState.update { it.copy(isPinVerificationOpen = false, securityPin = "") }
    }

    fun onKeypadPress(key: String) {
        val currentPin = _uiState.value.securityPin
        if (currentPin.length < 4) {
            val newPin = currentPin + key
            _uiState.update { it.copy(securityPin = newPin) }
            if (newPin.length == 4) {
                executeWithdrawal(newPin)
            }
        }
    }

    fun onKeypadBackspace() {
        val currentPin = _uiState.value.securityPin
        if (currentPin.isNotEmpty()) {
            _uiState.update { it.copy(securityPin = currentPin.dropLast(1)) }
        }
    }

    private fun executeWithdrawal(pin: String) {
        _uiState.update { it.copy(isPinVerificationOpen = false, isLoading = true) }
        viewModelScope.launch {
            val amount = _uiState.value.withdrawAmountString.toDoubleOrNull() ?: 0.0
            val walletId = if (_uiState.value.selectedCurrency == Currency.USD) "w_usd" else "w_kes"

            val req = WithdrawalRequest(
                idempotencyKey = "idemp_wd_${UUID.randomUUID().toString().take(12)}",
                userId = "usr_9901",
                walletId = walletId,
                method = _uiState.value.selectedWithdrawMethod,
                destination = _uiState.value.withdrawDestination,
                amountMinorUnits = Math.round(amount * 100.0),
                currency = _uiState.value.selectedCurrency,
                securityPin = pin
            )

            val result = repository.createWithdrawalRequest(req)
            _uiState.update { it.copy(isLoading = false, securityPin = "") }

            result.onSuccess { finTx ->
                val uiTx = TransactionUiModel(
                    id = finTx.transactionId,
                    title = finTx.description,
                    subtitle = "Ref: ${finTx.reference}",
                    type = com.example.novaplustrader.ui.model.TransactionType.WITHDRAWAL,
                    amount = finTx.amountDouble,
                    currency = finTx.currency,
                    isPositive = false,
                    timestamp = finTx.createdAt,
                    status = if (finTx.status == com.example.novaplustrader.domain.model.ledger.FinancialTransactionStatus.COMPLETED) com.example.novaplustrader.ui.model.TransactionStatus.COMPLETED else com.example.novaplustrader.ui.model.TransactionStatus.PENDING
                )
                _uiState.update { it.copy(lastCompletedTransaction = uiTx) }
            }.onFailure { err ->
                _uiState.update { it.copy(errorMessage = err.message ?: "Withdrawal request failed") }
            }
        }
    }

    // --- SEND MONEY FLOW ---
    fun updateRecipientId(id: String) {
        _uiState.update { it.copy(recipientId = id, validationError = null) }
    }

    fun updateSendAmount(amountStr: String) {
        val cleanStr = amountStr.filter { (it.isDigit()) || (it == '.') }
        _uiState.update { it.copy(sendAmountString = cleanStr, validationError = null) }
    }

    fun updateSendNote(note: String) {
        _uiState.update { it.copy(sendNote = note) }
    }

    fun openSendConfirmSheet() {
        val amount = _uiState.value.sendAmountString.toDoubleOrNull() ?: 0.0
        val currentBal = if (_uiState.value.selectedCurrency == Currency.USD) _uiState.value.usdBalance else _uiState.value.kesBalance

        if (_uiState.value.recipientId.isBlank()) {
            _uiState.update { it.copy(validationError = "Recipient Wallet ID or Email is required") }
            return
        }
        if (amount <= 0) {
            _uiState.update { it.copy(validationError = "Please enter a valid transfer amount") }
            return
        }
        if (amount > currentBal) {
            _uiState.update { it.copy(validationError = "Amount exceeds available balance") }
            return
        }

        _uiState.update { it.copy(validationError = null, isSendConfirmOpen = true) }
    }

    fun closeSendConfirmSheet() {
        _uiState.update { it.copy(isSendConfirmOpen = false) }
    }

    fun executeSendMoney() {
        _uiState.update { it.copy(isSendConfirmOpen = false, isLoading = true) }
        viewModelScope.launch {
            val amount = _uiState.value.sendAmountString.toDoubleOrNull() ?: 0.0
            val walletId = if (_uiState.value.selectedCurrency == Currency.USD) "w_usd" else "w_kes"

            val req = TransferRequest(
                idempotencyKey = "idemp_trf_${UUID.randomUUID().toString().take(12)}",
                senderUserId = "usr_9901",
                senderWalletId = walletId,
                recipientId = _uiState.value.recipientId,
                amountMinorUnits = Math.round(amount * 100.0),
                currency = _uiState.value.selectedCurrency,
                note = _uiState.value.sendNote
            )

            val result = repository.createTransferRequest(req)
            _uiState.update { it.copy(isLoading = false) }

            result.onSuccess { finTx ->
                val uiTx = TransactionUiModel(
                    id = finTx.transactionId,
                    title = finTx.description,
                    subtitle = "Ref: ${finTx.reference}",
                    type = com.example.novaplustrader.ui.model.TransactionType.TRANSFER,
                    amount = finTx.amountDouble,
                    currency = finTx.currency,
                    isPositive = false,
                    timestamp = finTx.createdAt,
                    status = if (finTx.status == com.example.novaplustrader.domain.model.ledger.FinancialTransactionStatus.COMPLETED) com.example.novaplustrader.ui.model.TransactionStatus.COMPLETED else com.example.novaplustrader.ui.model.TransactionStatus.PENDING
                )
                _uiState.update { it.copy(lastCompletedTransaction = uiTx) }
            }.onFailure { err ->
                _uiState.update { it.copy(errorMessage = err.message ?: "Transfer request failed") }
            }
        }
    }

    // --- RECEIVE MONEY FLOW ---
    fun updateRequestedReceiveAmount(amountStr: String) {
        val cleanStr = amountStr.filter { (it.isDigit()) || (it == '.') }
        val amt = cleanStr.toDoubleOrNull()
        val payload = repository.generateReceiveRequest(amt, _uiState.value.selectedCurrency)
        _uiState.update {
            it.copy(
                requestedReceiveAmountString = cleanStr,
                receiveQrPayload = payload
            )
        }
    }

    fun clearCompletedTransaction() {
        _uiState.update { it.copy(lastCompletedTransaction = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
