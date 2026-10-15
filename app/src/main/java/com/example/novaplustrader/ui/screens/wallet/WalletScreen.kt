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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.example.novaplustrader.ui.components.QuickActionButton
import com.example.novaplustrader.ui.components.TransactionList
import com.example.novaplustrader.ui.components.WalletCard
import com.example.novaplustrader.ui.model.Currency
import com.example.novaplustrader.ui.model.WalletUiModel
import com.example.novaplustrader.ui.theme.BackgroundDark
import com.example.novaplustrader.ui.theme.ElectricLime
import com.example.novaplustrader.ui.theme.FinancialRed
import com.example.novaplustrader.ui.theme.SurfaceBorder
import com.example.novaplustrader.ui.theme.SurfaceDarkCard
import com.example.novaplustrader.ui.theme.TextMuted
import com.example.novaplustrader.ui.theme.TextPrimary
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    viewModel: WalletViewModel,
    onNavigateToDeposit: () -> Unit,
    onNavigateToWithdraw: () -> Unit,
    onNavigateToSend: () -> Unit,
    onNavigateToReceive: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    val usdWallet = WalletUiModel(
        id = "w1",
        walletId = uiState.walletId,
        userName = uiState.userName,
        currency = Currency.USD,
        balanceAmount = uiState.usdBalance,
        cardNumberPlaceholder = "USD · TEST FUNDS",
        isPrimary = true
    )

    val kesWallet = WalletUiModel(
        id = "w2",
        walletId = uiState.walletId,
        userName = uiState.userName,
        currency = Currency.KES,
        balanceAmount = uiState.kesBalance,
        cardNumberPlaceholder = "KES · TEST FUNDS",
        isPrimary = false
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
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "My Wallets",
                        color = TextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "User wallet · USD and KES cash balances",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }

                IconActionButton(
                    icon = Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    onClick = onNavigateToNotifications
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 100.dp, start = 16.dp, end = 16.dp)
            ) {
                // USD Wallet Card
                item {
                    WalletCard(wallet = usdWallet)
                    Text(
                        text = "TEST FUNDS / PAPER MONEY · Simulated deposits, withdrawals and trades only. No real money or payouts.",
                        color = TextMuted,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
                    )
                }

                // KES Wallet Card
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    WalletCard(wallet = kesWallet)
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(SurfaceDarkCard)
                            .border(1.dp, SurfaceBorder, RoundedCornerShape(20.dp))
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Bitcoin & crypto portfolio",
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = when (uiState.cryptoWalletMode) {
                                "paper" -> "PAPER TRADING · Not withdrawable and not an on-chain wallet."
                                else -> "Internal Firestore portfolio · starts at zero · not an on-chain wallet."
                            },
                            color = if (uiState.cryptoWalletMode == "paper") ElectricLime else TextMuted,
                            fontSize = 11.sp
                        )
                        Text(
                            text = uiState.bitcoinWalletId
                                ?: uiState.errorMessage
                                ?: "Bitcoin wallet is being initialized",
                            color = ElectricLime,
                            fontSize = 12.sp
                        )
                        CryptoBalanceLine(
                            "Estimated portfolio value",
                            uiState.cryptoPortfolioValueUsd?.let { "$${String.format(Locale.US, "%.2f", it)}" }
                                ?: "Quote unavailable"
                        )
                        CryptoBalanceLine("BTC", "${String.format(Locale.US, "%.8f BTC", uiState.cryptoBtcBalance)} · ${formatCryptoPrice("BTC", uiState)}")
                        CryptoBalanceLine("ETH", "${String.format(Locale.US, "%.8f ETH", uiState.cryptoEthBalance)} · ${formatCryptoPrice("ETH", uiState)}")
                        CryptoBalanceLine("USDT", String.format(Locale.US, "%.2f USDT", uiState.cryptoUsdtBalance))
                        CryptoBalanceLine("USDC", String.format(Locale.US, "%.2f USDC", uiState.cryptoUsdcBalance))
                        CryptoBalanceLine("SOL", "${String.format(Locale.US, "%.6f SOL", uiState.cryptoSolBalance)} · ${formatCryptoPrice("SOL", uiState)}")
                        CryptoBalanceLine("BNB", "${String.format(Locale.US, "%.6f BNB", uiState.cryptoBnbBalance)} · ${formatCryptoPrice("BNB", uiState)}")
                        Text(
                            text = uiState.cryptoMarketError
                                ?: uiState.cryptoMarketUpdatedAt?.let { "Binance public quote · updated $it" }
                                ?: "Waiting for current Binance market quotes.",
                            color = if (uiState.cryptoMarketError == null) TextMuted else FinancialRed,
                            fontSize = 10.sp
                        )
                        Text(
                            text = "Market prices are valuation only. This internal Firestore portfolio is not an exchange account or on-chain wallet.",
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    }
                }

                // Quick Actions Bar
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        QuickActionButton(
                            label = "Deposit",
                            icon = Icons.Default.ArrowDownward,
                            onClick = onNavigateToDeposit
                        )
                        QuickActionButton(
                            label = "Withdraw",
                            icon = Icons.Default.ArrowUpward,
                            onClick = onNavigateToWithdraw
                        )
                        QuickActionButton(
                            label = "Send",
                            icon = Icons.AutoMirrored.Filled.Send,
                            onClick = onNavigateToSend
                        )
                        QuickActionButton(
                            label = "Receive",
                            icon = Icons.Default.ArrowDownward,
                            onClick = onNavigateToReceive
                        )
                    }

                }

                // Recent Transactions Section
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    TransactionList(
                        transactions = uiState.transactions,
                        onViewAllClick = onNavigateToHistory,
                        onTransactionClick = { tx ->
                            viewModel.selectTransactionDetails(tx)
                        }
                    )
                }
            }

        }
    }
}

private fun formatCryptoPrice(asset: String, state: WalletUiState): String =
    state.cryptoPricesUsd[asset]?.let { "$${String.format(Locale.US, "%.2f", it)}" } ?: "price unavailable"

@Composable
private fun CryptoBalanceLine(label: String, balance: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = TextMuted, fontSize = 12.sp)
        Text(text = balance, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}
