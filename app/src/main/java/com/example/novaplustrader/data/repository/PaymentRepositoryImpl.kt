package com.example.novaplustrader.data.repository

import com.example.novaplustrader.data.remote.PaymentProviderService
import com.example.novaplustrader.data.remote.PaymentProviderServiceImpl
import com.example.novaplustrader.domain.model.ledger.FinancialTransaction
import com.example.novaplustrader.domain.model.payment.PaymentLimitModel
import com.example.novaplustrader.domain.model.payment.PaymentProviderModel
import com.example.novaplustrader.domain.model.payment.PaymentProviderType
import com.example.novaplustrader.ui.model.Currency
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PaymentRepositoryImpl(
    private val paymentService: PaymentProviderService = PaymentProviderServiceImpl()
) : PaymentRepository {

    private val scope = CoroutineScope(Dispatchers.Default)
    private val _providers = MutableStateFlow<List<PaymentProviderModel>>(emptyList())

    init {
        scope.launch {
            paymentService.getAvailableProviders().collect { list ->
                _providers.value = list
            }
        }
    }

    override fun observeProviders(): StateFlow<List<PaymentProviderModel>> = _providers.asStateFlow()

    override fun getPaymentLimits(
        providerType: PaymentProviderType,
        currency: Currency
    ): PaymentLimitModel {
        return paymentService.getPaymentLimits(providerType, currency)
    }

    override suspend fun requestMpesaDeposit(
        phoneNumber: String,
        amountMinorUnits: Long,
        currency: Currency,
        idempotencyKey: String
    ): Result<FinancialTransaction> {
        return paymentService.initiateMpesaExpressDeposit(phoneNumber, amountMinorUnits, currency, idempotencyKey)
    }

    override suspend fun requestMpesaWithdrawal(
        phoneNumber: String,
        amountMinorUnits: Long,
        currency: Currency,
        securityPin: String,
        idempotencyKey: String
    ): Result<FinancialTransaction> {
        return paymentService.initiateMpesaWithdrawal(phoneNumber, amountMinorUnits, currency, securityPin, idempotencyKey)
    }

    override suspend fun queryPaymentStatus(transactionId: String): Result<FinancialTransaction> {
        return paymentService.queryPaymentStatus(transactionId)
    }
}
