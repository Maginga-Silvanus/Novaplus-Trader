package com.example.novaplustrader.ui.screens.home

import com.example.novaplustrader.ui.model.Currency
import com.example.novaplustrader.ui.model.TransactionUiModel
import com.example.novaplustrader.ui.model.WalletUiModel

data class HomeUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val notificationErrorMessage: String? = null,
    val cryptoPortfolioError: String? = null,
    val userName: String = "Leonard Lopez",
    val userInitials: String = "LL",
    val greeting: String = "Good afternoon",
    val unreadNotificationsCount: Int = 3,
    val selectedCurrency: Currency = Currency.USD,
    val totalBalanceUsd: Double = 5.00,
    val totalBalanceKes: Double = 0.0,
    val todayChangeAmountUsd: Double = 0.0,
    val todayChangeAmountKes: Double = 0.0,
    val todayChangePercentage: Double = 0.0,
    val wallets: List<WalletUiModel> = emptyList(),
    val recentTransactions: List<TransactionUiModel> = emptyList()
)
