package com.example.novaplustrader.ui.screens.casino

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.novaplustrader.data.repository.MainUserWalletRepository
import com.example.novaplustrader.data.repository.MainUserWalletRepositoryImpl
import com.example.novaplustrader.data.repository.NotificationRepository
import com.example.novaplustrader.data.repository.NotificationRepositoryImpl
import com.example.novaplustrader.ui.screens.casino.models.CasinoGameStatus
import com.example.novaplustrader.ui.screens.casino.models.CasinoRound
import com.example.novaplustrader.ui.screens.casino.models.defaultSpinSegments
import com.example.novaplustrader.ui.screens.notifications.models.NotificationCategory
import com.example.novaplustrader.ui.screens.notifications.models.NotificationModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CasinoViewModel(
    private val walletRepository: MainUserWalletRepository = MainUserWalletRepositoryImpl(),
    private val notificationRepository: NotificationRepository = NotificationRepositoryImpl(),
    private val authRepository: com.example.novaplustrader.data.repository.AuthRepository = com.example.novaplustrader.data.repository.AuthRepositoryImpl(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ViewModel() {

    private val _uiState = MutableStateFlow(CasinoUiState())
    val uiState: StateFlow<CasinoUiState> = _uiState.asStateFlow()

    init {
        observeWallet()
    }

    private fun observeWallet() {
        viewModelScope.launch {
            authRepository.observeAuthState().collect { user ->
                if (user != null) {
                    walletRepository.observeWallet(user.uid).collect { snapshot ->
                        _uiState.update { it.copy(walletBalanceUsd = snapshot.usdBalance) }
                    }
                }
            }
        }
    }

    fun updateDemoStake(stakeStr: String) {
        val cleanStr = stakeStr.filter { it.isDigit() || it == '.' }
        val stake = cleanStr.toDoubleOrNull() ?: 0.0
        val walletBal = _uiState.value.walletBalanceUsd

        val error = when {
            stake > walletBal -> "Stake exceeds available wallet balance"
            else -> null
        }

        _uiState.update {
            it.copy(
                selectedStakeString = cleanStr,
                selectedStakePreset = null,
                validationError = error
            )
        }
    }

    fun applyStakePreset(presetAmount: Int) {
        val walletBal = _uiState.value.walletBalanceUsd
        val error = if (presetAmount > walletBal) "Stake exceeds available wallet balance" else null

        _uiState.update {
            it.copy(
                selectedStakeString = presetAmount.toString(),
                selectedStakePreset = presetAmount,
                validationError = error
            )
        }
    }

    fun selectGameMode(mode: String) {
        _uiState.update { it.copy(gameMode = mode, isGameModeMenuExpanded = false) }
    }

    fun setGameModeMenuExpanded(expanded: Boolean) {
        _uiState.update { it.copy(isGameModeMenuExpanded = expanded) }
    }

    fun triggerSpin() {
        val stake = _uiState.value.selectedStakeString.toDoubleOrNull() ?: 0.0
        val walletBal = _uiState.value.walletBalanceUsd

        if (stake <= 0) {
            _uiState.update { it.copy(validationError = "Please enter a valid stake") }
            return
        }
        if (stake > walletBal) {
            _uiState.update { it.copy(validationError = "Insufficient wallet balance") }
            return
        }
        if (_uiState.value.status == CasinoGameStatus.SPINNING) {
            return
        }

        val runCount = _uiState.value.runCount
        // Precise stake-based win/loss algorithm:
        val isWin = if (runCount == 0) {
            stake < 7.0
        } else {
            val blockIndex = (runCount - 1) / 5
            val localIndex = (runCount - 1) % 5
            val winsCount = if (stake < 7.0) 3 else 2
            val outcomes = mutableListOf<Boolean>().apply {
                repeat(winsCount) { add(true) }
                repeat(5 - winsCount) { add(false) }
            }
            val seed = blockIndex * 31L + if (stake < 7.0) 7L else 13L
            outcomes.shuffle(java.util.Random(seed))
            outcomes[localIndex]
        }

        // Pick winning or losing segment index
        val segments = _uiState.value.segments
        val targetIndex = if (isWin) {
            segments.indices.filter { segments[it].multiplier > 0.0 }.random()
        } else {
            segments.indices.filter { segments[it].multiplier == 0.0 }.randomOrNull() ?: 0
        }

        val segmentAngle = 360f / segments.size
        val segmentCenterAngle = targetIndex * segmentAngle + segmentAngle / 2f
        val currentAngle = _uiState.value.targetRotationAngle
        val newTargetAngle = currentAngle + 1800f + (360f - segmentCenterAngle)

        val roundId = "cr_${System.currentTimeMillis()}"
        val stakeMinor = Math.round(stake * 100.0)
        val uid = auth.currentUser?.uid

        viewModelScope.launch {
            // Deduct the stake immediately when the spin starts
            if (uid != null) {
                try {
                    val walletRef = firestore.collection("users").document(uid).collection("wallets").document("main")
                    val stakeTxRef = walletRef.collection("transactions").document("${roundId}_stake")
                    val userStakeTxRef = firestore.collection("users").document(uid).collection("transactions").document("${roundId}_stake")

                    firestore.runTransaction { transaction ->
                        val snapshot = transaction.get(walletRef)
                        val currentUsdMinor = snapshot.getLong("usdBalanceMinorUnits") ?: 0L
                        if (currentUsdMinor < stakeMinor) {
                            throw IllegalStateException("Insufficient wallet balance")
                        }
                        val newUsdMinor = currentUsdMinor - stakeMinor
                        transaction.set(
                            walletRef,
                            mapOf("usdBalanceMinorUnits" to newUsdMinor),
                            SetOptions.merge()
                        )

                        val txData = mapOf(
                            "transactionId" to "${roundId}_stake",
                            "userId" to uid,
                            "walletId" to "${uid}-usd",
                            "type" to "CASINO_STAKE",
                            "currency" to "USD",
                            "amountMinorUnits" to stakeMinor,
                            "feeMinorUnits" to 0L,
                            "netAmountMinorUnits" to stakeMinor,
                            "status" to "COMPLETED",
                            "idempotencyKey" to "${roundId}_stake",
                            "reference" to roundId,
                            "description" to "Casino stake placed: $${String.format(Locale.US, "%.2f", stake)}",
                            "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
                            "createdAtMs" to System.currentTimeMillis(),
                            "testOnly" to true,
                            "operation" to "CASINO_STAKE"
                        )
                        transaction.set(stakeTxRef, txData)
                        transaction.set(userStakeTxRef, txData)
                    }.await()
                } catch (e: Exception) {
                    _uiState.update {
                        it.copy(
                            validationError = e.message ?: "Failed to deduct stake",
                            status = CasinoGameStatus.IDLE
                        )
                    }
                    return@launch
                }
            }

            _uiState.update {
                it.copy(
                    validationError = null,
                    targetSegmentIndex = targetIndex,
                    targetRotationAngle = newTargetAngle,
                    status = CasinoGameStatus.SPINNING,
                    runCount = runCount + 1
                )
            }

            delay(2500) // Spin animation duration

            val segment = segments[targetIndex]
            val winAmount = stake * segment.multiplier
            val winMinor = Math.round(winAmount * 100.0)

            if (uid != null) {
                try {
                    val walletRef = firestore.collection("users").document(uid).collection("wallets").document("main")
                    if (winAmount > 0) {
                        val winTxRef = walletRef.collection("transactions").document("${roundId}_win")
                        val userWinTxRef = firestore.collection("users").document(uid).collection("transactions").document("${roundId}_win")

                        firestore.runTransaction { transaction ->
                            val snapshot = transaction.get(walletRef)
                            val currentUsdMinor = snapshot.getLong("usdBalanceMinorUnits") ?: 0L
                            val newUsdMinor = currentUsdMinor + winMinor
                            transaction.set(
                                walletRef,
                                mapOf("usdBalanceMinorUnits" to newUsdMinor),
                                SetOptions.merge()
                            )

                            val txData = mapOf(
                                "transactionId" to "${roundId}_win",
                                "userId" to uid,
                                "walletId" to "${uid}-usd",
                                "type" to "CASINO_WIN",
                                "currency" to "USD",
                                "amountMinorUnits" to winMinor,
                                "feeMinorUnits" to 0L,
                                "netAmountMinorUnits" to winMinor,
                                "status" to "COMPLETED",
                                "idempotencyKey" to "${roundId}_win",
                                "reference" to roundId,
                                "description" to "Casino win: multiplier ${segment.multiplier}x, won $${String.format(Locale.US, "%.2f", winAmount)}",
                                "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
                                "createdAtMs" to System.currentTimeMillis(),
                                "testOnly" to true,
                                "operation" to "CASINO_WIN"
                            )
                            transaction.set(winTxRef, txData)
                            transaction.set(userWinTxRef, txData)
                        }.await()
                    }

                    // Guaranteed Notification Saving via notificationRepository.saveNotification(...)
                    val notifId = "notif_casino_${System.currentTimeMillis()}"
                    val notification = NotificationModel(
                        id = notifId,
                        userId = uid,
                        title = if (winAmount > 0) "Casino Win! 🎉" else "Casino Spin Completed",
                        body = if (winAmount > 0) "You won $${String.format(Locale.US, "%.2f", winAmount)} USD on the casino wheel (${segment.multiplier}x multiplier)." else "Casino spin completed with stake $${String.format(Locale.US, "%.2f", stake)} USD.",
                        category = NotificationCategory.CASINO_DEMO,
                        timestamp = SimpleDateFormat("HH:mm", Locale.US).format(Date()),
                        isRead = false
                    )
                    notificationRepository.saveNotification(notification)

                } catch (e: Exception) {
                    _uiState.update { it.copy(validationError = e.message ?: "Failed to process round result") }
                }
            }

            val round = CasinoRound(
                id = roundId,
                timestamp = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date()),
                demoStakeCredits = stake,
                multiplier = segment.multiplier,
                resultCredits = winAmount,
                demoBalanceAfter = _uiState.value.walletBalanceUsd
            )

            _uiState.update {
                it.copy(
                    lastRoundResult = round,
                    roundHistory = listOf(round) + it.roundHistory,
                    status = CasinoGameStatus.RESULT
                )
            }
        }
    }

    fun resetDemoCredits() {
        _uiState.update { it.copy(validationError = null, lastRoundResult = null, status = CasinoGameStatus.IDLE) }
    }

    fun openResponsibleGamingDialog() {
        _uiState.update { it.copy(isResponsibleGamingDialogOpen = true) }
    }

    fun closeResponsibleGamingDialog() {
        _uiState.update { it.copy(isResponsibleGamingDialogOpen = false) }
    }
}
