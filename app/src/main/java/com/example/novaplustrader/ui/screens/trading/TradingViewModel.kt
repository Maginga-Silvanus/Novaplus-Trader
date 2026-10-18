package com.example.novaplustrader.ui.screens.trading

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.novaplustrader.data.repository.MarketDataRepository
import com.example.novaplustrader.data.repository.MarketDataRepositoryImpl
import com.example.novaplustrader.data.repository.AuthRepository
import com.example.novaplustrader.data.repository.AuthRepositoryImpl
import com.example.novaplustrader.data.repository.CryptoWalletRepository
import com.example.novaplustrader.data.repository.CryptoWalletRepositoryImpl
import com.example.novaplustrader.data.repository.CryptoWalletSnapshot
import com.example.novaplustrader.data.repository.MainUserWalletRepository
import com.example.novaplustrader.data.repository.MainUserWalletRepositoryImpl
import com.example.novaplustrader.data.repository.SpotTradeRequest
import com.example.novaplustrader.domain.model.trading.MarketCandle
import com.example.novaplustrader.domain.model.trading.MarketConnectionState
import com.example.novaplustrader.domain.model.trading.MarketTick
import com.example.novaplustrader.ui.screens.trading.models.Timeframe
import com.example.novaplustrader.ui.screens.trading.models.TradeDirection
import com.example.novaplustrader.ui.screens.trading.models.TradeDuration
import com.example.novaplustrader.ui.screens.trading.models.TradingAsset
import com.example.novaplustrader.ui.model.Currency
import com.example.novaplustrader.ui.util.Formatters
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

