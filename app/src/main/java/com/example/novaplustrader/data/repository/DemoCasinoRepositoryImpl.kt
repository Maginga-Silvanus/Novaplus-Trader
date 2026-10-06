package com.example.novaplustrader.data.repository

import com.example.novaplustrader.ui.screens.casino.models.CasinoGameStatus
import com.example.novaplustrader.ui.screens.casino.models.CasinoRound
import com.example.novaplustrader.ui.screens.casino.models.defaultSpinSegments
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DemoCasinoRepositoryImpl : CasinoRepository {

    private val _demoBalance = MutableStateFlow(1000.0)
    private val _gameStatus = MutableStateFlow(CasinoGameStatus.IDLE)
    private val _history = MutableStateFlow<List<CasinoRound>>(
        listOf(
            CasinoRound(
                id = "cr_1",
                timestamp = getFormattedTime(),
                demoStakeCredits = 50.0,
                multiplier = 2.0,
                resultCredits = 100.0,
                demoBalanceAfter = 1050.0
            )
        )
    )

    override fun observeDemoBalance(): StateFlow<Double> = _demoBalance.asStateFlow()
    override fun observeGameStatus(): StateFlow<CasinoGameStatus> = _gameStatus.asStateFlow()
    override fun observeHistory(): StateFlow<List<CasinoRound>> = _history.asStateFlow()

    override suspend fun spinDemoRound(
        demoStake: Double,
        targetSegmentIndex: Int
    ): Result<CasinoRound> {
        val currentBal = _demoBalance.value
        if (demoStake > currentBal) {
            return Result.failure(IllegalArgumentException("Insufficient Demo Credits"))
        }

        _gameStatus.value = CasinoGameStatus.SPINNING

        // Deduct stake upfront
        _demoBalance.value = currentBal - demoStake

        delay(2500) // Realistic spin animation duration

        val segment = defaultSpinSegments[targetSegmentIndex.coerceIn(0, defaultSpinSegments.size - 1)]
        val winCredits = demoStake * segment.multiplier
        val newBal = _demoBalance.value + winCredits
        _demoBalance.value = newBal

        val round = CasinoRound(
            id = "cr_${System.currentTimeMillis()}",
            timestamp = getFormattedTime(),
            demoStakeCredits = demoStake,
            multiplier = segment.multiplier,
            resultCredits = winCredits,
            demoBalanceAfter = newBal
        )

        _history.update { listOf(round) + it }
        _gameStatus.value = CasinoGameStatus.RESULT

        return Result.success(round)
    }

    override fun resetDemoBalance() {
        _demoBalance.value = 1000.0
    }

    private fun getFormattedTime(): String {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.US)
        return sdf.format(Date())
    }
}
