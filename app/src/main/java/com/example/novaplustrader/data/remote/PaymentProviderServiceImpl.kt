package com.example.novaplustrader.data.remote

import com.example.novaplustrader.domain.model.ledger.FinancialTransaction
import com.example.novaplustrader.domain.model.payment.PaymentLimitModel
import com.example.novaplustrader.domain.model.payment.PaymentOperation
import com.example.novaplustrader.domain.model.payment.PaymentProviderModel
import com.example.novaplustrader.domain.model.payment.PaymentProviderStatus
import com.example.novaplustrader.domain.model.payment.PaymentProviderType
import com.example.novaplustrader.ui.model.Currency
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class PaymentProviderServiceImpl : PaymentProviderService {
    private val defaultLimits = PaymentLimitModel()

    private val providers = listOf(
        PaymentProviderModel(
            providerId = "p_mpesa",
            providerName = "M-Pesa Express",
            type = PaymentProviderType.MPESA,
            supportedCurrencies = listOf(Currency.KES, Currency.USD),
            supportedOperations = listOf(PaymentOperation.DEPOSIT, PaymentOperation.WITHDRAWAL),
            status = PaymentProviderStatus.OFFLINE
        ),
        PaymentProviderModel(
            providerId = "p_bank",
            providerName = "Bank Wire",
            type = PaymentProviderType.BANK_WIRE,
            supportedCurrencies = listOf(Currency.USD, Currency.KES),
            supportedOperations = listOf(PaymentOperation.DEPOSIT, PaymentOperation.WITHDRAWAL),
            status = PaymentProviderStatus.OFFLINE
        ),
        PaymentProviderModel(
            providerId = "p_card",
            providerName = "Visa / MasterCard",
            type = PaymentProviderType.DEBIT_CREDIT_CARD,
            supportedCurrencies = listOf(Currency.USD),
            supportedOperations = listOf(PaymentOperation.DEPOSIT),
            status = PaymentProviderStatus.OFFLINE
        )
    )

    override fun getAvailableProviders(): Flow<List<PaymentProviderModel>> = flowOf(providers)

    override fun getPaymentLimits(providerType: PaymentProviderType, currency: Currency): PaymentLimitModel =
        defaultLimits

    override suspend fun initiateMpesaExpressDeposit(
        phoneNumber: String,
        amountMinorUnits: Long,
        currency: Currency,
        idempotencyKey: String
    ): Result<FinancialTransaction> = unavailable()

    override suspend fun initiateMpesaWithdrawal(
        phoneNumber: String,
        amountMinorUnits: Long,
        currency: Currency,
        securityPin: String,
        idempotencyKey: String
    ): Result<FinancialTransaction> = unavailable()

    override suspend fun queryPaymentStatus(transactionId: String): Result<FinancialTransaction> = unavailable()

    private fun <T> unavailable(): Result<T> = Result.failure(
        IllegalStateException("Payment providers are not connected. No payment or wallet transaction was created.")
    )
}
