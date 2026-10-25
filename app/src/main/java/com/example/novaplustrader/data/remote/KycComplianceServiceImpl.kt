package com.example.novaplustrader.data.remote

import com.example.novaplustrader.domain.model.compliance.KycRequirement
import com.example.novaplustrader.domain.model.compliance.KycReviewDecision
import com.example.novaplustrader.domain.model.compliance.KycStatus
import com.example.novaplustrader.domain.model.compliance.KycVerificationRequest
import com.example.novaplustrader.domain.model.compliance.DocumentType
import com.example.novaplustrader.domain.model.compliance.RiskStatus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await

class KycComplianceServiceImpl(
    private val firestoreProvider: () -> FirebaseFirestore = { FirebaseFirestore.getInstance() },
    private val authProvider: () -> FirebaseAuth = { FirebaseAuth.getInstance() }
) : KycComplianceService {

    override fun observeKycStatus(userId: String): Flow<KycStatus> =
        observeUserStatus(userId, "kycStatus", KycStatus.NOT_STARTED, KycStatus.entries.toTypedArray())

    override fun observeRiskStatus(userId: String): Flow<RiskStatus> =
        observeUserStatus(userId, "riskStatus", RiskStatus.UNKNOWN, RiskStatus.entries.toTypedArray())

    override fun observeRequirements(userId: String): Flow<List<KycRequirement>> =
        flowOf(
            listOf(
                KycRequirement(
                    requirementId = "req_01",
                    type = "PERSONAL_INFO",
                    title = "Personal Information",
                    description = "Full legal name and date of birth"
                ),
                KycRequirement(
                    requirementId = "req_02",
                    type = "GOVT_ID",
                    title = "Government Identification Document",
                    description = "Valid Passport, National ID, or Driver's License"
                )
            )
        )

    override fun observeKycSubmission(userId: String): Flow<KycVerificationRequest?> = callbackFlow {
        if (userId.isBlank()) {
            trySend(null)
            close()
            return@callbackFlow
        }
        val registration = firestoreProvider().collection("users").document(userId)
            .addSnapshotListener { snapshot, error ->
                when {
                    error != null -> close(error)
                    snapshot == null -> close(IllegalStateException("Unable to load KYC details"))
                    !snapshot.exists() -> trySend(null)
                    else -> {
                        val details = snapshot.get("kycDetails") as? Map<*, *>
                        if (details == null) {
                            trySend(null)
                        } else {
                            val fullName = details["fullName"] as? String
                            val dateOfBirth = details["dateOfBirth"] as? String
                            val documentTypeName = details["documentType"] as? String
                            val documentNumber = details["documentNumber"] as? String
                            val documentType = DocumentType.entries.firstOrNull {
                                it.name == documentTypeName
                            }
                            if (
                                fullName.isNullOrBlank() ||
                                dateOfBirth.isNullOrBlank() ||
                                documentType == null ||
                                documentNumber.isNullOrBlank()
                            ) {
                                close(IllegalStateException("KYC record is incomplete"))
                            } else {
                                trySend(
                                    KycVerificationRequest(
                                        userId = userId,
                                        fullName = fullName,
                                        dateOfBirth = dateOfBirth,
                                        documentType = documentType,
                                        documentNumber = documentNumber
                                    )
                                )
                            }
                        }
                    }
                }
            }
        awaitClose { registration.remove() }
    }

    override suspend fun submitKycVerification(request: KycVerificationRequest): Result<KycStatus> = try {
        val uid = request.userId.ifBlank { authProvider().currentUser?.uid.orEmpty() }
        require(uid.isNotBlank()) { "Sign in before submitting KYC details." }
        val status = KycStatus.UNDER_REVIEW
        firestoreProvider().collection("users").document(uid).set(
            mapOf(
                "uid" to uid,
                "kycStatus" to status.name,
                "verificationStatus" to "PENDING",
                "kycDetails" to mapOf(
                    "fullName" to request.fullName,
                    "dateOfBirth" to request.dateOfBirth,
                    "documentType" to request.documentType.name,
                    "documentNumber" to request.documentNumber
                ),
                "kycSubmittedAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            ),
            SetOptions.merge()
        ).await()
        Result.success(status)
    } catch (error: CancellationException) {
        throw error
    } catch (error: Exception) {
        Result.failure(error)
    }

    override suspend fun reviewKycSubmission(
        userId: String,
        decision: KycReviewDecision,
        rejectionReason: String?
    ): Result<Boolean> = try {
        require(userId.isNotBlank()) { "A user ID is required to review KYC." }
        val status = when (decision) {
            KycReviewDecision.APPROVE -> KycStatus.VERIFIED
            KycReviewDecision.REJECT -> KycStatus.REJECTED
            KycReviewDecision.REQUEST_MORE_INFORMATION -> KycStatus.REQUIRES_UPDATE
        }
        firestoreProvider().collection("users").document(userId).set(
            mapOf(
                "kycStatus" to status.name,
                "verificationStatus" to if (decision == KycReviewDecision.APPROVE) "VERIFIED" else "PENDING",
                "kycReviewReason" to rejectionReason,
                "kycReviewedAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            ),
            SetOptions.merge()
        ).await()
        Result.success(true)
    } catch (error: CancellationException) {
        throw error
    } catch (error: Exception) {
        Result.failure(error)
    }

    private fun <T : Enum<T>> observeUserStatus(
        userId: String,
        field: String,
        defaultValue: T,
        values: Array<T>
    ): Flow<T> = callbackFlow {
        if (userId.isBlank()) {
            trySend(defaultValue)
            close()
            return@callbackFlow
        }
        val registration = firestoreProvider().collection("users").document(userId)
            .addSnapshotListener { snapshot, error ->
                when {
                    error != null -> close(error)
                    snapshot == null -> close(IllegalStateException("Unable to load KYC status"))
                    else -> {
                        val storedValue = snapshot.getString(field)
                        val parsedValue = if (storedValue == null) {
                            defaultValue
                        } else {
                            values.firstOrNull { it.name == storedValue }
                                ?: run {
                                    close(IllegalStateException("Unknown $field value: $storedValue"))
                                    return@addSnapshotListener
                                }
                        }
                        trySend(parsedValue)
                    }
                }
            }
        awaitClose { registration.remove() }
    }
}
