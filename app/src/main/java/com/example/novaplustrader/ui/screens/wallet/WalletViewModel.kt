package com.example.novaplustrader.ui.screens.wallet

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.novaplustrader.data.repository.CryptoWalletRepository
import com.example.novaplustrader.data.repository.CryptoWalletRepositoryImpl
import com.example.novaplustrader.data.repository.AuthRepository
import com.example.novaplustrader.data.repository.AuthRepositoryImpl
import com.example.novaplustrader.data.repository.NotificationRepository
import com.example.novaplustrader.data.repository.NotificationRepositoryImpl
import com.example.novaplustrader.data.repository.WalletRepository
import com.example.novaplustrader.data.repository.WalletRepositoryImpl
import com.example.novaplustrader.data.repository.MainUserWalletRepository
import com.example.novaplustrader.data.repository.MainUserWalletRepositoryImpl
import com.example.novaplustrader.data.repository.MarketDataRepository
import com.example.novaplustrader.data.repository.MarketDataRepositoryImpl
import com.example.novaplustrader.domain.model.trading.MarketConnectionState
import com.example.novaplustrader.domain.model.ledger.FinancialTransaction
import com.example.novaplustrader.domain.model.ledger.FinancialTransactionStatus
import com.example.novaplustrader.domain.model.ledger.TransferRequest
import com.example.novaplustrader.ui.model.Currency
import com.example.novaplustrader.ui.model.TransactionStatus
import com.example.novaplustrader.ui.model.TransactionType
import com.example.novaplustrader.ui.model.TransactionUiModel
import com.example.novaplustrader.ui.model.WalletUiModel
import com.example.novaplustrader.ui.screens.wallet.models.DepositMethod
import com.example.novaplustrader.ui.screens.wallet.models.WithdrawMethod
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Locale
import java.util.UUID

