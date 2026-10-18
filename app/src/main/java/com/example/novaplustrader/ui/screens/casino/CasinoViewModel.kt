package com.example.novaplustrader.ui.screens.casino

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.novaplustrader.data.repository.MainUserWalletRepository
import com.example.novaplustrader.data.repository.MainUserWalletRepositoryImpl
import com.example.novaplustrader.ui.screens.casino.models.CasinoGameStatus
import com.example.novaplustrader.ui.screens.casino.models.CasinoRound
import com.example.novaplustrader.ui.screens.casino.models.defaultSpinSegments
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
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ViewModel() {

    private val _uiState = MutableStateFlow(CasinoUiState())
    val uiState: StateFlow<CasinoUiState> = _uiState.asStateFlow()

    init {
        observeWallet()
    }

    private fun observeWallet() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            walletRepository.observeWallet(uid).collect { snapshot ->
                _uiState.update { it.copy(walletBalanceUsd = snapshot.usdBalance) }
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
        // Stake-based win/loss algorithm:
        // If stake < $7.00: First run is a win; subsequent runs follow a 3-wins-out-of-5-runs pattern.
        // If stake >= $7.00: First run is a loss; subsequent runs follow a 2-wins-out-of-5-runs pattern.
        val isWin = if (runCount == 0) {
            stake < 7.0
        } else {
            val positionInCycle = (runCount - 1) % 5
            if (stake < 7.0) positionInCycle < 3 else positionInCycle < 2
        }

        // Pick winning or losing segment index
        val segments = _uiState.value.segments
        val targetIndex = if (isWin) {
            segments.indices.filter { segments[it].multiplier > 0.0 }.random()
        } else {
            segments.indices.filter { segments[it].multiplier == 0.0 }.randomOrNull() ?: 0
        }

        val segmentAngle = 360f / segments.size
        // Stop squarely centered on random number segment rather than on dividing line:
        // Center of segment i is i * segmentAngle + segmentAngle / 2f
        val currentAngle = _uiState.value.targetRotationAngle
        val newTargetAngle = currentAngle + 1800f + (360f - (targetIndex * segmentAngle + segmentAngle / 2f))

        _uiState.update {
            it.copy(
                validationError = null,
                targetSegmentIndex = targetIndex,
                targetRotationAngle = newTargetAngle,
                status = CasinoGameStatus.SPINNING,
                runCount = runCount + 1
            )
        }

        viewModelScope.launch {
            delay(2500) // Spin animation duration

            val uid = auth.currentUser?.uid
            val segment = segments[targetIndex]
            val winAmount = stake * segment.multiplier
            val netChange = winAmount - stake

            if (uid != null) {
                try {
                    val walletRef = firestore.collection("users").document(uid).collection("wallets").document("main")
                    firestore.runTransaction { transaction ->
                        val snapshot = transaction.get(walletRef)
                        val currentUsdMinor = snapshot.getLong("usdBalanceMinorUnits") ?: 0L
                        val netChangeMinor = Math.round(netChange * 100.0)
                        val newUsdMinor = (currentUsdMinor + netChangeMinor).coerceAtLeast(0L)
                        transaction.set(
                            walletRef,
                            mapOf("usdBalanceMinorUnits" to newUsdMinor),
                            SetOptions.merge()
                        )
                    }.await()
                } catch (_: Exception) { }
            }

            val newBal = (_uiState.value.walletBalanceUsd + netChange).coerceAtLeast(0.0)
            val round = CasinoRound(
                id = "cr_${System.currentTimeMillis()}",
                timestamp = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date()),
                demoStakeCredits = stake,
                multiplier = segment.multiplier,
                resultCredits = winAmount,
                demoBalanceAfter = newBal
            )

            _uiState.update {
                it.copy(
                    walletBalanceUsd = newBal,
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
