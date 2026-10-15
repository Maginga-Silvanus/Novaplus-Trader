package com.example.novaplustrader.ui.screens.wallet.models

import com.example.novaplustrader.ui.model.Currency

enum class TransactionState(val label: String) {
    INITIATED("Initiated"),
    PENDING("Pending"),
    PROCESSING("Processing"),
    COMPLETED("Completed"),
    FAILED("Failed"),
    CANCELLED("Cancelled")
}

enum class DepositMethod(
    val title: String,
    val description: String,
    val feeText: String
) {
    MPESA(
        title = "TEST wallet credit",
        description = "Simulated balance only. No payment provider is contacted.",
        feeText = "TEST ONLY"
    ),
    BANK(
        title = "Bank Wire Transfer",
        description = "Direct deposit from localized banking partners",
        feeText = "1.2% Fee"
    ),
    CARD(
        title = "Debit / Credit Card",
        description = "Instant card authorization (Visa / MasterCard)",
        feeText = "1.5% Fee"
    ),
    CRYPTO(
        title = "Crypto Deposit",
        description = "Deposit USDT, BTC, or ETH directly to your vault address",
        feeText = "0% Fee"
    )
}

enum class WithdrawMethod(
    val title: String,
    val description: String,
    val feeText: String
) {
    MPESA(
        title = "TEST wallet withdrawal",
        description = "Simulated balance only. No payout is sent.",
        feeText = "TEST ONLY"
    ),
    BANK(
        title = "Bank Account",
        description = "Wire payout to connected bank account",
        feeText = "$2.50 Fee"
    ),
    CARD(
        title = "Debit / Credit Card",
        description = "Payout directly to connected Visa or MasterCard",
        feeText = "1.5% Fee"
    ),
    CRYPTO_WALLET(
        title = "External Crypto Wallet",
        description = "Transfer USDT or crypto to external wallet address",
        feeText = "Network Gas Fee"
    )
}

data class TransactionFee(
    val feeAmount: Double,
    val currency: Currency,
    val feeType: String = "FIXED",
    val description: String = "Standard processing fee"
)
