package com.example.novaplustrader.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class CryptoWalletRepositoryImpl(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val functions: FirebaseFunctions = FirebaseFunctions.getInstance()
) : CryptoWalletRepository {

    override suspend fun ensureWallet(userId: String): Result<CryptoWalletSnapshot> = try {
        requireCurrentUser(userId)
        val result = functions.getHttpsCallable("getOrCreateCryptoPortfolio").call().await().data as? Map<*, *>
            ?: error("The wallet service returned an invalid response")
        val portfolio = result["cryptoWallet"] as? Map<*, *>
            ?: error("The wallet service omitted the portfolio")
        Result.success(portfolio.asWalletSnapshot())
    } catch (error: CancellationException) {
        throw error
    } catch (error: Exception) {
        Result.failure(error)
    }

    override fun observeWallet(userId: String): Flow<CryptoWalletSnapshot> = callbackFlow {
        try {
            requireCurrentUser(userId)
        } catch (error: Exception) {
            close(error)
            return@callbackFlow
        }
        var portfolioSnapshot: DocumentSnapshot? = null
        var holdingsSnapshot: List<DocumentSnapshot>? = null
        fun emitWhenReady() {
            val portfolio = portfolioSnapshot ?: return
            val holdings = holdingsSnapshot ?: return
            if (!portfolio.exists()) {
                close(IllegalStateException("The crypto wallet has not been initialized"))
                return
            }
            try {
                trySend(portfolio.toCryptoWalletSnapshot(holdings))
            } catch (parseError: Exception) {
                close(parseError)
            }
        }
        val portfolioListener = walletDocument(userId).addSnapshotListener { snapshot, error ->
            when {
                error != null -> close(error)
                snapshot == null -> close(IllegalStateException("Unable to read the crypto wallet"))
                else -> {
                    portfolioSnapshot = snapshot
                    emitWhenReady()
                }
            }
        }
        val holdingsListener = walletDocument(userId).collection("holdings")
            .addSnapshotListener { snapshot, error ->
                when {
                    error != null -> close(error)
                    snapshot == null -> close(IllegalStateException("Unable to read crypto holdings"))
                    else -> {
                        holdingsSnapshot = snapshot.documents
                        emitWhenReady()
                    }
                }
            }
        awaitClose {
            portfolioListener.remove()
            holdingsListener.remove()
        }
    }

    override fun observeTradeRequests(userId: String): Flow<List<SpotTradeRecord>> = callbackFlow {
        try {
            requireCurrentUser(userId)
        } catch (error: Exception) {
            close(error)
            return@callbackFlow
        }
        val listener = firestore.collection("users")
            .document(userId)
            .collection("tradeRequests")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(20)
            .addSnapshotListener { snapshot, snapshotError ->
                when {
                    snapshotError != null -> close(snapshotError)
                    snapshot == null -> close(IllegalStateException("Unable to read crypto trade history"))
                    else -> try {
                        trySend(snapshot.documents.map { document ->
                            SpotTradeRecord(
                                idempotencyKey = document.getString("idempotencyKey")
                                    ?: error("Trade record is missing its idempotency key"),
                                asset = document.getString("asset") ?: error("Trade record is missing its asset"),
                                direction = document.getString("direction") ?: error("Trade record is missing its direction"),
                                amount = (document.get("amount") as? Number)?.toDouble()
                                    ?: error("Trade record is missing its amount"),
                                status = document.getString("status") ?: error("Trade record is missing its status"),
                                message = document.getString("message"),
                                quantity = (document.getLong("quantityAtomicUnits") ?: 0L) /
                                    ASSET_SCALES.getValue(document.getString("asset") ?: "BTC"),
                                executionPriceUsd = (document.get("executionPriceUsd") as? Number)?.toDouble(),
                                feeAmountUsd = (document.getLong("feeMinorUnits") ?: 0L) / 100.0
                            )
                        })
                    } catch (parseError: Exception) {
                        close(parseError)
                    }
                }
            }
        awaitClose { listener.remove() }
    }

    override suspend fun submitSpotTradeRequest(request: SpotTradeRequest): Result<SpotTradeResponse> = try {
        requireCurrentUser(auth.currentUser?.uid.orEmpty())
        val payload = mapOf(
            "idempotencyKey" to request.idempotencyKey,
            "asset" to request.asset,
            "pair" to request.pair,
            "direction" to request.direction.name,
            "amount" to request.amount,
            "amountDenomination" to request.amountDenomination,
            "quotePriceUsd" to request.quotePriceUsd,
            "limitPriceUsd" to request.limitPriceUsd,
            "quoteObservedAtMs" to request.quoteObservedAtMs
        )
        val response = functions.getHttpsCallable("submitSpotTradeRequest").call(payload).await().data as? Map<*, *>
            ?: error("The trading service returned an invalid response")
        val receipt = (response["receipt"] as? Map<*, *>)?.let { data ->
            SpotTradeReceipt(
                idempotencyKey = data["idempotencyKey"] as? String
                    ?: error("The trade receipt is missing its reference"),
                asset = data["asset"] as? String ?: error("The trade receipt is missing its asset"),
                direction = data["direction"] as? String ?: error("The trade receipt is missing its direction"),
                quantityAtomicUnits = (data["quantityAtomicUnits"] as? Number)?.toLong()
                    ?: error("The trade receipt is missing its quantity"),
                executionPriceUsd = (data["executionPriceUsd"] as? Number)?.toDouble()
                    ?: error("The trade receipt is missing its execution price"),
                grossAmountMinorUnits = (data["grossAmountMinorUnits"] as? Number)?.toLong()
                    ?: error("The trade receipt is missing its order value"),
                feeMinorUnits = (data["feeMinorUnits"] as? Number)?.toLong()
                    ?: error("The trade receipt is missing its fee"),
                netAmountMinorUnits = (data["netAmountMinorUnits"] as? Number)?.toLong()
                    ?: error("The trade receipt is missing its cash amount"),
                createdAtMs = (data["createdAtMs"] as? Number)?.toLong()
                    ?: error("The trade receipt is missing its timestamp"),
                status = data["status"] as? String ?: error("The trade receipt is missing its status")
            )
        }
        Result.success(
            SpotTradeResponse(
                status = response["status"] as? String ?: error("The trade response is missing its status"),
                message = response["message"] as? String,
                receipt = receipt
            )
        )
    } catch (error: CancellationException) {
        throw error
    } catch (error: Exception) {
        Result.failure(error)
    }

    private fun requireCurrentUser(userId: String) {
        require(userId.isNotBlank() && auth.currentUser?.uid == userId) {
            "The signed-in account does not match this wallet"
        }
    }

    private fun walletDocument(userId: String) = firestore.collection("users")
        .document(userId)
        .collection("cryptoWallets")
        .document("spot")

    private fun DocumentSnapshot.toCryptoWalletSnapshot(holdings: List<DocumentSnapshot>): CryptoWalletSnapshot {
        val uid = reference.parent.parent?.id ?: "trader"
        val btcWalletId = getString("bitcoinWalletId") ?: "BTC-$uid"
        val mktWalletId = getString("marketingWalletId") ?: "MKT-$uid"
        val trdWalletId = getString("tradingWalletId") ?: "TRD-$uid"
        val marketingBalance = (getLong("marketingBalanceMinorUnits") ?: 0L) / 100.0
        val tradingBalance = (getLong("tradingBalanceMinorUnits") ?: 0L) / 100.0

        val balancesByAsset = holdings.associate { holding ->
            val symbol = holding.getString("symbol") ?: "BTC"
            val amount = holding.getLong("quantityAtomicUnits") ?: 0L
            symbol to amount
        }
        val assetMap = SUPPORTED_ASSETS.associateWith { asset ->
            (balancesByAsset[asset] ?: 0L) / ASSET_SCALES.getValue(asset)
        }
        val btcBalance = assetMap["BTC"] ?: 0.0

        val subWallets = listOf(
            CryptoSubWallet(
                id = btcWalletId,
                name = "Bitcoin & Crypto Vault",
                purpose = "Bitcoin & Native Crypto Storage",
                assetSymbol = "BTC",
                balance = btcBalance,
                receiveAddress = btcWalletId
            ),
            CryptoSubWallet(
                id = mktWalletId,
                name = "Marketing Funds Wallet",
                purpose = "Marketing, Referral & Promotional Reserves",
                assetSymbol = "USDT / USD",
                balance = marketingBalance,
                receiveAddress = mktWalletId
            ),
            CryptoSubWallet(
                id = trdWalletId,
                name = "Trading & Liquidity Vault",
                purpose = "Spot & Forex Algorithmic Liquidity",
                assetSymbol = "USD / Multi-Asset",
                balance = tradingBalance,
                receiveAddress = trdWalletId
            )
        )

        return CryptoWalletSnapshot(
            bitcoinWalletId = btcWalletId,
            marketingWalletId = mktWalletId,
            tradingWalletId = trdWalletId,
            assetBalances = assetMap,
            mode = getString("mode") ?: "disabled",
            paperTradeFeeBps = getLong("paperTradeFeeBps")?.toInt() ?: 10,
            minimumTradeUsdMinorUnits = getLong("minimumTradeUsdMinorUnits") ?: 100L,
            subWallets = subWallets
        )
    }

    private fun Map<*, *>.asWalletSnapshot(): CryptoWalletSnapshot {
        val btcWalletId = this["bitcoinWalletId"] as? String
            ?: error("The crypto wallet service omitted the Bitcoin wallet ID")
        val mktWalletId = this["marketingWalletId"] as? String
            ?: error("The crypto wallet service omitted the marketing wallet ID")
        val trdWalletId = this["tradingWalletId"] as? String
            ?: error("The crypto wallet service omitted the trading wallet ID")
        val balances = this["assetBalancesAtomicUnits"] as? Map<*, *>
            ?: error("The crypto wallet service omitted asset balances")
        val assetMap = SUPPORTED_ASSETS.associateWith { asset ->
            ((balances[asset] as? Number)?.toLong()
                ?: error("The crypto wallet service omitted the $asset balance")) / ASSET_SCALES.getValue(asset)
        }
        val btcBalance = assetMap["BTC"] ?: 0.0
        val marketingBalance = ((this["marketingBalanceMinorUnits"] as? Number)?.toLong()
            ?: error("The crypto wallet service omitted the marketing wallet balance")) / 100.0
        val tradingBalance = ((this["tradingBalanceMinorUnits"] as? Number)?.toLong()
            ?: error("The crypto wallet service omitted the trading wallet balance")) / 100.0

        val subWallets = listOf(
            CryptoSubWallet(
                id = btcWalletId,
                name = "Bitcoin & Crypto Vault",
                purpose = "Bitcoin & Native Crypto Storage",
                assetSymbol = "BTC",
                balance = btcBalance,
                receiveAddress = btcWalletId
            ),
            CryptoSubWallet(
                id = mktWalletId,
                name = "Marketing Funds Wallet",
                purpose = "Marketing, Referral & Promotional Reserves",
                assetSymbol = "USDT / USD",
                balance = marketingBalance,
                receiveAddress = mktWalletId
            ),
            CryptoSubWallet(
                id = trdWalletId,
                name = "Trading & Liquidity Vault",
                purpose = "Spot & Forex Algorithmic Liquidity",
                assetSymbol = "USD / Multi-Asset",
                balance = tradingBalance,
                receiveAddress = trdWalletId
            )
        )

        return CryptoWalletSnapshot(
            bitcoinWalletId = btcWalletId,
            marketingWalletId = mktWalletId,
            tradingWalletId = trdWalletId,
            assetBalances = assetMap,
            mode = this["mode"] as? String ?: "disabled",
            paperTradeFeeBps = (this["paperTradeFeeBps"] as? Number)?.toInt() ?: 10,
            minimumTradeUsdMinorUnits = (this["minimumTradeUsdMinorUnits"] as? Number)?.toLong() ?: 100L,
            subWallets = subWallets
        )
    }

    private companion object {
        val SUPPORTED_ASSETS = setOf("BTC", "ETH", "SOL", "BNB", "XRP", "ADA", "DOGE", "LTC", "USDT", "USDC")
        val ASSET_SCALES = mapOf(
            "BTC" to 100_000_000.0,
            "ETH" to 100_000_000.0,
            "SOL" to 1_000_000_000.0,
            "BNB" to 100_000_000.0,
            "XRP" to 1_000_000.0,
            "ADA" to 1_000_000.0,
            "DOGE" to 1_000_000.0,
            "LTC" to 100_000_000.0,
            "USDT" to 1_000_000.0,
            "USDC" to 1_000_000.0
        )
    }
}
