package com.example.novaplustrader.data.remote

import com.example.novaplustrader.domain.model.trading.CryptoAssetMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ConcurrentHashMap

interface CryptoMetadataProvider {
    suspend fun fetchGlobalCryptoMetadata(): List<CryptoAssetMetadata>
}

class CoingeckoCryptoMetadataProvider : CryptoMetadataProvider {
    private val cache = ConcurrentHashMap<String, List<CryptoAssetMetadata>>()
    private var lastFetchMs: Long = 0L
    private val cacheTtlMs = 15 * 60 * 1000L // 15 mins cache

    private val fallbackMetadata = listOf(
        CryptoAssetMetadata("bitcoin", "BTC", "Bitcoin", "https://assets.coingecko.com/coins/images/1/large/bitcoin.png", 1_350_000_000_000.0, 1, 2.5, 35_000_000_000.0, System.currentTimeMillis(), true, 1230988800000L),
        CryptoAssetMetadata("ethereum", "ETH", "Ethereum", "https://assets.coingecko.com/coins/images/279/large/ethereum.png", 450_000_000_000.0, 2, 1.8, 18_000_000_000.0, System.currentTimeMillis(), true, 1438992000000L),
        CryptoAssetMetadata("tether", "USDT", "Tether", "https://assets.coingecko.com/coins/images/325/large/Tether.png", 115_000_000_000.0, 3, 0.01, 55_000_000_000.0, System.currentTimeMillis(), true, 1451606400000L),
        CryptoAssetMetadata("binancecoin", "BNB", "BNB", "https://assets.coingecko.com/coins/images/825/large/bnb-icon2_2x.png", 85_000_000_000.0, 4, -0.5, 1_200_000_000.0, System.currentTimeMillis(), true, 1500336000000L),
        CryptoAssetMetadata("solana", "SOL", "Solana", "https://assets.coingecko.com/coins/images/4128/large/solana.png", 65_000_000_000.0, 5, 5.4, 4_500_000_000.0, System.currentTimeMillis(), true, 1577836800000L),
        CryptoAssetMetadata("cardano", "ADA", "Cardano", "https://assets.coingecko.com/coins/images/975/large/cardano.png", 15_000_000_000.0, 10, -1.2, 400_000_000.0, System.currentTimeMillis(), true, 1517443200000L),
        CryptoAssetMetadata("ripple", "XRP", "XRP", "https://assets.coingecko.com/coins/images/44/large/xrp-symbol-white-128.png", 30_000_000_000.0, 7, 0.8, 1_100_000_000.0, System.currentTimeMillis(), true, 1377993600000L),
        CryptoAssetMetadata("dogecoin", "DOGE", "Dogecoin", "https://assets.coingecko.com/coins/images/5/large/dogecoin.png", 22_000_000_000.0, 8, 3.2, 2_100_000_000.0, System.currentTimeMillis(), true, 1370044800000L)
    )

    override suspend fun fetchGlobalCryptoMetadata(): List<CryptoAssetMetadata> = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        if (cache.containsKey("global") && (now - lastFetchMs < cacheTtlMs)) {
            return@withContext cache["global"]!!
        }

        try {
            val url = URL("https://api.coingecko.com/api/v3/coins/markets?vs_currency=usd&order=market_cap_desc&per_page=50&page=1&sparkline=false")
            val connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 5000
                readTimeout = 5000
                setRequestProperty("Accept", "application/json")
            }

            if (connection.responseCode == 200) {
                val responseString = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonArray = JSONArray(responseString)
                val list = mutableListOf<CryptoAssetMetadata>()

                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val id = obj.optString("id", "")
                    val symbol = obj.optString("symbol", "").uppercase(java.util.Locale.US)
                    val name = obj.optString("name", "")
                    val image = obj.optString("image", null)
                    val marketCap = if (obj.has("market_cap") && !obj.isNull("market_cap")) obj.optDouble("market_cap") else null
                    val marketCapRank = if (obj.has("market_cap_rank") && !obj.isNull("market_cap_rank")) obj.optInt("market_cap_rank") else null
                    val change24h = if (obj.has("price_change_percentage_24h") && !obj.isNull("price_change_percentage_24h")) obj.optDouble("price_change_percentage_24h") else null
                    val volume24h = if (obj.has("total_volume") && !obj.isNull("total_volume")) obj.optDouble("total_volume") else null
                    
                    val isTradable = symbol in setOf("BTC", "ETH", "USDT", "BNB", "SOL", "ADA", "XRP", "DOGE")

                    list.add(
                        CryptoAssetMetadata(
                            id = id,
                            symbol = symbol,
                            name = name,
                            logoUrl = image,
                            marketCapUsd = marketCap,
                            marketCapRank = marketCapRank,
                            change24h = change24h,
                            volume24h = volume24h,
                            lastUpdatedMs = now,
                            isTradableOnBinance = isTradable
                        )
                    )
                }

                if (list.isNotEmpty()) {
                    cache["global"] = list
                    lastFetchMs = now
                    return@withContext list
                }
            }
        } catch (_: Exception) {
            // Fallback on network or rate limit error
        }

        fallbackMetadata
    }
}
