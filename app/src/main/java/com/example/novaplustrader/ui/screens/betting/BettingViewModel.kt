package com.example.novaplustrader.ui.screens.betting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.novaplustrader.data.repository.BettingRepository
import com.example.novaplustrader.data.repository.DemoBettingRepositoryImpl
import com.example.novaplustrader.ui.screens.betting.models.BetSlipItem
import com.example.novaplustrader.ui.screens.betting.models.BettingSelection
import com.example.novaplustrader.ui.screens.betting.models.DemoBet
import com.example.novaplustrader.ui.screens.betting.models.Sport
import com.example.novaplustrader.ui.screens.betting.models.SportEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

class BettingViewModel(
    private val repository: BettingRepository = DemoBettingRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(BettingUiState())
    val uiState: StateFlow<BettingUiState> = _uiState.asStateFlow()

    init {
        observeRepositoryData()
    }

    private fun observeRepositoryData() {
        viewModelScope.launch {
            launch {
                repository.observeDemoBettingBalance().collect { bal ->
                    _uiState.update { it.copy(demoBettingBalance = bal) }
                }
            }
            launch {
                repository.observeSports().collect { sports ->
                    _uiState.update { it.copy(sports = sports) }
                }
            }
            launch {
                repository.observeEvents().collect { events ->
                    _uiState.update { it.copy(events = events) }
                }
            }
            launch {
                repository.observeMyDemoBets().collect { bets ->
                    _uiState.update { it.copy(myDemoBets = bets) }
                }
            }
        }
    }

    fun selectSport(sport: Sport) {
        _uiState.update { it.copy(selectedSport = sport) }
    }

    fun selectEventDetails(event: SportEvent?) {
        _uiState.update { it.copy(selectedEvent = event) }
    }

    fun toggleSelection(selection: BettingSelection, eventName: String, competitionName: String) {
        val currentItems = _uiState.value.betSlipItems.toMutableList()
        val existingIndex = currentItems.indexOfFirst { it.selection.id == selection.id }

        if (existingIndex >= 0) {
            currentItems.removeAt(existingIndex)
        } else {
            // Remove any other selection from the same event to avoid incompatible same-event bets
            currentItems.removeAll { it.selection.eventId == selection.eventId }
            currentItems.add(
                BetSlipItem(
                    selection = selection,
                    eventName = eventName,
                    competitionName = competitionName
                )
            )
        }

        _uiState.update { it.copy(betSlipItems = currentItems) }
    }

    fun removeBetSlipItem(selectionId: String) {
        val currentItems = _uiState.value.betSlipItems.filterNot { it.selection.id == selectionId }
        _uiState.update { it.copy(betSlipItems = currentItems) }
    }

    fun clearBetSlip() {
        _uiState.update { it.copy(betSlipItems = emptyList()) }
    }

    fun updateDemoStake(stakeStr: String) {
        val cleanStr = stakeStr.filter { it.isDigit() || it == '.' }
        val stake = cleanStr.toDoubleOrNull() ?: 0.0
        val bal = _uiState.value.demoBettingBalance

        val error = when {
            stake > bal -> "Stake exceeds available Demo Credits"
            else -> null
        }

        _uiState.update {
            it.copy(
                demoStakeString = cleanStr,
                selectedStakePreset = null,
                validationError = error
            )
        }
    }

    fun applyStakePreset(presetAmount: Int) {
        val bal = _uiState.value.demoBettingBalance
        val error = if (presetAmount > bal) "Stake exceeds available Demo Credits" else null

        _uiState.update {
            it.copy(
                demoStakeString = presetAmount.toString(),
                selectedStakePreset = presetAmount,
                validationError = error
            )
        }
    }

    fun openConfirmSheet() {
        val items = _uiState.value.betSlipItems
        val stake = _uiState.value.demoStakeString.toDoubleOrNull() ?: 0.0
        val bal = _uiState.value.demoBettingBalance

        if (items.isEmpty()) {
            _uiState.update { it.copy(validationError = "Please select at least one outcome") }
            return
        }
        if (stake <= 0) {
            _uiState.update { it.copy(validationError = "Please enter a valid demo stake") }
            return
        }
        if (stake > bal) {
            _uiState.update { it.copy(validationError = "Stake exceeds available Demo Credits") }
            return
        }

        _uiState.update { it.copy(validationError = null, isConfirmSheetOpen = true) }
    }

    fun closeConfirmSheet() {
        _uiState.update { it.copy(isConfirmSheetOpen = false) }
    }

    fun executePlaceDemoBet() {
        val items = _uiState.value.betSlipItems
        val stake = _uiState.value.demoStakeString.toDoubleOrNull() ?: 0.0

        _uiState.update { it.copy(isConfirmSheetOpen = false, isLoading = true) }

        viewModelScope.launch {
            val result = repository.placeDemoBet(items, stake)
            _uiState.update { it.copy(isLoading = false) }

            result.onSuccess { bet ->
                _uiState.update {
                    it.copy(
                        lastPlacedDemoBet = bet,
                        betSlipItems = emptyList()
                    )
                }
            }.onFailure { err ->
                _uiState.update { it.copy(errorMessage = err.message ?: "Failed to place demo bet") }
            }
        }
    }

    fun selectBetFilter(filter: String) {
        _uiState.update { it.copy(selectedBetFilter = filter) }
    }

    fun selectDemoBetDetails(bet: DemoBet?) {
        _uiState.update { it.copy(selectedDemoBet = bet) }
    }

    fun clearLastPlacedBet() {
        _uiState.update { it.copy(lastPlacedDemoBet = null) }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun resetDemoBalance() {
        repository.resetDemoBettingBalance()
    }
}
