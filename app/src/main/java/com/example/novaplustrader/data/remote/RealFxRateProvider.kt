package com.example.novaplustrader.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class RealFxRateProvider : FxRateProvider {
    private var cachedRate: FxRate? = null
    private var lastFetchMs: Long = 0L
    private val cacheTtlMs = 60 * 60 * 1000L // 1 hour TTL

    override fun observeUsdKesRate(): Flow<FxRate?> = flow {
        emit(getOrFetchRate())
    }

    private suspend fun getOrFetchRate(): FxRate = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        if (cachedRate != null && (now - lastFetchMs < cacheTtlMs)) {
            return@withContext cachedRate!!
        }

        try {
            val url = URL("https://open.er-api.com/v6/latest/USD")
            val connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 5000
                readTimeout = 5000
                setRequestProperty("Accept", "application/json")
            }

            if (connection.responseCode == 200) {
                val jsonString = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonObj = JSONObject(jsonString)
                val rates = jsonObj.optJSONObject("rates")
                val kesRate = rates?.optDouble("KES", 0.0) ?: 0.0

                if (kesRate > 0.0) {
                    val rate = FxRate(
                        baseCurrency = "USD",
                        quoteCurrency = "KES",
                        rate = kesRate,
                        source = "ExchangeRate-API",
                        observedAtMs = now,
                        isStale = false
                    )
                    cachedRate = rate
                    lastFetchMs = now
                    return@withContext rate
                }
            }
        } catch (_: Exception) {
            // Fallback on error
        }

        // Reliable fallback rate if network fails
        cachedRate ?: FxRate(
            baseCurrency = "USD",
            quoteCurrency = "KES",
            rate = 129.50,
            source = "NovaFallbackProvider",
            observedAtMs = now,
            isStale = true
        )
    }
}
