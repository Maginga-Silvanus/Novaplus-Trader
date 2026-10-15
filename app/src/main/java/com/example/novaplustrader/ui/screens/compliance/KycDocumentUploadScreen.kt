package com.example.novaplustrader.ui.screens.compliance

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.novaplustrader.domain.model.compliance.DocumentType
import com.example.novaplustrader.ui.components.IconActionButton
import com.example.novaplustrader.ui.components.PrimaryButton
import com.example.novaplustrader.ui.components.SuccessAnimation
import com.example.novaplustrader.ui.theme.BackgroundDark
import com.example.novaplustrader.ui.theme.ElectricLime
import com.example.novaplustrader.ui.theme.ElectricLimeSurface
import com.example.novaplustrader.ui.theme.FinancialGreen
import com.example.novaplustrader.ui.theme.FinancialRed
import com.example.novaplustrader.ui.theme.OverlayDark
import com.example.novaplustrader.ui.theme.SurfaceBorder
import com.example.novaplustrader.ui.theme.SurfaceDarkCard
import com.example.novaplustrader.ui.theme.TextMuted
import com.example.novaplustrader.ui.theme.TextPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KycDocumentUploadScreen(
    viewModel: KycViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

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
                    text = "Upload Identity Documents",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Box(modifier = Modifier.padding(start = 24.dp))
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 100.dp, start = 16.dp, end = 16.dp)
            ) {
                // Document Type Selector
                item {
                    Text(
                        text = "DOCUMENT TYPE",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(DocumentType.entries) { type ->
                            val isSelected = type == uiState.selectedDocumentType
                            val bg = if (isSelected) ElectricLimeSurface else SurfaceDarkCard
                            val border = if (isSelected) ElectricLime else SurfaceBorder

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(bg)
                                    .border(1.dp, border, RoundedCornerShape(20.dp))
                                    .clickable { viewModel.selectDocumentType(type) }
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = type.displayName,
                                    color = if (isSelected) ElectricLime else TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // Full Legal Name Input
                item {
                    Spacer(modifier = Modifier.height(18.dp))
                    Text(
                        text = "FULL LEGAL NAME",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = uiState.fullNameInput,
                        onValueChange = { viewModel.updateFullNameInput(it) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
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

                // Document Number Input
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "DOCUMENT NUMBER",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = uiState.documentNumberInput,
                        onValueChange = { viewModel.updateDocumentNumberInput(it) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
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

                    if (uiState.validationError != null) {
                        Text(
                            text = uiState.validationError ?: "",
                            color = FinancialRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                        )
                    }
                }

                // Document Upload Buttons
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "ATTACHMENT / CAPTURE",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        DocumentAttachmentRow(
                            title = "Front Image of ${uiState.selectedDocumentType.displayName}",
                            isAttached = uiState.frontImageUri != null,
                            icon = Icons.Default.UploadFile,
                            onClick = { viewModel.setFrontImageUri("uri_front_attached") }
                        )

                        DocumentAttachmentRow(
                            title = "Back Image (if applicable)",
                            isAttached = uiState.backImageUri != null,
                            icon = Icons.Default.UploadFile,
                            onClick = { viewModel.setBackImageUri("uri_back_attached") }
                        )

                        DocumentAttachmentRow(
                            title = "Liveness Verification Selfie Photo",
                            isAttached = uiState.selfieImageUri != null,
                            icon = Icons.Default.CameraAlt,
                            onClick = { viewModel.setSelfieImageUri("uri_selfie_attached") }
                        )
                    }
                }

                // Submit Button
                item {
                    Spacer(modifier = Modifier.height(28.dp))

                    PrimaryButton(
                        text = "SUBMIT FOR VERIFICATION",
                        onClick = { viewModel.submitKyc() },
                        isLoading = uiState.isSubmitting
                    )
                }
            }
        }

        // Success Dialog
        AnimatedVisibility(
            visible = uiState.isSubmissionSuccess,
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
                    title = "Documents Submitted!",
                    subtitle = "Your identity verification request is being reviewed by our compliance department.",
                    onDoneClick = {
                        viewModel.clearMessages()
                        onBackClick()
                    }
                )
            }
        }
    }
}

@Composable
private fun DocumentAttachmentRow(
    title: String,
    isAttached: Boolean,
    icon: ImageVector,
    onClick: () -> Unit
) {
    val borderColor = if (isAttached) FinancialGreen else SurfaceBorder

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceDarkCard)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isAttached) FinancialGreen else ElectricLime
            )

            Text(
                text = title,
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }

        if (isAttached) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Attached",
                tint = FinancialGreen
            )
        } else {
            Text(
                text = "ATTACH",
                color = ElectricLime,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
