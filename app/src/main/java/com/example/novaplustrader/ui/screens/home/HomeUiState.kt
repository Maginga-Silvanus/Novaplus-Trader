package com.example.novaplustrader.ui.screens.home

import com.example.novaplustrader.ui.model.Currency
import com.example.novaplustrader.ui.model.TransactionUiModel
import com.example.novaplustrader.ui.model.WalletUiModel

data class HomeUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val userName: String = "Leonard Lopez",
    val userInitials: String = "LL",
    val greeting: String = "Good afternoon",
    val unreadNotificationsCount: Int = 3,
    val selectedCurrency: Currency = Currency.USD,
    val totalBalanceUsd: Double = 6324.49,
    val totalBalanceKes: Double = 824000.00,
    val todayChangeAmountUsd: Double = 284.50,
    val todayChangeAmountKes: Double = 37000.00,
    val todayChangePercentage: Double = 4.82,
    val wallets: List<WalletUiModel> = emptyList(),
    val recentTransactions: List<TransactionUiModel> = emptyList()
)
