package com.example.novaplustrader.ui.screens.casino

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.novaplustrader.ui.components.ConfirmationSheet
import com.example.novaplustrader.ui.components.IconActionButton
import com.example.novaplustrader.ui.components.PrimaryButton
import com.example.novaplustrader.ui.components.SecondaryButton
import com.example.novaplustrader.ui.model.Currency
import com.example.novaplustrader.ui.screens.casino.components.SpinWheel
import com.example.novaplustrader.ui.screens.casino.models.CasinoGameStatus
import com.example.novaplustrader.ui.theme.BackgroundDark
import com.example.novaplustrader.ui.theme.ElectricLime
import com.example.novaplustrader.ui.theme.ElectricLimeSurface
import com.example.novaplustrader.ui.theme.FinancialGreen
import com.example.novaplustrader.ui.theme.FinancialRed
import com.example.novaplustrader.ui.theme.SurfaceBorder
import com.example.novaplustrader.ui.theme.SurfaceDarkCard
import com.example.novaplustrader.ui.theme.SurfaceDarkElevated
import com.example.novaplustrader.ui.theme.TextMuted
import com.example.novaplustrader.ui.theme.TextPrimary
import com.example.novaplustrader.ui.util.Formatters
import com.example.novaplustrader.ui.util.rememberNovaHaptics

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CasinoScreen(
    viewModel: CasinoViewModel,
    onBackClick: () -> Unit,
    onNavigateToHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val haptics = rememberNovaHaptics()
    val isSpinning = uiState.status == CasinoGameStatus.SPINNING

    // Animated Wheel Rotation
    val animatedRotation by animateFloatAsState(
        targetValue = uiState.targetRotationAngle,
        animationSpec = tween(
            durationMillis = if (isSpinning) 2500 else 0,
            easing = FastOutSlowInEasing
        ),
        label = "WheelSpinAnimation"
    )

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

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Casino Spin",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(ElectricLimeSurface)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "DEMO",
                                color = ElectricLime,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Text(
                        text = "Wallet Balance: ${Formatters.formatCurrency(uiState.walletBalanceUsd, Currency.USD)}",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }

                IconActionButton(
                    icon = Icons.Default.Info,
                    contentDescription = "Info",
                    onClick = { viewModel.openResponsibleGamingDialog() }
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 100.dp, start = 16.dp, end = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Hero Section & Spin Wheel
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(SurfaceDarkCard)
                            .border(1.dp, SurfaceBorder, RoundedCornerShape(24.dp))
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "NOVA SPIN DEMO",
                            color = ElectricLime,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Spin to test probability multipliers",
                            color = TextMuted,
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        SpinWheel(
                            rotationAngle = animatedRotation,
                            segments = uiState.segments,
                            isSpinning = isSpinning
                        )
                    }
                }

                // Demo Balance & Stake Selection
                item {
                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "DEMO CREDITS BALANCE", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = "${uiState.demoBalance.toInt()} Demo Credits",
                                color = ElectricLime,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (uiState.demoBalance < 10) {
                            IconActionButton(
                                icon = Icons.Default.Refresh,
                                contentDescription = "Reset Credits",
                                onClick = { viewModel.resetDemoCredits() }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "DEMO STAKE (CREDITS)",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = uiState.selectedStakeString,
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp, start = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Preset Stake Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(10, 25, 50, 100).forEach { preset ->
                            val isSelected = uiState.selectedStakePreset == preset
                            val bg = if (isSelected) ElectricLimeSurface else SurfaceDarkCard
                            val border = if (isSelected) ElectricLime else SurfaceBorder

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(bg)
                                    .border(1.dp, border, RoundedCornerShape(12.dp))
                                    .clickable(enabled = !isSpinning) {
                                        haptics.lightTap()
                                        viewModel.applyStakePreset(preset)
                                    }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = preset.toString(),
                                    color = if (isSelected) ElectricLime else TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Spin Action Button
                item {
                    Spacer(modifier = Modifier.height(20.dp))

                    PrimaryButton(
                        text = if (isSpinning) "SPINNING..." else "SPIN WHEEL",
                        onClick = {
                            haptics.confirmation()
                            viewModel.triggerSpin()
                        },
                        enabled = !isSpinning
                    )
                }

                // Demo Round Result Display
                if ((uiState.status == CasinoGameStatus.RESULT) && (uiState.lastRoundResult != null)) {
                    val result = uiState.lastRoundResult!!
                    val isWin = result.resultCredits > 0

                    item {
                        Spacer(modifier = Modifier.height(20.dp))

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(SurfaceDarkCard)
                                .border(1.dp, SurfaceBorder, RoundedCornerShape(20.dp))
                                .padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "DEMO RESULT",
                                color = TextMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = if (isWin) "MULTIPLIER: ${result.multiplier}x" else "NO WIN (0x)",
                                color = if (isWin) FinancialGreen else FinancialRed,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = "+${result.resultCredits.toInt()} Demo Credits",
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // History Quick Access Button
                item {
                    Spacer(modifier = Modifier.height(20.dp))

                    SecondaryButton(
                        text = "View Demo Game History",
                        onClick = onNavigateToHistory
                    )
                }

                // Responsible Gaming Notice Card
                item {
                    Spacer(modifier = Modifier.height(20.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(SurfaceDarkElevated)
                            .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
                            .padding(14.dp)
                    ) {
                        Text(
                            text = "DEMO MODE NOTICE",
                            color = ElectricLime,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "This feature is operating in Demo Mode using simulated credits. Demo results do not represent or guarantee real-money outcomes, nor do they modify your real wallet balance.",
                            color = TextMuted,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        // Responsible Gaming Information Modal Sheet
        if (uiState.isResponsibleGamingDialogOpen) {
            ConfirmationSheet(
                title = "About Demo Mode & Gaming Policy",
                details = listOf(
                    "Feature Type" to "Demonstration Only",
                    "Currency" to "Demo Credits (Non-Monetary)",
                    "Real Balance Impact" to "None (0.00 USD)",
                    "Payouts" to "No real cash payouts"
                ),
                confirmButtonText = "I Understand",
                onConfirm = { viewModel.closeResponsibleGamingDialog() },
                onDismiss = { viewModel.closeResponsibleGamingDialog() }
            )
        }
    }
}
