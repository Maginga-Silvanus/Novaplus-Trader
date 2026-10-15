package com.example.novaplustrader.data.repository

import com.example.novaplustrader.domain.model.ledger.FinancialTransaction
import com.example.novaplustrader.domain.model.payment.PaymentLimitModel
import com.example.novaplustrader.domain.model.payment.PaymentProviderModel
import com.example.novaplustrader.domain.model.payment.PaymentProviderType
import com.example.novaplustrader.ui.model.Currency
import kotlinx.coroutines.flow.StateFlow

interface PaymentRepository {
    fun observeProviders(): StateFlow<List<PaymentProviderModel>>
    fun getPaymentLimits(providerType: PaymentProviderType, currency: Currency): PaymentLimitModel

    suspend fun requestMpesaDeposit(
        phoneNumber: String,
        amountMinorUnits: Long,
        currency: Currency,
        idempotencyKey: String
    ): Result<FinancialTransaction>

    suspend fun requestMpesaWithdrawal(
        phoneNumber: String,
        amountMinorUnits: Long,
        currency: Currency,
        securityPin: String,
        idempotencyKey: String
    ): Result<FinancialTransaction>

    suspend fun queryPaymentStatus(transactionId: String): Result<FinancialTransaction>
}
