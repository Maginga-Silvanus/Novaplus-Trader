package com.example.novaplustrader.ui.screens.casino

import com.example.novaplustrader.ui.screens.casino.models.CasinoGameStatus
import com.example.novaplustrader.ui.screens.casino.models.CasinoRound
import com.example.novaplustrader.ui.screens.casino.models.CasinoSegment
import com.example.novaplustrader.ui.screens.casino.models.defaultSpinSegments

data class CasinoUiState(
    val isLoading: Boolean = false,
    val walletBalanceUsd: Double = 0.0,
    val selectedStakeString: String = "5",
    val selectedStakePreset: Int? = 5,
    val status: CasinoGameStatus = CasinoGameStatus.IDLE,
    val segments: List<CasinoSegment> = defaultSpinSegments,
    val targetSegmentIndex: Int = 0,
    val targetRotationAngle: Float = 0f,
    val lastRoundResult: CasinoRound? = null,
    val roundHistory: List<CasinoRound> = emptyList(),
    val validationError: String? = null,
    val isResponsibleGamingDialogOpen: Boolean = false,
    val gameMode: String = "Classic Spin",
    val isGameModeMenuExpanded: Boolean = false,
    val runCount: Int = 0
)
