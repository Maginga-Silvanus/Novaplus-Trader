package com.example.novaplustrader.ui.screens.gaming

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.example.novaplustrader.domain.model.gaming.ExclusionPeriod
import com.example.novaplustrader.ui.components.ConfirmationSheet
import com.example.novaplustrader.ui.components.IconActionButton
import com.example.novaplustrader.ui.components.AccessibleDropdownSelector
import com.example.novaplustrader.ui.components.PrimaryButton
import com.example.novaplustrader.ui.components.SuccessAnimation
import com.example.novaplustrader.ui.theme.BackgroundDark
import com.example.novaplustrader.ui.theme.ElectricLime
import com.example.novaplustrader.ui.theme.ElectricLimeSurface
import com.example.novaplustrader.ui.theme.FinancialRed
import com.example.novaplustrader.ui.theme.OverlayDark
import com.example.novaplustrader.ui.theme.SurfaceBorder
import com.example.novaplustrader.ui.theme.SurfaceDarkCard
import com.example.novaplustrader.ui.theme.TextMuted
import com.example.novaplustrader.ui.theme.TextPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelfExclusionScreen(
    viewModel: ResponsibleGamingViewModel,
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
                    text = "Self-Exclusion Lock",
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
                // Exclusion Period Selector
                item {
                    Text(
                        text = "EXCLUSION DURATION",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    AccessibleDropdownSelector(
                        label = "Exclusion duration",
                        options = ExclusionPeriod.entries,
                        selectedOption = uiState.selectedExclusionPeriod,
                        optionLabel = { it.label },
                        onOptionSelected = viewModel::selectExclusionPeriod
                    )
                }

                // Consequences Warning Box
                item {
                    Spacer(modifier = Modifier.height(20.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(SurfaceDarkCard)
                            .border(1.dp, FinancialRed.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                            .padding(18.dp)
                    ) {
                        Text(
                            text = "SELF-EXCLUSION CONSEQUENCES",
                            color = FinancialRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "1. Access to Casino Spin and Sportsbook demo features will be locked immediately.\n2. The restriction cannot be revoked or unlocked until the duration expires.\n3. Your real wallet balance, trading, and bot features remain unaffected.",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                    }
                }

                // Activate Button
                item {
                    Spacer(modifier = Modifier.height(28.dp))

                    PrimaryButton(
                        text = "ACTIVATE SELF-EXCLUSION",
                        onClick = { viewModel.openExclusionConfirmSheet() },
                        isLoading = uiState.isSubmitting
                    )
                }
            }
        }

        // Confirmation Sheet
        if (uiState.isExclusionConfirmOpen) {
            ConfirmationSheet(
                title = "Confirm Self-Exclusion?",
                details = listOf(
                    "Selected Period" to uiState.selectedExclusionPeriod.label,
                    "Locked Features" to "Casino Spin & Sportsbook Demo",
                    "Notice" to "This lock cannot be undone or bypassed before expiration."
                ),
                confirmButtonText = "Activate Lock Now",
                isLoading = uiState.isSubmitting,
                onConfirm = { viewModel.confirmSelfExclusion() },
                onDismiss = { viewModel.closeExclusionConfirmSheet() }
            )
        }

        // Success Dialog
        AnimatedVisibility(
            visible = uiState.isExclusionSuccess,
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
                    title = "Self-Exclusion Active",
                    subtitle = "Your self-exclusion lock has been saved. Gaming features are now restricted.",
                    onDoneClick = {
                        viewModel.clearMessages()
                        onBackClick()
                    }
                )
            }
        }
    }
}
