package com.example.novaplustrader.ui.screens.admin

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.novaplustrader.ui.components.ConfirmationSheet
import com.example.novaplustrader.ui.components.PrimaryButton
import com.example.novaplustrader.ui.screens.admin.components.AdminFilterChipBar
import com.example.novaplustrader.ui.screens.admin.components.AdminHeaderBar
import com.example.novaplustrader.ui.screens.admin.models.AdminNotificationAudience
import com.example.novaplustrader.ui.screens.admin.models.AdminNotificationCategory
import com.example.novaplustrader.ui.screens.admin.models.AdminNotificationPriority
import com.example.novaplustrader.ui.theme.BackgroundDark
import com.example.novaplustrader.ui.theme.ElectricLime
import com.example.novaplustrader.ui.theme.ElectricLimeSurface
import com.example.novaplustrader.ui.theme.FinancialRed
import com.example.novaplustrader.ui.theme.FinancialRedGlow
import com.example.novaplustrader.ui.theme.SurfaceBorder
import com.example.novaplustrader.ui.theme.SurfaceDarkCard
import com.example.novaplustrader.ui.theme.TextMuted
import com.example.novaplustrader.ui.theme.TextPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminNotificationsScreen(
    viewModel: AdminNotificationsViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    val categories = AdminNotificationCategory.values().map { it.label }
    val audiences = AdminNotificationAudience.values().map { it.label }
    val priorities = AdminNotificationPriority.values().map { it.label }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        AdminHeaderBar(
            title = "Push & Operational Alerts",
            onBackClick = onBackClick
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Info
            item {
                Text(
                    text = "PREPARE OPERATIONAL NOTIFICATION",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            // Feedback Messages
            if (uiState.successMessage != null) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(ElectricLimeSurface)
                            .border(1.dp, ElectricLime, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = uiState.successMessage!!,
                            color = ElectricLime,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            if (uiState.errorMessage != null) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(FinancialRedGlow)
                            .border(1.dp, FinancialRed, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = uiState.errorMessage!!,
                            color = FinancialRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Form Inputs Card
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(SurfaceDarkCard)
                        .border(1.dp, SurfaceBorder, RoundedCornerShape(20.dp))
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Notification Category",
                        color = TextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    AdminFilterChipBar(
                        options = categories,
                        selectedOption = uiState.category.label,
                        onOptionSelected = { label ->
                            AdminNotificationCategory.values().find { it.label == label }?.let {
                                viewModel.updateCategory(it)
                            }
                        }
                    )

                    Text(
                        text = "Target Audience",
                        color = TextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    AdminFilterChipBar(
                        options = audiences,
                        selectedOption = uiState.audience.label,
                        onOptionSelected = { label ->
                            AdminNotificationAudience.values().find { it.label == label }?.let {
                                viewModel.updateAudience(it)
                            }
                        }
                    )

                    if (uiState.audience == AdminNotificationAudience.SPECIFIC_USER) {
                        OutlinedTextField(
                            value = uiState.targetUserId,
                            onValueChange = viewModel::updateTargetUserId,
                            label = { Text("Target User ID (UID)", color = TextMuted) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ElectricLime,
                                unfocusedBorderColor = SurfaceBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    OutlinedTextField(
                        value = uiState.title,
                        onValueChange = viewModel::updateTitle,
                        label = { Text("Notification Title", color = TextMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricLime,
                            unfocusedBorderColor = SurfaceBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = uiState.message,
                        onValueChange = viewModel::updateMessage,
                        label = { Text("Notification Message Body", color = TextMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricLime,
                            unfocusedBorderColor = SurfaceBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Text(
                        text = "Priority Level",
                        color = TextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    AdminFilterChipBar(
                        options = priorities,
                        selectedOption = uiState.priority.label,
                        onOptionSelected = { label ->
                            AdminNotificationPriority.values().find { it.label == label }?.let {
                                viewModel.updatePriority(it)
                            }
                        }
                    )

                    OutlinedTextField(
                        value = uiState.deepLinkRoute,
                        onValueChange = viewModel::updateDeepLinkRoute,
                        label = { Text("Deep-link Route (Optional, e.g. 'wallet' or 'trade')", color = TextMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricLime,
                            unfocusedBorderColor = SurfaceBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    PrimaryButton(
                        text = "Prepare & Submit Notification",
                        onClick = viewModel::initiateSendConfirmation
                    )
                }
            }
        }

        // Confirmation Sheet
        if (uiState.isConfirmationShowing) {
            ConfirmationSheet(
                title = "Confirm Operational Notification Request",
                confirmButtonText = "Dispatch Notification",
                isLoading = uiState.isSending,
                details = listOf(
                    Pair("Title", uiState.title),
                    Pair("Category", uiState.category.label),
                    Pair("Audience", uiState.audience.label),
                    Pair("Priority", uiState.priority.label),
                    Pair("Notice", "Message will be delivered via backend FCM messaging service")
                ),
                onConfirm = viewModel::confirmSendNotification,
                onDismiss = viewModel::dismissConfirmation
            )
        }
    }
}
