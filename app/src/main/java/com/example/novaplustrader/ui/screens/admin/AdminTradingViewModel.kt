package com.example.novaplustrader.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.novaplustrader.data.repository.AdminRepository
import com.example.novaplustrader.data.repository.AdminRepositoryImpl
import com.example.novaplustrader.ui.screens.admin.models.AdminTradeModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminTradingUiState(
    val trades: List<AdminTradeModel> = emptyList(),
    val searchQuery: String = "",
    val selectedStatusFilter: String = "ALL", // ALL, ACTIVE, PENDING, COMPLETED, FAILED, CANCELLED
    val selectedPairFilter: String = "ALL", // ALL, BTC/USD, ETH/USD, EUR/USD
    val selectedTradeForCancellation: AdminTradeModel? = null,
    val cancellationReason: String = "",
    val isActionInProgress: Boolean = false,
    val isLoading: Boolean = false,
    val actionSuccessMessage: String? = null,
    val errorMessage: String? = null
)

class AdminTradingViewModel(
    private val repository: AdminRepository = AdminRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminTradingUiState())
    val uiState: StateFlow<AdminTradingUiState> = _uiState.asStateFlow()

    init {
        loadTrades()
    }

    fun loadTrades() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val statusFilter = if (_uiState.value.selectedStatusFilter == "ALL") null else _uiState.value.selectedStatusFilter
            val pairFilter = if (_uiState.value.selectedPairFilter == "ALL") null else _uiState.value.selectedPairFilter

            repository.getTrades(
                searchQuery = _uiState.value.searchQuery,
                statusFilter = statusFilter,
                pairFilter = pairFilter
            ).collect { list ->
                _uiState.update {
                    it.copy(
                        trades = list,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        loadTrades()
    }

    fun onStatusFilterSelected(status: String) {
        _uiState.update { it.copy(selectedStatusFilter = status) }
        loadTrades()
    }

    fun onPairFilterSelected(pair: String) {
        _uiState.update { it.copy(selectedPairFilter = pair) }
        loadTrades()
    }

    fun initiateTradeCancellation(trade: AdminTradeModel) {
        _uiState.update {
            it.copy(
                selectedTradeForCancellation = trade,
                cancellationReason = ""
            )
        }
    }

    fun updateCancellationReason(reason: String) {
        _uiState.update { it.copy(cancellationReason = reason) }
    }

    fun dismissCancellation() {
        _uiState.update {
            it.copy(
                selectedTradeForCancellation = null,
                cancellationReason = ""
            )
        }
    }

    fun confirmTradeCancellationRequest() {
        val trade = _uiState.value.selectedTradeForCancellation ?: return
        val reason = _uiState.value.cancellationReason.ifBlank { "Administrative trade intervention" }

        viewModelScope.launch {
            _uiState.update { it.copy(isActionInProgress = true) }
            val result = repository.requestTradeCancellation(trade.tradeId, reason)
            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        isActionInProgress = false,
                        selectedTradeForCancellation = null,
                        actionSuccessMessage = "Trade cancellation request submitted to backend settlement engine.",
                        cancellationReason = ""
                    )
                }
                loadTrades()
            } else {
                _uiState.update {
                    it.copy(
                        isActionInProgress = false,
                        errorMessage = "Trade cancellation request failed."
                    )
                }
            }
        }
    }

    fun clearSuccessMessage() {
        _uiState.update { it.copy(actionSuccessMessage = null) }
    }
}
