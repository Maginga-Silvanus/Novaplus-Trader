package com.example.novaplustrader.ui.screens.wallet

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.novaplustrader.ui.components.CurrencySwitcher
import com.example.novaplustrader.ui.components.IconActionButton
import com.example.novaplustrader.ui.components.PrimaryButton
import com.example.novaplustrader.ui.components.SecondaryButton
import com.example.novaplustrader.ui.components.UserAvatar
import com.example.novaplustrader.ui.theme.BackgroundDark
import com.example.novaplustrader.ui.theme.ElectricLime
import com.example.novaplustrader.ui.theme.ElectricLimeSurface
import com.example.novaplustrader.ui.theme.SurfaceBorder
import com.example.novaplustrader.ui.theme.SurfaceDarkCard
import com.example.novaplustrader.ui.theme.TextMuted
import com.example.novaplustrader.ui.theme.TextPrimary
import com.example.novaplustrader.ui.util.rememberNovaHaptics
import com.google.zxing.WriterException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiveMoneyScreen(
    viewModel: WalletViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val haptics = rememberNovaHaptics()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val payload = uiState.receiveQrPayload
    val qrBitmap: Bitmap? = remember(payload) {
        if (payload.isBlank()) {
            null
        } else {
            try {
                QrCodeGenerator.createBitmap(payload)
            } catch (_: WriterException) {
                null
            }
        }
    }
    val isBitcoinWallet = uiState.receiveWalletType == "BITCOIN_WALLET"
    val receiveIdentifier = if (isBitcoinWallet) uiState.bitcoinWalletId.orEmpty() else uiState.walletId

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
                    text = "Receive Money",
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
                contentPadding = PaddingValues(bottom = 100.dp, start = 20.dp, end = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(SurfaceDarkCard)
                            .border(1.dp, SurfaceBorder, RoundedCornerShape(24.dp))
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        UserAvatar(
                            initials = uiState.userInitials,
                            size = 56.dp,
                            hasOnlineBadge = false
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = uiState.userName,
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isBitcoinWallet) "Bitcoin wallet ID" else "Wallet ID",
                            color = ElectricLime,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = receiveIdentifier.ifBlank {
                                if (isBitcoinWallet && uiState.walletId.isNotBlank()) {
                                    "Creating your Bitcoin wallet…"
                                } else if (isBitcoinWallet) {
                                    "Sign in to create your Bitcoin wallet"
                                } else {
                                    "Sign in to view your wallet"
                                }
                            },
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                        if (isBitcoinWallet) {
                            Text(
                                text = "NovaPlus internal wallet ID; this QR is not an on-chain Bitcoin address.",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                        if (qrBitmap != null) {
                            Image(
                                bitmap = qrBitmap.asImageBitmap(),
                                contentDescription = "QR code for ${uiState.userName}'s receive wallet",
                                modifier = Modifier
                                    .size(224.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(androidx.compose.ui.graphics.Color.White)
                                    .padding(10.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (isBitcoinWallet) {
                                    "Scan to identify this NovaPlus Bitcoin wallet"
                                } else {
                                    "Scan to send money to ${uiState.userName}"
                                },
                                color = TextMuted,
                                fontSize = 12.sp
                            )
                        } else {
                            Text(
                                text = if (isBitcoinWallet) {
                                    uiState.errorMessage
                                        ?: "Your account's Bitcoin wallet ID is still being created. Please try again shortly."
                                } else if (uiState.walletId.isBlank()) {
                                    "Sign in to display your personal receive QR code."
                                } else {
                                    "Unable to create this QR code. Please try again."
                                },
                                color = TextMuted,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "REQUEST SPECIFIC AMOUNT (OPTIONAL)",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = uiState.requestedReceiveAmountString,
                        onValueChange = { viewModel.updateRequestedReceiveAmount(it) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        prefix = {
                            Text(
                                text = if (isBitcoinWallet) "BTC " else "${uiState.selectedCurrency.symbol} ",
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
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        enabled = !isBitcoinWallet || uiState.bitcoinWalletId != null
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SecondaryButton(
                            text = "Copy Wallet",
                            onClick = {
                                if (receiveIdentifier.isNotBlank()) {
                                    val clipboard = context.getSystemService(ClipboardManager::class.java)
                                    clipboard.setPrimaryClip(
                                        ClipData.newPlainText(
                                            if (isBitcoinWallet) "Bitcoin wallet ID" else "Wallet ID",
                                            receiveIdentifier
                                        )
                                    )
                                    haptics.confirmation()
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar(
                                            if (isBitcoinWallet) "Bitcoin wallet ID copied" else "Wallet ID copied"
                                        )
                                    }
                                } else {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("No wallet ID is available to copy")
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                        PrimaryButton(
                            text = "Share QR",
                            onClick = {
                                val bitmap = qrBitmap
                                if (bitmap == null) {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("A receive QR code is not available")
                                    }
                                } else {
                                    haptics.lightTap()
                                    coroutineScope.launch {
                                        try {
                                            val imageUri = withContext(Dispatchers.IO) {
                                                val shareDirectory = File(context.cacheDir, "qr-share")
                                                if (!shareDirectory.exists() && !shareDirectory.mkdirs()) {
                                                    throw IOException("Unable to create QR share cache")
                                                }
                                                val imageFile = File(
                                                    shareDirectory,
                                                    "receive-${System.currentTimeMillis()}.png"
                                                )
                                                FileOutputStream(imageFile).use { output ->
                                                    if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)) {
                                                        throw IOException("Unable to encode receive QR image")
                                                    }
                                                }
                                                FileProvider.getUriForFile(
                                                    context,
                                                    "${context.packageName}.fileprovider",
                                                    imageFile
                                                )
                                            }
                                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                type = "image/png"
                                                putExtra(Intent.EXTRA_STREAM, imageUri)
                                                putExtra(
                                                    Intent.EXTRA_TEXT,
                                                    if (isBitcoinWallet) {
                                                        "NovaPlus Bitcoin wallet ID for ${uiState.userName}: $payload"
                                                    } else {
                                                        "Receive to ${uiState.userName}'s wallet: $payload"
                                                    }
                                                )
                                                clipData = ClipData.newUri(
                                                    context.contentResolver,
                                                    "Receive QR code",
                                                    imageUri
                                                )
                                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                            }
                                            context.startActivity(
                                                Intent.createChooser(shareIntent, "Share receive QR code")
                                            )
                                        } catch (error: Exception) {
                                            snackbarHostState.showSnackbar(
                                                error.localizedMessage ?: "Unable to share the QR code"
                                            )
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
