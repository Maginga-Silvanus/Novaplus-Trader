package com.example.novaplustrader.data.remote

import com.example.novaplustrader.domain.model.compliance.KycRequirement
import com.example.novaplustrader.domain.model.compliance.KycReviewDecision
import com.example.novaplustrader.domain.model.compliance.KycStatus
import com.example.novaplustrader.domain.model.compliance.KycVerificationRequest
import com.example.novaplustrader.domain.model.compliance.RiskStatus
import kotlinx.coroutines.flow.Flow

interface KycComplianceService {
    fun observeKycStatus(userId: String): Flow<KycStatus>
    fun observeRiskStatus(userId: String): Flow<RiskStatus>
    fun observeRequirements(userId: String): Flow<List<KycRequirement>>
    fun observeKycSubmission(userId: String): Flow<KycVerificationRequest?>

    suspend fun submitKycVerification(request: KycVerificationRequest): Result<KycStatus>
    suspend fun reviewKycSubmission(
        userId: String,
        decision: KycReviewDecision,
        rejectionReason: String?
    ): Result<Boolean>
}
