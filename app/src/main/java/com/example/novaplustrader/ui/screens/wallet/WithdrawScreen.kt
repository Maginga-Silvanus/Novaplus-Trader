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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.novaplustrader.ui.theme.ElectricLimeSurface
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
    onMpesaClick: () -> Unit = {},
    onCardClick: () -> Unit = {},
    onAddPaymentMethodClick: () -> Unit = {},
    method: WithdrawMethod? = null,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(method) {
        method?.let(viewModel::selectWithdrawMethod)
    }

    if (method == null) {
        WithdrawMethodPicker(
            onBackClick = onBackClick,
            onMpesaClick = onMpesaClick,
            onCardClick = onCardClick,
            onAddPaymentMethodClick = onAddPaymentMethodClick,
            modifier = modifier
        )
        return
    }

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
                    text = "Withdraw",
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
                // Withdrawal Method Selection
                item {
                    Text(
                        text = "WITHDRAWAL METHOD",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(SurfaceDarkCard)
                            .border(1.dp, ElectricLime, RoundedCornerShape(16.dp))
                            .padding(14.dp)
                    ) {
                        Text(uiState.selectedWithdrawMethod.title, color = ElectricLime, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(uiState.selectedWithdrawMethod.description, color = TextMuted, fontSize = 12.sp)
                    }
                }

                // Dynamic Method-Specific Withdrawal Input
                item {
                    when (uiState.selectedWithdrawMethod) {
                        WithdrawMethod.MPESA -> {
                            // Removed pending info text
                        }

                        WithdrawMethod.BANK -> {
                            Spacer(modifier = Modifier.height(18.dp))
                            Text(
                                text = "SELECT LOCAL BANK & ACCOUNT NUMBER",
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
                                Text(text = "Bank Account Number Withdrawing To", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                OutlinedTextField(
                                    value = uiState.withdrawDestination,
                                    onValueChange = { viewModel.updateWithdrawDestination(it) },
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

                        WithdrawMethod.CARD -> {
                            Spacer(modifier = Modifier.height(18.dp))
                            Text(
                                text = "DEBIT / CREDIT CARD PAYOUT",
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
                                Text(text = "Select Saved Card or Add New", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    uiState.savedCards.forEachIndexed { idx, card ->
                                        val isSelected = idx == uiState.selectedSavedCardIndex && !uiState.isAddNewCardForWithdrawal
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(if (isSelected) ElectricLimeSurface else BackgroundDark)
                                                .border(1.dp, if (isSelected) ElectricLime else SurfaceBorder, RoundedCornerShape(10.dp))
                                                .clickable { viewModel.selectSavedCardForWithdrawal(idx) }
                                                .padding(vertical = 10.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(text = card, color = if (isSelected) ElectricLime else TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    val isNewSelected = uiState.isAddNewCardForWithdrawal
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (isNewSelected) ElectricLimeSurface else BackgroundDark)
                                            .border(1.dp, if (isNewSelected) ElectricLime else SurfaceBorder, RoundedCornerShape(10.dp))
                                            .clickable { viewModel.selectSavedCardForWithdrawal(-1) }
                                            .padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = "+ Add New", color = if (isNewSelected) ElectricLime else TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                if (uiState.isAddNewCardForWithdrawal) {
                                    Spacer(modifier = Modifier.height(4.dp))
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

                                    Text(text = "Card Number Withdrawing To", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                    OutlinedTextField(
                                        value = uiState.cardNumber,
                                        onValueChange = {
                                            viewModel.updateCardDetails(uiState.cardHolderName, it, uiState.cardExpiry, uiState.cardCvv, uiState.isCardSaved)
                                            viewModel.updateWithdrawDestination("Card $it")
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        placeholder = { Text("e.g. 4532 •••• •••• 8842", color = TextMuted) },
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
                                } else {
                                    val currentCard = uiState.savedCards.getOrNull(uiState.selectedSavedCardIndex) ?: "Visa •••• 8842"
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(ElectricLimeSurface)
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = "Card Info",
                                            tint = ElectricLime,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Selected Payout Card: $currentCard",
                                            color = ElectricLime,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }

                        WithdrawMethod.CRYPTO_WALLET -> {
                            Spacer(modifier = Modifier.height(18.dp))
                            Text(
                                text = "EXTERNAL CRYPTO / BITCOIN WALLET",
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

                                Text(text = "External ${uiState.selectedCryptoAsset} Wallet Address", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                OutlinedTextField(
                                    value = uiState.withdrawDestination,
                                    onValueChange = { viewModel.updateWithdrawDestination(it) },
                                    modifier = Modifier.fillMaxWidth(),
                                    placeholder = { Text("e.g. bc1q... or 0x...", color = TextMuted) },
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
                        }
                    }
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
                title = "Confirm withdrawal request",
                details = listOf(
                    "Withdrawal Amount" to Formatters.formatCurrency(withdrawAmt, uiState.selectedCurrency),
                    "Estimated Fee" to Formatters.formatCurrency(feeAmt, uiState.selectedCurrency),
                    "Estimated Total" to Formatters.formatCurrency(totalDeduction, uiState.selectedCurrency),
                    "Status" to "Pending provider confirmation; balance unchanged"
                ),
                confirmButtonText = "Submit request",
                isLoading = uiState.isLoading,
                onConfirm = { viewModel.executeTestWithdrawal() },
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
                        title = "Withdrawal request submitted",
                        subtitle = "Your request is pending provider confirmation. No payout has been sent.",
                        onDoneClick = { viewModel.clearCompletedTransaction() }
                    )
                }
            }
        }
    }
}

@Composable
fun WithdrawMethodPicker(
    onBackClick: () -> Unit,
    onMpesaClick: () -> Unit,
    onCardClick: () -> Unit,
    onAddPaymentMethodClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
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
                    text = "Withdraw",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.size(40.dp))
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Select withdrawal destination:",
                    color = TextMuted,
                    fontSize = 14.sp
                )

                // M-Pesa Option
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceDarkCard)
                        .clickable(onClick = onMpesaClick)
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(ElectricLime.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhoneAndroid,
                            contentDescription = null,
                            tint = ElectricLime
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "M-Pesa Mobile Money",
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Instant transfer to your Safaricom wallet",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }
                }

                // Card Option
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceDarkCard)
                        .clickable(onClick = onCardClick)
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(ElectricLime.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CreditCard,
                            contentDescription = null,
                            tint = ElectricLime
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Credit / Debit Card",
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Withdraw to Visa or Mastercard",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                PrimaryButton(
                    text = "ADD NEW PAYMENT METHOD",
                    onClick = onAddPaymentMethodClick
                )
            }
        }
    }
}
