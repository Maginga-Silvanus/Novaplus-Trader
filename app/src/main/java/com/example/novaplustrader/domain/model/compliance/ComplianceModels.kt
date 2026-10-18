package com.example.novaplustrader.domain.model.compliance

enum class KycStatus(val label: String) {
    NOT_STARTED("Not Started"),
    IN_PROGRESS("In Progress"),
    SUBMITTED("Submitted"),
    UNDER_REVIEW("Under Review"),
    VERIFIED("Verified"),
    REJECTED("Verification Rejected"),
    EXPIRED("Verification Expired"),
    REQUIRES_UPDATE("Re-verification Required")
}

enum class RiskStatus(val label: String) {
    UNKNOWN("Risk Status Unavailable"),
    NORMAL("Normal Account"),
    REVIEW_REQUIRED("Pending Risk Review"),
    RESTRICTED("Account Restricted"),
    SUSPENDED("Account Suspended")
}

enum class DocumentType(val displayName: String) {
    NATIONAL_ID("National ID Card"),
    PASSPORT("Passport"),
    DRIVERS_LICENSE("Driver's License")
}

data class KycRequirement(
    val requirementId: String,
    val type: String,
    val title: String,
    val description: String,
    val required: Boolean = true,
    val status: String = "PENDING",
    val expiresAt: String? = null
)

data class KycVerificationRequest(
    val userId: String = "",
    val fullName: String,
    val dateOfBirth: String,
    val documentType: DocumentType,
    val documentNumber: String,
    val frontImageUri: String? = null,
    val backImageUri: String? = null,
    val selfieImageUri: String? = null,
    val submissionTimestamp: String = ""
)

enum class KycReviewDecision(val label: String) {
    APPROVE("Approve Verification"),
    REJECT("Reject Verification"),
    REQUEST_MORE_INFORMATION("Request Additional Documents")
}

data class KycReviewRecord(
    val reviewId: String,
    val userId: String,
    val decision: KycReviewDecision,
    val reviewerUid: String,
    val rejectionReason: String? = null,
    val timestamp: String
)
