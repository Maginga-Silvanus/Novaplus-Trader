package com.example.novaplustrader.ui.screens.trading

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.novaplustrader.ui.screens.trading.models.Timeframe
import com.example.novaplustrader.ui.screens.trading.models.TradeDirection
import com.example.novaplustrader.ui.screens.trading.models.TradeDuration
import com.example.novaplustrader.ui.screens.trading.models.TradingAsset
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

class TradingViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(TradingUiState())
    val uiState: StateFlow<TradingUiState> = _uiState.asStateFlow()

    fun selectAsset(asset: TradingAsset) {
        _uiState.update { it.copy(selectedAsset = asset, isAssetPickerOpen = false) }
    }

    fun selectTimeframe(timeframe: Timeframe) {
        _uiState.update { it.copy(selectedTimeframe = timeframe) }
    }

    fun selectDirection(direction: TradeDirection) {
        _uiState.update { it.copy(selectedDirection = direction) }
    }

    fun updateTradeAmount(amountStr: String) {
        val cleanStr = amountStr.filter { (it.isDigit()) || (it == '.') }
        val amount = cleanStr.toDoubleOrNull() ?: 0.0
        val balance = _uiState.value.availableBalance

        val error = when {
            amount > balance -> "Amount exceeds available balance ($${String.format(Locale.US, "%.2f", balance)})"
            else -> null
        }

        _uiState.update {
            it.copy(
                tradeAmountString = cleanStr,
                selectedPercentage = null,
                validationError = error
            )
        }
    }

    fun applyPercentageShortcut(percentage: Int) {
        val balance = _uiState.value.availableBalance
        val calculatedAmount = (balance * (percentage / 100.0))
        val formattedAmount = String.format(Locale.US, "%.2f", calculatedAmount)

        _uiState.update {
            it.copy(
                tradeAmountString = formattedAmount,
                selectedPercentage = percentage,
                validationError = null
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
        val amount = _uiState.value.tradeAmountString.toDoubleOrNull() ?: 0.0
        val balance = _uiState.value.availableBalance

        if (amount <= 0) {
            _uiState.update { it.copy(validationError = "Please enter a valid trade amount") }
            return
        }
        if (amount > balance) {
            _uiState.update { it.copy(validationError = "Amount exceeds available balance") }
            return
        }

        _uiState.update { it.copy(validationError = null, isConfirmSheetOpen = true) }
    }

    fun closeConfirmSheet() {
        _uiState.update { it.copy(isConfirmSheetOpen = false) }
    }

    fun executeTrade() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            delay(800) // Simulate order placement network call

            val amount = _uiState.value.tradeAmountString.toDoubleOrNull() ?: 0.0
            val newBalance = _uiState.value.availableBalance - amount

            _uiState.update {
                it.copy(
                    isLoading = false,
                    isConfirmSheetOpen = false,
                    isSuccessDialogOpen = true,
                    availableBalance = newBalance
                )
            }
        }
    }

    fun dismissSuccessDialog() {
        _uiState.update { it.copy(isSuccessDialogOpen = false) }
    }
}
