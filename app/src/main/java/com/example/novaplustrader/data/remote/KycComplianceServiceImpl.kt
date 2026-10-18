package com.example.novaplustrader.data.remote

import com.example.novaplustrader.domain.model.compliance.KycRequirement
import com.example.novaplustrader.domain.model.compliance.KycReviewDecision
import com.example.novaplustrader.domain.model.compliance.KycStatus
import com.example.novaplustrader.domain.model.compliance.KycVerificationRequest
import com.example.novaplustrader.domain.model.compliance.RiskStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class KycComplianceServiceImpl : KycComplianceService {

    private val _kycStatus = MutableStateFlow(KycStatus.NOT_STARTED)
    private val _riskStatus = MutableStateFlow(RiskStatus.UNKNOWN)

    private val _requirements = MutableStateFlow(
        listOf(
            KycRequirement(
                requirementId = "req_01",
                type = "PERSONAL_INFO",
                title = "Personal Information",
                description = "Full legal name and date of birth",
                required = true,
                status = "PENDING"
            ),
            KycRequirement(
                requirementId = "req_02",
                type = "GOVT_ID",
                title = "Government Identification Document",
                description = "Valid Passport, National ID, or Driver's License",
                required = true,
                status = "PENDING"
            ),
            KycRequirement(
                requirementId = "req_03",
                type = "SELFIE",
                title = "Liveness Verification / Selfie",
                description = "Selfie photograph for facial match verification",
                required = true,
                status = "PENDING"
            )
        )
    )

    override fun observeKycStatus(userId: String): StateFlow<KycStatus> = _kycStatus.asStateFlow()
    override fun observeRiskStatus(userId: String): StateFlow<RiskStatus> = _riskStatus.asStateFlow()
    override fun observeRequirements(userId: String): Flow<List<KycRequirement>> = _requirements.asStateFlow()

    override suspend fun submitKycVerification(request: KycVerificationRequest): Result<KycStatus> {
        return Result.failure(
            IllegalStateException("KYC document storage and verification are not configured. Nothing was submitted.")
        )
    }

    override suspend fun reviewKycSubmission(
        userId: String,
        decision: KycReviewDecision,
        rejectionReason: String?
    ): Result<Boolean> {
        return Result.failure(
            IllegalStateException("The trusted KYC review backend is not configured. No review decision was saved.")
        )
    }
}
