package com.example.novaplustrader.ui.screens.betting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.novaplustrader.data.repository.BettingRepository
import com.example.novaplustrader.data.repository.DemoBettingRepositoryImpl
import com.example.novaplustrader.data.repository.MainUserWalletRepository
import com.example.novaplustrader.data.repository.MainUserWalletRepositoryImpl
import com.example.novaplustrader.ui.screens.betting.models.BetSlipItem
import com.example.novaplustrader.ui.screens.betting.models.BettingSelection
import com.example.novaplustrader.ui.screens.betting.models.DemoBet
import com.example.novaplustrader.ui.screens.betting.models.Sport
import com.example.novaplustrader.ui.screens.betting.models.SportEvent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class BettingViewModel(
    private val repository: BettingRepository = DemoBettingRepositoryImpl(),
    private val walletRepository: MainUserWalletRepository = MainUserWalletRepositoryImpl(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    private val _uiState = MutableStateFlow(BettingUiState())
    val uiState: StateFlow<BettingUiState> = _uiState.asStateFlow()

    init {
        observeRepositoryData()
        observeWallet()
    }

    private fun observeWallet() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            walletRepository.observeWallet(uid).collect { snapshot ->
                _uiState.update { it.copy(demoBettingBalance = snapshot.usdBalance, walletBalanceUsd = snapshot.usdBalance) }
            }
        }
    }

    private fun observeRepositoryData() {
        viewModelScope.launch {
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
            stake > bal -> "Stake exceeds available wallet balance"
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
        val error = if (presetAmount > bal) "Stake exceeds available wallet balance" else null

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
            _uiState.update { it.copy(validationError = "Please enter a valid stake") }
            return
        }
        if (stake > bal) {
            _uiState.update { it.copy(validationError = "Stake exceeds available wallet balance") }
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
            val uid = auth.currentUser?.uid
            if (uid != null) {
                try {
                    val db = FirebaseFirestore.getInstance()
                    val walletRef = db.collection("users").document(uid).collection("wallets").document("main")
                    val txId = "bet_${System.currentTimeMillis()}"
                    val txRef = walletRef.collection("transactions").document(txId)
                    val userTxRef = db.collection("users").document(uid).collection("transactions").document(txId)
                    val notifRef = db.collection("users").document(uid).collection("notifications").document()

                    db.runTransaction { transaction ->
                        val snapshot = transaction.get(walletRef)
                        val currentUsdMinor = snapshot.getLong("usdBalanceMinorUnits") ?: 500L
                        val stakeMinor = Math.round(stake * 100.0)
                        val newUsdMinor = (currentUsdMinor - stakeMinor).coerceAtLeast(0L)
                        transaction.set(
                            walletRef,
                            mapOf("usdBalanceMinorUnits" to newUsdMinor),
                            SetOptions.merge()
                        )

                        val txData = mapOf(
                            "transactionId" to txId,
                            "userId" to uid,
                            "walletId" to "${uid}-usd",
                            "type" to "BETTING_STAKE",
                            "currency" to "USD",
                            "amountMinorUnits" to stakeMinor,
                            "feeMinorUnits" to 0L,
                            "netAmountMinorUnits" to stakeMinor,
                            "status" to "COMPLETED",
                            "idempotencyKey" to txId,
                            "reference" to txId,
                            "description" to "Sports bet placed on ${items.firstOrNull()?.eventName ?: "Sports Event"}",
                            "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
                            "createdAtMs" to System.currentTimeMillis(),
                            "testOnly" to true,
                            "operation" to "BETTING_STAKE"
                        )
                        transaction.set(txRef, txData)
                        transaction.set(userTxRef, txData)

                        val notifData = mapOf(
                            "id" to notifRef.id,
                            "userId" to uid,
                            "title" to "Sports Bet Placed ⚽",
                            "body" to "Your bet of $${String.format(java.util.Locale.US, "%.2f", stake)} USD was placed successfully.",
                            "category" to "BETTING",
                            "timestamp" to "Just now",
                            "isRead" to false
                        )
                        transaction.set(notifRef, notifData)
                    }.await()
                } catch (_: Exception) {}
            }

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
                _uiState.update { it.copy(errorMessage = err.message ?: "Failed to place bet") }
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
