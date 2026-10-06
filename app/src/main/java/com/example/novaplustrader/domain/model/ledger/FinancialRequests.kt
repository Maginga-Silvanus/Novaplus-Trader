package com.example.novaplustrader.domain.model.ledger

import com.example.novaplustrader.ui.model.Currency
import com.example.novaplustrader.ui.screens.wallet.models.DepositMethod
import com.example.novaplustrader.ui.screens.wallet.models.WithdrawMethod

data class DepositRequest(
    val idempotencyKey: String,
    val userId: String,
    val walletId: String,
    val method: DepositMethod,
    val amountMinorUnits: Long,
    val currency: Currency,
    val providerReference: String? = null
)

data class WithdrawalRequest(
    val idempotencyKey: String,
    val userId: String,
    val walletId: String,
    val method: WithdrawMethod,
    val destination: String,
    val amountMinorUnits: Long,
    val currency: Currency,
    val securityPin: String
)

data class TransferRequest(
    val idempotencyKey: String,
    val senderUserId: String,
    val senderWalletId: String,
    val recipientId: String,
    val amountMinorUnits: Long,
    val currency: Currency,
    val note: String = ""
)

data class RefundRequest(
    val idempotencyKey: String,
    val originalTransactionId: String,
    val reason: String,
    val requestedByUid: String
)

data class ReversalRequest(
    val idempotencyKey: String,
    val originalTransactionId: String,
    val reason: String,
    val requestedByUid: String
)
