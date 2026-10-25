package com.example.novaplustrader.data.repository

import com.example.novaplustrader.domain.model.compliance.DocumentType
import com.example.novaplustrader.domain.model.compliance.KycRequirement
import com.example.novaplustrader.domain.model.compliance.KycReviewDecision
import com.example.novaplustrader.domain.model.compliance.KycStatus
import com.example.novaplustrader.domain.model.compliance.KycVerificationRequest
import com.example.novaplustrader.domain.model.compliance.RiskStatus
import kotlinx.coroutines.flow.Flow

interface KycRepository {
    fun observeKycStatus(): Flow<KycStatus>
    fun observeRiskStatus(): Flow<RiskStatus>
    fun observeRequirements(): Flow<List<KycRequirement>>
    fun observeKycSubmission(userId: String): Flow<KycVerificationRequest?>

    suspend fun submitKycVerification(
        fullName: String,
        dateOfBirth: String,
        documentType: DocumentType,
        documentNumber: String
    ): Result<KycStatus>

    suspend fun reviewKycSubmission(
        userId: String,
        decision: KycReviewDecision,
        rejectionReason: String?
    ): Result<Boolean>
}
