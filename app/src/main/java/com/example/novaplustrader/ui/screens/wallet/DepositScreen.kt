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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.novaplustrader.ui.components.CurrencySwitcher
import com.example.novaplustrader.ui.components.IconActionButton
import com.example.novaplustrader.ui.components.PrimaryButton
import com.example.novaplustrader.ui.components.SuccessAnimation
import com.example.novaplustrader.ui.screens.wallet.models.DepositMethod
import com.example.novaplustrader.ui.theme.BackgroundDark
import com.example.novaplustrader.ui.theme.ElectricLime
import com.example.novaplustrader.ui.theme.FinancialGreen
import com.example.novaplustrader.ui.theme.FinancialRed
import com.example.novaplustrader.ui.theme.OverlayDark
import com.example.novaplustrader.ui.theme.SurfaceBorder
import com.example.novaplustrader.ui.theme.SurfaceDarkCard
import com.example.novaplustrader.ui.theme.TextMuted
import com.example.novaplustrader.ui.theme.TextPrimary
import com.example.novaplustrader.ui.util.Formatters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DepositScreen(
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
                    text = "Deposit Funds",
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
                // Deposit Method Selection
                item {
                    Text(
                        text = "SELECT DEPOSIT METHOD",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        DepositMethod.entries.forEach { method ->
                            val isSelected = method == uiState.selectedDepositMethod
                            val borderColor = if (isSelected) ElectricLime else SurfaceBorder

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(SurfaceDarkCard)
                                    .border(1.dp, borderColor, RoundedCornerShape(16.dp))
                                    .clickable { viewModel.selectDepositMethod(method) }
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

                // Deposit Amount Input
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "DEPOSIT AMOUNT",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = uiState.depositAmountString,
                        onValueChange = { viewModel.updateDepositAmount(it) },
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
                }

                // Summary & Submit
                item {
                    Spacer(modifier = Modifier.height(24.dp))

                    val depositAmt = uiState.depositAmountString.toDoubleOrNull() ?: 0.0
                    val feeAmt = depositAmt * (uiState.depositFee / 100.0)
                    val expectedAmt = depositAmt - feeAmt

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
                            Text(text = "Processing Fee (${uiState.selectedDepositMethod.feeText})", color = TextMuted, fontSize = 12.sp)
                            Text(text = Formatters.formatCurrency(feeAmt, uiState.selectedCurrency), color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Expected Received Amount", color = TextMuted, fontSize = 12.sp)
                            Text(text = Formatters.formatCurrency(expectedAmt, uiState.selectedCurrency), color = ElectricLime, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    PrimaryButton(
                        text = "CONTINUE DEPOSIT",
                        onClick = { viewModel.openDepositConfirmSheet() },
                        isLoading = uiState.isLoading
                    )
                }
            }
        }

        // Deposit Confirmation Sheet
        if (uiState.isDepositConfirmOpen) {
            val depositAmt = uiState.depositAmountString.toDoubleOrNull() ?: 0.0
            val feeAmt = depositAmt * (uiState.depositFee / 100.0)
            val expectedAmt = depositAmt - feeAmt

            ConfirmationSheet(
                title = "Confirm Deposit Request",
                details = listOf(
                    "Deposit Method" to uiState.selectedDepositMethod.title,
                    "Deposit Amount" to Formatters.formatCurrency(depositAmt, uiState.selectedCurrency),
                    "Estimated Fee" to Formatters.formatCurrency(feeAmt, uiState.selectedCurrency),
                    "Credited Balance" to Formatters.formatCurrency(expectedAmt, uiState.selectedCurrency)
                ),
                confirmButtonText = "Initiate Deposit",
                isLoading = uiState.isLoading,
                onConfirm = { viewModel.executeDeposit() },
                onDismiss = { viewModel.closeDepositConfirmSheet() }
            )
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
                        title = "Deposit Successful!",
                        subtitle = "Credited ${Formatters.formatCurrency(tx.amount, tx.currency)} via ${tx.title}. Ref: ${tx.id}",
                        onDoneClick = { viewModel.clearCompletedTransaction() }
                    )
                }
            }
        }
    }
}
