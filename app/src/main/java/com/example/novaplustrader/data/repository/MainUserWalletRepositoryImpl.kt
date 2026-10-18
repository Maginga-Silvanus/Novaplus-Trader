package com.example.novaplustrader.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.functions.FirebaseFunctions
import com.example.novaplustrader.ui.model.Currency
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await

class MainUserWalletRepositoryImpl(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val functions: FirebaseFunctions = FirebaseFunctions.getInstance()
) : MainUserWalletRepository {
    override fun observeWallet(userId: String): Flow<MainUserWalletSnapshot> = callbackFlow {
        if (userId.isBlank() || auth.currentUser?.uid != userId) {
            close(IllegalArgumentException("The signed-in account does not match this user wallet"))
            return@callbackFlow
        }
        val registration = firestore.collection("users")
            .document(userId)
            .collection("wallets")
            .document("main")
            .addSnapshotListener { snapshot, error ->
                when {
                    error != null -> close(error)
                    snapshot == null -> close(IllegalStateException("Unable to read the user wallet"))
                    !snapshot.exists() -> trySend(MainUserWalletSnapshot())
                    else -> {
                        val usd = snapshot.getLong("usdBalanceMinorUnits")
                        val kes = snapshot.getLong("kesBalanceMinorUnits")
                        if (usd == null || kes == null || usd < 0L || kes < 0L) {
                            close(IllegalStateException("The user wallet contains invalid balances"))
                        } else {
                            trySend(
                                MainUserWalletSnapshot(
                                    usdBalanceMinorUnits = usd,
                                    kesBalanceMinorUnits = kes,
                                    mode = snapshot.getString("mode") ?: "testing",
                                    initialized = true
                                )
                            )
                        }
                    }
                }
            }
        awaitClose { registration.remove() }
    }

    override fun observeTransactions(userId: String): Flow<List<TestWalletTransaction>> = callbackFlow {
        if (userId.isBlank() || auth.currentUser?.uid != userId) {
            close(IllegalArgumentException("The signed-in account does not match this user wallet"))
            return@callbackFlow
        }
        val registration = walletDocument(userId).collection("transactions")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(25)
            .addSnapshotListener { snapshot, error ->
                when {
                    error != null -> close(error)
                    snapshot == null -> close(IllegalStateException("Unable to read test wallet transactions"))
                    else -> try {
                        trySend(snapshot.documents.filter { it.getBoolean("testOnly") == true }.map { document ->
                            TestWalletTransaction(
                                id = document.id,
                                operation = document.getString("operation")
                                    ?: error("Wallet transaction is missing its operation"),
                                currency = when (document.getString("currency")) {
                                    "USD" -> Currency.USD
                                    "KES" -> Currency.KES
                                    else -> error("Wallet transaction has an unsupported currency")
                                },
                                amountMinorUnits = document.getLong("amountMinorUnits")
                                    ?: error("Wallet transaction is missing its amount"),
                                status = document.getString("status")
                                    ?: error("Wallet transaction is missing its status"),
                                createdAt = document.getTimestamp("createdAt")?.toDate()?.toString().orEmpty()
                            )
                        })
                    } catch (parseError: Exception) {
                        close(parseError)
                    }
                }
            }
        awaitClose { registration.remove() }
    }

    override suspend fun simulateOperation(
        operation: String,
        currency: Currency,
        amountMinorUnits: Long,
        idempotencyKey: String
    ): Result<TestWalletOperationResponse> = try {
        val uid = auth.currentUser?.uid ?: error("Sign in before using test funds")
        requireCurrentUser(uid)
        val response = functions.getHttpsCallable("simulateTestWalletOperation").call(
            mapOf(
                "operation" to operation,
                "currency" to currency.name,
                "amountMinorUnits" to amountMinorUnits,
                "idempotencyKey" to idempotencyKey
            )
        ).await().data as? Map<*, *> ?: error("The test wallet service returned an invalid response")
        Result.success(
            TestWalletOperationResponse(
                status = response["status"] as? String ?: error("The wallet response omitted its status"),
                message = response["message"] as? String ?: error("The wallet response omitted its message")
            )
        )
    } catch (error: CancellationException) {
        throw error
    } catch (error: Exception) {
        Result.failure(error)
    }

    private fun walletDocument(userId: String) = firestore.collection("users")
        .document(userId)
        .collection("wallets")
        .document("main")

    private fun requireCurrentUser(userId: String) {
        require(userId.isNotBlank() && auth.currentUser?.uid == userId) {
            "The signed-in account does not match this user wallet"
        }
    }
}
