package com.example.novaplustrader.data.remote

import com.example.novaplustrader.domain.model.ledger.DepositRequest
import com.example.novaplustrader.domain.model.ledger.FeeModel
import com.example.novaplustrader.domain.model.ledger.FinancialTransaction
import com.example.novaplustrader.domain.model.ledger.FinancialTransactionStatus
import com.example.novaplustrader.domain.model.ledger.FinancialTransactionType
import com.example.novaplustrader.domain.model.ledger.RefundRequest
import com.example.novaplustrader.domain.model.ledger.ReversalRequest
import com.example.novaplustrader.domain.model.ledger.TransferRequest
import com.example.novaplustrader.domain.model.ledger.WalletBalanceModel
import com.example.novaplustrader.domain.model.ledger.WithdrawalRequest
import com.example.novaplustrader.ui.model.Currency
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import java.util.Date

class FinancialBackendServiceImpl(
    private val authProvider: () -> FirebaseAuth = { FirebaseAuth.getInstance() },
    private val firestoreProvider: () -> FirebaseFirestore = { FirebaseFirestore.getInstance() },
    private val functionsProvider: () -> FirebaseFunctions = { FirebaseFunctions.getInstance() }
) : FinancialBackendService {

    private val auth: FirebaseAuth get() = authProvider()
    private val firestore: FirebaseFirestore get() = firestoreProvider()
    private val functions: FirebaseFunctions get() = functionsProvider()

    override suspend fun createDepositRequest(request: DepositRequest): Result<FinancialTransaction> =
        createPendingRequest(
            mapOf(
                "type" to FinancialTransactionType.DEPOSIT.name,
                "idempotencyKey" to request.idempotencyKey,
                "currency" to request.currency.code,
                "amountMinorUnits" to request.amountMinorUnits,
                "method" to request.method.name
            ) + mapOfNotNull("providerReference" to request.providerReference?.takeIf { it.isNotBlank() })
        )

    override suspend fun createWithdrawalRequest(request: WithdrawalRequest): Result<FinancialTransaction> =
        createPendingRequest(
            mapOf(
                "type" to FinancialTransactionType.WITHDRAWAL.name,
                "idempotencyKey" to request.idempotencyKey,
                "currency" to request.currency.code,
                "amountMinorUnits" to request.amountMinorUnits,
                "method" to request.method.name,
                "destination" to request.destination
            )
        )

    override suspend fun createTransferRequest(request: TransferRequest): Result<FinancialTransaction> =
        createPendingRequest(
            mapOf(
                "type" to FinancialTransactionType.TRANSFER.name,
                "idempotencyKey" to request.idempotencyKey,
                "currency" to request.currency.code,
                "amountMinorUnits" to request.amountMinorUnits,
                "recipientId" to request.recipientId,
                "note" to request.note
            )
        )

    private suspend fun createPendingRequest(data: Map<String, Any>): Result<FinancialTransaction> = try {
        if (auth.currentUser == null) throw IllegalStateException("Sign in before creating a financial request.")
        val response = functions.getHttpsCallable("createFinancialRequest").call(data).await().data
        @Suppress("UNCHECKED_CAST")
        val record = response as? Map<String, Any?>
            ?: throw IllegalStateException("Financial backend returned an invalid request receipt.")
        Result.success(record.toFinancialTransaction())
    } catch (error: CancellationException) {
        throw error
    } catch (error: Exception) {
        Result.failure(error)
    }

    override suspend fun getTransaction(transactionId: String): Result<FinancialTransaction?> = try {
        val uid = requireUid()
        val snapshot = mainTransactions(uid).document(transactionId).get().await()
        Result.success(if (snapshot.exists()) snapshot.toFinancialTransaction() else null)
    } catch (error: CancellationException) {
        throw error
    } catch (error: Exception) {
        Result.failure(error)
    }

    override fun observeTransaction(transactionId: String): Flow<FinancialTransaction?> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid.isNullOrBlank()) {
            close(IllegalStateException("Sign in to view financial requests."))
            return@callbackFlow
        }
        val registration = mainTransactions(uid).document(transactionId)
            .addSnapshotListener { snapshot, error ->
                when {
                    error != null -> close(error)
                    snapshot == null -> close(IllegalStateException("Unable to load transaction."))
                    !snapshot.exists() -> trySend(null)
                    else -> try {
                        trySend(snapshot.toFinancialTransaction())
                    } catch (parseError: Exception) {
                        close(parseError)
                    }
                }
            }
        awaitClose { registration.remove() }
    }

    override suspend fun getWalletBalance(walletId: String): Result<WalletBalanceModel> = try {
        val uid = requireUid()
        val currency = currencyFromWalletId(walletId)
        val snapshot = mainWallet(uid).get().await()
        if (!snapshot.exists()) throw IllegalStateException("Wallet is not initialized.")
        Result.success(snapshot.toWalletBalance(uid, currency, walletId))
    } catch (error: CancellationException) {
        throw error
    } catch (error: Exception) {
        Result.failure(error)
    }

    override fun observeWalletBalance(walletId: String): Flow<WalletBalanceModel> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid.isNullOrBlank()) {
            close(IllegalStateException("Sign in to view wallet balances."))
            return@callbackFlow
        }
        val currency = currencyFromWalletId(walletId)
        val registration = mainWallet(uid).addSnapshotListener { snapshot, error ->
            when {
                error != null -> close(error)
                snapshot == null || !snapshot.exists() -> close(IllegalStateException("Wallet is not initialized."))
                else -> try {
                    trySend(snapshot.toWalletBalance(uid, currency, walletId))
                } catch (parseError: Exception) {
                    close(parseError)
                }
            }
        }
        awaitClose { registration.remove() }
    }

    override suspend fun getTransactionHistory(
        walletId: String,
        page: Int,
        pageSize: Int,
        type: FinancialTransactionType?,
        status: FinancialTransactionStatus?
    ): Result<List<FinancialTransaction>> = try {
        val transactions = observeTransactionHistory(walletId).first()
        Result.success(transactions.filter {
            (walletId.isBlank() || it.currency == currencyFromWalletId(walletId)) &&
                (type == null || it.type == type) &&
                (status == null || it.status == status)
        }
                .drop(((page.coerceAtLeast(1) - 1) * pageSize.coerceAtLeast(1)))
                .take(pageSize.coerceAtLeast(1))
        )
    } catch (error: CancellationException) {
        throw error
    } catch (error: Exception) {
        Result.failure(error)
    }

    override fun observeTransactionHistory(walletId: String): Flow<List<FinancialTransaction>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid.isNullOrBlank()) {
            close(IllegalStateException("Sign in to view transaction history."))
            return@callbackFlow
        }
        val registration = mainTransactions(uid)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(100)
            .addSnapshotListener { snapshot, error ->
                when {
                    error != null -> close(error)
                    snapshot == null -> close(IllegalStateException("Unable to load transaction history."))
                    else -> try {
                        val transactions = snapshot.documents.map { it.toFinancialTransaction() }
                        trySend(
                            if (walletId.isBlank()) transactions
                            else transactions.filter { it.currency == currencyFromWalletId(walletId) }
                        )
                    } catch (parseError: Exception) {
                        close(parseError)
                    }
                }
            }
        awaitClose { registration.remove() }
    }

    override suspend fun requestRefund(request: RefundRequest): Result<FinancialTransaction> = unavailable()
    override suspend fun requestReversal(request: ReversalRequest): Result<FinancialTransaction> = unavailable()

    override suspend fun calculateFee(
        type: FinancialTransactionType,
        amountMinorUnits: Long,
        currency: Currency
    ): Result<FeeModel> = unavailable()

    private fun mainWallet(uid: String) = firestore.collection("users").document(uid)
        .collection("wallets").document("main")

    private fun mainTransactions(uid: String) = mainWallet(uid).collection("transactions")

    private fun requireUid(): String = auth.currentUser?.uid
        ?.takeIf(String::isNotBlank)
        ?: throw IllegalStateException("Sign in to access financial data.")

    private fun currencyFromWalletId(walletId: String): Currency =
        if (walletId.endsWith("-kes", ignoreCase = true)) Currency.KES else Currency.USD

    private fun mapOfNotNull(vararg entries: Pair<String, String?>): Map<String, String> =
        entries.mapNotNull { (key, value) -> value?.let { key to it } }.toMap()

    private fun DocumentSnapshot.toWalletBalance(
        uid: String,
        currency: Currency,
        walletId: String
    ): WalletBalanceModel {
        val field = if (currency == Currency.KES) "kesBalanceMinorUnits" else "usdBalanceMinorUnits"
        return WalletBalanceModel(
            walletId = walletId,
            userId = uid,
            currency = currency,
            availableBalanceMinorUnits = getLong(field) ?: 0L
        )
    }

    private fun DocumentSnapshot.toFinancialTransaction(): FinancialTransaction {
        val transactionType = getString("type") ?: getString("operation") ?: "TRADE"
        val type = runCatching { FinancialTransactionType.valueOf(transactionType) }
            .getOrElse { FinancialTransactionType.TRADE }
        val state = when (getString("status")) {
            "COMPLETED_TEST", "FILLED_TEST", "COMPLETED" -> FinancialTransactionStatus.COMPLETED
            "APPROVED" -> FinancialTransactionStatus.COMPLETED
            "SUBMITTED" -> FinancialTransactionStatus.SUBMITTED
            "FAILED", "REJECTED" -> FinancialTransactionStatus.FAILED
            "CANCELLED" -> FinancialTransactionStatus.CANCELLED
            "REVERSED" -> FinancialTransactionStatus.REVERSED
            "PROCESSING" -> FinancialTransactionStatus.PROCESSING
            else -> FinancialTransactionStatus.PENDING
        }
        val timestamp = getTimestamp("createdAt")?.toDate() ?: Date(
            getLong("createdAtMs") ?: System.currentTimeMillis()
        )
        return FinancialTransaction(
            transactionId = getString("transactionId") ?: id,
            userId = getString("userId").orEmpty(),
            walletId = getString("walletId").orEmpty(),
            type = type,
            currency = runCatching {
                Currency.valueOf(getString("currency") ?: "USD")
            }.getOrDefault(Currency.USD),
            amountMinorUnits = getLong("amountMinorUnits") ?: 0L,
            feeMinorUnits = getLong("feeMinorUnits") ?: 0L,
            netAmountMinorUnits = getLong("netAmountMinorUnits")
                ?: getLong("amountMinorUnits") ?: 0L,
            status = state,
            idempotencyKey = getString("idempotencyKey") ?: id,
            reference = getString("reference") ?: id,
            providerReference = getString("providerReference"),
            description = getString("description").orEmpty(),
            createdAt = timestamp.toString(),
            updatedAt = getTimestamp("updatedAt")?.toDate()?.toString() ?: timestamp.toString()
        )
    }

    private fun Map<String, Any?>.toFinancialTransaction(): FinancialTransaction {
        val createdAt = Date((this["createdAtMs"] as? Number)?.toLong() ?: System.currentTimeMillis())
        val type = runCatching {
            FinancialTransactionType.valueOf(this["type"] as? String ?: "PENDING")
        }.getOrDefault(FinancialTransactionType.DEPOSIT)
        return FinancialTransaction(
            transactionId = this["transactionId"] as? String ?: "",
            userId = this["userId"] as? String ?: auth.currentUser?.uid.orEmpty(),
            walletId = this["walletId"] as? String ?: "",
            type = type,
            currency = runCatching {
                Currency.valueOf(this["currency"] as? String ?: "USD")
            }.getOrDefault(Currency.USD),
            amountMinorUnits = (this["amountMinorUnits"] as? Number)?.toLong() ?: 0L,
            feeMinorUnits = (this["feeMinorUnits"] as? Number)?.toLong() ?: 0L,
            netAmountMinorUnits = (this["netAmountMinorUnits"] as? Number)?.toLong()
                ?: (this["amountMinorUnits"] as? Number)?.toLong() ?: 0L,
            status = when (this["status"] as? String) {
                "SUBMITTED" -> FinancialTransactionStatus.SUBMITTED
                else -> FinancialTransactionStatus.PENDING
            },
            idempotencyKey = this["idempotencyKey"] as? String ?: "",
            reference = this["reference"] as? String ?: "",
            providerReference = this["providerReference"] as? String,
            description = this["description"] as? String ?: "",
            createdAt = createdAt.toString(),
            updatedAt = createdAt.toString()
        )
    }

    private fun <T> unavailable(): Result<T> = Result.failure(
        IllegalStateException("This financial operation is not available until its trusted backend is configured.")
    )
}
