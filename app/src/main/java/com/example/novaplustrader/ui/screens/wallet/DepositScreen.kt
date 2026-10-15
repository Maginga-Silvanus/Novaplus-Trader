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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.example.novaplustrader.ui.theme.ElectricLimeSurface
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
                    text = "Add TEST Funds",
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
                        text = "TEST FUNDS ONLY",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        listOf(DepositMethod.MPESA).forEach { method ->
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

                // Dynamic Method-Specific Inputs
                item {
                    when (uiState.selectedDepositMethod) {
                        DepositMethod.MPESA -> {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "TEST WALLET CREDIT · No card, phone, bank or crypto payment will be requested or processed.",
                                color = ElectricLime,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(vertical = 10.dp)
                            )
                        }

                        DepositMethod.BANK -> {
                            Spacer(modifier = Modifier.height(18.dp))
                            Text(
                                text = "SELECT LOCAL BANK (KENYA)",
                                color = TextMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(SurfaceDarkCard)
                                    .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                val kenyaBanks = listOf("KCB Bank", "DTB Bank", "Equity Bank", "Co-op Bank", "NCBA Bank", "Absa Bank")

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    kenyaBanks.take(3).forEach { bank ->
                                        val isSelected = bank == uiState.selectedBankName
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(if (isSelected) ElectricLimeSurface else BackgroundDark)
                                                .border(1.dp, if (isSelected) ElectricLime else SurfaceBorder, RoundedCornerShape(10.dp))
                                                .clickable { viewModel.selectBankName(bank) }
                                                .padding(vertical = 10.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(text = bank, color = if (isSelected) ElectricLime else TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    kenyaBanks.drop(3).forEach { bank ->
                                        val isSelected = bank == uiState.selectedBankName
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(if (isSelected) ElectricLimeSurface else BackgroundDark)
                                                .border(1.dp, if (isSelected) ElectricLime else SurfaceBorder, RoundedCornerShape(10.dp))
                                                .clickable { viewModel.selectBankName(bank) }
                                                .padding(vertical = 10.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(text = bank, color = if (isSelected) ElectricLime else TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = "Bank Account Number", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                OutlinedTextField(
                                    value = uiState.bankAccountNumber,
                                    onValueChange = { viewModel.updateBankAccountNumber(it) },
                                    modifier = Modifier.fillMaxWidth(),
                                    placeholder = { Text("e.g. 1122334455", color = TextMuted) },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = BackgroundDark,
                                        unfocusedContainerColor = BackgroundDark,
                                        focusedBorderColor = ElectricLime,
                                        unfocusedBorderColor = SurfaceBorder,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary
                                    ),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true
                                )
                            }
                        }

                        DepositMethod.CARD -> {
                            Spacer(modifier = Modifier.height(18.dp))
                            Text(
                                text = "CARD DETAILS & SAVING PROCESS",
                                color = TextMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(SurfaceDarkCard)
                                    .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(text = "Cardholder Name", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                OutlinedTextField(
                                    value = uiState.cardHolderName,
                                    onValueChange = { viewModel.updateCardDetails(it, uiState.cardNumber, uiState.cardExpiry, uiState.cardCvv, uiState.isCardSaved) },
                                    modifier = Modifier.fillMaxWidth(),
                                    placeholder = { Text("e.g. ${uiState.userName}", color = TextMuted) },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = BackgroundDark,
                                        unfocusedContainerColor = BackgroundDark,
                                        focusedBorderColor = ElectricLime,
                                        unfocusedBorderColor = SurfaceBorder,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary
                                    ),
                                    singleLine = true
                                )

                                Text(text = "Card Number", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                OutlinedTextField(
                                    value = uiState.cardNumber,
                                    onValueChange = { viewModel.updateCardDetails(uiState.cardHolderName, it, uiState.cardExpiry, uiState.cardCvv, uiState.isCardSaved) },
                                    modifier = Modifier.fillMaxWidth(),
                                    placeholder = { Text("4532 •••• •••• 8842", color = TextMuted) },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = BackgroundDark,
                                        unfocusedContainerColor = BackgroundDark,
                                        focusedBorderColor = ElectricLime,
                                        unfocusedBorderColor = SurfaceBorder,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary
                                    ),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = "Expiry (MM/YY)", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        OutlinedTextField(
                                            value = uiState.cardExpiry,
                                            onValueChange = { viewModel.updateCardDetails(uiState.cardHolderName, uiState.cardNumber, it, uiState.cardCvv, uiState.isCardSaved) },
                                            modifier = Modifier.fillMaxWidth(),
                                            placeholder = { Text("12/28", color = TextMuted) },
                                            shape = RoundedCornerShape(12.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedContainerColor = BackgroundDark,
                                                unfocusedContainerColor = BackgroundDark,
                                                focusedBorderColor = ElectricLime,
                                                unfocusedBorderColor = SurfaceBorder,
                                                focusedTextColor = TextPrimary,
                                                unfocusedTextColor = TextPrimary
                                            ),
                                            singleLine = true
                                        )
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = "CVV", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        OutlinedTextField(
                                            value = uiState.cardCvv,
                                            onValueChange = { viewModel.updateCardDetails(uiState.cardHolderName, uiState.cardNumber, uiState.cardExpiry, it, uiState.isCardSaved) },
                                            modifier = Modifier.fillMaxWidth(),
                                            placeholder = { Text("882", color = TextMuted) },
                                            shape = RoundedCornerShape(12.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedContainerColor = BackgroundDark,
                                                unfocusedContainerColor = BackgroundDark,
                                                focusedBorderColor = ElectricLime,
                                                unfocusedBorderColor = SurfaceBorder,
                                                focusedTextColor = TextPrimary,
                                                unfocusedTextColor = TextPrimary
                                            ),
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                            singleLine = true
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.updateCardDetails(uiState.cardHolderName, uiState.cardNumber, uiState.cardExpiry, uiState.cardCvv, !uiState.isCardSaved) }
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Switch(
                                        checked = uiState.isCardSaved,
                                        onCheckedChange = { viewModel.updateCardDetails(uiState.cardHolderName, uiState.cardNumber, uiState.cardExpiry, uiState.cardCvv, it) },
                                        colors = SwitchDefaults.colors(checkedThumbColor = ElectricLime, checkedTrackColor = ElectricLimeSurface)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(text = "Save card securely before deposit", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                        }

                        DepositMethod.CRYPTO -> {
                            Spacer(modifier = Modifier.height(18.dp))
                            Text(
                                text = "SELECT CRYPTOCURRENCY ASSET",
                                color = TextMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(SurfaceDarkCard)
                                    .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                val cryptoAssets = listOf("BTC", "ETH", "USDT", "USDC")
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    cryptoAssets.forEach { asset ->
                                        val isSelected = asset == uiState.selectedCryptoAsset
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(if (isSelected) ElectricLimeSurface else BackgroundDark)
                                                .border(1.dp, if (isSelected) ElectricLime else SurfaceBorder, RoundedCornerShape(10.dp))
                                                .clickable { viewModel.selectCryptoAsset(asset) }
                                                .padding(vertical = 10.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(text = asset, color = if (isSelected) ElectricLime else TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                val currentCryptoBal = when (uiState.selectedCryptoAsset) {
                                    "BTC" -> "${String.format("%.4f", uiState.cryptoBtcBalance)} BTC"
                                    "ETH" -> "${String.format("%.2f", uiState.cryptoEthBalance)} ETH"
                                    "USDT" -> "${String.format("%.2f", uiState.cryptoUsdtBalance)} USDT"
                                    "USDC" -> "${String.format("%.2f", uiState.cryptoUsdcBalance)} USDC"
                                    else -> "${String.format("%.2f", uiState.cryptoUsdtBalance)} USDT"
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "Current Vault Balance:", color = TextMuted, fontSize = 12.sp)
                                    Text(text = currentCryptoBal, color = ElectricLime, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }

                                Text(
                                    text = "External ${uiState.selectedCryptoAsset} deposits are not enabled. Buy crypto using your internal USD balance on the Spot Trade screen.",
                                    color = TextMuted,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }

                // Deposit Amount Input
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "TEST FUNDS AMOUNT",
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
                        placeholder = { Text("0.00", color = TextMuted) },
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
                    uiState.testWalletNotice?.let { notice ->
                        Text(
                            text = notice,
                            color = ElectricLime,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 8.dp)
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
                            Text(text = "TEST funding fee", color = TextMuted, fontSize = 12.sp)
                            Text(text = Formatters.formatCurrency(feeAmt, uiState.selectedCurrency), color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "TEST balance credit", color = TextMuted, fontSize = 12.sp)
                            Text(text = Formatters.formatCurrency(expectedAmt, uiState.selectedCurrency), color = ElectricLime, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    PrimaryButton(
                        text = "ADD TEST FUNDS",
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
                title = "Confirm TEST Wallet Credit",
                details = listOf(
                    "Mode" to "PAPER MONEY · No payment is processed",
                    "TEST Amount" to Formatters.formatCurrency(depositAmt, uiState.selectedCurrency),
                    "TEST fee" to Formatters.formatCurrency(feeAmt, uiState.selectedCurrency),
                    "TEST balance credit" to Formatters.formatCurrency(expectedAmt, uiState.selectedCurrency)
                ),
                confirmButtonText = "Record TEST Credit",
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
                        title = "TEST Credit Recorded",
                        subtitle = "Added ${Formatters.formatCurrency(tx.amount, tx.currency)} in PAPER MONEY. No payment was processed.",
                        onDoneClick = { viewModel.clearCompletedTransaction() }
                    )
                }
            }
        }
    }
}
