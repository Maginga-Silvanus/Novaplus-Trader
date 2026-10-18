package com.example.novaplustrader.data.repository

import com.example.novaplustrader.ui.screens.betting.models.Sport
import com.example.novaplustrader.ui.screens.betting.models.SportEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

data class LiveSportsFeedState(
    val events: List<SportEvent>,
    val sports: List<Sport>,
    val source: String,
    val lastUpdatedMs: Long,
    val isStale: Boolean,
    val errorMessage: String?
)

interface LiveSportsRepository {
    fun observeLiveSportsFeed(): StateFlow<LiveSportsFeedState>
    fun observeLiveEvent(eventId: String): Flow<SportEvent?>
    suspend fun refreshLiveScores(): Result<Unit>
}
