package com.example.novaplustrader.ui.screens.casino

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.novaplustrader.data.repository.CasinoRepository
import com.example.novaplustrader.data.repository.DemoCasinoRepositoryImpl
import com.example.novaplustrader.ui.screens.casino.models.CasinoGameStatus
import com.example.novaplustrader.ui.screens.casino.models.defaultSpinSegments
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

class CasinoViewModel(
    private val repository: CasinoRepository = DemoCasinoRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(CasinoUiState())
    val uiState: StateFlow<CasinoUiState> = _uiState.asStateFlow()

    init {
        observeRepository()
    }

    private fun observeRepository() {
        viewModelScope.launch {
            launch {
                repository.observeDemoBalance().collect { bal ->
                    _uiState.update { it.copy(demoBalance = bal) }
                }
            }
            launch {
                repository.observeGameStatus().collect { status ->
                    _uiState.update { it.copy(status = status) }
                }
            }
            launch {
                repository.observeHistory().collect { history ->
                    _uiState.update { it.copy(roundHistory = history) }
                }
            }
        }
    }

    fun updateDemoStake(stakeStr: String) {
        val cleanStr = stakeStr.filter { it.isDigit() }
        val stake = cleanStr.toDoubleOrNull() ?: 0.0
        val demoBal = _uiState.value.demoBalance

        val error = when {
            stake > demoBal -> "Stake exceeds available Demo Credits"
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
        val demoBal = _uiState.value.demoBalance
        val error = if (presetAmount > demoBal) "Stake exceeds available Demo Credits" else null

        _uiState.update {
            it.copy(
                selectedStakeString = presetAmount.toString(),
                selectedStakePreset = presetAmount,
                validationError = error
            )
        }
    }

    fun triggerSpin() {
        val stake = _uiState.value.selectedStakeString.toDoubleOrNull() ?: 0.0
        val demoBal = _uiState.value.demoBalance

        if (stake <= 0) {
            _uiState.update { it.copy(validationError = "Please enter a valid demo stake") }
            return
        }
        if (stake > demoBal) {
            _uiState.update { it.copy(validationError = "Insufficient Demo Credits") }
            return
        }
        if (_uiState.value.status == CasinoGameStatus.SPINNING) {
            return
        }

        // Random target segment selection for demo mode
        val randomIndex = Random.nextInt(0, defaultSpinSegments.size)
        val segmentAngle = 360f / defaultSpinSegments.size
        // Add 5 full rotations (1800 deg) for dramatic spin deceleration
        val currentAngle = _uiState.value.targetRotationAngle
        val newTargetAngle = currentAngle + 1800f + (360f - (randomIndex * segmentAngle))

        _uiState.update {
            it.copy(
                validationError = null,
                targetSegmentIndex = randomIndex,
                targetRotationAngle = newTargetAngle,
                status = CasinoGameStatus.SPINNING
            )
        }

        viewModelScope.launch {
            val result = repository.spinDemoRound(stake, randomIndex)
            result.onSuccess { round ->
                _uiState.update {
                    it.copy(
                        lastRoundResult = round,
                        status = CasinoGameStatus.RESULT
                    )
                }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        validationError = err.message ?: "Spin failed",
                        status = CasinoGameStatus.ERROR
                    )
                }
            }
        }
    }

    fun resetDemoCredits() {
        repository.resetDemoBalance()
        _uiState.update { it.copy(validationError = null, lastRoundResult = null, status = CasinoGameStatus.IDLE) }
    }

    fun openResponsibleGamingDialog() {
        _uiState.update { it.copy(isResponsibleGamingDialogOpen = true) }
    }

    fun closeResponsibleGamingDialog() {
        _uiState.update { it.copy(isResponsibleGamingDialogOpen = false) }
    }
}
