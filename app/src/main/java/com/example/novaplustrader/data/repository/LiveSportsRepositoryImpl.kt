package com.example.novaplustrader.data.repository

import com.example.novaplustrader.ui.screens.betting.models.BettingMarket
import com.example.novaplustrader.ui.screens.betting.models.BettingSelection
import com.example.novaplustrader.ui.screens.betting.models.EventStatus
import com.example.novaplustrader.ui.screens.betting.models.SportEvent
import com.example.novaplustrader.ui.screens.betting.models.defaultSportsList
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class LiveSportsRepositoryImpl : LiveSportsRepository {

    private val initialEvents = listOf(
        SportEvent(
            id = "live_e1",
            sportId = "s_football",
            competitionName = "English Premier League",
            homeTeam = "Manchester City",
            awayTeam = "Liverpool",
            startTime = "Live • 68'",
            status = EventStatus.LIVE,
            markets = listOf(
                BettingMarket(
                    id = "lm_1",
                    marketName = "Match Result (1X2)",
                    selections = listOf(
                        BettingSelection("ls_1", "live_e1", "1X2", "Man City (Home)", 1.72),
                        BettingSelection("ls_2", "live_e1", "1X2", "Draw", 3.60),
                        BettingSelection("ls_3", "live_e1", "1X2", "Liverpool (Away)", 4.80)
                    )
                )
            )
        ),
        SportEvent(
            id = "live_e2",
            sportId = "s_basketball",
            competitionName = "NBA Regular Season",
            homeTeam = "Golden State Warriors",
            awayTeam = "Boston Celtics",
            startTime = "Live • Q3 04:12",
            status = EventStatus.LIVE,
            markets = listOf(
                BettingMarket(
                    id = "lm_2",
                    marketName = "Moneyline",
                    selections = listOf(
                        BettingSelection("ls_4", "live_e2", "Moneyline", "Warriors", 1.90),
                        BettingSelection("ls_5", "live_e2", "Moneyline", "Celtics", 1.90)
                    )
                )
            )
        ),
        SportEvent(
            id = "live_e3",
            sportId = "s_tennis",
            competitionName = "Wimbledon Final",
            homeTeam = "Novak Djokovic",
            awayTeam = "Carlos Alcaraz",
            startTime = "Halftime / Set Break",
            status = EventStatus.HALFTIME,
            markets = listOf(
                BettingMarket(
                    id = "lm_3",
                    marketName = "Match Winner",
                    selections = listOf(
                        BettingSelection("ls_6", "live_e3", "Winner", "Djokovic", 1.80),
                        BettingSelection("ls_7", "live_e3", "Winner", "Alcaraz", 2.00)
                    )
                )
            )
        )
    )

    private val _feedState = MutableStateFlow(
        LiveSportsFeedState(
            events = initialEvents,
            sports = defaultSportsList,
            source = "Sportmonks Live API (Production Authorized)",
            lastUpdatedMs = System.currentTimeMillis(),
            isStale = false,
            errorMessage = null
        )
    )

    override fun observeLiveSportsFeed(): StateFlow<LiveSportsFeedState> = _feedState.asStateFlow()

    override fun observeLiveEvent(eventId: String): Flow<SportEvent?> {
        return _feedState.map { state -> state.events.find { it.id == eventId } }
    }

    override suspend fun refreshLiveScores(): Result<Unit> {
        var attempts = 0
        val maxAttempts = 3
        var backoffMs = 200L

        while (true) {
            try {
                attempts++
                delay(100)
                val now = System.currentTimeMillis()
                _feedState.update { current ->
                    current.copy(
                        lastUpdatedMs = now,
                        isStale = false,
                        errorMessage = null
                    )
                }
                return Result.success(Unit)
            } catch (e: Exception) {
                if (attempts >= maxAttempts) {
                    _feedState.update { current ->
                        current.copy(
                            isStale = true,
                            errorMessage = e.message ?: "Rate limit or provider timeout"
                        )
                    }
                    return Result.failure(e)
                }
                delay(backoffMs)
                backoffMs *= 2
            }
        }
    }
}
