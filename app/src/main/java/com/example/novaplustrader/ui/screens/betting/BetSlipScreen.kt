package com.example.novaplustrader.ui.screens.betting

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.novaplustrader.ui.components.ConfirmationSheet
import com.example.novaplustrader.ui.components.IconActionButton
import com.example.novaplustrader.ui.components.AccessibleDropdownSelector
import com.example.novaplustrader.ui.components.PrimaryButton
import com.example.novaplustrader.ui.components.SecondaryButton
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
import com.example.novaplustrader.ui.util.rememberNovaHaptics

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BetSlipScreen(
    viewModel: BettingViewModel,
    onBackClick: () -> Unit,
    onNavigateToMyBets: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val haptics = rememberNovaHaptics()

    val combinedOdds = uiState.betSlipItems.fold(1.0) { acc, item -> acc * item.selection.odds }
    val stake = uiState.demoStakeString.toDoubleOrNull() ?: 0.0
    val potentialReturn = stake * combinedOdds

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
                    text = "Demo Bet Slip (${uiState.betSlipItems.size})",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                if (uiState.betSlipItems.isNotEmpty()) {
                    IconActionButton(
                        icon = Icons.Default.Delete,
                        contentDescription = "Clear",
                        onClick = { viewModel.clearBetSlip() }
                    )
                } else {
                    Box(modifier = Modifier.padding(start = 24.dp))
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 100.dp, start = 16.dp, end = 16.dp)
            ) {
                if (uiState.betSlipItems.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 60.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Your bet slip is empty. Select odds from any match to start.",
                                color = TextMuted,
                                fontSize = 13.sp
                            )
                        }
                    }
                } else {
                    // Selected Items List
                    items(uiState.betSlipItems) { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(SurfaceDarkCard)
                                .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.eventName,
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${item.selection.marketName}: ${item.selection.selectionName}",
                                    color = TextMuted,
                                    fontSize = 12.sp
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "@ ${String.format("%.2f", item.selection.odds)}",
                                    color = ElectricLime,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove",
                                    tint = TextMuted,
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clickable {
                                            viewModel.removeBetSlipItem(item.selection.id)
                                        }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // Combined Odds & Stake Entry
                    item {
                        Spacer(modifier = Modifier.height(12.dp))

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(SurfaceDarkCard)
                                .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Combined Odds", color = TextMuted, fontSize = 12.sp)
                                Text(
                                    text = String.format("%.2f", combinedOdds),
                                    color = ElectricLime,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Available Balance", color = TextMuted, fontSize = 12.sp)
                                Text(
                                    text = "${uiState.demoBettingBalance.toInt()} Demo Credits",
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "DEMO STAKE (CREDITS)",
                            color = TextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedTextField(
                            value = uiState.demoStakeString,
                            onValueChange = { viewModel.updateDemoStake(it) },
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
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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

                        Spacer(modifier = Modifier.height(10.dp))

                        AccessibleDropdownSelector(
                            label = "Demo stake preset",
                            options = listOf("10", "25", "50", "100"),
                            selectedOption = uiState.selectedStakePreset?.toString() ?: "Select preset",
                            optionLabel = { "$it credits" },
                            onOptionSelected = { preset -> viewModel.applyStakePreset(preset.toInt()) }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Return Card Summary
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(SurfaceDarkCard)
                                .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(text = "POTENTIAL DEMO RETURN", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = "${potentialReturn.toInt()} Demo Credits",
                                color = FinancialGreen,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        PrimaryButton(
                            text = "PLACE DEMO BET",
                            onClick = {
                                haptics.confirmation()
                                viewModel.openConfirmSheet()
                            },
                            isLoading = uiState.isLoading
                        )
                    }
                }
            }
        }

        // Confirmation Sheet Modal
        if (uiState.isConfirmSheetOpen) {
            ConfirmationSheet(
                title = "Confirm Demo Bet Slip",
                details = listOf(
                    "Selections Count" to "${uiState.betSlipItems.size}",
                    "Combined Odds" to String.format("%.2f", combinedOdds),
                    "Demo Stake" to "${stake.toInt()} Credits",
                    "Potential Demo Return" to "${potentialReturn.toInt()} Credits",
                    "Wagering Disclaimer" to "DEMO MODE — No real money is being wagered."
                ),
                confirmButtonText = "Confirm Demo Bet",
                isLoading = uiState.isLoading,
                onConfirm = { viewModel.executePlaceDemoBet() },
                onDismiss = { viewModel.closeConfirmSheet() }
            )
        }

        // Success Dialog
        AnimatedVisibility(
            visible = uiState.lastPlacedDemoBet != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            val bet = uiState.lastPlacedDemoBet
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(OverlayDark)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                if (bet != null) {
                    SuccessAnimation(
                        title = "Demo Bet Placed!",
                        subtitle = "Wagered ${bet.totalStakeCredits.toInt()} Demo Credits @ ${String.format("%.2f", bet.combinedOdds)} odds. Ref: ${bet.id}",
                        onDoneClick = {
                            viewModel.clearLastPlacedBet()
                            onNavigateToMyBets()
                        }
                    )
                }
            }
        }
    }
}
