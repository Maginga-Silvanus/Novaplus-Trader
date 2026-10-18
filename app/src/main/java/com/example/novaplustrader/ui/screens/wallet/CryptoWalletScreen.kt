package com.example.novaplustrader.ui.screens.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.example.novaplustrader.ui.components.IconActionButton
import com.example.novaplustrader.ui.components.PrimaryButton
import com.example.novaplustrader.ui.theme.BackgroundDark
import com.example.novaplustrader.ui.theme.ElectricLime
import com.example.novaplustrader.ui.theme.ElectricLimeSurface
import com.example.novaplustrader.ui.theme.FinancialGreen
import com.example.novaplustrader.ui.theme.SurfaceBorder
import com.example.novaplustrader.ui.theme.SurfaceDarkCard
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

                        PrimaryButton(
                            text = "OPEN FOREX & SPOT TRADING HUB",
                            onClick = onNavigateToTrade
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))
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
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(SurfaceDarkCard)
                                .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(asset, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Text("Vault Asset Holdings", color = TextMuted, fontSize = 11.sp)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(String.format(Locale.US, "%.8f", amount), color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                Text("Verified", color = FinancialGreen, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
