package com.example.novaplustrader.ui.screens.bot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.novaplustrader.data.repository.AuthRepository
import com.example.novaplustrader.data.repository.AuthRepositoryImpl
import com.example.novaplustrader.data.repository.MainUserWalletRepository
import com.example.novaplustrader.data.repository.MainUserWalletRepositoryImpl
import com.example.novaplustrader.data.repository.TradingBotRepository
import com.example.novaplustrader.data.repository.TradingBotRepositoryImpl
import com.example.novaplustrader.ui.screens.bot.models.BotConfiguration
import com.example.novaplustrader.ui.screens.bot.models.BotRiskLevel
import com.example.novaplustrader.ui.screens.bot.models.BotStrategy
import com.example.novaplustrader.ui.screens.trading.models.TradingAsset
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

class TradingBotViewModel(
    private val repository: TradingBotRepository = TradingBotRepositoryImpl(),
    private val authRepository: AuthRepository = AuthRepositoryImpl(),
    private val mainUserWalletRepository: MainUserWalletRepository = MainUserWalletRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(TradingBotUiState())
    val uiState: StateFlow<TradingBotUiState> = _uiState.asStateFlow()

    init {
        observeRepositoryData()
        observeWalletBalance()
    }

    private fun observeWalletBalance() {
        viewModelScope.launch {
            authRepository.observeAuthState().collect { user ->
                if (user != null) {
                    mainUserWalletRepository.observeWallet(user.uid).collect { wallet ->
                        _uiState.update { it.copy(availableBalance = wallet.usdBalance) }
                    }
                }
            }
        }
    }

    private fun observeRepositoryData() {
        viewModelScope.launch {
            launch {
                repository.observeBotStatus().collect { status ->
                    _uiState.update { it.copy(botStatus = status) }
                }
            }
            launch {
                repository.observeBotConfiguration().collect { config ->
                    _uiState.update { it.copy(configuration = config) }
                }
            }
            launch {
                repository.observeActiveTrades().collect { trades ->
                    _uiState.update { it.copy(activeTrades = trades) }
                }
            }
            launch {
                repository.observeBotPerformance().collect { perf ->
                    _uiState.update { it.copy(performance = perf) }
                }
            }
            launch {
                repository.observeBotHistory().collect { history ->
                    _uiState.update { it.copy(activityHistory = history) }
                }
            }
        }
    }

    fun selectAsset(asset: TradingAsset) {
        val updatedConfig = _uiState.value.configuration.copy(asset = asset)
        repository.updateConfiguration(updatedConfig)
        _uiState.update { it.copy(isAssetPickerOpen = false) }
    }

    fun selectStrategy(strategy: BotStrategy) {
        val updatedConfig = _uiState.value.configuration.copy(strategy = strategy)
        repository.updateConfiguration(updatedConfig)
    }

    fun selectRiskLevel(riskLevel: BotRiskLevel) {
        val updatedConfig = _uiState.value.configuration.copy(riskLevel = riskLevel)
        repository.updateConfiguration(updatedConfig)
    }

    fun updateAllocatedAmount(amountStr: String) {
        val cleanStr = amountStr.filter { it.isDigit() || it == '.' }
        val amount = cleanStr.toDoubleOrNull() ?: 0.0
        val balance = _uiState.value.availableBalance

        val error = when {
            amount > balance -> "Allocation exceeds available balance ($${String.format(Locale.US, "%.2f", balance)})"
            else -> null
        }

        val updatedConfig = _uiState.value.configuration.copy(allocatedAmount = amount)
        repository.updateConfiguration(updatedConfig)

        _uiState.update {
            it.copy(
                selectedPercentage = null,
                validationError = error
            )
        }
    }

    fun applyPercentageShortcut(percentage: Int) {
        val balance = _uiState.value.availableBalance
        val calculatedAmount = (balance * (percentage / 100.0))

        val updatedConfig = _uiState.value.configuration.copy(allocatedAmount = calculatedAmount)
        repository.updateConfiguration(updatedConfig)

        _uiState.update {
            it.copy(
                selectedPercentage = percentage,
                validationError = null
            )
        }
    }

    fun updateCustomParameters(
        entryThreshold: Double,
        stopLoss: Double,
        takeProfit: Double,
        maxTrades: Int,
        durationHours: Int
    ) {
        val updatedConfig = _uiState.value.configuration.copy(
            entryThresholdPercent = entryThreshold.coerceAtLeast(0.1),
            stopLossPercent = stopLoss.coerceAtLeast(0.1),
            takeProfitPercent = takeProfit.coerceAtLeast(0.1),
            maxOpenTrades = maxTrades.coerceAtLeast(1),
            maxSessionDurationHours = durationHours.coerceAtLeast(1)
        )
        repository.updateConfiguration(updatedConfig)
    }

    fun openAssetPicker() {
        _uiState.update { it.copy(isAssetPickerOpen = true) }
    }

    fun closeAssetPicker() {
        _uiState.update { it.copy(isAssetPickerOpen = false) }
    }

    fun onRequestStartBot() {
        val config = _uiState.value.configuration
        if (config.allocatedAmount <= 0) {
            _uiState.update { it.copy(validationError = "Please enter a valid allocation amount") }
            return
        }
        if (config.allocatedAmount > _uiState.value.availableBalance) {
            _uiState.update { it.copy(validationError = "Allocation exceeds available balance") }
            return
        }

        _uiState.update { it.copy(validationError = null, isStartConfirmSheetOpen = true) }
    }

    fun closeStartConfirmSheet() {
        _uiState.update { it.copy(isStartConfirmSheetOpen = false) }
    }

    fun confirmStartBot() {
        _uiState.update { it.copy(isStartConfirmSheetOpen = false, isLoading = true) }
        viewModelScope.launch {
            val result = repository.startBot(securityPinVerified = true)
            _uiState.update { it.copy(isLoading = false) }
            if (result.isFailure) {
                _uiState.update { it.copy(errorMessage = result.exceptionOrNull()?.message ?: "Failed to start bot") }
            }
        }
    }

    fun onRequestStopBot() {
        _uiState.update { it.copy(isStopConfirmSheetOpen = true) }
    }

    fun closeStopConfirmSheet() {
        _uiState.update { it.copy(isStopConfirmSheetOpen = false) }
    }

    fun confirmStopBot() {
        _uiState.update { it.copy(isStopConfirmSheetOpen = false, isLoading = true) }
        viewModelScope.launch {
            repository.stopBot()
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
