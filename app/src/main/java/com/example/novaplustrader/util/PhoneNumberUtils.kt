package com.example.novaplustrader.util

object PhoneNumberUtils {

    fun normalizeKenyanPhone(rawPhone: String): String? {
        val clean = rawPhone.replace("\\s+".toRegex(), "").replace("-", "").replace("+", "")

        return when {
            clean.startsWith("254") && clean.length == 12 && (clean[3] == '7' || clean[3] == '1') -> clean
            clean.startsWith("0") && clean.length == 10 && (clean[1] == '7' || clean[1] == '1') -> "254${clean.substring(1)}"
            clean.length == 9 && (clean[0] == '7' || clean[0] == '1') -> "254$clean"
            else -> null
        }
    }

    fun isValidKenyanPhone(rawPhone: String): Boolean {
        return normalizeKenyanPhone(rawPhone) != null
    }

    fun maskPhoneNumber(rawPhone: String): String {
        val normalized = normalizeKenyanPhone(rawPhone) ?: rawPhone
        if (normalized.length < 8) return normalized
        val start = normalized.take(4)
        val end = normalized.takeLast(4)
        return "$start****$end"
    }
}
