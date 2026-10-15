package com.example.novaplustrader.data.remote

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

data class FxRate(
    val baseCurrency: String,
    val quoteCurrency: String,
    val rate: Double,
    val source: String,
    val observedAtMs: Long,
    val isStale: Boolean
)

interface FxRateProvider {
    fun observeUsdKesRate(): Flow<FxRate?>
}

class UnavailableFxRateProvider : FxRateProvider {
    override fun observeUsdKesRate(): Flow<FxRate?> = flowOf(null)
}
