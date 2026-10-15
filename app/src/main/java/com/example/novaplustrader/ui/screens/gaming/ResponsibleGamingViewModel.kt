package com.example.novaplustrader.ui.screens.gaming

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.novaplustrader.data.repository.ResponsibleGamingRepository
import com.example.novaplustrader.data.repository.ResponsibleGamingRepositoryImpl
import com.example.novaplustrader.domain.model.gaming.ExclusionPeriod
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ResponsibleGamingViewModel(
    private val repository: ResponsibleGamingRepository = ResponsibleGamingRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ResponsibleGamingUiState())
    val uiState: StateFlow<ResponsibleGamingUiState> = _uiState.asStateFlow()

    init {
        observeGamingData()
    }

    private fun observeGamingData() {
        viewModelScope.launch {
            launch {
                repository.observeProfile().collect { prof ->
                    _uiState.update { it.copy(profile = prof) }
                }
            }
            launch {
                repository.observeActivityLogs().collect { logs ->
                    _uiState.update { it.copy(activityLogs = logs) }
                }
            }
        }
    }

    fun selectExclusionPeriod(period: ExclusionPeriod) {
        _uiState.update { it.copy(selectedExclusionPeriod = period) }
    }

    fun updateExclusionReasonInput(reason: String) {
        _uiState.update { it.copy(exclusionReasonInput = reason) }
    }

    fun openExclusionConfirmSheet() {
        _uiState.update { it.copy(isExclusionConfirmOpen = true) }
    }

    fun closeExclusionConfirmSheet() {
        _uiState.update { it.copy(isExclusionConfirmOpen = false) }
    }

    fun confirmSelfExclusion() {
        _uiState.update { it.copy(isExclusionConfirmOpen = false, isSubmitting = true) }

        viewModelScope.launch {
            val result = repository.requestSelfExclusion(
                period = _uiState.value.selectedExclusionPeriod,
                reason = _uiState.value.exclusionReasonInput
            )

            _uiState.update { it.copy(isSubmitting = false) }

            result.onSuccess {
                _uiState.update { it.copy(isExclusionSuccess = true) }
            }.onFailure { err ->
                _uiState.update { it.copy(errorMessage = err.message ?: "Failed to activate self-exclusion") }
            }
        }
    }

    fun dismissRealityCheckAlert() {
        _uiState.update { it.copy(isRealityCheckAlertVisible = false) }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, isExclusionSuccess = false) }
    }
}
