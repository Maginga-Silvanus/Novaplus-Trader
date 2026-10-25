package com.example.novaplustrader.data.repository

import kotlinx.coroutines.flow.Flow
import com.example.novaplustrader.ui.model.Currency

data class MainUserWalletSnapshot(
    val usdBalanceMinorUnits: Long = 500L,
    val kesBalanceMinorUnits: Long = 0L,
    val mode: String = "testing",
    val initialized: Boolean = false,
    val rewardClaimed: Boolean = false
) {
    val usdBalance: Double get() = usdBalanceMinorUnits / 100.0
    val kesBalance: Double get() = kesBalanceMinorUnits / 100.0
}

interface MainUserWalletRepository {
    fun observeWallet(userId: String): Flow<MainUserWalletSnapshot>
    fun observeTransactions(userId: String): Flow<List<TestWalletTransaction>>
    suspend fun simulateOperation(
        operation: String,
        currency: Currency,
        amountMinorUnits: Long,
        idempotencyKey: String
    ): Result<TestWalletOperationResponse>
    suspend fun claimFirstTimeReward(userId: String): Result<Unit>
}

data class TestWalletTransaction(
    val id: String,
    val operation: String,
    val currency: Currency,
    val amountMinorUnits: Long,
    val status: String,
    val createdAt: String
)

data class TestWalletOperationResponse(
    val status: String,
    val message: String
)
