package com.example.novaplustrader.data.repository

import com.example.novaplustrader.ui.screens.betting.models.BetSlipItem
import com.example.novaplustrader.ui.screens.betting.models.BetStatus
import com.example.novaplustrader.ui.screens.betting.models.BettingMarket
import com.example.novaplustrader.ui.screens.betting.models.BettingSelection
import com.example.novaplustrader.ui.screens.betting.models.DemoBet
import com.example.novaplustrader.ui.screens.betting.models.EventStatus
import com.example.novaplustrader.ui.screens.betting.models.Sport
import com.example.novaplustrader.ui.screens.betting.models.SportEvent
import com.example.novaplustrader.ui.screens.betting.models.defaultSportsList
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DemoBettingRepositoryImpl : BettingRepository {

    private val _demoBalance = MutableStateFlow(1000.0)
    private val _sports = MutableStateFlow(defaultSportsList)

    private val sampleEvents = listOf(
        SportEvent(
            id = "e1",
            sportId = "s_football",
            competitionName = "Premier League",
            homeTeam = "Arsenal",
            awayTeam = "Chelsea",
            startTime = "Today • 18:00",
            status = EventStatus.LIVE,
            markets = listOf(
                BettingMarket(
                    id = "m1_1",
                    marketName = "Match Result (1X2)",
                    selections = listOf(
                        BettingSelection("s1", "e1", "1X2", "Arsenal (Home)", 1.85),
                        BettingSelection("s2", "e1", "1X2", "Draw", 3.40),
                        BettingSelection("s3", "e1", "1X2", "Chelsea (Away)", 4.20)
                    )
                ),
                BettingMarket(
                    id = "m1_2",
                    marketName = "Double Chance",
                    selections = listOf(
                        BettingSelection("s4", "e1", "Double Chance", "Home or Draw", 1.22),
                        BettingSelection("s5", "e1", "Double Chance", "Home or Away", 1.30),
                        BettingSelection("s6", "e1", "Double Chance", "Draw or Away", 1.90)
                    )
                ),
                BettingMarket(
                    id = "m1_3",
                    marketName = "Total Goals (Over/Under 2.5)",
                    selections = listOf(
                        BettingSelection("s7", "e1", "Over/Under 2.5", "Over 2.5", 1.75),
                        BettingSelection("s8", "e1", "Over/Under 2.5", "Under 2.5", 2.05)
                    )
                ),
                BettingMarket(
                    id = "m1_4",
                    marketName = "Both Teams To Score",
                    selections = listOf(
                        BettingSelection("s9", "e1", "BTTS", "Yes", 1.65),
                        BettingSelection("s10", "e1", "BTTS", "No", 2.15)
                    )
                )
            )
        ),
        SportEvent(
            id = "e2",
            sportId = "s_football",
            competitionName = "UEFA Champions League",
            homeTeam = "Real Madrid",
            awayTeam = "Bayern Munich",
            startTime = "Tomorrow • 21:00",
            status = EventStatus.UPCOMING,
            markets = listOf(
                BettingMarket(
                    id = "m2_1",
                    marketName = "Match Result (1X2)",
                    selections = listOf(
                        BettingSelection("s11", "e2", "1X2", "Real Madrid (Home)", 2.10),
                        BettingSelection("s12", "e2", "1X2", "Draw", 3.50),
                        BettingSelection("s13", "e2", "1X2", "Bayern Munich (Away)", 3.20)
                    )
                )
            )
        ),
        SportEvent(
            id = "e3",
            sportId = "s_basketball",
            competitionName = "NBA Finals",
            homeTeam = "Lakers",
            awayTeam = "Celtics",
            startTime = "Today • 22:30",
            status = EventStatus.UPCOMING,
            markets = listOf(
                BettingMarket(
                    id = "m3_1",
                    marketName = "Match Winner",
                    selections = listOf(
                        BettingSelection("s14", "e3", "Winner", "Lakers", 1.95),
                        BettingSelection("s15", "e3", "Winner", "Celtics", 1.85)
                    )
                )
            )
        )
    )

    private val _events = MutableStateFlow(sampleEvents)

    private val _myBets = MutableStateFlow<List<DemoBet>>(
        listOf(
            DemoBet(
                id = "db_881",
                timestamp = "Today, 14:20",
                items = listOf(
                    BetSlipItem(
                        selection = BettingSelection("s1", "e1", "1X2", "Arsenal (Home)", 1.85),
                        eventName = "Arsenal vs Chelsea",
                        competitionName = "Premier League"
                    )
                ),
                totalStakeCredits = 50.0,
                combinedOdds = 1.85,
                potentialReturnCredits = 92.5,
                status = BetStatus.OPEN
            )
        )
    )

    override fun observeDemoBettingBalance(): StateFlow<Double> = _demoBalance.asStateFlow()
    override fun observeSports(): StateFlow<List<Sport>> = _sports.asStateFlow()
    override fun observeEvents(sportId: String?): StateFlow<List<SportEvent>> = _events.asStateFlow()

    override fun observeEventById(eventId: String): Flow<SportEvent?> {
        return _events.map { list -> list.find { it.id == eventId } }
    }

    override suspend fun placeDemoBet(
        items: List<BetSlipItem>,
        stake: Double
    ): Result<DemoBet> {
        val currentBal = _demoBalance.value
        if (stake > currentBal) {
            return Result.failure(IllegalArgumentException("Insufficient Demo Credits for stake"))
        }

        delay(700) // Simulate order placement network verification

        val combinedOdds = items.fold(1.0) { acc, item -> acc * item.selection.odds }
        val potentialReturn = stake * combinedOdds

        // Deduct demo balance
        _demoBalance.value = currentBal - stake

        val newBet = DemoBet(
            id = "db_${System.currentTimeMillis().toString().takeLast(6)}",
            timestamp = "Just now (${getFormattedTime()})",
            items = items,
            totalStakeCredits = stake,
            combinedOdds = combinedOdds,
            potentialReturnCredits = potentialReturn,
            status = BetStatus.OPEN
        )

        _myBets.update { listOf(newBet) + it }
        return Result.success(newBet)
    }

    override fun observeMyDemoBets(): StateFlow<List<DemoBet>> = _myBets.asStateFlow()

    override fun observeDemoBetById(betId: String): Flow<DemoBet?> {
        return _myBets.map { list -> list.find { it.id == betId } }
    }

    override fun resetDemoBettingBalance() {
        _demoBalance.value = 1000.0
    }

    private fun getFormattedTime(): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.US)
        return sdf.format(Date())
    }
}
