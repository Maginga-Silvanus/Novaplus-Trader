package com.example.novaplustrader.data.remote

import com.example.novaplustrader.domain.model.compliance.KycRequirement
import com.example.novaplustrader.domain.model.compliance.KycReviewDecision
import com.example.novaplustrader.domain.model.compliance.KycStatus
import com.example.novaplustrader.domain.model.compliance.KycVerificationRequest
import com.example.novaplustrader.domain.model.compliance.RiskStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface KycComplianceService {
    fun observeKycStatus(userId: String): StateFlow<KycStatus>
    fun observeRiskStatus(userId: String): StateFlow<RiskStatus>
    fun observeRequirements(userId: String): Flow<List<KycRequirement>>

    suspend fun submitKycVerification(request: KycVerificationRequest): Result<KycStatus>
    suspend fun reviewKycSubmission(
        userId: String,
        decision: KycReviewDecision,
        rejectionReason: String?
    ): Result<Boolean>
}
