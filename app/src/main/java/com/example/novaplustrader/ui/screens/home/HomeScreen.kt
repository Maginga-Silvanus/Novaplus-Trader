package com.example.novaplustrader.ui.screens.home

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.novaplustrader.ui.components.BalanceCard
import com.example.novaplustrader.ui.components.ErrorState
import com.example.novaplustrader.ui.components.LoadingState
import com.example.novaplustrader.ui.components.PremiumTopBar
import com.example.novaplustrader.ui.components.QRActionCard
import com.example.novaplustrader.ui.components.QuickActionButton
import com.example.novaplustrader.ui.components.TransactionList
import com.example.novaplustrader.ui.model.Currency
import com.example.novaplustrader.ui.theme.BackgroundDark
import com.example.novaplustrader.ui.theme.TextMuted

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToNotifications: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToDeposit: () -> Unit,
    onNavigateToWithdraw: () -> Unit,
    onNavigateToSend: () -> Unit,
    onNavigateToReceive: () -> Unit,
    onNavigateToScanQR: () -> Unit,
    onNavigateToActivity: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        PremiumTopBar(
            greeting = uiState.greeting,
            userName = uiState.userName,
            userInitials = uiState.userInitials,
            unreadNotificationsCount = uiState.unreadNotificationsCount,
            onNotificationClick = onNavigateToNotifications,
            onProfileClick = onNavigateToProfile
        )

        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                LoadingState()
            }
        } else if (uiState.errorMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                ErrorState(
                    message = uiState.errorMessage ?: "Failed to load dashboard data.",
                    onRetry = { viewModel.loadHomeData() }
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                // Total Balance Card
                item {
                    val currentBalance = if (uiState.selectedCurrency == Currency.USD) {
                        uiState.totalBalanceUsd
                    } else {
                        uiState.totalBalanceKes
                    }
                    val currentTodayChange = if (uiState.selectedCurrency == Currency.USD) {
                        uiState.todayChangeAmountUsd
                    } else {
                        uiState.todayChangeAmountKes
                    }

                    Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                        BalanceCard(
                            balanceAmount = currentBalance,
                            currency = uiState.selectedCurrency,
                            todayChangeAmount = currentTodayChange,
                            todayChangePercentage = uiState.todayChangePercentage,
                            onCurrencySelected = { currency ->
                                viewModel.selectCurrency(currency)
                            }
                        )
                    }
                }

                // Primary Quick Actions
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "QUICK ACTIONS",
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

                // QR Action Card
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) {
                        QRActionCard(
                            onScanClick = onNavigateToScanQR
                        )
                    }
                }

                // Recent Transactions List
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                        TransactionList(
                            transactions = uiState.recentTransactions,
                            onTransactionClick = { /* Handled in Activity screen detail */ },
                            onViewAllClick = onNavigateToActivity
                        )
                    }
                }
            }
        }
    }
}
