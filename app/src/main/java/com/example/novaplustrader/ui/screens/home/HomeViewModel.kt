package com.example.novaplustrader.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.novaplustrader.data.repository.NotificationRepository
import com.example.novaplustrader.data.repository.NotificationRepositoryImpl
import com.example.novaplustrader.ui.model.Currency
import com.example.novaplustrader.ui.model.TransactionStatus
import com.example.novaplustrader.ui.model.TransactionType
import com.example.novaplustrader.ui.model.TransactionUiModel
import com.example.novaplustrader.ui.model.WalletUiModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val notificationRepository: NotificationRepository = NotificationRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeData()
        observeUnreadNotifications()
    }

    private fun observeUnreadNotifications() {
        viewModelScope.launch {
            notificationRepository.observeUnreadCount().collect { count ->
                _uiState.update { it.copy(unreadNotificationsCount = count) }
            }
        }
    }

    fun loadHomeData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            delay(600) // Simulate fast UI state load

            val sampleWallets = listOf(
                WalletUiModel(
                    id = "w1",
                    walletId = "#NV-92841",
                    userName = "Leonard Lopez",
                    currency = Currency.USD,
                    balanceAmount = 6324.49,
                    cardNumberPlaceholder = "•••• •••• •••• 8842",
                    isPrimary = true
                ),
                WalletUiModel(
                    id = "w2",
                    walletId = "#NV-92842",
                    userName = "Leonard Lopez",
                    currency = Currency.KES,
                    balanceAmount = 824000.00,
                    cardNumberPlaceholder = "•••• •••• •••• 4109",
                    isPrimary = false
                )
            )

            val sampleTransactions = listOf(
                TransactionUiModel(
                    id = "t1",
                    title = "Bolt Food",
                    subtitle = "Payment",
                    type = TransactionType.PAYMENT,
                    amount = 26.81,
                    currency = Currency.USD,
                    isPositive = false,
                    timestamp = "Today, 1:45 PM",
                    status = TransactionStatus.COMPLETED
                ),
                TransactionUiModel(
                    id = "t2",
                    title = "Maria Wanjiku",
                    subtitle = "Transfer",
                    type = TransactionType.TRANSFER,
                    amount = 100.00,
                    currency = Currency.USD,
                    isPositive = false,
                    timestamp = "Today, 10:12 AM",
                    status = TransactionStatus.COMPLETED
                ),
                TransactionUiModel(
                    id = "t3",
                    title = "Salary Deposit",
                    subtitle = "Bank Deposit",
                    type = TransactionType.DEPOSIT,
                    amount = 2400.00,
                    currency = Currency.USD,
                    isPositive = true,
                    timestamp = "Yesterday, 4:30 PM",
                    status = TransactionStatus.COMPLETED
                ),
                TransactionUiModel(
                    id = "t4",
                    title = "Bitcoin Buy",
                    subtitle = "Crypto Purchase",
                    type = TransactionType.CRYPTO_BUY,
                    amount = 500.00,
                    currency = Currency.USD,
                    isPositive = false,
                    timestamp = "12 Oct, 9:15 AM",
                    status = TransactionStatus.COMPLETED
                )
            )

            _uiState.update {
                it.copy(
                    isLoading = false,
                    wallets = sampleWallets,
                    recentTransactions = sampleTransactions
                )
            }
        }
    }

    fun selectCurrency(currency: Currency) {
        _uiState.update { it.copy(selectedCurrency = currency) }
    }
}
