package com.example.novaplustrader.data.remote

import com.example.novaplustrader.domain.model.ledger.FinancialTransaction
import com.example.novaplustrader.domain.model.payment.PaymentLimitModel
import com.example.novaplustrader.domain.model.payment.PaymentProviderModel
import com.example.novaplustrader.domain.model.payment.PaymentProviderType
import com.example.novaplustrader.domain.model.payment.PaymentRequest
import com.example.novaplustrader.ui.model.Currency
import kotlinx.coroutines.flow.Flow

interface PaymentProviderService {
    fun getAvailableProviders(): Flow<List<PaymentProviderModel>>
    fun getPaymentLimits(providerType: PaymentProviderType, currency: Currency): PaymentLimitModel

    suspend fun initiateMpesaExpressDeposit(
        phoneNumber: String,
        amountMinorUnits: Long,
        currency: Currency,
        idempotencyKey: String
    ): Result<FinancialTransaction>

    suspend fun initiateMpesaWithdrawal(
        phoneNumber: String,
        amountMinorUnits: Long,
        currency: Currency,
        securityPin: String,
        idempotencyKey: String
    ): Result<FinancialTransaction>

    suspend fun queryPaymentStatus(transactionId: String): Result<FinancialTransaction>
}
