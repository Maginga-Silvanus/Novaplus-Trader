package com.example.novaplustrader.ui.screens.wallet

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.novaplustrader.ui.components.QRActionCard
import com.example.novaplustrader.ui.components.SuccessAnimation
import com.example.novaplustrader.ui.model.Currency
import com.example.novaplustrader.ui.model.TransactionStatus
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
fun SendMoneyScreen(
    viewModel: WalletViewModel,
    onBackClick: () -> Unit,
    onScanQRClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    val notifPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

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
                    text = "Send Money",
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
                item {
                    QRActionCard(
                        title = "Scan QR to Pay",
                        subtitle = "Scan recipient's NovaPay or Bitcoin QR code",
                        onScanClick = onScanQRClick
                    )
                }

                // Send Lookup Method Choice (Selectable Dropdown)
                item {
                    Spacer(modifier = Modifier.height(18.dp))
                    Text(
                        text = "FIND RECIPIENT BY",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val lookupMethods = mapOf(
                        "EMAIL" to "Email Address",
                        "WALLET_CODE" to "Wallet / BTC Code",
                        "PHONE_KYC" to "KYC Phone No."
                    )
                    var isLookupExpanded by remember { mutableStateOf(false) }
                    val currentLookupLabel = lookupMethods[uiState.sendLookupMethod] ?: "Email Address"

                    Box(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(SurfaceDarkCard)
                                .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
                                .clickable { isLookupExpanded = true }
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = currentLookupLabel, color = ElectricLime, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = "Select", tint = ElectricLime)
                        }

                        DropdownMenu(
                            expanded = isLookupExpanded,
                            onDismissRequest = { isLookupExpanded = false },
                            modifier = Modifier.background(SurfaceDarkCard)
                        ) {
                            lookupMethods.forEach { (code, label) ->
                                DropdownMenuItem(
                                    text = { Text(label, color = if (code == uiState.sendLookupMethod) ElectricLime else TextPrimary, fontWeight = if (code == uiState.sendLookupMethod) FontWeight.Bold else FontWeight.Normal) },
                                    onClick = {
                                        viewModel.selectSendLookupMethod(code)
                                        isLookupExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Recipient Wallet Target Choice (Selectable Dropdown)
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "TARGET RECIPIENT WALLET",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val targetWallets = mapOf(
                        "USER_WALLET" to "User Main Wallet",
                        "BITCOIN_WALLET" to "Crypto / BTC Wallet"
                    )
                    var isTargetExpanded by remember { mutableStateOf(false) }
                    val currentTargetLabel = targetWallets[uiState.recipientWalletType] ?: "User Main Wallet"

                    Box(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(SurfaceDarkCard)
                                .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
                                .clickable { isTargetExpanded = true }
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = currentTargetLabel, color = ElectricLime, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = "Select", tint = ElectricLime)
                        }

                        DropdownMenu(
                            expanded = isTargetExpanded,
                            onDismissRequest = { isTargetExpanded = false },
                            modifier = Modifier.background(SurfaceDarkCard)
                        ) {
                            targetWallets.forEach { (code, label) ->
                                DropdownMenuItem(
                                    text = { Text(label, color = if (code == uiState.recipientWalletType) ElectricLime else TextPrimary, fontWeight = if (code == uiState.recipientWalletType) FontWeight.Bold else FontWeight.Normal) },
                                    onClick = {
                                        viewModel.selectRecipientWalletType(code)
                                        isTargetExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Recipient Input
                item {
                    Spacer(modifier = Modifier.height(16.dp))

                    val hintText = when (uiState.sendLookupMethod) {
                        "EMAIL" -> "e.g. maria.wanjiku@novapay.io"
                        "WALLET_CODE" -> "e.g. NV-<wallet code>"
                        else -> "e.g. +254712345678"
                    }

                    val labelText = when (uiState.sendLookupMethod) {
                        "EMAIL" -> "RECIPIENT EMAIL ADDRESS"
                        "WALLET_CODE" -> "RECIPIENT WALLET CODE"
                        else -> "RECIPIENT KYC PHONE NUMBER"
                    }

                    Text(
                        text = labelText,
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = uiState.recipientId,
                        onValueChange = { viewModel.updateRecipientId(it) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        placeholder = { Text(hintText, color = TextMuted) },
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

                    if (uiState.recipientId.isNotBlank() &&
                        (uiState.isCheckingRecipient || uiState.recipientVerificationStatus != null)
                    ) {
                        val verified = uiState.isRecipientVerifiedInDb
                        val verificationColor = when {
                            uiState.isCheckingRecipient -> TextMuted
                            verified -> FinancialGreen
                            else -> FinancialRed
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(start = 4.dp)
                        ) {
                            if (verified) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Verified",
                                    tint = verificationColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(
                                text = when {
                                    uiState.isCheckingRecipient -> "Checking recipient wallet…"
                                    verified -> uiState.recipientVerificationStatus ?: "Recipient wallet verified"
                                    else -> uiState.recipientVerificationStatus ?: "Recipient wallet not found"
                                },
                                color = verificationColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // Send Amount
                item {
                    Spacer(modifier = Modifier.height(20.dp))

                    val currentBal = if (uiState.selectedCurrency == Currency.USD) uiState.usdBalance else uiState.kesBalance

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TRANSFER AMOUNT",
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
                        value = uiState.sendAmountString,
                        onValueChange = { viewModel.updateSendAmount(it) },
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
                }

                // Transfer Note
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "NOTE / REASON (OPTIONAL)",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = uiState.sendNote,
                        onValueChange = { viewModel.updateSendNote(it) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        placeholder = { Text("What is this transfer for?", color = TextMuted) },
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

                // Submit
                item {
                    Spacer(modifier = Modifier.height(24.dp))

                    PrimaryButton(
                        text = "CONFIRM & SEND",
                        onClick = { viewModel.openSendConfirmSheet() },
                        isLoading = uiState.isLoading
                    )
                }
            }
        }

        // Confirmation Sheet
        if (uiState.isSendConfirmOpen) {
            val sendAmt = uiState.sendAmountString.toDoubleOrNull() ?: 0.0

            ConfirmationSheet(
                title = "Approve Transfer Amount",
                details = listOf(
                    "Recipient" to uiState.recipientId,
                    "Target Wallet" to if (uiState.recipientWalletType == "USER_WALLET") "User Main Wallet" else "Crypto / BTC Wallet",
                    "Transfer Amount" to Formatters.formatCurrency(sendAmt, uiState.selectedCurrency),
                    "Transfer Fee" to "Free (0%)",
                    "Note" to if (uiState.sendNote.isBlank()) "None" else uiState.sendNote
                ),
                confirmButtonText = "Approve Amount & Enter PIN",
                isLoading = uiState.isLoading,
                onConfirm = { viewModel.openSendPinVerification() },
                onDismiss = { viewModel.closeSendConfirmSheet() }
            )
        }

        // Security PIN Verification Sheet
        if (uiState.isPinVerificationOpen && uiState.pinActionType == "SEND") {
            ModalBottomSheet(
                onDismissRequest = { viewModel.closePinVerification() },
                containerColor = BackgroundDark,
                scrimColor = OverlayDark
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Security Authorization",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Enter your 4-digit security PIN to confirm transfer",
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
                        title = "Transaction completed successfully!",
                        subtitle = if (tx.status == TransactionStatus.COMPLETED) {
                            "Sent ${Formatters.formatCurrency(tx.amount, tx.currency)} to ${uiState.recipientId}. Ref: ${tx.id}"
                        } else {
                            "Request recorded as pending. No wallet was debited or credited. Ref: ${tx.id}"
                        },
                        onDoneClick = { viewModel.clearCompletedTransaction() }
                    )
                }
            }
        }
    }
}
