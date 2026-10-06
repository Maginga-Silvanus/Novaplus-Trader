package com.example.novaplustrader.ui.screens.wallet

import com.example.novaplustrader.domain.model.ledger.FinancialTransaction
import com.example.novaplustrader.ui.model.Currency
import com.example.novaplustrader.ui.model.TransactionUiModel
import com.example.novaplustrader.ui.model.WalletUiModel
import com.example.novaplustrader.ui.screens.wallet.models.DepositMethod
import com.example.novaplustrader.ui.screens.wallet.models.WithdrawMethod

data class WalletUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val userName: String = "Leonard Lopez",
    val walletId: String = "#NV-92841",
    val selectedCurrency: Currency = Currency.USD,

    // Granular Ledger Balances
    val usdBalance: Double = 6324.49,
    val usdPendingBalance: Double = 0.0,
    val usdLockedBalance: Double = 0.0,

    val kesBalance: Double = 824000.00,
    val kesPendingBalance: Double = 0.0,
    val kesLockedBalance: Double = 0.0,

    val todayChangeAmountUsd: Double = 284.50,
    val todayChangeAmountKes: Double = 37000.00,
    val todayChangePercentage: Double = 4.82,
    val wallets: List<WalletUiModel> = emptyList(),
    val transactions: List<TransactionUiModel> = emptyList(),
    val financialTransactions: List<FinancialTransaction> = emptyList(),
    val selectedTransactionFilter: String = "ALL",
    val selectedTransaction: TransactionUiModel? = null,

    // Deposit Flow
    val selectedDepositMethod: DepositMethod = DepositMethod.MPESA,
    val depositAmountString: String = "100",
    val depositFee: Double = 0.0,
    val isDepositConfirmOpen: Boolean = false,
    val lastCompletedTransaction: TransactionUiModel? = null,

    // Withdraw Flow
    val selectedWithdrawMethod: WithdrawMethod = WithdrawMethod.MPESA,
    val withdrawDestination: String = "+254 712 345 678",
    val withdrawAmountString: String = "50",
    val withdrawFee: Double = 0.50,
    val securityPin: String = "",
    val isWithdrawConfirmOpen: Boolean = false,
    val isPinVerificationOpen: Boolean = false,

    // Send Flow
    val recipientId: String = "maria.wanjiku@novapay.io",
    val sendAmountString: String = "25",
    val sendNote: String = "Dinner split",
    val isSendConfirmOpen: Boolean = false,

    // Receive Flow
    val requestedReceiveAmountString: String = "",
    val receiveQrPayload: String = "novapay://transfer?recipient=#NV-92841&currency=USD",

    val selectedPercentage: Int? = null,
    val validationError: String? = null
) {
    val currentAvailableBalance: Double
        get() = if (selectedCurrency == Currency.USD) usdBalance else kesBalance

    val currentPendingBalance: Double
        get() = if (selectedCurrency == Currency.USD) usdPendingBalance else kesPendingBalance

    val currentLockedBalance: Double
        get() = if (selectedCurrency == Currency.USD) usdLockedBalance else kesLockedBalance

    val currentTotalBalance: Double
        get() = currentAvailableBalance + currentPendingBalance + currentLockedBalance
}
