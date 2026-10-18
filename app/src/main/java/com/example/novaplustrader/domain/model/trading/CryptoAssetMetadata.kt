package com.example.novaplustrader.domain.model.trading

data class CryptoAssetMetadata(
    val id: String,
    val symbol: String,
    val name: String,
    val logoUrl: String?,
    val marketCapUsd: Double?,
    val marketCapRank: Int?,
    val change24h: Double?,
    val volume24h: Double?,
    val lastUpdatedMs: Long,
    val isTradableOnBinance: Boolean,
    val listingDateMs: Long? = null
)
