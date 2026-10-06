package com.example.novaplustrader.ui.screens.casino

import com.example.novaplustrader.ui.screens.casino.models.CasinoGameStatus
import com.example.novaplustrader.ui.screens.casino.models.CasinoRound
import com.example.novaplustrader.ui.screens.casino.models.CasinoSegment
import com.example.novaplustrader.ui.screens.casino.models.defaultSpinSegments

data class CasinoUiState(
    val isLoading: Boolean = false,
    val demoBalance: Double = 1000.0,
    val walletBalanceUsd: Double = 6324.49,
    val selectedStakeString: String = "25",
    val selectedStakePreset: Int? = 25,
    val status: CasinoGameStatus = CasinoGameStatus.IDLE,
    val segments: List<CasinoSegment> = defaultSpinSegments,
    val targetSegmentIndex: Int = 0,
    val targetRotationAngle: Float = 0f,
    val lastRoundResult: CasinoRound? = null,
    val roundHistory: List<CasinoRound> = emptyList(),
    val validationError: String? = null,
    val isResponsibleGamingDialogOpen: Boolean = false
)
