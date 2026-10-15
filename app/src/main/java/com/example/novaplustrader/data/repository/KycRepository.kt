package com.example.novaplustrader.data.repository

import com.example.novaplustrader.domain.model.compliance.DocumentType
import com.example.novaplustrader.domain.model.compliance.KycRequirement
import com.example.novaplustrader.domain.model.compliance.KycReviewDecision
import com.example.novaplustrader.domain.model.compliance.KycStatus
import com.example.novaplustrader.domain.model.compliance.RiskStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface KycRepository {
    fun observeKycStatus(): StateFlow<KycStatus>
    fun observeRiskStatus(): StateFlow<RiskStatus>
    fun observeRequirements(): Flow<List<KycRequirement>>

    suspend fun submitKycVerification(
        fullName: String,
        dateOfBirth: String,
        documentType: DocumentType,
        documentNumber: String,
        frontImageUri: String?,
        backImageUri: String?,
        selfieImageUri: String?
    ): Result<KycStatus>

    suspend fun reviewKycSubmission(
        userId: String,
        decision: KycReviewDecision,
        rejectionReason: String?
    ): Result<Boolean>
}
