package com.example.novaplustrader.ui.screens.compliance

import com.example.novaplustrader.domain.model.compliance.DocumentType
import com.example.novaplustrader.domain.model.compliance.KycRequirement
import com.example.novaplustrader.domain.model.compliance.KycStatus
import com.example.novaplustrader.domain.model.compliance.RiskStatus

data class KycUiState(
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val validationError: String? = null,
    val kycStatus: KycStatus = KycStatus.NOT_STARTED,
    val riskStatus: RiskStatus = RiskStatus.UNKNOWN,
    val requirements: List<KycRequirement> = emptyList(),

    // Document Upload Form
    val fullNameInput: String = "",
    val dateOfBirthInput: String = "",
    val selectedDocumentType: DocumentType = DocumentType.NATIONAL_ID,
    val documentNumberInput: String = "",
    val frontImageUri: String? = null,
    val backImageUri: String? = null,
    val selfieImageUri: String? = null,
    val isSubmissionSuccess: Boolean = false
)
