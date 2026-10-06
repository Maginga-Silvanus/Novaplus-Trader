package com.example.novaplustrader.ui.screens.wallet

import androidx.compose.foundation.background
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import com.example.novaplustrader.ui.components.BalanceCard
import com.example.novaplustrader.ui.components.IconActionButton
import com.example.novaplustrader.ui.components.QuickActionButton
import com.example.novaplustrader.ui.components.TransactionList
import com.example.novaplustrader.ui.components.WalletCard
import com.example.novaplustrader.ui.model.Currency
import com.example.novaplustrader.ui.theme.BackgroundDark
import com.example.novaplustrader.ui.theme.TextMuted
import com.example.novaplustrader.ui.theme.TextPrimary
import kotlin.math.absoluteValue

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

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Wallet",
                color = TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            IconActionButton(
                icon = Icons.Default.Notifications,
                contentDescription = "Notifications",
                badgeCount = 2,
                onClick = onNavigateToNotifications
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // Balance Card Section
            item {
                val currentBal = if (uiState.selectedCurrency == Currency.USD) uiState.usdBalance else uiState.kesBalance
                val currentChange = if (uiState.selectedCurrency == Currency.USD) uiState.todayChangeAmountUsd else uiState.todayChangeAmountKes

                Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
                    BalanceCard(
                        balanceAmount = currentBal,
                        currency = uiState.selectedCurrency,
                        todayChangeAmount = currentChange,
                        todayChangePercentage = uiState.todayChangePercentage,
                        onCurrencySelected = { currency ->
                            viewModel.selectCurrency(currency)
                        }
                    )
                }
            }

            // Wallet Cards Pager Section
            if (uiState.wallets.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "ACCOUNTS & CARDS",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
                    )

                    val pagerState = rememberPagerState { uiState.wallets.size }

                    HorizontalPager(
                        state = pagerState,
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        pageSpacing = 12.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) { page ->
                        val wallet = uiState.wallets[page]
                        val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction).absoluteValue

                        WalletCard(
                            wallet = wallet,
                            modifier = Modifier.graphicsLayer {
                                val scale = lerp(0.95f, 1f, 1f - pageOffset.coerceIn(0f, 1f))
                                scaleX = scale
                                scaleY = scale
                                alpha = lerp(0.7f, 1f, 1f - pageOffset.coerceIn(0f, 1f))
                            }
                        )
                    }
                }
            }

            // Quick Actions Section
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "WALLET ACTIONS",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    QuickActionButton(
                        label = "Deposit",
                        icon = Icons.Default.ArrowDownward,
                        isPrimary = true,
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
                        icon = Icons.Default.ArrowUpward,
                        onClick = onNavigateToReceive
                    )
                }
            }

            // Recent Transactions List
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                    TransactionList(
                        transactions = uiState.transactions,
                        onTransactionClick = { tx ->
                            viewModel.selectTransactionDetails(tx)
                        },
                        onViewAllClick = onNavigateToHistory
                    )
                }
            }
        }
    }
}