class TradingViewModel(
    private val authRepository: AuthRepository = AuthRepositoryImpl(),
    private val cryptoWalletRepository: CryptoWalletRepository = CryptoWalletRepositoryImpl(),
    private val mainUserWalletRepository: MainUserWalletRepository = MainUserWalletRepositoryImpl(),
    private val marketDataRepository: MarketDataRepository = MarketDataRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(TradingUiState())
    val uiState: StateFlow<TradingUiState> = _uiState.asStateFlow()
    private val marketSelection = MutableStateFlow(defaultAssetList.first().symbol to Timeframe.M1)

    init {
        observeCryptoWallet()
        observeMarketData()
    }

    private fun observeMarketData() {
        viewModelScope.launch {
            marketDataRepository.observeConnectionState().collect { connectionState ->
                _uiState.update { it.copy(marketConnectionState = connectionState) }
            }
        }
        viewModelScope.launch {
            marketSelection.collectLatest { (asset, timeframe) ->
                launch {
                    marketDataRepository.observeMarketTicks(asset)
                        .catch { error ->
                            _uiState.update {
                                it.copy(
                                    marketConnectionState = MarketConnectionState.DISCONNECTED,
                                    marketDataError = error.localizedMessage ?: "Unable to load live market prices"
                                )
                            }
                        }
                        .collect { tick -> applyMarketTick(tick) }
                }
                launch {
                    marketDataRepository.observeMarketCandles(asset, timeframe)
                        .catch { error ->
                            _uiState.update {
                                it.copy(marketDataError = error.localizedMessage ?: "Unable to load market chart data")
                            }
                        }
                        .collect { candles ->
                            if (candles.isNotEmpty()) applyMarketCandles(asset, timeframe, candles)
                        }
                }
            }
        }
        viewModelScope.launch {
            marketDataRepository.observeMarkets()
                .catch { error ->
                    _uiState.update {
                        it.copy(
                            marketIsLoading = it.marketSummaries.isEmpty(),
                            marketDataError = error.localizedMessage ?: "Unable to load Binance spot markets"
                        )
                    }
                }
                .collect { markets ->
                    val summariesByPair = markets.associateBy { it.pair }
                    _uiState.update { state ->
                        val updatedAssets = state.availableAssets.map { asset ->
                            val summary = summariesByPair[asset.symbol] ?: return@map asset
                            asset.copy(
                                name = summary.name,
                                currentPrice = summary.lastPrice,
                                change24hPercentage = summary.priceChangePercent24h,
                                high24h = summary.highPrice24h,
                                low24h = summary.lowPrice24h,
                                volume24h = formatCompactVolume(summary.quoteVolume24h)
                            )
                        }
                        state.copy(
                            marketSummaries = markets,
                            marketIsLoading = false,
                            marketDataError = null,
                            availableAssets = updatedAssets,
                            selectedAsset = updatedAssets.firstOrNull { it.symbol == state.selectedAsset.symbol }
                                ?: state.selectedAsset
                        )
                    }
                }
        }
    }

    private fun applyMarketTick(tick: MarketTick) {
        _uiState.update { state ->
            val updateAsset: (TradingAsset) -> TradingAsset = { asset ->
                if (asset.symbol != tick.asset) {
                    asset
                } else {
                    asset.copy(
                        currentPrice = tick.lastPriceDouble,
                        change24hPercentage = tick.change24hPercentage ?: asset.change24hPercentage,
                        high24h = tick.high24h ?: asset.high24h,
                        low24h = tick.low24h ?: asset.low24h,
                        volume24h = tick.quoteVolume24h?.let(::formatCompactVolume) ?: asset.volume24h
                    )
                }
            }
            val selectedAsset = updateAsset(state.selectedAsset)
            state.copy(
                selectedAsset = selectedAsset,
                availableAssets = state.availableAssets.map(updateAsset),
                marketConnectionState = if (tick.isStale) MarketConnectionState.STALE else MarketConnectionState.CONNECTED,
                marketDataError = if (tick.isStale) "Showing the last price received; live market data is unavailable." else null,
                marketDataTimestamp = tick.timestamp,
                isMarketPriceStale = tick.isStale
            )
        }
    }

    private fun applyMarketCandles(assetSymbol: String, timeframe: Timeframe, candles: List<MarketCandle>) {
        _uiState.update { state ->
            val selectedCandles = candles.filter { it.asset == assetSymbol && it.timeframe == timeframe }
            val updateAsset: (TradingAsset) -> TradingAsset = { asset ->
                if (asset.symbol == assetSymbol) {
                    asset.copy(
                        candleDataMap = asset.candleDataMap + (timeframe to selectedCandles)
                    )
                } else {
                    asset
                }
            }
            val selectedAsset = updateAsset(state.selectedAsset)
            state.copy(
                selectedAsset = selectedAsset,
                availableAssets = state.availableAssets.map(updateAsset),
                marketDataError = null
            )
        }
    }

    private fun formatCompactVolume(volume: Double): String = when {
        volume >= 1_000_000_000 -> String.format(Locale.US, "%.2fB", volume / 1_000_000_000)
        volume >= 1_000_000 -> String.format(Locale.US, "%.2fM", volume / 1_000_000)
        volume >= 1_000 -> String.format(Locale.US, "%.2fK", volume / 1_000)
        else -> String.format(Locale.US, "%.2f", volume)
    }

    private fun observeCryptoWallet() {
        viewModelScope.launch {
            authRepository.observeAuthState().collectLatest { user ->
                if (user == null) {
                    _uiState.update {
                        it.copy(
                            userId = null,
                            bitcoinWalletId = null,
                            assetBalances = emptyMap(),
                            availableBalance = 0.0,
                            recentTradeRequests = emptyList(),
                            walletMode = "disabled"
                        )
                    }
                    return@collectLatest
                }

                _uiState.update { it.copy(userId = user.uid) }
                cryptoWalletRepository.ensureWallet(user.uid)
                    .onFailure { error ->
                        _uiState.update {
                            it.copy(validationError = error.localizedMessage ?: "Unable to initialize your crypto wallet")
                        }
                    }
                coroutineScope {
                    launch {
                        mainUserWalletRepository.observeWallet(user.uid)
                            .catch { error ->
                                _uiState.update {
                                    it.copy(validationError = error.localizedMessage ?: "Unable to load TEST USD balance")
                                }
                            }
                            .collect { wallet ->
                                _uiState.update {
                                    it.copy(availableBalance = wallet.usdBalance, currency = Currency.USD)
                                }
                            }
                    }
                    launch {
                        cryptoWalletRepository.observeTradeRequests(user.uid)
                            .catch { error ->
                                _uiState.update {
                                    it.copy(validationError = error.localizedMessage ?: "Unable to load crypto trade history")
                                }
                            }
                            .collect { requests ->
                                _uiState.update { it.copy(recentTradeRequests = requests) }
                        }
                    }
                    cryptoWalletRepository.observeWallet(user.uid)
                        .catch { error ->
                            _uiState.update {
                                it.copy(validationError = error.localizedMessage ?: "Unable to load your crypto wallet")
                            }
                        }
                        .collect { wallet ->
                            _uiState.update { it.withWallet(wallet) }
                        }
                }
            }
        }
    }

    fun selectAsset(asset: TradingAsset) {
        _uiState.update {
            val updated = it.copy(
                selectedAsset = asset,
                isAssetPickerOpen = false,
                isMarketPriceStale = true,
                marketDataTimestamp = null,
                marketDataError = null,
                pendingTradeRequest = null
            )
            updated.copy(validationError = validateOrder(updated, updated.tradeAmountString.toDoubleOrNull() ?: 0.0))
        }
        marketSelection.value = asset.symbol to _uiState.value.selectedTimeframe
    }

    fun selectTimeframe(timeframe: Timeframe) {
        _uiState.update { it.copy(selectedTimeframe = timeframe, isMarketPriceStale = true) }
        marketSelection.value = _uiState.value.selectedAsset.symbol to timeframe
    }

    fun selectDirection(direction: TradeDirection) {
        _uiState.update {
            val updated = it.copy(selectedDirection = direction, pendingTradeRequest = null)
            updated.copy(validationError = validateOrder(updated, updated.tradeAmountString.toDoubleOrNull() ?: 0.0))
        }
    }

    fun updateTradeAmount(amountStr: String) {
        val cleanStr = amountStr.filter { (it.isDigit()) || (it == '.') }
        val amount = cleanStr.toDoubleOrNull() ?: 0.0
        val currentState = _uiState.value
        val error = validateOrder(currentState, amount)

        _uiState.update {
            it.copy(
                tradeAmountString = cleanStr,
                selectedPercentage = null,
                pendingTradeRequest = null,
                validationError = error
            )
        }
    }

    fun applyPercentageShortcut(percentage: Int) {
        val currentState = _uiState.value
        val balance = availableOrderBalance(currentState)
        val calculatedAmount = (balance * (percentage / 100.0))
        val formattedAmount = if (currentState.selectedDirection == TradeDirection.BUY) {
            String.format(Locale.US, "%.2f", calculatedAmount)
        } else {
            String.format(Locale.US, "%.8f", calculatedAmount)
        }

        _uiState.update {
            it.copy(
                tradeAmountString = formattedAmount,
                selectedPercentage = percentage,
                pendingTradeRequest = null,
                validationError = validateOrder(currentState, calculatedAmount)
            )
        }
    }

    fun selectDuration(duration: TradeDuration) {
        _uiState.update { it.copy(selectedDuration = duration) }
    }

    fun openAssetPicker() {
        _uiState.update { it.copy(isAssetPickerOpen = true) }
    }

    fun closeAssetPicker() {
        _uiState.update { it.copy(isAssetPickerOpen = false) }
    }

    fun openConfirmSheet() {
        val currentState = _uiState.value
        val amount = currentState.tradeAmountString.toDoubleOrNull() ?: 0.0
        val validationError = validateOrder(currentState, amount)
        if (validationError != null) {
            _uiState.update { it.copy(validationError = validationError) }
            return
        }

        _uiState.update { it.copy(validationError = null, isConfirmSheetOpen = true) }
    }

    fun closeConfirmSheet() {
        _uiState.update { it.copy(isConfirmSheetOpen = false) }
    }

    fun executeTrade() {
        val state = _uiState.value
        val userId = state.userId
        if (userId.isNullOrBlank()) {
            _uiState.update { it.copy(validationError = "Sign in to buy or sell crypto") }
            return
        }
        val amount = state.tradeAmountString.toDoubleOrNull() ?: 0.0
        val validationError = validateOrder(state, amount)
        if (validationError != null) {
            _uiState.update { it.copy(validationError = validationError) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, validationError = null) }
            val assetCode = state.selectedAsset.symbol.substringBefore("/")
            val quote = state.selectedAsset.currentPrice
            val slippage = 0.01
            val limitPrice = when (state.selectedDirection) {
                TradeDirection.BUY -> quote * (1 + slippage)
                TradeDirection.SELL -> quote * (1 - slippage)
            }
            val request = state.pendingTradeRequest ?: SpotTradeRequest(
                    idempotencyKey = UUID.randomUUID().toString(),
                    asset = assetCode,
                    pair = state.selectedAsset.symbol,
                    direction = state.selectedDirection,
                    amount = amount,
                    amountDenomination = if (state.selectedDirection == TradeDirection.BUY) "USD" else "ASSET",
                    quotePriceUsd = quote,
                    limitPriceUsd = limitPrice,
                    quoteObservedAtMs = System.currentTimeMillis()
                )
            _uiState.update { it.copy(pendingTradeRequest = request) }
            val result = cryptoWalletRepository.submitSpotTradeRequest(request)
            result.onSuccess { response ->
                if (response.status == "FILLED_TEST") {
                    val receiptMessage = response.receipt?.let(::formatSimulatedTradeReceipt)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isConfirmSheetOpen = false,
                            isSuccessDialogOpen = true,
                            tradeSuccessMessage = receiptMessage ?: response.message
                                ?: "SIMULATED TRADE recorded. No exchange order or on-chain transfer occurred.",
                            pendingTradeRequest = null,
                            validationError = null
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isConfirmSheetOpen = false,
                            pendingTradeRequest = null,
                            validationError = response.message ?: "Trade request status: ${response.status}"
                        )
                    }
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isConfirmSheetOpen = true,
                        validationError = error.localizedMessage ?: "Crypto order failed"
                    )
                }
            }
        }
    }

    fun selectChartType(chartType: String) {
        _uiState.update { it.copy(selectedChartType = chartType) }
    }

    fun selectPercentage(percentage: Int) {
        val total = availableOrderBalance(_uiState.value)
        val target = total * (percentage / 100.0)
        _uiState.update {
            it.copy(
                selectedPercentage = percentage,
                tradeAmountString = String.format(Locale.US, "%.2f", target),
                validationError = null
            )
        }
    }

    fun dismissSuccessDialog() {
        _uiState.update { it.copy(isSuccessDialogOpen = false) }
    }

    private fun validateOrder(state: TradingUiState, amount: Double): String? {
        if (!amount.isFinite() || amount <= 0.0) return "Please enter a valid trade amount"
        if (state.isMarketPriceStale || state.selectedAsset.currentPrice <= 0.0) {
            return "Waiting for a live market price. Try again when the market feed is connected."
        }
        val assetCode = state.selectedAsset.symbol.substringBefore("/")
        return when (state.selectedDirection) {
            TradeDirection.BUY -> {
                if (amount * 100.0 < state.minimumTradeUsdMinorUnits) {
                    "Minimum paper order is $${String.format(Locale.US, "%.2f", state.minimumTradeUsdMinorUnits / 100.0)}"
                } else {
                    val grossMinorUnits = kotlin.math.round(amount * 100.0).toLong()
                    val feeMinorUnits = kotlin.math.ceil(grossMinorUnits * state.paperTradeFeeBps / 10_000.0).toLong()
                    if (grossMinorUnits + feeMinorUnits > (state.availableBalance * 100.0).toLong()) {
                        "Amount plus the simulated fee exceeds TEST USD funds"
                    } else null
                }
            }
            TradeDirection.SELL -> {
                val assetBalance = state.assetBalances[assetCode] ?: 0.0
                if (amount * state.selectedAsset.currentPrice < state.minimumTradeUsdMinorUnits / 100.0) {
                    "Minimum paper sale value is $${String.format(Locale.US, "%.2f", state.minimumTradeUsdMinorUnits / 100.0)}"
                } else if (amount > assetBalance) {
                    "Insufficient $assetCode balance to sell this amount"
                } else {
                    null
                }
            }
        }
    }

    private fun availableOrderBalance(state: TradingUiState): Double =
        when (state.selectedDirection) {
            TradeDirection.BUY -> state.availableBalance
            TradeDirection.SELL -> {
                val assetCode = state.selectedAsset.symbol.substringBefore("/")
                state.assetBalances[assetCode] ?: 0.0
            }
        }

    private fun formatSimulatedTradeReceipt(receipt: com.example.novaplustrader.data.repository.SpotTradeReceipt): String {
        val scale = when (receipt.asset) {
            "SOL" -> 1_000_000_000.0
            "USDT", "USDC" -> 1_000_000.0
            else -> 100_000_000.0
        }
        val decimals = when (receipt.asset) {
            "SOL" -> 9
            "USDT", "USDC" -> 6
            else -> 8
        }
        val quantity = String.format(Locale.US, "%.${decimals}f", receipt.quantityAtomicUnits / scale)
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss 'UTC'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(Date(receipt.createdAtMs))
        return listOf(
            "SIMULATED TRADE · TEST FUNDS ONLY",
            "${receipt.direction} $quantity ${receipt.asset}",
            "Execution rate: ${Formatters.formatCurrency(receipt.executionPriceUsd, Currency.USD)} per ${receipt.asset}",
            "Order value: ${Formatters.formatCurrency(receipt.grossAmountMinorUnits / 100.0, Currency.USD)}",
            "Simulated fee: ${Formatters.formatCurrency(receipt.feeMinorUnits / 100.0, Currency.USD)}",
            "Cash ${if (receipt.direction == "BUY") "debited" else "credited"}: ${Formatters.formatCurrency(receipt.netAmountMinorUnits / 100.0, Currency.USD)}",
            "Status: ${receipt.status}",
            "Reference: ${receipt.idempotencyKey}",
            "Recorded: $timestamp",
            "No exchange order or on-chain transfer occurred."
        ).joinToString(separator = "\n")
    }

    private fun TradingUiState.withWallet(wallet: CryptoWalletSnapshot): TradingUiState = copy(
        bitcoinWalletId = wallet.bitcoinWalletId,
        assetBalances = wallet.assetBalances,
        walletMode = wallet.mode,
        paperTradeFeeBps = wallet.paperTradeFeeBps,
        minimumTradeUsdMinorUnits = wallet.minimumTradeUsdMinorUnits,
        validationError = null
    )
}
