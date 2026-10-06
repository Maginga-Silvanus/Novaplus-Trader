package com.example.novaplustrader.ui.util

import com.example.novaplustrader.ui.model.Currency
import java.text.NumberFormat
import java.util.Locale

object Formatters {
    fun formatCurrency(amount: Double, currency: Currency): String {
        val numberFormat = NumberFormat.getNumberInstance(Locale.US).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }
        val formattedNumber = numberFormat.format(amount)
        return when (currency) {
            Currency.USD -> "$$formattedNumber"
            Currency.KES -> "KES $formattedNumber"
        }
    }

    fun formatPercentage(percentage: Double): String {
        val prefix = if (percentage >= 0) "+" else ""
        return "$prefix${String.format(Locale.US, "%.2f", percentage)}%"
    }
}
