package com.example.novaplustrader.data.repository

import com.example.novaplustrader.ui.screens.casino.models.CasinoGameStatus
import com.example.novaplustrader.ui.screens.casino.models.CasinoRound
import kotlinx.coroutines.flow.StateFlow

interface CasinoRepository {
    fun observeDemoBalance(): StateFlow<Double>
    fun observeGameStatus(): StateFlow<CasinoGameStatus>
    fun observeHistory(): StateFlow<List<CasinoRound>>

    suspend fun spinDemoRound(
        demoStake: Double,
        targetSegmentIndex: Int
    ): Result<CasinoRound>

    fun resetDemoBalance()
}
