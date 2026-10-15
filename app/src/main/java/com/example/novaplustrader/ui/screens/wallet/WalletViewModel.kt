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
import kotlinx.coroutines.launch
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

    init {
        observeWalletRepository()
        observeCryptoMarketData()
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
        listOf("BTC", "ETH", "SOL", "BNB").forEach { asset ->
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
                                                usdPendingBalance = 0.0,
                                                usdLockedBalance = 0.0,
                                                kesBalance = wallet.kesBalance,
                                                kesPendingBalance = 0.0,
                                                kesLockedBalance = 0.0,
                                                wallets = listOf(
                                                    WalletUiModel(
                                                        id = "main-usd",
                                                        walletId = current.walletId,
                                                        userName = current.userName,
                                                        currency = Currency.USD,
                                                        balanceAmount = wallet.usdBalance,
                                                        cardNumberPlaceholder = "USD · TEST FUNDS",
                                                        isPrimary = true
                                                    ),
                                                    WalletUiModel(
                                                        id = "main-kes",
                                                        walletId = current.walletId,
                                                        userName = current.userName,
                                                        currency = Currency.KES,
                                                        balanceAmount = wallet.kesBalance,
                                                        cardNumberPlaceholder = "KES · TEST FUNDS",
                                                        isPrimary = false
                                                    )
                                                )
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
                                            current.copy(transactions = records.map { transaction ->
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
                                            })
                                        }
                                    }
                            }
                        }
                    }
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
        val amountMinorUnits = Math.round(amount * 100.0)
        if (amount <= 0.0 || !amount.isFinite() || amountMinorUnits <= 0L ||
            kotlin.math.abs(amount * 100.0 - kotlin.math.round(amount * 100.0)) > 1e-8
        ) {
            _uiState.update { it.copy(validationError = "Enter a valid TEST funds amount") }
            return
        }
        val idempotencyKey = testWalletOperationKey("DEPOSIT", state.selectedCurrency, amountMinorUnits)
        _uiState.update { it.copy(isDepositConfirmOpen = false, isLoading = true, testWalletNotice = null) }
        viewModelScope.launch {
            val userId = authRepository.getCurrentUser()?.uid
            if (userId.isNullOrBlank()) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Sign in before requesting a deposit") }
                return@launch
            }
            val result = mainWalletRepository.simulateOperation(
                operation = "DEPOSIT",
                currency = state.selectedCurrency,
                amountMinorUnits = amountMinorUnits,
                idempotencyKey = idempotencyKey
            )
            result.onSuccess { finTx ->
                clearTestWalletOperationKey(idempotencyKey)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        testWalletNotice = finTx.message,
                        lastCompletedTransaction = if (finTx.status == "COMPLETED_TEST") {
                            TransactionUiModel(
                                id = idempotencyKey,
                                title = "TEST funds added",
                                subtitle = finTx.message,
                                type = TransactionType.DEPOSIT,
                                amount = amountMinorUnits / 100.0,
                                currency = state.selectedCurrency,
                                isPositive = true,
                                timestamp = "Just now",
                                status = TransactionStatus.COMPLETED
                            )
                        } else null,
                        errorMessage = if (finTx.status == "COMPLETED_TEST") null else finTx.message
                    )
                }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        testWalletNotice = err.message ?: "TEST deposit failed",
                        errorMessage = err.message ?: "TEST deposit failed"
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
                executeTestWithdrawal()
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
        val amountMinorUnits = Math.round(amount * 100.0)
        if (!amount.isFinite() || amount <= 0.0 || amountMinorUnits <= 0L ||
            kotlin.math.abs(amount * 100.0 - kotlin.math.round(amount * 100.0)) > 1e-8
        ) {
            _uiState.update { it.copy(validationError = "Enter a valid TEST withdrawal amount") }
            return
        }
        val idempotencyKey = testWalletOperationKey("WITHDRAWAL", state.selectedCurrency, amountMinorUnits)
        _uiState.update {
            it.copy(isWithdrawConfirmOpen = false, isPinVerificationOpen = false, isLoading = true, testWalletNotice = null)
        }
        viewModelScope.launch {
            val userId = authRepository.getCurrentUser()?.uid
            if (userId.isNullOrBlank()) {
                _uiState.update { it.copy(isLoading = false, securityPin = "", errorMessage = "Sign in before requesting a withdrawal") }
                return@launch
            }
            val result = mainWalletRepository.simulateOperation(
                operation = "WITHDRAWAL",
                currency = state.selectedCurrency,
                amountMinorUnits = amountMinorUnits,
                idempotencyKey = idempotencyKey
            )
            result.onSuccess { response ->
                clearTestWalletOperationKey(idempotencyKey)
                val succeeded = response.status == "COMPLETED_TEST"
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        securityPin = "",
                        testWalletNotice = response.message,
                        errorMessage = if (succeeded) null else response.message,
                        lastCompletedTransaction = if (succeeded) TransactionUiModel(
                            id = idempotencyKey,
                            title = "TEST funds withdrawn",
                            subtitle = response.message,
                            type = TransactionType.WITHDRAWAL,
                            amount = amountMinorUnits / 100.0,
                            currency = state.selectedCurrency,
                            isPositive = false,
                            timestamp = "Just now",
                            status = TransactionStatus.COMPLETED
                        ) else null
                    )
                }

            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        securityPin = "",
                        testWalletNotice = err.message ?: "TEST withdrawal failed",
                        errorMessage = err.message ?: "TEST withdrawal failed"
                    )
                }
            }
        }
    }

    private fun testWalletOperationKey(operation: String, currency: Currency, amountMinorUnits: Long): String {
        val fingerprint = "$operation|${currency.name}|$amountMinorUnits"
        val current = _uiState.value
        if (current.pendingTestWalletOperationFingerprint == fingerprint) {
            return current.pendingTestWalletOperationKey
                ?: error("Pending test wallet operation is missing its idempotency key")
        }
        val key = UUID.randomUUID().toString()
        _uiState.update {
            it.copy(
                pendingTestWalletOperationKey = key,
                pendingTestWalletOperationFingerprint = fingerprint
            )
        }
        return key
    }

    private fun clearTestWalletOperationKey(key: String) {
        _uiState.update { current ->
            if (current.pendingTestWalletOperationKey == key) {
                current.copy(
                    pendingTestWalletOperationKey = null,
                    pendingTestWalletOperationFingerprint = null
                )
            } else current
        }
    }

    // --- SEND MONEY FLOW ---
    fun selectSendLookupMethod(method: String) {
        _uiState.update { it.copy(sendLookupMethod = method, recipientId = "", validationError = null) }
    }

    fun selectRecipientWalletType(walletType: String) {
        _uiState.update {
            it.copy(recipientWalletType = walletType, recipientName = "", validationError = null)
        }
    }

    fun updateRecipientId(id: String) {
        _uiState.update {
            it.copy(recipientId = id, recipientName = "", validationError = null)
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
            val walletId = if (_uiState.value.selectedCurrency == Currency.USD) "w_usd" else "w_kes"

            val req = TransferRequest(
                idempotencyKey = "idemp_trf_${UUID.randomUUID().toString().take(12)}",
                senderUserId = userId,
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
                        lastCompletedTransaction = uiTx.takeIf { finTx.status == FinancialTransactionStatus.COMPLETED },
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
        FinancialTransactionStatus.FAILED,
        FinancialTransactionStatus.CANCELLED,
        FinancialTransactionStatus.REVERSED -> TransactionStatus.FAILED
        else -> TransactionStatus.PENDING
    }
}
