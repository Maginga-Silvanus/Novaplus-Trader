package com.example.novaplustrader

import com.example.novaplustrader.data.remote.KycComplianceServiceImpl
import com.example.novaplustrader.domain.model.compliance.DocumentType
import com.example.novaplustrader.domain.model.compliance.KycRequirement
import com.example.novaplustrader.domain.model.compliance.KycReviewDecision
import com.example.novaplustrader.domain.model.compliance.KycStatus
import com.example.novaplustrader.domain.model.compliance.KycVerificationRequest
import com.example.novaplustrader.domain.model.compliance.RiskStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlinx.coroutines.runBlocking

class KycComplianceTest {

    @Test
    fun testKycStatusLabelsAndEnums() {
        assertEquals("Verified", KycStatus.VERIFIED.label)
        assertEquals("Under Review", KycStatus.UNDER_REVIEW.label)
        assertEquals("Verification Rejected", KycStatus.REJECTED.label)
        assertNotNull(KycStatus.SUBMITTED)
    }

    @Test
    fun testRiskStatusLabelsAndEnums() {
        assertEquals("Normal Account", RiskStatus.NORMAL.label)
        assertEquals("Account Restricted", RiskStatus.RESTRICTED.label)
        assertEquals("Account Suspended", RiskStatus.SUSPENDED.label)
    }

    @Test
    fun testDocumentTypeDisplayNames() {
        assertEquals("National ID Card", DocumentType.NATIONAL_ID.displayName)
        assertEquals("Passport", DocumentType.PASSPORT.displayName)
        assertEquals("Driver's License", DocumentType.DRIVERS_LICENSE.displayName)
    }

    @Test
    fun testKycVerificationRequestModel() {
        val req = KycVerificationRequest(
            userId = "usr_001",
            fullName = "Leonard Lopez",
            dateOfBirth = "1994-08-14",
            documentType = DocumentType.NATIONAL_ID,
            documentNumber = "ID-9920194"
        )

        assertEquals("usr_001", req.userId)
        assertEquals("Leonard Lopez", req.fullName)
        assertEquals(DocumentType.NATIONAL_ID, req.documentType)
        assertTrue(req.documentNumber.isNotBlank())
    }

    @Test
    fun testKycRequirementModel() {
        val requirement = KycRequirement(
            requirementId = "req_101",
            type = "GOVT_ID",
            title = "Government ID",
            description = "Valid Passport or National ID",
            required = true,
            status = "VERIFIED"
        )

        assertEquals("Government ID", requirement.title)
        assertTrue(requirement.required)
    }

    @Test
    fun unconfiguredVerificationDoesNotClaimSuccessOrVerifiedStatus() = runBlocking {
        val service = KycComplianceServiceImpl()
        val request = KycVerificationRequest(
            userId = "firebase-uid",
            fullName = "Test User",
            dateOfBirth = "1990-01-01",
            documentType = DocumentType.NATIONAL_ID,
            documentNumber = "TEST-1"
        )

        val submission = service.submitKycVerification(request)
        val review = service.reviewKycSubmission(
            userId = request.userId,
            decision = KycReviewDecision.APPROVE,
            rejectionReason = null
        )

        assertTrue(submission.isFailure)
        assertTrue(review.isFailure)
        assertEquals(KycStatus.NOT_STARTED, service.observeKycStatus(request.userId).value)
    }
}
