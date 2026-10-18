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
                val currentBal = uiState.currentAvailableBalance
                val currentChange = if (uiState.selectedCurrency == Currency.USD) uiState.todayChangeAmountUsd else uiState.todayChangeAmountKes

                Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
                    BalanceCard(
                        balanceAmount = currentBal,
                        currency = uiState.selectedCurrency,
                        todayChangeAmount = currentChange,
                        todayChangePercentage = uiState.todayChangePercentage,
                        pendingAmount = uiState.currentPendingBalance,
                        lockedAmount = uiState.currentLockedBalance,
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
                        modifier = Modifier.fillMaxWidth()
                    ) { page ->
                        val wallet = uiState.wallets[page]
                        val pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction

                        WalletCard(
                            wallet = wallet,
                            modifier = Modifier.graphicsLayer {
                                alpha = lerp(
                                    start = 0.6f,
                                    stop = 1f,
                                    fraction = 1f - pageOffset.absoluteValue.coerceIn(0f, 1f)
                                )
                                scaleY = lerp(
                                    start = 0.92f,
                                    stop = 1f,
                                    fraction = 1f - pageOffset.absoluteValue.coerceIn(0f, 1f)
                                )
                            }
                        )
                    }
                }
            }

            // Quick Actions Bar
            item {
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
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
                Box(modifier = Modifier.padding(horizontal = 20.dp)) {
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
