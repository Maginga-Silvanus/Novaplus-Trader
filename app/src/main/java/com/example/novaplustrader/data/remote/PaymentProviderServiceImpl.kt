package com.example.novaplustrader.data.remote

import com.example.novaplustrader.domain.model.ledger.FinancialTransaction
import com.example.novaplustrader.domain.model.ledger.FinancialTransactionStatus
import com.example.novaplustrader.domain.model.ledger.FinancialTransactionType
import com.example.novaplustrader.domain.model.payment.PaymentError
import com.example.novaplustrader.domain.model.payment.PaymentLimitModel
import com.example.novaplustrader.domain.model.payment.PaymentOperation
import com.example.novaplustrader.domain.model.payment.PaymentProviderModel
import com.example.novaplustrader.domain.model.payment.PaymentProviderStatus
import com.example.novaplustrader.domain.model.payment.PaymentProviderType
import com.example.novaplustrader.ui.model.Currency
import com.example.novaplustrader.util.PhoneNumberUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PaymentProviderServiceImpl : PaymentProviderService {

    private val defaultLimits = PaymentLimitModel()

    private val _providers = MutableStateFlow(
        listOf(
            PaymentProviderModel(
                providerId = "p_mpesa",
                providerName = "M-Pesa Express",
                type = PaymentProviderType.MPESA,
                supportedCurrencies = listOf(Currency.KES, Currency.USD),
                supportedOperations = listOf(PaymentOperation.DEPOSIT, PaymentOperation.WITHDRAWAL),
                status = PaymentProviderStatus.OPERATIONAL
            ),
            PaymentProviderModel(
                providerId = "p_bank",
                providerName = "Bank Wire",
                type = PaymentProviderType.BANK_WIRE,
                supportedCurrencies = listOf(Currency.USD, Currency.KES),
                supportedOperations = listOf(PaymentOperation.DEPOSIT, PaymentOperation.WITHDRAWAL),
                status = PaymentProviderStatus.OPERATIONAL
            ),
            PaymentProviderModel(
                providerId = "p_card",
                providerName = "Visa / MasterCard",
                type = PaymentProviderType.DEBIT_CREDIT_CARD,
                supportedCurrencies = listOf(Currency.USD),
                supportedOperations = listOf(PaymentOperation.DEPOSIT),
                status = PaymentProviderStatus.OPERATIONAL
            )
        )
    )

    override fun getAvailableProviders(): Flow<List<PaymentProviderModel>> = _providers.asStateFlow()

    override fun getPaymentLimits(
        providerType: PaymentProviderType,
        currency: Currency
    ): PaymentLimitModel = defaultLimits

    override suspend fun initiateMpesaExpressDeposit(
        phoneNumber: String,
        amountMinorUnits: Long,
        currency: Currency,
        idempotencyKey: String
    ): Result<FinancialTransaction> {
        delay(700) // Backend validation & M-Pesa STK Push API invocation

        val normalizedPhone = PhoneNumberUtils.normalizeKenyanPhone(phoneNumber)
            ?: return Result.failure(Exception(PaymentError.INVALID_PHONE.userMessage))

        if (amountMinorUnits < defaultLimits.minDepositMinorUnits) {
            return Result.failure(Exception(PaymentError.LIMIT_EXCEEDED.userMessage))
        }

        val txId = "tx_mpesa_dep_${System.currentTimeMillis().toString().takeLast(6)}"
        val time = getFormattedTime()

        val transaction = FinancialTransaction(
            transactionId = txId,
            userId = "usr_9901",
            walletId = if (currency == Currency.USD) "w_usd" else "w_kes",
            type = FinancialTransactionType.DEPOSIT,
            currency = currency,
            amountMinorUnits = amountMinorUnits,
            feeMinorUnits = 0L,
            status = FinancialTransactionStatus.PENDING, // PENDING user STK Push authorization
            idempotencyKey = idempotencyKey,
            reference = "MPESA-STK-${normalizedPhone.takeLast(4)}",
            providerReference = "ws_CO_01022024_${System.currentTimeMillis().toString().takeLast(8)}",
            description = "M-Pesa STK Deposit to ${normalizedPhone}",
            createdAt = time,
            updatedAt = time
        )

        return Result.success(transaction)
    }

    override suspend fun initiateMpesaWithdrawal(
        phoneNumber: String,
        amountMinorUnits: Long,
        currency: Currency,
        securityPin: String,
        idempotencyKey: String
    ): Result<FinancialTransaction> {
        delay(800) // Backend balance check & security PIN verification

        if (securityPin.length < 4) {
            return Result.failure(Exception(PaymentError.VERIFICATION_FAILED.userMessage))
        }

        val normalizedPhone = PhoneNumberUtils.normalizeKenyanPhone(phoneNumber)
            ?: return Result.failure(Exception(PaymentError.INVALID_PHONE.userMessage))

        if (amountMinorUnits < defaultLimits.minWithdrawalMinorUnits) {
            return Result.failure(Exception(PaymentError.LIMIT_EXCEEDED.userMessage))
        }

        val txId = "tx_mpesa_wd_${System.currentTimeMillis().toString().takeLast(6)}"
        val time = getFormattedTime()
        val fee = if (currency == Currency.KES) 3000L else 50L // KES 30 or $0.50

        val transaction = FinancialTransaction(
            transactionId = txId,
            userId = "usr_9901",
            walletId = if (currency == Currency.USD) "w_usd" else "w_kes",
            type = FinancialTransactionType.WITHDRAWAL,
            currency = currency,
            amountMinorUnits = amountMinorUnits,
            feeMinorUnits = fee,
            status = FinancialTransactionStatus.PROCESSING, // RESERVED & PROCESSING payout
            idempotencyKey = idempotencyKey,
            reference = "MPESA-B2C-${normalizedPhone.takeLast(4)}",
            providerReference = "B2C_${System.currentTimeMillis().toString().takeLast(8)}",
            description = "M-Pesa B2C Withdrawal to ${normalizedPhone}",
            createdAt = time,
            updatedAt = time
        )

        return Result.success(transaction)
    }

    override suspend fun queryPaymentStatus(transactionId: String): Result<FinancialTransaction> {
        delay(300)
        val time = getFormattedTime()

        val mockCompleted = FinancialTransaction(
            transactionId = transactionId,
            userId = "usr_9901",
            walletId = "w_kes",
            type = FinancialTransactionType.DEPOSIT,
            currency = Currency.KES,
            amountMinorUnits = 500000L,
            feeMinorUnits = 0L,
            status = FinancialTransactionStatus.COMPLETED,
            idempotencyKey = "idemp_query_$transactionId",
            reference = "MPESA-CONFIRMED",
            providerReference = "MPESA_REF_991823",
            description = "M-Pesa Express Deposit Confirmed",
            createdAt = time,
            updatedAt = time,
            completedAt = time
        )

        return Result.success(mockCompleted)
    }

    private fun getFormattedTime(): String {
        val sdf = SimpleDateFormat("dd MMM, HH:mm", Locale.US)
        return sdf.format(Date())
    }
}
