package com.example.novaplustrader.ui.model

data class WalletUiModel(
    val id: String,
    val walletId: String,
    val userName: String,
    val currency: Currency,
    val balanceAmount: Double,
    val cardNumberPlaceholder: String,
    val isPrimary: Boolean = false
)
