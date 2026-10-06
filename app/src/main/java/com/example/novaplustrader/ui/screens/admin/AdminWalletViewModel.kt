package com.example.novaplustrader.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.novaplustrader.data.repository.AdminRepository
import com.example.novaplustrader.data.repository.AdminRepositoryImpl
import com.example.novaplustrader.ui.screens.admin.models.AdminWalletModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminWalletUiState(
    val wallets: List<AdminWalletModel> = emptyList(),
    val searchQuery: String = "",
    val currencyFilter: String = "ALL", // ALL, USD, KES, EUR
    val statusFilter: String = "ALL", // ALL, ACTIVE, HOLD, LOCKED
    val isLoading: Boolean = false,
    val selectedWalletForAction: AdminWalletModel? = null,
    val showRiskHoldConfirmation: Boolean = false,
    val showLedgerAdjustmentConfirmation: Boolean = false,
    val adjustmentAmount: String = "",
    val actionReason: String = "",
    val isActionInProgress: Boolean = false,
    val actionSuccessMessage: String? = null,
    val errorMessage: String? = null
)

class AdminWalletViewModel(
    private val repository: AdminRepository = AdminRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminWalletUiState())
    val uiState: StateFlow<AdminWalletUiState> = _uiState.asStateFlow()

    init {
        loadWallets()
    }

    fun loadWallets() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val cFilter = if (_uiState.value.currencyFilter == "ALL") null else _uiState.value.currencyFilter
            val sFilter = if (_uiState.value.statusFilter == "ALL") null else _uiState.value.statusFilter

            repository.getWallets(
                searchQuery = _uiState.value.searchQuery,
                currencyFilter = cFilter,
                statusFilter = sFilter
            ).collect { list ->
                _uiState.update {
                    it.copy(
                        wallets = list,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        loadWallets()
    }

    fun onCurrencyFilterSelected(currency: String) {
        _uiState.update { it.copy(currencyFilter = currency) }
        loadWallets()
    }

    fun onStatusFilterSelected(status: String) {
        _uiState.update { it.copy(statusFilter = status) }
        loadWallets()
    }

    fun initiateRiskHoldToggle(wallet: AdminWalletModel) {
        _uiState.update {
            it.copy(
                selectedWalletForAction = wallet,
                showRiskHoldConfirmation = true,
                actionReason = ""
            )
        }
    }

    fun initiateLedgerAdjustmentRequest(wallet: AdminWalletModel) {
        _uiState.update {
            it.copy(
                selectedWalletForAction = wallet,
                showLedgerAdjustmentConfirmation = true,
                adjustmentAmount = "",
                actionReason = ""
            )
        }
    }

    fun updateActionReason(reason: String) {
        _uiState.update { it.copy(actionReason = reason) }
    }

    fun updateAdjustmentAmount(amount: String) {
        _uiState.update { it.copy(adjustmentAmount = amount) }
    }

    fun dismissConfirmations() {
        _uiState.update {
            it.copy(
                selectedWalletForAction = null,
                showRiskHoldConfirmation = false,
                showLedgerAdjustmentConfirmation = false,
                actionReason = "",
                adjustmentAmount = ""
            )
        }
    }

    fun confirmRiskHoldToggle() {
        val wallet = _uiState.value.selectedWalletForAction ?: return
        val newRiskState = !wallet.riskHold
        val reason = _uiState.value.actionReason.ifBlank { "Administrative risk flag toggle" }

        viewModelScope.launch {
            _uiState.update { it.copy(isActionInProgress = true) }
            val result = repository.requestWalletRiskHold(wallet.walletId, newRiskState, reason)
            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        isActionInProgress = false,
                        showRiskHoldConfirmation = false,
                        selectedWalletForAction = null,
                        actionSuccessMessage = "Wallet risk hold status updated.",
                        actionReason = ""
                    )
                }
                loadWallets()
            } else {
                _uiState.update {
                    it.copy(
                        isActionInProgress = false,
                        errorMessage = "Failed to update risk hold status."
                    )
                }
            }
        }
    }

    fun confirmLedgerAdjustmentRequest() {
        val wallet = _uiState.value.selectedWalletForAction ?: return
        val amount = _uiState.value.adjustmentAmount.toDoubleOrNull() ?: 0.0
        val reason = _uiState.value.actionReason.ifBlank { "Operational ledger correction request" }

        if (amount == 0.0) {
            _uiState.update { it.copy(errorMessage = "Please specify a valid non-zero adjustment amount.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isActionInProgress = true) }
            val result = repository.requestLedgerAdjustment(wallet.walletId, amount, wallet.currency, reason)
            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        isActionInProgress = false,
                        showLedgerAdjustmentConfirmation = false,
                        selectedWalletForAction = null,
                        actionSuccessMessage = "Ledger adjustment request submitted to backend authoritative service.",
                        adjustmentAmount = "",
                        actionReason = ""
                    )
                }
                loadWallets()
            } else {
                _uiState.update {
                    it.copy(
                        isActionInProgress = false,
                        errorMessage = "Backend ledger adjustment request rejected."
                    )
                }
            }
        }
    }

    fun clearSuccessMessage() {
        _uiState.update { it.copy(actionSuccessMessage = null) }
    }
}
