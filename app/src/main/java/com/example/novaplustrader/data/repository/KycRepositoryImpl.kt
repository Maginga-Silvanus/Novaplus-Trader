package com.example.novaplustrader.data.repository

import com.example.novaplustrader.data.remote.KycComplianceService
import com.example.novaplustrader.data.remote.KycComplianceServiceImpl
import com.example.novaplustrader.domain.model.compliance.DocumentType
import com.example.novaplustrader.domain.model.compliance.KycRequirement
import com.example.novaplustrader.domain.model.compliance.KycReviewDecision
import com.example.novaplustrader.domain.model.compliance.KycStatus
import com.example.novaplustrader.domain.model.compliance.KycVerificationRequest
import com.example.novaplustrader.domain.model.compliance.RiskStatus
import com.example.novaplustrader.ui.screens.notifications.models.NotificationCategory
import com.example.novaplustrader.ui.screens.notifications.models.NotificationModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow

class KycRepositoryImpl(
    private val service: KycComplianceService = KycComplianceServiceImpl(),
    private val notificationRepository: NotificationRepository = NotificationRepositoryImpl(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : KycRepository {

    private val userId: String
        get() = auth.currentUser?.uid.orEmpty()

    override fun observeKycStatus(): Flow<KycStatus> = service.observeKycStatus(userId)
    override fun observeRiskStatus(): Flow<RiskStatus> = service.observeRiskStatus(userId)
    override fun observeRequirements(): Flow<List<KycRequirement>> = service.observeRequirements(userId)
    override fun observeKycSubmission(userId: String) = service.observeKycSubmission(userId)

    override suspend fun submitKycVerification(
        fullName: String,
        dateOfBirth: String,
        documentType: DocumentType,
        documentNumber: String
    ): Result<KycStatus> {
        val uid = auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("Sign in before submitting KYC documents."))
        val request = KycVerificationRequest(
            userId = uid,
            fullName = fullName,
            dateOfBirth = dateOfBirth,
            documentType = documentType,
            documentNumber = documentNumber,
            submissionTimestamp = System.currentTimeMillis().toString()
        )

        val result = service.submitKycVerification(request)

        result.onSuccess {
            notificationRepository.saveNotification(
                NotificationModel(
                    id = "notif_kyc_sub_${System.currentTimeMillis()}",
                    title = "KYC Documents Submitted",
                    body = "Your identity verification documents have been submitted and are under review.",
                    category = NotificationCategory.SECURITY,
                    timestamp = "Just now",
                    deepLink = "kyc"
                )
            )
        }

        return result
    }

    override suspend fun reviewKycSubmission(
        userId: String,
        decision: KycReviewDecision,
        rejectionReason: String?
    ): Result<Boolean> {
        val result = service.reviewKycSubmission(userId, decision, rejectionReason)

        result.onSuccess {
            val title = when (decision) {
                KycReviewDecision.APPROVE -> "Account Verified!"
                KycReviewDecision.REJECT -> "Verification Rejected"
                KycReviewDecision.REQUEST_MORE_INFORMATION -> "Additional Documents Required"
            }
            val body = when (decision) {
                KycReviewDecision.APPROVE -> "Your account verification is complete. Full wallet & trading capabilities are enabled."
                KycReviewDecision.REJECT -> "Verification rejected. Reason: ${rejectionReason ?: "Document unreadable"}."
                KycReviewDecision.REQUEST_MORE_INFORMATION -> "Please re-upload a clearer image of your government ID."
            }

            notificationRepository.saveNotification(
                NotificationModel(
                    id = "notif_kyc_rev_${System.currentTimeMillis()}",
                    title = title,
                    body = body,
                    category = NotificationCategory.SECURITY,
                    timestamp = "Just now",
                    deepLink = "kyc"
                )
            )
        }

        return result
    }
}
