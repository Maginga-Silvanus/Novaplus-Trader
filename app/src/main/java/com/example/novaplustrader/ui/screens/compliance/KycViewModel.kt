package com.example.novaplustrader.ui.screens.compliance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.novaplustrader.data.repository.KycRepository
import com.example.novaplustrader.data.repository.KycRepositoryImpl
import com.example.novaplustrader.domain.model.compliance.DocumentType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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
                repository.observeKycStatus().collect { status ->
                    _uiState.update { it.copy(kycStatus = status) }
                }
            }
            launch {
                repository.observeRiskStatus().collect { risk ->
                    _uiState.update { it.copy(riskStatus = risk) }
                }
            }
            launch {
                repository.observeRequirements().collect { reqs ->
                    _uiState.update { it.copy(requirements = reqs) }
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

    fun setFrontImageUri(uri: String?) {
        _uiState.update { it.copy(frontImageUri = uri) }
    }

    fun setBackImageUri(uri: String?) {
        _uiState.update { it.copy(backImageUri = uri) }
    }

    fun setSelfieImageUri(uri: String?) {
        _uiState.update { it.copy(selfieImageUri = uri) }
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
        if (docNum.isBlank()) {
            _uiState.update { it.copy(validationError = "Document number is required") }
            return
        }

        _uiState.update { it.copy(isSubmitting = true, validationError = null) }

        viewModelScope.launch {
            val result = repository.submitKycVerification(
                fullName = name,
                dateOfBirth = dob,
                documentType = _uiState.value.selectedDocumentType,
                documentNumber = docNum,
                frontImageUri = _uiState.value.frontImageUri,
                backImageUri = _uiState.value.backImageUri,
                selfieImageUri = _uiState.value.selfieImageUri
            )

            _uiState.update { it.copy(isSubmitting = false) }

            result.onSuccess { status ->
                _uiState.update { it.copy(kycStatus = status, isSubmissionSuccess = true) }
            }.onFailure { err ->
                _uiState.update { it.copy(errorMessage = err.message ?: "Failed to submit KYC verification") }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, isSubmissionSuccess = false) }
    }
}
