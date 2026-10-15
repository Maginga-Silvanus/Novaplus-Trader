package com.example.novaplustrader

import com.example.novaplustrader.domain.model.payment.PaymentError
import com.example.novaplustrader.domain.model.payment.PaymentLimitModel
import com.example.novaplustrader.domain.model.payment.PaymentProviderModel
import com.example.novaplustrader.domain.model.payment.PaymentProviderStatus
import com.example.novaplustrader.domain.model.payment.PaymentProviderType
import com.example.novaplustrader.ui.model.Currency
import com.example.novaplustrader.util.PhoneNumberUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PaymentProviderTest {

    @Test
    fun testKenyanPhoneNumberNormalization() {
        assertEquals("254712345678", PhoneNumberUtils.normalizeKenyanPhone("0712345678"))
        assertEquals("254112345678", PhoneNumberUtils.normalizeKenyanPhone("0112345678"))
        assertEquals("254712345678", PhoneNumberUtils.normalizeKenyanPhone("+254 712 345 678"))
        assertEquals("254712345678", PhoneNumberUtils.normalizeKenyanPhone("254712345678"))

        assertNull(PhoneNumberUtils.normalizeKenyanPhone("12345"))
        assertNull(PhoneNumberUtils.normalizeKenyanPhone("0812345678"))
    }

    @Test
    fun testPhoneNumberMasking() {
        val masked = PhoneNumberUtils.maskPhoneNumber("0712345678")
        assertEquals("2547****5678", masked)
    }

    @Test
    fun testPaymentLimitsValidation() {
        val limits = PaymentLimitModel()
        assertEquals(10.00, limits.minDepositDouble, 0.001)
        assertEquals(700000.00, limits.maxDepositDouble, 0.001)
        assertEquals(100.00, limits.minWithdrawalDouble, 0.001)
        assertEquals(250000.00, limits.maxWithdrawalDouble, 0.001)
    }

    @Test
    fun testPaymentProviderModel() {
        val provider = PaymentProviderModel(
            providerId = "p_mpesa",
            providerName = "M-Pesa Express",
            type = PaymentProviderType.MPESA,
            supportedCurrencies = listOf(Currency.KES, Currency.USD),
            supportedOperations = emptyList(),
            status = PaymentProviderStatus.OPERATIONAL
        )

        assertEquals("M-Pesa Express", provider.providerName)
        assertEquals(PaymentProviderStatus.OPERATIONAL, provider.status)
        assertTrue(provider.supportedCurrencies.contains(Currency.KES))
    }

    @Test
    fun testPaymentErrorUserMessages() {
        val err = PaymentError.INVALID_PHONE
        assertTrue(err.userMessage.contains("Kenyan phone number"))
    }
}
