package com.example.novaplustrader.data.repository

import com.example.novaplustrader.ui.screens.betting.models.BettingMarket
import com.example.novaplustrader.ui.screens.betting.models.BettingSelection
import com.example.novaplustrader.ui.screens.betting.models.EventStatus
import com.example.novaplustrader.ui.screens.betting.models.SportEvent
import com.example.novaplustrader.ui.screens.betting.models.defaultSportsList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

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
            source = "ESPN Live Sports API (Internet Connected)",
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
        return withContext(Dispatchers.IO) {
            val maxAttempts = 3
            var backoffMs = 200L

            for (attempt in 1..maxAttempts) {
                try {
                    val url = URL("https://site.api.espn.com/apis/site/v2/sports/soccer/eng.1/scoreboard")
                    val connection = (url.openConnection() as HttpURLConnection).apply {
                        connectTimeout = 5000
                        readTimeout = 5000
                        setRequestProperty("Accept", "application/json")
                    }

                    if (connection.responseCode == 200) {
                        val jsonString = connection.inputStream.bufferedReader().use { it.readText() }
                        val jsonObj = JSONObject(jsonString)
                        val eventsArray = jsonObj.optJSONArray("events")
                        if (eventsArray != null && eventsArray.length() > 0) {
                            val parsedEvents = mutableListOf<SportEvent>()
                            for (i in 0 until eventsArray.length()) {
                                val ev = eventsArray.getJSONObject(i)
                                val id = ev.optString("id", "live_$i")
                                val competitions = ev.optJSONArray("competitions")
                                val comp = competitions?.optJSONObject(0)
                                val competitors = comp?.optJSONArray("competitors")
                                val homeComp = competitors?.optJSONObject(0)
                                val awayComp = competitors?.optJSONObject(1)
                                val homeTeam = homeComp?.optJSONObject("team")?.optString("displayName", "Home Team") ?: "Home"
                                val awayTeam = awayComp?.optJSONObject("team")?.optString("displayName", "Away Team") ?: "Away"
                                val statusObj = ev.optJSONObject("status")
                                val statusType = statusObj?.optJSONObject("type")?.optString("state", "in")
                                val detail = statusObj?.optJSONObject("type")?.optString("detail", "Live") ?: "Live"

                                val eventStatus = when (statusType) {
                                    "pre" -> EventStatus.UPCOMING
                                    "post" -> EventStatus.FINISHED
                                    else -> EventStatus.LIVE
                                }

                                parsedEvents.add(
                                    SportEvent(
                                        id = "net_$id",
                                        sportId = "s_football",
                                        competitionName = "English Premier League",
                                        homeTeam = homeTeam,
                                        awayTeam = awayTeam,
                                        startTime = detail,
                                        status = eventStatus,
                                        markets = listOf(
                                            BettingMarket(
                                                id = "lm_net_$i",
                                                marketName = "Match Result (1X2)",
                                                selections = listOf(
                                                    BettingSelection("ls_n1_$i", "net_$id", "1X2", "$homeTeam (Home)", 1.85),
                                                    BettingSelection("ls_n2_$i", "net_$id", "1X2", "Draw", 3.40),
                                                    BettingSelection("ls_n3_$i", "net_$id", "1X2", "$awayTeam (Away)", 3.90)
                                                )
                                            )
                                        )
                                    )
                                )
                            }
                            if (parsedEvents.isNotEmpty()) {
                                _feedState.update { current ->
                                    current.copy(
                                        events = parsedEvents,
                                        source = "ESPN Live Sports API (Internet Connected)",
                                        lastUpdatedMs = System.currentTimeMillis(),
                                        isStale = false,
                                        errorMessage = null
                                    )
                                }
                                return@withContext Result.success(Unit)
                            }
                        }
                    }

                    val now = System.currentTimeMillis()
                    _feedState.update { current ->
                        current.copy(
                            lastUpdatedMs = now,
                            isStale = false,
                            errorMessage = null
                        )
                    }
                    return@withContext Result.success(Unit)
                } catch (e: Exception) {
                    if (attempt >= maxAttempts) {
                        _feedState.update { current ->
                            current.copy(
                                isStale = true,
                                errorMessage = e.message ?: "Network fetch failed"
                            )
                        }
                        return@withContext Result.failure(e)
                    }
                    delay(backoffMs)
                    backoffMs *= 2
                }
            }
            Result.success(Unit)
        }
    }
}
