package com.example.novaplustrader.ui.screens.betting.models

enum class EventStatus(val label: String) {
    UPCOMING("Upcoming"),
    LIVE("LIVE"),
    FINISHED("Finished")
}

enum class BetStatus(val label: String) {
    OPEN("Open"),
    WON("Won"),
    LOST("Lost"),
    VOID("Void"),
    CANCELLED("Cancelled")
}

data class Sport(
    val id: String,
    val name: String,
    val iconName: String
)

data class BettingSelection(
    val id: String,
    val eventId: String,
    val marketName: String,
    val selectionName: String,
    val odds: Double
)

data class BettingMarket(
    val id: String,
    val marketName: String,
    val selections: List<BettingSelection>
)

data class SportEvent(
    val id: String,
    val sportId: String,
    val competitionName: String,
    val homeTeam: String,
    val awayTeam: String,
    val startTime: String,
    val status: EventStatus = EventStatus.UPCOMING,
    val markets: List<BettingMarket> = emptyList()
)

data class BetSlipItem(
    val selection: BettingSelection,
    val eventName: String,
    val competitionName: String
)

data class DemoBet(
    val id: String,
    val timestamp: String,
    val items: List<BetSlipItem>,
    val totalStakeCredits: Double,
    val combinedOdds: Double,
    val potentialReturnCredits: Double,
    val status: BetStatus = BetStatus.OPEN
)

val defaultSportsList = listOf(
    Sport(id = "s_football", name = "Football", iconName = "sports_soccer"),
    Sport(id = "s_basketball", name = "Basketball", iconName = "sports_basketball"),
    Sport(id = "s_tennis", name = "Tennis", iconName = "sports_tennis"),
    Sport(id = "s_baseball", name = "Baseball", iconName = "sports_baseball"),
    Sport(id = "s_rugby", name = "Rugby", iconName = "sports_rugby"),
    Sport(id = "s_cricket", name = "Cricket", iconName = "sports_cricket")
)
