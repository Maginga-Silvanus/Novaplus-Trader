package com.example.novaplustrader.ui.screens.admin

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.novaplustrader.data.repository.KycRepository
import com.example.novaplustrader.data.repository.KycRepositoryImpl
import com.example.novaplustrader.domain.model.compliance.KycReviewDecision
import com.example.novaplustrader.ui.components.IconActionButton
import com.example.novaplustrader.ui.components.PrimaryButton
import com.example.novaplustrader.ui.components.SecondaryButton
import com.example.novaplustrader.ui.components.SuccessAnimation
import com.example.novaplustrader.ui.theme.BackgroundDark
import com.example.novaplustrader.ui.theme.ElectricLime
import com.example.novaplustrader.ui.theme.OverlayDark
import com.example.novaplustrader.ui.theme.SurfaceBorder
import com.example.novaplustrader.ui.theme.SurfaceDarkCard
import com.example.novaplustrader.ui.theme.SurfaceDarkElevated
import com.example.novaplustrader.ui.theme.TextMuted
import com.example.novaplustrader.ui.theme.TextPrimary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KycReviewScreen(
    userId: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    repository: KycRepository = KycRepositoryImpl()
) {
    val scope = rememberCoroutineScope()
    var rejectionReasonInput by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconActionButton(
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    onClick = onBackClick
                )

                Text(
                    text = "KYC Applicant Review",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Box(modifier = Modifier.padding(start = 24.dp))
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 100.dp, start = 16.dp, end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Applicant Summary
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(SurfaceDarkCard)
                            .border(1.dp, SurfaceBorder, RoundedCornerShape(20.dp))
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "APPLICANT SUMMARY",
                            color = ElectricLime,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        DetailRow("Full Legal Name", "Leonard Lopez")
                        DetailRow("User Account ID", userId)
                        DetailRow("Date of Birth", "1994-08-14")
                        DetailRow("Document Type", "National ID Card")
                        DetailRow("Document Number", "ID-9920194")
                    }
                }

                // Document Verification Attachments
                item {
                    Text(
                        text = "DOCUMENT ATTACHMENTS",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        AttachmentPlaceholderBox("Front Image of National ID Card (Attached)")
                        AttachmentPlaceholderBox("Back Image of National ID Card (Attached)")
                        AttachmentPlaceholderBox("Liveness Verification Selfie Photo (Verified)")
                    }
                }

                // Rejection Reason Input
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "FEEDBACK / REJECTION REASON (IF APPLICABLE)",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = rejectionReasonInput,
                        onValueChange = { rejectionReasonInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        placeholder = { Text("Enter feedback or rejection reason...", color = TextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = SurfaceDarkCard,
                            unfocusedContainerColor = SurfaceDarkCard,
                            focusedBorderColor = ElectricLime,
                            unfocusedBorderColor = SurfaceBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        singleLine = true
                    )
                }

                // Action Buttons
                item {
                    Spacer(modifier = Modifier.height(16.dp))

                    PrimaryButton(
                        text = "APPROVE VERIFICATION",
                        onClick = {
                            isSubmitting = true
                            scope.launch {
                                repository.reviewKycSubmission(userId, KycReviewDecision.APPROVE, null)
                                isSubmitting = false
                                successMessage = "KYC Verification Approved!"
                            }
                        },
                        isLoading = isSubmitting
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    SecondaryButton(
                        text = "REJECT VERIFICATION",
                        onClick = {
                            isSubmitting = true
                            scope.launch {
                                repository.reviewKycSubmission(userId, KycReviewDecision.REJECT, rejectionReasonInput.ifBlank { "Document unreadable" })
                                isSubmitting = false
                                successMessage = "KYC Verification Rejected"
                            }
                        }
                    )
                }
            }
        }

        // Success Dialog
        AnimatedVisibility(
            visible = successMessage != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(OverlayDark)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                SuccessAnimation(
                    title = successMessage ?: "Review Submitted",
                    subtitle = "The compliance decision has been saved and dispatched to the user.",
                    onDoneClick = {
                        successMessage = null
                        onBackClick()
                    }
                )
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = TextMuted, fontSize = 12.sp)
        Text(text = value, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun AttachmentPlaceholderBox(title: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceDarkElevated)
            .border(1.dp, SurfaceBorder, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Text(text = title, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}
