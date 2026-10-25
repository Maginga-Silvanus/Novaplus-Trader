package com.example.novaplustrader.ui.screens.compliance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import com.example.novaplustrader.data.repository.KycRepository
import com.example.novaplustrader.data.repository.KycRepositoryImpl
import com.example.novaplustrader.domain.model.compliance.DocumentType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeParseException

class KycViewModel(
    private val repository: KycRepository = KycRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(KycUiState())
    val uiState: StateFlow<KycUiState> = _uiState.asStateFlow()

    init {
        observeComplianceState()
    }

    private fun observeComplianceState() {
        viewModelScope.launch {
            launch {
                try {
                    repository.observeKycStatus().collect { status ->
                        _uiState.update { it.copy(kycStatus = status) }
                    }
                } catch (error: CancellationException) {
                    throw error
                } catch (error: Exception) {
                    _uiState.update { it.copy(errorMessage = error.message ?: "Unable to load KYC status") }
                }
            }
            launch {
                try {
                    repository.observeRiskStatus().collect { risk ->
                        _uiState.update { it.copy(riskStatus = risk) }
                    }
                } catch (error: CancellationException) {
                    throw error
                } catch (error: Exception) {
                    _uiState.update { it.copy(errorMessage = error.message ?: "Unable to load risk status") }
                }
            }
            launch {
                try {
                    repository.observeRequirements().collect { reqs ->
                        _uiState.update { it.copy(requirements = reqs) }
                    }
                } catch (error: CancellationException) {
                    throw error
                } catch (error: Exception) {
                    _uiState.update { it.copy(errorMessage = error.message ?: "Unable to load KYC requirements") }
                }
            }
        }
    }

    fun updateFullNameInput(name: String) {
        _uiState.update { it.copy(fullNameInput = name, validationError = null) }
    }

    fun updateDateOfBirthInput(dob: String) {
        _uiState.update { it.copy(dateOfBirthInput = dob, validationError = null) }
    }

    fun selectDocumentType(type: DocumentType) {
        _uiState.update { it.copy(selectedDocumentType = type) }
    }

    fun updateDocumentNumberInput(num: String) {
        _uiState.update { it.copy(documentNumberInput = num, validationError = null) }
    }

    fun submitKyc() {
        val name = _uiState.value.fullNameInput.trim()
        val dob = _uiState.value.dateOfBirthInput.trim()
        val docNum = _uiState.value.documentNumberInput.trim()

        if (name.isBlank()) {
            _uiState.update { it.copy(validationError = "Full legal name is required") }
            return
        }
        if (dob.isBlank()) {
            _uiState.update { it.copy(validationError = "Date of birth is required") }
            return
        }
        if (!isValidDateOfBirth(dob)) {
            _uiState.update { it.copy(validationError = "Enter a valid date of birth in YYYY-MM-DD format") }
            return
        }
        if (docNum.isBlank()) {
            _uiState.update { it.copy(validationError = "Document number is required") }
            return
        }

        _uiState.update { it.copy(isSubmitting = true, validationError = null, errorMessage = null) }

        viewModelScope.launch {
            val result = repository.submitKycVerification(
                fullName = name,
                dateOfBirth = dob,
                documentType = _uiState.value.selectedDocumentType,
                documentNumber = docNum
            )

            _uiState.update { it.copy(isSubmitting = false) }

            result.onSuccess { status ->
                _uiState.update { it.copy(kycStatus = status, isSubmissionSuccess = true) }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(errorMessage = err.message ?: "Failed to submit KYC verification")
                }
            }
        }
    }

    private fun isValidDateOfBirth(value: String): Boolean {
        if (!Regex("""\d{4}-\d{2}-\d{2}""").matches(value)) return false
        return try {
            val date = LocalDate.parse(value)
            !date.isAfter(LocalDate.now())
        } catch (_: DateTimeParseException) {
            false
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, isSubmissionSuccess = false) }
    }
}
