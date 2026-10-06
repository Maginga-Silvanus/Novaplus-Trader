package com.example.novaplustrader.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.novaplustrader.data.repository.AdminRepository
import com.example.novaplustrader.data.repository.AdminRepositoryImpl
import com.example.novaplustrader.ui.screens.admin.models.AdminTransactionModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminTransactionsUiState(
    val transactions: List<AdminTransactionModel> = emptyList(),
    val searchQuery: String = "",
    val selectedTypeFilter: String = "ALL", // ALL, DEPOSIT, WITHDRAWAL, TRANSFER, TRADE, FEE, REFUND, ADJUSTMENT
    val selectedStatusFilter: String = "ALL", // ALL, PENDING, PROCESSING, COMPLETED, FAILED, CANCELLED, REVERSED
    val selectedTransactionDetails: AdminTransactionModel? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class AdminTransactionsViewModel(
    private val repository: AdminRepository = AdminRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminTransactionsUiState())
    val uiState: StateFlow<AdminTransactionsUiState> = _uiState.asStateFlow()

    init {
        loadTransactions()
    }

    fun loadTransactions() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val typeFilter = if (_uiState.value.selectedTypeFilter == "ALL") null else _uiState.value.selectedTypeFilter
            val statusFilter = if (_uiState.value.selectedStatusFilter == "ALL") null else _uiState.value.selectedStatusFilter

            repository.getTransactions(
                searchQuery = _uiState.value.searchQuery,
                typeFilter = typeFilter,
                statusFilter = statusFilter
            ).collect { list ->
                _uiState.update {
                    it.copy(
                        transactions = list,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        loadTransactions()
    }

    fun onTypeFilterSelected(type: String) {
        _uiState.update { it.copy(selectedTypeFilter = type) }
        loadTransactions()
    }

    fun onStatusFilterSelected(status: String) {
        _uiState.update { it.copy(selectedStatusFilter = status) }
        loadTransactions()
    }

    fun selectTransactionDetails(tx: AdminTransactionModel?) {
        _uiState.update { it.copy(selectedTransactionDetails = tx) }
    }
}
