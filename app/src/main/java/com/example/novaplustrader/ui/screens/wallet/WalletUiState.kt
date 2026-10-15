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
    val testWalletNotice: String? = null,
    val pendingTestWalletOperationKey: String? = null,
    val pendingTestWalletOperationFingerprint: String? = null,
    val userName: String = "Nova Trader",
    val walletId: String = "",
    val selectedCurrency: Currency = Currency.USD,

    // Granular Ledger Balances
    val usdBalance: Double = 0.0,
    val usdPendingBalance: Double = 0.0,
    val usdLockedBalance: Double = 0.0,

    val kesBalance: Double = 0.0,
    val kesPendingBalance: Double = 0.0,
    val kesLockedBalance: Double = 0.0,

    val todayChangeAmountUsd: Double = 0.0,
    val todayChangeAmountKes: Double = 0.0,
    val todayChangePercentage: Double = 0.0,
    val wallets: List<WalletUiModel> = emptyList(),
    val transactions: List<TransactionUiModel> = emptyList(),
    val financialTransactions: List<FinancialTransaction> = emptyList(),
    val selectedTransactionFilter: String = "ALL",
    val selectedTransaction: TransactionUiModel? = null,

    // Deposit Flow
    val selectedDepositMethod: DepositMethod = DepositMethod.MPESA,
    val depositAmountString: String = "",
    val depositFee: Double = 0.0,
    val mpesaPhoneNumber: String = "",
    val selectedBankName: String = "KCB Bank",
    val bankAccountNumber: String = "",
    val cardHolderName: String = "",
    val cardNumber: String = "",
    val cardExpiry: String = "",
    val cardCvv: String = "",
    val isCardSaved: Boolean = false,
    val selectedCryptoAsset: String = "BTC",
    val cryptoBtcBalance: Double = 0.0,
    val cryptoEthBalance: Double = 0.0,
    val cryptoUsdtBalance: Double = 0.0,
    val cryptoUsdcBalance: Double = 0.0,
    val cryptoSolBalance: Double = 0.0,
    val cryptoBnbBalance: Double = 0.0,
    val cryptoWalletMode: String = "disabled",
    val cryptoPricesUsd: Map<String, Double> = emptyMap(),
    val cryptoMarketUpdatedAt: String? = null,
    val cryptoMarketError: String? = null,
    val isDepositConfirmOpen: Boolean = false,
    val lastCompletedTransaction: TransactionUiModel? = null,

    // Withdraw Flow
    val selectedWithdrawMethod: WithdrawMethod = WithdrawMethod.MPESA,
    val withdrawDestination: String = "",
    val withdrawAmountString: String = "",
    val withdrawFee: Double = 0.50,
    val savedCards: List<String> = emptyList(),
    val selectedSavedCardIndex: Int = 0,
    val isAddNewCardForWithdrawal: Boolean = false,
    val securityPin: String = "",
    val isWithdrawConfirmOpen: Boolean = false,
    val isPinVerificationOpen: Boolean = false,

    // Send Flow
    val sendLookupMethod: String = "EMAIL", // "EMAIL", "WALLET_CODE", "PHONE_KYC", "QR_CODE"
    val recipientWalletType: String = "USER_WALLET", // "USER_WALLET", "BITCOIN_WALLET"
    val recipientId: String = "",
    val recipientName: String = "",
    val sendAmountString: String = "",
    val sendNote: String = "",
    val isSendConfirmOpen: Boolean = false,

    // Receive Flow
    val receiveWalletType: String = "USER_WALLET", // "USER_WALLET", "CRYPTO_WALLET"
    val receiveCryptoAsset: String = "BTC",
    val requestedReceiveAmountString: String = "",
    val bitcoinWalletId: String? = null,
    val receiveQrPayload: String = "",

    val selectedPercentage: Int? = null,
    val validationError: String? = null
) {
    val userInitials: String
        get() {
            val parts = userName.trim().split(" ").filter { it.isNotBlank() }
            return parts.mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString("").uppercase().ifEmpty { "NT" }
        }

    val currentAvailableBalance: Double
        get() = if (selectedCurrency == Currency.USD) usdBalance else kesBalance

    val currentPendingBalance: Double
        get() = if (selectedCurrency == Currency.USD) usdPendingBalance else kesPendingBalance

    val currentLockedBalance: Double
        get() = if (selectedCurrency == Currency.USD) usdLockedBalance else kesLockedBalance

    val currentTotalBalance: Double
        get() = currentAvailableBalance + currentPendingBalance + currentLockedBalance

    val cryptoPortfolioValueUsd: Double?
        get() {
            val holdings = mapOf(
                "BTC" to cryptoBtcBalance,
                "ETH" to cryptoEthBalance,
                "SOL" to cryptoSolBalance,
                "BNB" to cryptoBnbBalance,
                "USDT" to cryptoUsdtBalance,
                "USDC" to cryptoUsdcBalance
            )
            if (holdings.all { it.value == 0.0 }) return 0.0
            if (holdings.any { (asset, quantity) ->
                    quantity > 0.0 && cryptoPricesUsd[asset]?.let { it > 0.0 } != true
                }
            ) return null
            return holdings.entries.sumOf { (asset, quantity) ->
                quantity * (cryptoPricesUsd[asset] ?: 0.0)
            }
        }
}