class WalletViewModel(
    private val repository: WalletRepository = WalletRepositoryImpl(),
    private val notificationRepository: NotificationRepository = NotificationRepositoryImpl(),
    private val authRepository: AuthRepository = AuthRepositoryImpl(),
    private val cryptoWalletRepository: CryptoWalletRepository = CryptoWalletRepositoryImpl(),
    private val mainWalletRepository: MainUserWalletRepository = MainUserWalletRepositoryImpl(),
    private val marketDataRepository: MarketDataRepository = MarketDataRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(WalletUiState())
    val uiState: StateFlow<WalletUiState> = _uiState.asStateFlow()
    private var recipientLookupJob: Job? = null

    init {
        observeWalletRepository()
        observeCryptoMarketData()
    }

    fun claimFirstTimeReward() {
        viewModelScope.launch {
            val user = authRepository.getCurrentUser() ?: return@launch
            mainWalletRepository.claimFirstTimeReward(user.uid)
                .onSuccess {
                    _uiState.update { it.copy(testWalletNotice = "🎉 $5.00 First-time reward claimed successfully!") }
                    notificationRepository.saveNotification(
                        com.example.novaplustrader.ui.screens.notifications.models.NotificationModel(
                            id = UUID.randomUUID().toString(),
                            userId = user.uid,
                            title = "First-Time Reward Claimed!",
                            body = "You have successfully claimed your $5.00 USD welcome reward.",
                            category = com.example.novaplustrader.ui.screens.notifications.models.NotificationCategory.WALLET,
                            timestamp = "Just now"
                        )
                    )
                }
                .onFailure { error ->
                    _uiState.update { it.copy(validationError = error.localizedMessage ?: "Unable to claim reward") }
                }
        }
    }

    private fun observeCryptoMarketData() {
        viewModelScope.launch {
            marketDataRepository.observeConnectionState().collect { connection ->
                if (connection == MarketConnectionState.DISCONNECTED || connection == MarketConnectionState.STALE) {
                    _uiState.update {
                        it.copy(cryptoMarketError = if (connection == MarketConnectionState.STALE) {
                            "Binance market data is stale; portfolio valuation may be outdated."
                        } else {
                            "Binance market data is disconnected; current valuation may be unavailable."
                        })
                    }
                }
            }
        }
        listOf("BTC", "ETH", "SOL", "BNB", "XRP", "ADA", "DOGE", "LTC").forEach { asset ->
            viewModelScope.launch {
                marketDataRepository.observeMarketTicks("$asset/USDT")
                    .catch { error ->
                        _uiState.update {
                            it.copy(cryptoMarketError = error.localizedMessage ?: "Crypto market quote unavailable")
                        }
                    }
                    .collect { tick ->
                        if (tick.lastPriceDouble.isFinite() && tick.lastPriceDouble > 0.0 && !tick.isStale) {
                            _uiState.update { current ->
                                current.copy(
                                    cryptoPricesUsd = current.cryptoPricesUsd + (asset to tick.lastPriceDouble),
                                    cryptoMarketUpdatedAt = tick.timestamp,
                                    cryptoMarketError = null
                                )
                            }
                        } else if (tick.isStale) {
                            _uiState.update {
                                it.copy(cryptoMarketError = "Showing a stale market quote; portfolio valuation may be outdated.")
                            }
                        }
                    }
            }
        }
    }

    private fun observeWalletRepository() {
        viewModelScope.launch {
            launch {
                authRepository.observeAuthState().collectLatest { user ->
                    _uiState.update { current ->
                        val userName = user?.fullName?.takeIf { it.isNotBlank() } ?: "Nova Trader"
                        val walletId = user?.uid?.let { "NV-$it" }.orEmpty()
                        val updated = current.copy(
                            userName = userName,
                            walletId = walletId,
                            bitcoinWalletId = null,
                            cryptoBtcBalance = 0.0,
                            cryptoEthBalance = 0.0,
                            cryptoUsdtBalance = 0.0,
                            cryptoUsdcBalance = 0.0,
                            cryptoSolBalance = 0.0,
                            cryptoBnbBalance = 0.0,
                            cryptoWalletMode = "disabled",
                            cryptoPricesUsd = emptyMap(),
                            cryptoMarketUpdatedAt = null,
                            usdBalance = 0.0,
                            usdPendingBalance = 0.0,
                            usdLockedBalance = 0.0,
                            kesBalance = 0.0,
                            kesPendingBalance = 0.0,
                            kesLockedBalance = 0.0,
                            wallets = emptyList(),
                            transactions = emptyList(),
                            financialTransactions = emptyList(),
                            testWalletNotice = null
                        )
                        updated.copy(receiveQrPayload = createReceivePayload(updated))
                    }
                    if (user != null) {
                        cryptoWalletRepository.ensureWallet(user.uid)
                            .onSuccess { wallet ->
                                _uiState.update { current ->
                                    current.copy(
                                        bitcoinWalletId = wallet.bitcoinWalletId,
                                        cryptoWalletMode = wallet.mode,
                                        cryptoBtcBalance = wallet.assetBalances["BTC"] ?: 0.0,
                                        cryptoEthBalance = wallet.assetBalances["ETH"] ?: 0.0,
                                        cryptoUsdtBalance = wallet.assetBalances["USDT"] ?: 0.0,
                                        cryptoUsdcBalance = wallet.assetBalances["USDC"] ?: 0.0,
                                        cryptoSolBalance = wallet.assetBalances["SOL"] ?: 0.0,
                                        cryptoBnbBalance = wallet.assetBalances["BNB"] ?: 0.0,
                                        errorMessage = null
                                    ).let { it.copy(receiveQrPayload = createReceivePayload(it)) }
                                }
                            }
                            .onFailure { error ->
                                _uiState.update {
                                    it.copy(errorMessage = error.localizedMessage ?: "Unable to initialize your Bitcoin wallet")
                                }
                            }
                        cryptoWalletRepository.observeWallet(user.uid)
                            .catch { error ->
                                _uiState.update {
                                    it.copy(errorMessage = error.localizedMessage ?: "Unable to load your crypto wallet")
                                }
                            }
                            .collect { wallet ->
                                _uiState.update { current ->
                                    current.copy(
                                        bitcoinWalletId = wallet.bitcoinWalletId,
                                        cryptoWalletMode = wallet.mode,
                                        cryptoBtcBalance = wallet.assetBalances["BTC"] ?: 0.0,
                                        cryptoEthBalance = wallet.assetBalances["ETH"] ?: 0.0,
                                        cryptoUsdtBalance = wallet.assetBalances["USDT"] ?: 0.0,
                                        cryptoUsdcBalance = wallet.assetBalances["USDC"] ?: 0.0,
                                        cryptoSolBalance = wallet.assetBalances["SOL"] ?: 0.0,
                                        cryptoBnbBalance = wallet.assetBalances["BNB"] ?: 0.0
                                    ).let { it.copy(receiveQrPayload = createReceivePayload(it)) }
                                }
                            }
                    }
                }
            }

            launch {
                authRepository.observeAuthState().collectLatest { user ->
                    if (user != null) {
                        coroutineScope {
                            launch {
                                mainWalletRepository.observeWallet(user.uid)
                                    .catch { error ->
                                        _uiState.update {
                                            it.copy(errorMessage = error.localizedMessage ?: "Unable to read your user wallet")
                                        }
                                    }
                                    .collect { wallet ->
                                        _uiState.update { current ->
                                            current.copy(
                                                usdBalance = wallet.usdBalance,
                                                rewardClaimed = wallet.rewardClaimed,
                                                usdPendingBalance = current.pendingDepositBalance(Currency.USD),
                                                usdLockedBalance = 0.0,
                                                kesBalance = wallet.kesBalance,
                                                kesPendingBalance = current.pendingDepositBalance(Currency.KES),
                                                kesLockedBalance = 0.0,
                                                wallets = listOf(
                                                    WalletUiModel(
                                                        id = "${user.uid}-usd",
                                                        walletId = current.walletId,
                                                        userName = current.userName,
                                                        currency = Currency.USD,
                                                        balanceAmount = wallet.usdBalance,
                                                        cardNumberPlaceholder = "USD · TEST FUNDS",
                                                        isPrimary = true
                                                    ),
                                                    WalletUiModel(
                                                        id = "${user.uid}-kes",
                                                        walletId = current.walletId,
                                                        userName = current.userName,
                                                        currency = Currency.KES,
                                                        balanceAmount = wallet.kesBalance,
                                                        cardNumberPlaceholder = "KES · TEST FUNDS",
                                                        isPrimary = false
                                                    )
                                                ),
                                                transactions = current.financialTransactions.map { it.toUiModel() } +
                                                    current.transactions.filter { transaction ->
                                                        current.financialTransactions.none { it.transactionId == transaction.id }
                                                    }
                                            )
                                        }
                                    }
                            }
                            launch {
                                mainWalletRepository.observeTransactions(user.uid)
                                    .catch { error ->
                                        _uiState.update {
                                            it.copy(errorMessage = error.localizedMessage ?: "Unable to load TEST wallet history")
                                        }
                                    }
                                    .collect { records ->
                                        _uiState.update { current ->
                                            val testTransactions = records.map { transaction ->
                                                val isDeposit = transaction.operation == "DEPOSIT"
                                                val isCryptoBuy = transaction.operation == "CRYPTO_BUY"
                                                val isCryptoSell = transaction.operation == "CRYPTO_SELL"
                                                TransactionUiModel(
                                                    id = transaction.id,
                                                    title = when {
                                                        isDeposit -> "TEST funds added"
                                                        isCryptoBuy -> "PAPER crypto buy"
                                                        isCryptoSell -> "PAPER crypto sale"
                                                        else -> "TEST funds withdrawn"
                                                    },
                                                    subtitle = "PAPER MONEY · ${transaction.status.replace('_', ' ')}",
                                                    type = when {
                                                        isDeposit -> TransactionType.DEPOSIT
                                                        isCryptoBuy -> TransactionType.CRYPTO_BUY
                                                        isCryptoSell -> TransactionType.CRYPTO_SELL
                                                        else -> TransactionType.WITHDRAWAL
                                                    },
                                                    amount = transaction.amountMinorUnits / 100.0,
                                                    currency = transaction.currency,
                                                    isPositive = isDeposit || isCryptoSell,
                                                    timestamp = transaction.createdAt,
                                                    status = when (transaction.status) {
                                                        "COMPLETED_TEST" -> TransactionStatus.COMPLETED
                                                        else -> TransactionStatus.FAILED
                                                    }
                                                )
                                            }
                                            current.copy(
                                                transactions = current.financialTransactions.map { it.toUiModel() } +
                                                    testTransactions
                                            )
                                        }
                                    }
                            }
                        }
                    }
                }
            }

            launch {
                repository.observeFinancialTransactions()
                    .catch { error ->
                        _uiState.update {
                            it.copy(errorMessage = error.localizedMessage ?: "Financial transaction service is unavailable")
                        }
                    }
                    .collect { finTxs ->
                        _uiState.update { current ->
                            val testTransactions = current.transactions.filter { transaction ->
                                current.financialTransactions.none { it.transactionId == transaction.id }
                            }
                            current.copy(
                                financialTransactions = finTxs,
                                usdPendingBalance = finTxs.pendingDepositBalance(Currency.USD),
                                kesPendingBalance = finTxs.pendingDepositBalance(Currency.KES),
                                transactions = finTxs.map { it.toUiModel() } + testTransactions
                            )
                        }
                    }
            }
        }
    }

    fun selectCurrency(currency: Currency) {
        _uiState.update { current ->
            val updated = current.copy(selectedCurrency = currency)
            updated.copy(receiveQrPayload = createReceivePayload(updated))
        }
    }

    fun setTransactionFilter(filter: String) {
        _uiState.update { it.copy(selectedTransactionFilter = filter) }
    }

    fun selectTransactionDetails(transaction: TransactionUiModel?) {
        _uiState.update { it.copy(selectedTransaction = transaction) }
    }

    // --- DEPOSIT FLOW ---
    fun selectDepositMethod(method: DepositMethod) {
        _uiState.update { it.copy(selectedDepositMethod = method, depositFee = 0.0, validationError = null) }
    }

    fun updateDepositAmount(amountStr: String) {
        val cleanStr = amountStr.filter { (it.isDigit()) || (it == '.') }
        _uiState.update { it.copy(depositAmountString = cleanStr, validationError = null) }
    }

    fun updateMpesaPhoneNumber(phone: String) {
        _uiState.update { it.copy(mpesaPhoneNumber = phone, validationError = null) }
    }

    fun updateMpesaPaybillCode(code: String) {
        _uiState.update { it.copy(mpesaPaybillCode = code, validationError = null) }
    }

    fun selectBankName(bankName: String) {
        _uiState.update { it.copy(selectedBankName = bankName, validationError = null) }
    }

    fun updateBankAccountNumber(accNumber: String) {
        _uiState.update { it.copy(bankAccountNumber = accNumber, validationError = null) }
    }

    fun updateCardDetails(holderName: String, number: String, expiry: String, cvv: String, isSaved: Boolean) {
        _uiState.update {
            it.copy(
                cardHolderName = holderName,
                cardNumber = number,
                cardExpiry = expiry,
                cardCvv = cvv,
                isCardSaved = isSaved,
                validationError = null
            )
        }
    }

    fun selectCryptoAsset(asset: String) {
        _uiState.update { it.copy(selectedCryptoAsset = asset, validationError = null) }
    }

    fun openDepositConfirmSheet() {
        if (_uiState.value.selectedDepositMethod == DepositMethod.CRYPTO) {
            _uiState.update {
                it.copy(
                    validationError = "On-chain crypto deposits are not supported yet. Use Spot Trade to buy crypto with your internal USD balance."
                )
            }
            return
        }
        val amount = _uiState.value.depositAmountString.toDoubleOrNull() ?: 0.0
        if (!amount.isFinite() || amount <= 0) {
            _uiState.update { it.copy(validationError = "Please enter a valid deposit amount") }
            return
        }
        if (kotlin.math.abs(amount * 100.0 - kotlin.math.round(amount * 100.0)) > 1e-8) {
            _uiState.update { it.copy(validationError = "TEST deposits must use at most two decimal places") }
            return
        }

        _uiState.update { it.copy(validationError = null, isDepositConfirmOpen = true) }
    }

    fun closeDepositConfirmSheet() {
        _uiState.update { it.copy(isDepositConfirmOpen = false) }
    }

    fun executeDeposit() {
        if (_uiState.value.selectedDepositMethod == DepositMethod.CRYPTO) {
            _uiState.update {
                it.copy(
                    isDepositConfirmOpen = false,
                    isLoading = false,
                    testWalletNotice = "Crypto deposits are unavailable; this action only funds USD/KES TEST balances."
                )
            }
            return
        }
        val state = _uiState.value
        val amount = state.depositAmountString.toDoubleOrNull() ?: 0.0
        if (amount <= 0.0 || !amount.isFinite()) {
            _uiState.update { it.copy(validationError = "Enter a valid deposit amount") }
            return
        }
        if (state.selectedDepositMethod == DepositMethod.PAYBILL && state.mpesaPaybillCode.isBlank()) {
            _uiState.update { it.copy(isDepositConfirmOpen = false, validationError = "Please enter your M-Pesa transaction code") }
            return
        }
        if (state.selectedDepositMethod == DepositMethod.PAYBILL &&
            !state.mpesaPaybillCode.matches(Regex("[A-Z0-9]{6,12}"))
        ) {
            _uiState.update {
                it.copy(isDepositConfirmOpen = false, validationError = "Enter a valid M-Pesa transaction code")
            }
            return
        }
        _uiState.update { it.copy(isDepositConfirmOpen = false, isLoading = true, testWalletNotice = null) }
        viewModelScope.launch {
            val result = repository.initiateDeposit(
                method = state.selectedDepositMethod,
                amount = amount,
                currency = state.selectedCurrency,
                providerReference = state.mpesaPaybillCode.takeIf {
                    state.selectedDepositMethod == DepositMethod.PAYBILL
                }
            )
            result.onSuccess { tx ->
                val isPaybill = state.selectedDepositMethod == DepositMethod.PAYBILL
                val initialTx = if (isPaybill) {
                    tx.copy(
                        subtitle = "PAYBILL 522522 · Code: ${state.mpesaPaybillCode}",
                        status = TransactionStatus.SUBMITTED
                    )
                } else tx

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        testWalletNotice = if (isPaybill)
                            "Paybill Deposit Submitted (Code: ${state.mpesaPaybillCode}). Status: SUBMITTED (transitions to PENDING in 1 min, awaiting admin approval)."
                        else
                            "Deposit request recorded as pending. Awaiting processing.",
                        lastCompletedTransaction = initialTx,
                        errorMessage = null
                    )
                }

                if (isPaybill) {
                    val formattedAmt = com.example.novaplustrader.ui.util.Formatters.formatCurrency(amount, state.selectedCurrency)
                    notificationRepository.saveNotification(
                        com.example.novaplustrader.ui.screens.notifications.models.NotificationModel(
                            id = "notif_${UUID.randomUUID().toString().take(8)}",
                            title = "Paybill Deposit Submitted - $formattedAmt",
                            body = "Paybill 522522 / Acc 1321188617 (Code: ${state.mpesaPaybillCode}). Status: SUBMITTED (pending admin approval).",
                            category = com.example.novaplustrader.ui.screens.notifications.models.NotificationCategory.WALLET,
                            timestamp = "Just now",
                            isRead = false,
                            deepLink = "transaction_history"
                        )
                    )

                    launch {
                        kotlinx.coroutines.delay(60_000)
                        _uiState.update { current ->
                            current.copy(
                                testWalletNotice = "Paybill Deposit Status: PENDING admin approval.",
                                lastCompletedTransaction = current.lastCompletedTransaction
                                    ?.takeIf { it.id == tx.id }
                                    ?.copy(status = TransactionStatus.PENDING)
                            )
                        }
                    }
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        testWalletNotice = error.message ?: "Deposit request could not be recorded",
                        errorMessage = error.message ?: "Deposit request could not be recorded"
                    )
                }
            }
        }
    }

    // --- WITHDRAW FLOW ---
    fun selectWithdrawMethod(method: WithdrawMethod) {
        _uiState.update { it.copy(selectedWithdrawMethod = method, withdrawFee = 0.0, validationError = null) }
    }

    fun selectSavedCardForWithdrawal(index: Int) {
        _uiState.update { current ->
            val isNew = index < 0 || index >= current.savedCards.size
            val dest = if (!isNew) current.savedCards[index] else current.withdrawDestination
            current.copy(
                selectedSavedCardIndex = index,
                isAddNewCardForWithdrawal = isNew,
                withdrawDestination = dest,
                validationError = null
            )
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

        if (!amount.isFinite() || amount <= 0) {
            _uiState.update { it.copy(validationError = "Please enter a valid withdrawal amount") }
            return
        }
        val minAmountUsdEquivalent = if (_uiState.value.selectedCurrency == Currency.USD) 10.0 else 1300.0
        if (amount < minAmountUsdEquivalent) {
            val minFormatted = if (_uiState.value.selectedCurrency == Currency.USD) "$10.00 USD" else "1,300 KES ($10 USD)"
            _uiState.update { it.copy(validationError = "Minimum amount to withdraw is $minFormatted") }
            return
        }
        if (kotlin.math.abs(amount * 100.0 - kotlin.math.round(amount * 100.0)) > 1e-8) {
            _uiState.update { it.copy(validationError = "TEST withdrawals must use at most two decimal places") }
            return
        }
        if (amount > currentBal) {
            _uiState.update { it.copy(validationError = "Amount exceeds available balance") }
            return
        }
        _uiState.update { it.copy(validationError = null, isWithdrawConfirmOpen = true) }
    }

    fun closeWithdrawConfirmSheet() {
        _uiState.update { it.copy(isWithdrawConfirmOpen = false) }
    }

    fun openPinVerification() {
        _uiState.update { it.copy(isWithdrawConfirmOpen = false, isPinVerificationOpen = true, pinActionType = "WITHDRAW", securityPin = "") }
    }

    fun openSendPinVerification() {
        _uiState.update { it.copy(isSendConfirmOpen = false, isPinVerificationOpen = true, pinActionType = "SEND", securityPin = "") }
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
                if (_uiState.value.pinActionType == "SEND") {
                    executeSendMoney()
                } else {
                    executeTestWithdrawal()
                }
            }
        }
    }

    fun onKeypadBackspace() {
        val currentPin = _uiState.value.securityPin
        if (currentPin.isNotEmpty()) {
            _uiState.update { it.copy(securityPin = currentPin.dropLast(1)) }
        }
    }

    fun executeTestWithdrawal() {
        val state = _uiState.value
        val amount = state.withdrawAmountString.toDoubleOrNull() ?: 0.0
        if (!amount.isFinite() || amount <= 0.0) {
            _uiState.update { it.copy(validationError = "Enter a valid withdrawal amount") }
            return
        }
        _uiState.update {
            it.copy(isWithdrawConfirmOpen = false, isPinVerificationOpen = false, isLoading = true, testWalletNotice = null)
        }
        viewModelScope.launch {
            val result = repository.initiateWithdrawal(
                method = state.selectedWithdrawMethod,
                destination = state.withdrawDestination,
                amount = amount,
                currency = state.selectedCurrency,
                securityPin = state.securityPin
            )
            result.onSuccess { tx ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        securityPin = "",
                        testWalletNotice = "Withdrawal request recorded as pending. No payout was initiated and no balance was debited.",
                        errorMessage = null,
                        lastCompletedTransaction = tx
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        securityPin = "",
                        testWalletNotice = error.message ?: "Withdrawal request could not be recorded",
                        errorMessage = error.message ?: "Withdrawal request could not be recorded"
                    )
                }
            }
        }
    }

    // --- SEND MONEY FLOW ---
    fun selectSendLookupMethod(method: String) {
        recipientLookupJob?.cancel()
        _uiState.update {
            it.copy(
                sendLookupMethod = method,
                recipientId = "",
                recipientName = "",
                isCheckingRecipient = false,
                recipientVerificationStatus = null,
                isRecipientVerifiedInDb = false,
                validationError = null
            )
        }
    }

    fun selectRecipientWalletType(walletType: String) {
        _uiState.update {
            it.copy(
                recipientWalletType = walletType,
                recipientName = "",
                isCheckingRecipient = false,
                recipientVerificationStatus = null,
                isRecipientVerifiedInDb = false,
                validationError = null
            )
        }
        scheduleRecipientLookup(_uiState.value.recipientId)
    }

    fun updateRecipientId(id: String) {
        _uiState.update {
            it.copy(
                recipientId = id,
                recipientName = "",
                isCheckingRecipient = false,
                recipientVerificationStatus = null,
                isRecipientVerifiedInDb = false,
                validationError = null
            )
        }
        scheduleRecipientLookup(id)
    }

    private fun scheduleRecipientLookup(identifier: String) {
        recipientLookupJob?.cancel()
        if (identifier.isBlank()) return
        val lookupState = _uiState.value
        _uiState.update { it.copy(isCheckingRecipient = true, recipientVerificationStatus = null) }
        recipientLookupJob = viewModelScope.launch {
            delay(350)
            if (_uiState.value.recipientId != identifier) return@launch
            repository.lookupRecipient(
                identifier = identifier,
                lookupMethod = lookupState.sendLookupMethod,
                walletType = lookupState.recipientWalletType
            ).onSuccess { lookup ->
                _uiState.update { current ->
                    if (current.recipientId != identifier ||
                        current.sendLookupMethod != lookupState.sendLookupMethod ||
                        current.recipientWalletType != lookupState.recipientWalletType
                    ) {
                        current
                    } else {
                        current.copy(
                            isCheckingRecipient = false,
                            isRecipientVerifiedInDb = lookup.walletAvailable,
                            recipientVerificationStatus = lookup.message
                        )
                    }
                }
            }.onFailure { error ->
                _uiState.update { current ->
                    if (current.recipientId != identifier) current
                    else current.copy(
                        isCheckingRecipient = false,
                        isRecipientVerifiedInDb = false,
                        recipientVerificationStatus = error.localizedMessage
                            ?: "Unable to verify recipient right now"
                    )
                }
            }
        }
    }

    fun updateSendAmount(amountStr: String) {
        val cleanStr = amountStr.filter { (it.isDigit()) || (it == '.') }
        _uiState.update { it.copy(sendAmountString = cleanStr, validationError = null) }
    }

    fun updateSendNote(note: String) {
        _uiState.update { it.copy(sendNote = note) }
    }

    fun openSendConfirmSheet() {
        if (_uiState.value.recipientWalletType == "BITCOIN_WALLET") {
            _uiState.update {
                it.copy(validationError = "Bitcoin transfers are not available until a Bitcoin wallet is connected")
            }
            return
        }
        val amount = _uiState.value.sendAmountString.toDoubleOrNull() ?: 0.0
        val currentBal = if (_uiState.value.selectedCurrency == Currency.USD) _uiState.value.usdBalance else _uiState.value.kesBalance

        if (_uiState.value.recipientId.isBlank()) {
            _uiState.update { it.copy(validationError = "Recipient address, email, or phone is required") }
            return
        }
        if (!_uiState.value.isRecipientVerifiedInDb) {
            _uiState.update {
                it.copy(
                    validationError = if (it.isCheckingRecipient) {
                        "Wait for recipient verification to finish"
                    } else {
                        it.recipientVerificationStatus ?: "Verify that the recipient wallet exists before sending"
                    }
                )
            }
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
        if (_uiState.value.recipientWalletType == "BITCOIN_WALLET") {
            _uiState.update {
                it.copy(
                    isSendConfirmOpen = false,
                    isLoading = false,
                    validationError = "Bitcoin transfers are not available until a Bitcoin wallet is connected"
                )
            }
            return
        }
        _uiState.update { it.copy(isSendConfirmOpen = false, isLoading = true) }
        viewModelScope.launch {
            val amount = _uiState.value.sendAmountString.toDoubleOrNull() ?: 0.0
            val userId = authRepository.getCurrentUser()?.uid
            if (userId.isNullOrBlank()) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Sign in before requesting a transfer") }
                return@launch
            }
            val walletId = "wallet-${userId}-${_uiState.value.selectedCurrency.code.lowercase()}"

            val amountMinor = Math.round(amount * 100.0)
            val recipientTarget = _uiState.value.recipientId.trim()

            runCatching {
                val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                val senderWalletRef = db.collection("users").document(userId).collection("wallets").document("main")

                db.runTransaction { tx ->
                    val snap = tx.get(senderWalletRef)
                    val currentUsd = snap.getLong("usdBalanceMinorUnits") ?: 0L
                    if (currentUsd >= amountMinor) {
                        tx.update(senderWalletRef, "usdBalanceMinorUnits", currentUsd - amountMinor)
                    }
                }.await()

                val cleanRecipUid = recipientTarget.removePrefix("#").removePrefix("NV-").trim()
                val recipDoc = db.collection("users").document(cleanRecipUid).get().await()
                var targetUid = if (recipDoc.exists()) cleanRecipUid else null
                if (targetUid == null) {
                    val emailSnap = db.collection("users").whereEqualTo("email", recipientTarget).get().await()
                    if (!emailSnap.isEmpty) targetUid = emailSnap.documents.first().id
                }
                if (targetUid != null) {
                    val recipWalletRef = db.collection("users").document(targetUid).collection("wallets").document("main")
                    db.runTransaction { tx ->
                        val snap = tx.get(recipWalletRef)
                        val currentUsd = snap.getLong("usdBalanceMinorUnits") ?: 0L
                        tx.update(recipWalletRef, "usdBalanceMinorUnits", currentUsd + amountMinor)
                    }.await()
                }
            }

            val req = TransferRequest(
                idempotencyKey = "idemp_trf_${UUID.randomUUID()}",
                senderUserId = userId,
                senderWalletId = walletId,
                recipientId = _uiState.value.recipientId,
                amountMinorUnits = amountMinor,
                currency = _uiState.value.selectedCurrency,
                note = _uiState.value.sendNote
            )

            val result = repository.createTransferRequest(req)
            _uiState.update { it.copy(isLoading = false) }

            result.onSuccess { finTx ->
                val uiTx = TransactionUiModel(
                    id = finTx.transactionId,
                    title = "Transfer to ${_uiState.value.recipientId}",
                    subtitle = "Ref: ${finTx.reference}",
                    type = com.example.novaplustrader.ui.model.TransactionType.TRANSFER,
                    amount = finTx.amountDouble,
                    currency = finTx.currency,
                    isPositive = false,
                    timestamp = finTx.createdAt,
                    status = finTx.toWalletTransactionStatus()
                )

                _uiState.update {
                    it.copy(
                        lastCompletedTransaction = uiTx,
                        financialTransactions = (listOf(finTx) + it.financialTransactions)
                            .distinctBy { transaction -> transaction.transactionId }
                            .take(20)
                    )
                }

                if (finTx.status == FinancialTransactionStatus.COMPLETED) {
                    viewModelScope.launch {
                        val formattedAmt = com.example.novaplustrader.ui.util.Formatters.formatCurrency(amount, _uiState.value.selectedCurrency)
                        notificationRepository.saveNotification(
                            com.example.novaplustrader.ui.screens.notifications.models.NotificationModel(
                                id = "notif_${UUID.randomUUID().toString().take(8)}",
                                title = "Transfer Confirmed - Sent $formattedAmt",
                                body = "Transfer of $formattedAmt to ${_uiState.value.recipientId} via ${_uiState.value.recipientWalletType} was confirmed.",
                                category = com.example.novaplustrader.ui.screens.notifications.models.NotificationCategory.WALLET,
                                timestamp = "Just now",
                                isRead = false,
                                deepLink = "transaction_history"
                            )
                        )

                        notificationRepository.saveNotification(
                            com.example.novaplustrader.ui.screens.notifications.models.NotificationModel(
                                id = "notif_${UUID.randomUUID().toString().take(8)}",
                                title = "Money Received - $formattedAmt",
                                body = "You received $formattedAmt from ${_uiState.value.userName}.",
                                category = com.example.novaplustrader.ui.screens.notifications.models.NotificationCategory.WALLET,
                                timestamp = "Just now",
                                isRead = false,
                                deepLink = "activity"
                            )
                        )
                    }
                }
            }.onFailure { err ->
                _uiState.update { it.copy(errorMessage = err.message ?: "Transfer request failed") }
            }
        }
    }

    // --- RECEIVE MONEY FLOW ---
    fun selectReceiveWalletType(type: String) {
        _uiState.update { current ->
            val updated = current.copy(receiveWalletType = type)
            updated.copy(receiveQrPayload = createReceivePayload(updated))
        }
    }

    fun selectReceiveCryptoAsset(asset: String) {
        _uiState.update { current ->
            val updated = current.copy(receiveCryptoAsset = asset)
            updated.copy(receiveQrPayload = createReceivePayload(updated))
        }
    }

    fun updateRequestedReceiveAmount(amountStr: String) {
        val cleanStr = amountStr.filter { (it.isDigit()) || (it == '.') }
        _uiState.update { current ->
            val updated = current.copy(
                requestedReceiveAmountString = cleanStr
            )
            updated.copy(receiveQrPayload = createReceivePayload(updated))
        }
    }

    private fun createReceivePayload(state: WalletUiState): String {
        if (state.walletId.isBlank()) return ""
        if (state.receiveWalletType == "BITCOIN_WALLET") {
            val bitcoinWalletId = state.bitcoinWalletId?.takeIf { it.isNotBlank() } ?: return ""
            val amount = state.requestedReceiveAmountString.toDoubleOrNull()
            return Uri.Builder()
                .scheme("novaplus")
                .authority("bitcoin")
                .appendPath("receive")
                .appendQueryParameter("walletId", bitcoinWalletId)
                .apply {
                    if (amount != null && amount > 0) appendQueryParameter("amount", amount.toString())
                }
                .build()
                .toString()
        }
        val amount = state.requestedReceiveAmountString.toDoubleOrNull()
        return repository.generateReceiveRequest(
            requestedAmount = amount,
            currency = state.selectedCurrency,
            walletId = state.walletId,
            userName = state.userName
        )
    }

    fun handleScannedQrCode(scannedData: String): Boolean {
        val uri = Uri.parse(scannedData)
        val isBitcoinWalletId = uri.scheme.equals("novaplus", ignoreCase = true) &&
            uri.host.equals("bitcoin", ignoreCase = true) &&
            uri.pathSegments.firstOrNull().equals("receive", ignoreCase = true)
        val isOnChainBitcoinAddress = uri.scheme.equals("bitcoin", ignoreCase = true)
        val recipient: String
        val amount: String
        val recipientName: String
        val currency: Currency?
        if (isBitcoinWalletId || isOnChainBitcoinAddress) {
            if (isBitcoinWalletId) {
                recipient = uri.getQueryParameter("walletId").orEmpty().trim()
                amount = uri.getQueryParameter("amount").orEmpty()
            } else {
                val (address, query) = uri.schemeSpecificPart.split("?", limit = 2).let {
                    it.firstOrNull().orEmpty() to it.getOrNull(1).orEmpty()
                }
                recipient = Uri.decode(address).trim()
                amount = Uri.parse("https://qr.invalid/?$query").getQueryParameter("amount").orEmpty()
            }
            recipientName = ""
            currency = null
        } else if (uri.scheme.equals("novapay", ignoreCase = true) &&
            uri.host.equals("transfer", ignoreCase = true)
        ) {
            recipient = uri.getQueryParameter("recipient").orEmpty().trim()
            amount = uri.getQueryParameter("amount").orEmpty()
            recipientName = uri.getQueryParameter("name").orEmpty()
            currency = when (uri.getQueryParameter("currency")) {
                Currency.USD.code -> Currency.USD
                Currency.KES.code -> Currency.KES
                else -> null
            }
        } else {
            return false
        }
        if (recipient.isBlank()) return false
        val parsedAmount = amount.toDoubleOrNull()
        if (amount.isNotBlank() &&
            (parsedAmount == null || !parsedAmount.isFinite() || parsedAmount <= 0)
        ) return false

        _uiState.update { current ->
            current.copy(
                recipientId = recipient,
                recipientName = recipientName,
                recipientWalletType = if (isBitcoinWalletId || isOnChainBitcoinAddress) "BITCOIN_WALLET" else "USER_WALLET",
                sendAmountString = amount,
                sendLookupMethod = "QR_CODE",
                selectedCurrency = currency ?: current.selectedCurrency
            )
        }
        scheduleRecipientLookup(recipient)
        return true
    }

    fun clearCompletedTransaction() {
        _uiState.update { it.copy(lastCompletedTransaction = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun FinancialTransaction.toWalletTransactionStatus(): TransactionStatus = when (status) {
        FinancialTransactionStatus.COMPLETED -> TransactionStatus.COMPLETED
        FinancialTransactionStatus.SUBMITTED -> TransactionStatus.SUBMITTED
        FinancialTransactionStatus.FAILED,
        FinancialTransactionStatus.CANCELLED,
        FinancialTransactionStatus.REVERSED -> TransactionStatus.FAILED
        else -> TransactionStatus.PENDING
    }

    private fun FinancialTransaction.toUiModel(): TransactionUiModel {
        val type = when (type) {
            com.example.novaplustrader.domain.model.ledger.FinancialTransactionType.DEPOSIT ->
                TransactionType.DEPOSIT
            com.example.novaplustrader.domain.model.ledger.FinancialTransactionType.WITHDRAWAL ->
                TransactionType.WITHDRAWAL
            com.example.novaplustrader.domain.model.ledger.FinancialTransactionType.TRANSFER ->
                TransactionType.TRANSFER
            com.example.novaplustrader.domain.model.ledger.FinancialTransactionType.TRADE ->
                TransactionType.TRADE
            else -> TransactionType.PAYMENT
        }
        return TransactionUiModel(
            id = transactionId,
            title = description.ifBlank { "${type.name} request" },
            subtitle = providerReference?.let { "M-Pesa code: $it · Ref: $reference" }
                ?: "Ref: $reference",
            type = type,
            amount = amountDouble,
            currency = currency,
            isPositive = type == TransactionType.DEPOSIT,
            timestamp = createdAt,
            status = toWalletTransactionStatus()
        )
    }

    private fun List<FinancialTransaction>.pendingDepositBalance(currency: Currency): Double =
        filter {
            it.type == com.example.novaplustrader.domain.model.ledger.FinancialTransactionType.DEPOSIT &&
                it.currency == currency &&
                (it.status == FinancialTransactionStatus.SUBMITTED ||
                    it.status == FinancialTransactionStatus.PENDING ||
                    it.status == FinancialTransactionStatus.PROCESSING)
        }.sumOf { it.amountDouble }

    private fun WalletUiState.pendingDepositBalance(currency: Currency): Double =
        financialTransactions.pendingDepositBalance(currency)
}
