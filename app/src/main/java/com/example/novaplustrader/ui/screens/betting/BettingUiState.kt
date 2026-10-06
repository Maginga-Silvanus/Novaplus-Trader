package com.example.novaplustrader.ui.screens.betting

import com.example.novaplustrader.ui.screens.betting.models.BetSlipItem
import com.example.novaplustrader.ui.screens.betting.models.BettingSelection
import com.example.novaplustrader.ui.screens.betting.models.DemoBet
import com.example.novaplustrader.ui.screens.betting.models.Sport
import com.example.novaplustrader.ui.screens.betting.models.SportEvent
import com.example.novaplustrader.ui.screens.betting.models.defaultSportsList

data class BettingUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val demoBettingBalance: Double = 1000.0,
    val walletBalanceUsd: Double = 6324.49,
    val sports: List<Sport> = defaultSportsList,
    val selectedSport: Sport = defaultSportsList[0],
    val events: List<SportEvent> = emptyList(),
    val selectedEvent: SportEvent? = null,
    val betSlipItems: List<BetSlipItem> = emptyList(),
    val demoStakeString: String = "25",
    val selectedStakePreset: Int? = 25,
    val myDemoBets: List<DemoBet> = emptyList(),
    val selectedBetFilter: String = "OPEN",
    val selectedDemoBet: DemoBet? = null,
    val lastPlacedDemoBet: DemoBet? = null,
    val isConfirmSheetOpen: Boolean = false,
    val validationError: String? = null,
    val searchQuery: String = ""
)
