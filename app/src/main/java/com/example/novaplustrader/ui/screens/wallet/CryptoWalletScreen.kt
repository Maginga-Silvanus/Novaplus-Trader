package com.example.novaplustrader.ui.screens.wallet

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CurrencyBitcoin
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.novaplustrader.data.repository.CryptoSubWallet
import com.example.novaplustrader.ui.components.IconActionButton
import com.example.novaplustrader.ui.components.PrimaryButton
import com.example.novaplustrader.ui.components.SecondaryButton
import com.example.novaplustrader.ui.theme.BackgroundDark
import com.example.novaplustrader.ui.theme.ElectricLime
import com.example.novaplustrader.ui.theme.ElectricLimeSurface
import com.example.novaplustrader.ui.theme.FinancialGreen
import com.example.novaplustrader.ui.theme.OverlayDark
import com.example.novaplustrader.ui.theme.SurfaceBorder
import com.example.novaplustrader.ui.theme.SurfaceDarkCard
import com.example.novaplustrader.ui.theme.SurfaceDarkElevated
import com.example.novaplustrader.ui.theme.TextMuted
import com.example.novaplustrader.ui.theme.TextPrimary
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CryptoWalletScreen(
    viewModel: CryptoWalletViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onBackClick: () -> Unit,
    onNavigateToTrade: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val snapshot = state.snapshot

    var isReceiveSheetOpen by remember { mutableStateOf(false) }
    var selectedReceiveWalletIndex by remember { mutableStateOf(0) }

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
                    text = "Crypto & Bitcoin Wallet",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.size(40.dp))
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 100.dp, start = 16.dp, end = 16.dp)
            ) {
                item {
                    // Crypto Vault Banner
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(SurfaceDarkCard)
                            .border(1.dp, SurfaceBorder, RoundedCornerShape(20.dp))
                            .padding(20.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(ElectricLimeSurface),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CurrencyBitcoin,
                                    contentDescription = "BTC",
                                    tint = ElectricLime,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Dedicated Crypto Vault", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Text("Separate from Fiat USD/KES Main Wallet", color = TextMuted, fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Internal crypto wallet ID: ${snapshot?.bitcoinWalletId ?: if (state.isLoading) "Loading…" else "Unavailable"}",
                            color = ElectricLime,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "This is a NovaPlus identifier, not an on-chain deposit address.",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Mode: ${snapshot?.mode?.uppercase(Locale.US) ?: "PAPER"} · Fee: ${snapshot?.paperTradeFeeBps ?: 10} bps",
                            color = TextMuted,
                            fontSize = 11.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                PrimaryButton(
                                    text = "RECEIVE",
                                    onClick = { isReceiveSheetOpen = true }
                                )
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                SecondaryButton(
                                    text = "TRADE HUB",
                                    onClick = onNavigateToTrade
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "STORED VAULT SUB-WALLETS",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                val subWallets = snapshot?.subWallets ?: emptyList()
                if (subWallets.isNotEmpty()) {
                    items(subWallets, key = { it.id }) { wallet ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(SurfaceDarkCard)
                                .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(wallet.name, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Text(wallet.assetSymbol, color = ElectricLime, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Text("Database ID: ${wallet.id}", color = TextMuted, fontSize = 12.sp)
                            Text("Purpose: ${wallet.purpose}", color = TextMuted, fontSize = 11.sp)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Stored Balance:", color = TextMuted, fontSize = 12.sp)
                                Text("${wallet.balance} ${wallet.assetSymbol.split(" ").first()}", color = FinancialGreen, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                            SecondaryButton(
                                text = "Exchange",
                                onClick = onNavigateToTrade
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "CRYPTO ASSET HOLDINGS",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                val balances = snapshot?.assetBalances?.entries?.toList() ?: emptyList()
                if (balances.isEmpty()) {
                    item {
                        Text(
                            text = if (state.isLoading) "Loading crypto portfolio holdings…" else "No crypto holdings found.",
                            color = TextMuted,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(vertical = 18.dp)
                        )
                    }
                } else {
                    items(balances, key = { entry -> entry.key }) { entry ->
                        val asset = entry.key
                        val amount = entry.value
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(SurfaceDarkCard)
                                .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
                                .padding(16.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(asset, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    Text("Paper-trading asset balance", color = TextMuted, fontSize = 11.sp)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(String.format(Locale.US, "%.8f", amount), color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                    Text("TEST ASSET", color = FinancialGreen, fontSize = 10.sp)
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            SecondaryButton(
                                text = "Exchange in Paper Trade Hub",
                                onClick = onNavigateToTrade
                            )
                        }
                    }
                }
            }
        }

        // Receive Crypto Bottom Sheet with QR Code
        if (isReceiveSheetOpen) {
            val subWallets = snapshot?.subWallets ?: emptyList()
            val currentWallet: CryptoSubWallet? = subWallets.getOrNull(selectedReceiveWalletIndex) ?: subWallets.firstOrNull()

            ModalBottomSheet(
                onDismissRequest = { isReceiveSheetOpen = false },
                containerColor = BackgroundDark,
                scrimColor = OverlayDark
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Receive Crypto & Vault Funds",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // Wallet Tab Selector
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceDarkCard)
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        subWallets.forEachIndexed { idx, w ->
                            val isSelected = idx == selectedReceiveWalletIndex
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) ElectricLimeSurface else SurfaceDarkCard)
                                    .clickable { selectedReceiveWalletIndex = idx }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = w.name.split(" ").first(),
                                    color = if (isSelected) ElectricLime else TextMuted,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }

                    if (currentWallet != null) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(SurfaceDarkElevated)
                                .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("Target Vault: ${currentWallet.name}", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("Database ID: ${currentWallet.id}", color = ElectricLime, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Text("Purpose: ${currentWallet.purpose}", color = TextMuted, fontSize = 12.sp)
                            Text("Internal wallet code: ${currentWallet.receiveAddress}", color = TextMuted, fontSize = 11.sp)
                            Text(
                                "This is a NovaPlus wallet ID, not an on-chain address. External crypto deposits are not enabled.",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }

                        // Generate QR Code bitmap
                        val payload = "novaplus://crypto/receive?walletId=${currentWallet.id}&asset=${currentWallet.assetSymbol}"
                        val qrBitmap = remember(payload) {
                            runCatching { QrCodeGenerator.createBitmap(payload, 500) }.getOrNull()
                        }

                        if (qrBitmap != null) {
                            Box(
                                modifier = Modifier
                                    .size(220.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(SurfaceDarkCard)
                                    .padding(12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    bitmap = qrBitmap.asImageBitmap(),
                                    contentDescription = "Receive Crypto QR Code",
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            Text(
                                text = "QR code for this internal NovaPlus wallet identifier.",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        } else {
                            Text("Unable to generate this wallet's QR code.", color = TextMuted, fontSize = 12.sp)
                        }
                    }

                    SecondaryButton(
                        text = "Close",
                        onClick = { isReceiveSheetOpen = false }
                    )
                }
            }
        }
    }
}
