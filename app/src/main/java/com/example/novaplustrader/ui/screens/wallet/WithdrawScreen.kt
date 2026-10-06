package com.example.novaplustrader.ui.screens.wallet

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
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
import com.example.novaplustrader.ui.components.CurrencySwitcher
import com.example.novaplustrader.ui.components.IconActionButton
import com.example.novaplustrader.ui.components.NumericKeypad
import com.example.novaplustrader.ui.components.PrimaryButton
import com.example.novaplustrader.ui.components.SuccessAnimation
import com.example.novaplustrader.ui.model.Currency
import com.example.novaplustrader.ui.screens.trading.components.PercentageShortcuts
import com.example.novaplustrader.ui.screens.wallet.models.WithdrawMethod
import com.example.novaplustrader.ui.theme.BackgroundDark
import com.example.novaplustrader.ui.theme.ElectricLime
import com.example.novaplustrader.ui.theme.FinancialGreen
import com.example.novaplustrader.ui.theme.FinancialRed
import com.example.novaplustrader.ui.theme.OverlayDark
import com.example.novaplustrader.ui.theme.SurfaceBorder
import com.example.novaplustrader.ui.theme.SurfaceDarkCard
import com.example.novaplustrader.ui.theme.SurfaceDarkElevated
import com.example.novaplustrader.ui.theme.TextMuted
import com.example.novaplustrader.ui.theme.TextPrimary
import com.example.novaplustrader.ui.util.Formatters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WithdrawScreen(
    viewModel: WalletViewModel,
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
                    text = "Withdraw Funds",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                CurrencySwitcher(
                    selectedCurrency = uiState.selectedCurrency,
                    onCurrencySelected = { viewModel.selectCurrency(it) }
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 100.dp, start = 16.dp, end = 16.dp)
            ) {
                // Withdrawal Method
                item {
                    Text(
                        text = "WITHDRAWAL DESTINATION METHOD",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        WithdrawMethod.entries.forEach { method ->
                            val isSelected = method == uiState.selectedWithdrawMethod
                            val borderColor = if (isSelected) ElectricLime else SurfaceBorder

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(SurfaceDarkCard)
                                    .border(1.dp, borderColor, RoundedCornerShape(16.dp))
                                    .clickable { viewModel.selectWithdrawMethod(method) }
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = method.title,
                                        color = if (isSelected) ElectricLime else TextPrimary,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = method.description,
                                        color = TextMuted,
                                        fontSize = 12.sp
                                    )
                                }

                                Text(
                                    text = method.feeText,
                                    color = FinancialGreen,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }
                }

                // Destination Address / Phone Input
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "DESTINATION ACCOUNT / PHONE",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = uiState.withdrawDestination,
                        onValueChange = { viewModel.updateWithdrawDestination(it) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        placeholder = { Text("Enter destination address or phone", color = TextMuted) },
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

                // Amount & Percentage Shortcuts
                item {
                    Spacer(modifier = Modifier.height(20.dp))

                    val currentBal = if (uiState.selectedCurrency == Currency.USD) uiState.usdBalance else uiState.kesBalance

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "WITHDRAWAL AMOUNT",
                            color = TextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Available: ${Formatters.formatCurrency(currentBal, uiState.selectedCurrency)}",
                            color = ElectricLime,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = uiState.withdrawAmountString,
                        onValueChange = { viewModel.updateWithdrawAmount(it) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        prefix = {
                            Text(
                                text = "${uiState.selectedCurrency.symbol} ",
                                color = ElectricLime,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        },
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

                    PercentageShortcuts(
                        selectedPercentage = uiState.selectedPercentage,
                        onPercentageSelected = { viewModel.applyWithdrawPercentageShortcut(it) }
                    )
                }

                // Submit
                item {
                    Spacer(modifier = Modifier.height(24.dp))

                    PrimaryButton(
                        text = "CONTINUE WITHDRAWAL",
                        onClick = { viewModel.openWithdrawConfirmSheet() },
                        isLoading = uiState.isLoading
                    )
                }
            }
        }

        // Confirmation Sheet
        if (uiState.isWithdrawConfirmOpen) {
            val withdrawAmt = uiState.withdrawAmountString.toDoubleOrNull() ?: 0.0
            val feeAmt = uiState.withdrawFee
            val totalDeduction = withdrawAmt + feeAmt

            ConfirmationSheet(
                title = "Confirm Withdrawal",
                details = listOf(
                    "Method" to uiState.selectedWithdrawMethod.title,
                    "Destination" to uiState.withdrawDestination,
                    "Withdrawal Amount" to Formatters.formatCurrency(withdrawAmt, uiState.selectedCurrency),
                    "Estimated Fee" to Formatters.formatCurrency(feeAmt, uiState.selectedCurrency),
                    "Total Deduction" to Formatters.formatCurrency(totalDeduction, uiState.selectedCurrency)
                ),
                confirmButtonText = "Authorize with Security PIN",
                isLoading = uiState.isLoading,
                onConfirm = { viewModel.openPinVerification() },
                onDismiss = { viewModel.closeWithdrawConfirmSheet() }
            )
        }

        // Security PIN Overlay Verification
        if (uiState.isPinVerificationOpen) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(OverlayDark)
                    .padding(20.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(28.dp))
                        .background(SurfaceDarkElevated)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Security",
                                tint = ElectricLime,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "SECURITY PIN",
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        IconActionButton(
                            icon = Icons.Default.Close,
                            contentDescription = "Close",
                            onClick = { viewModel.closePinVerification() }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Enter your 4-digit PIN to authorize withdrawal",
                        color = TextMuted,
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // PIN Dots
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(4) { index ->
                            val isFilled = index < uiState.securityPin.length
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(if (isFilled) ElectricLime else SurfaceBorder)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    NumericKeypad(
                        onKeyPress = { viewModel.onKeypadPress(it) },
                        onBackspace = { viewModel.onKeypadBackspace() }
                    )
                }
            }
        }

        // Success Dialog
        AnimatedVisibility(
            visible = uiState.lastCompletedTransaction != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            val tx = uiState.lastCompletedTransaction
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(OverlayDark)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                if (tx != null) {
                    SuccessAnimation(
                        title = "Withdrawal Executed!",
                        subtitle = "Sent ${Formatters.formatCurrency(tx.amount, tx.currency)} via ${tx.title}. Ref: ${tx.id}",
                        onDoneClick = { viewModel.clearCompletedTransaction() }
                    )
                }
            }
        }
    }
}
