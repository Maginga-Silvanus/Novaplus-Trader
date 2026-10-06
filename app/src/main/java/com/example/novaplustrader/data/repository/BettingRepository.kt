package com.example.novaplustrader.data.repository

import com.example.novaplustrader.ui.screens.betting.models.BetSlipItem
import com.example.novaplustrader.ui.screens.betting.models.DemoBet
import com.example.novaplustrader.ui.screens.betting.models.Sport
import com.example.novaplustrader.ui.screens.betting.models.SportEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface BettingRepository {
    fun observeDemoBettingBalance(): StateFlow<Double>
    fun observeSports(): StateFlow<List<Sport>>
    fun observeEvents(sportId: String? = null): StateFlow<List<SportEvent>>
    fun observeEventById(eventId: String): Flow<SportEvent?>

    suspend fun placeDemoBet(
        items: List<BetSlipItem>,
        stake: Double
    ): Result<DemoBet>

    fun observeMyDemoBets(): StateFlow<List<DemoBet>>
    fun observeDemoBetById(betId: String): Flow<DemoBet?>

    fun resetDemoBettingBalance()
}
