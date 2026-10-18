package com.example.novaplustrader.ui.screens.admin

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.novaplustrader.ui.components.LoadingState
import com.example.novaplustrader.ui.screens.admin.components.AdminHeaderBar
import com.example.novaplustrader.ui.screens.admin.components.AdminStatCard
import com.example.novaplustrader.ui.screens.admin.models.AdminRole
import com.example.novaplustrader.ui.theme.BackgroundDark
import com.example.novaplustrader.ui.theme.ElectricLime
import com.example.novaplustrader.ui.theme.ElectricLimeSurface
import com.example.novaplustrader.ui.theme.FinancialGreen
import com.example.novaplustrader.ui.theme.FinancialRed
import com.example.novaplustrader.ui.theme.SurfaceBorder
import com.example.novaplustrader.ui.theme.SurfaceDarkCard
import com.example.novaplustrader.ui.theme.TextMuted
import com.example.novaplustrader.ui.theme.TextPrimary

@Composable
fun AdminDashboardScreen(
    viewModel: AdminDashboardViewModel,
    onNavigateToUsers: () -> Unit,
    onNavigateToWallets: () -> Unit,
    onNavigateToTransactions: () -> Unit,
    onNavigateToTrading: () -> Unit,
    onNavigateToBots: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToSupport: () -> Unit,
    onNavigateToAuditLogs: () -> Unit,
    onNavigateToSystemStatus: () -> Unit,
    onSignOutClick: () -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToCompliance: () -> Unit = {},
    onNavigateToFeatureAccess: () -> Unit = {},
    onNavigateToMonitoringDashboard: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val m = uiState.metrics

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        AdminHeaderBar(
            title = "Operations Center",
            role = uiState.currentRole,
            onSignOutClick = onSignOutClick
        )

        if (uiState.currentRole == AdminRole.SUPPORT ||
            uiState.currentRole == AdminRole.OPERATIONS ||
            uiState.currentRole == AdminRole.COMPLIANCE
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "AUTHORIZED WORKSPACE",
                        color = TextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
                when (uiState.currentRole) {
                    AdminRole.SUPPORT -> item {
                        AdminNavTile(
                            "Support Desk",
                            Icons.Default.SupportAgent,
                            onNavigateToSupport,
                            Modifier.fillMaxWidth()
                        )
                    }
                    AdminRole.OPERATIONS -> {
                        item {
                            AdminNavTile(
                                "Wallets & Ledger",
                                Icons.Default.AccountBalanceWallet,
                                onNavigateToWallets,
                                Modifier.fillMaxWidth()
                            )
                        }
                        item {
                            AdminNavTile(
                                "Transactions",
                                Icons.Default.Receipt,
                                onNavigateToTransactions,
                                Modifier.fillMaxWidth()
                            )
                        }
                        item {
                            AdminNavTile(
                                "Trading Desk",
                                Icons.Default.Analytics,
                                onNavigateToTrading,
                                Modifier.fillMaxWidth()
                            )
                        }
                        item {
                            AdminNavTile(
                                "Trading Bots",
                                Icons.Default.SmartToy,
                                onNavigateToBots,
                                Modifier.fillMaxWidth()
                            )
                        }
                    }
                    AdminRole.COMPLIANCE -> item {
                        AdminNavTile(
                            "KYC & Compliance",
                            Icons.Default.Badge,
                            onNavigateToCompliance,
                            Modifier.fillMaxWidth()
                        )
                    }
                    else -> Unit
                }
            }
        } else if (uiState.isLoading && m == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                LoadingState()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // System Summary Banner
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(SurfaceDarkCard)
                            .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
                            .clickable { onNavigateToSystemStatus() }
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(ElectricLimeSurface),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudDone,
                                    contentDescription = "System Status",
                                    tint = ElectricLime,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "System Health Summary",
                                    color = TextMuted,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = m?.systemStatusSummary ?: "Unavailable",
                                    color = if (m != null) FinancialGreen else FinancialRed,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "View System",
                            tint = TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Section: Quick Control Center Navigation
                item {
                    Text(
                        text = "OPERATIONAL SECTIONS",
                        color = TextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AdminNavTile(
                                title = "KYC & Compliance",
                                icon = Icons.Default.Badge,
                                onClick = onNavigateToCompliance,
                                modifier = Modifier.weight(1f)
                            )
                            AdminNavTile(
                                title = "Feature Gates",
                                icon = Icons.Default.Lock,
                                onClick = onNavigateToFeatureAccess,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AdminNavTile(
                                title = "User Directory",
                                icon = Icons.Default.Group,
                                onClick = onNavigateToUsers,
                                modifier = Modifier.weight(1f)
                            )
                            AdminNavTile(
                                title = "Wallets & Ledger",
                                icon = Icons.Default.AccountBalanceWallet,
                                onClick = onNavigateToWallets,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AdminNavTile(
                                title = "Transactions",
                                icon = Icons.Default.Receipt,
                                onClick = onNavigateToTransactions,
                                modifier = Modifier.weight(1f)
                            )
                            AdminNavTile(
                                title = "Trading Desk",
                                icon = Icons.Default.Analytics,
                                onClick = onNavigateToTrading,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AdminNavTile(
                                title = "Trading Bots",
                                icon = Icons.Default.SmartToy,
                                onClick = onNavigateToBots,
                                modifier = Modifier.weight(1f)
                            )
                            AdminNavTile(
                                title = "Notifications",
                                icon = Icons.Default.NotificationsActive,
                                onClick = onNavigateToNotifications,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AdminNavTile(
                                title = "Support Desk",
                                icon = Icons.Default.SupportAgent,
                                onClick = onNavigateToSupport,
                                modifier = Modifier.weight(1f)
                            )
                            AdminNavTile(
                                title = "Audit Logs",
                                icon = Icons.Default.History,
                                onClick = onNavigateToAuditLogs,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AdminNavTile(
                                title = "Prod Monitoring",
                                icon = Icons.Default.Analytics,
                                onClick = onNavigateToMonitoringDashboard,
                                modifier = Modifier.weight(1f)
                            )
                            AdminNavTile(
                                title = "Subsystem Health",
                                icon = Icons.Default.CloudDone,
                                onClick = onNavigateToSystemStatus,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Section: Users Metrics
                item {
                    Text(
                        text = "USER METRICS",
                        color = TextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AdminStatCard(
                            title = "Total Users",
                            value = m?.totalUsers?.toString(),
                            icon = Icons.Default.Group,
                            subValue = m?.let { "+${it.newUsersToday} today" },
                            onClick = onNavigateToUsers,
                            modifier = Modifier.weight(1f)
                        )
                        AdminStatCard(
                            title = "Suspended Users",
                            value = m?.suspendedUsers?.toString(),
                            icon = Icons.Default.Group,
                            isWarning = (m?.suspendedUsers ?: 0) > 0,
                            onClick = onNavigateToUsers,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Section: Wallet Metrics
                item {
                    Text(
                        text = "WALLET & TRANSACTION METRICS",
                        color = TextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AdminStatCard(
                            title = "Pending Deposits",
                            value = m?.pendingDeposits?.toString(),
                            icon = Icons.Default.AccountBalanceWallet,
                            onClick = onNavigateToTransactions,
                            modifier = Modifier.weight(1f)
                        )
                        AdminStatCard(
                            title = "Pending Withdrawals",
                            value = m?.pendingWithdrawals?.toString(),
                            icon = Icons.Default.Receipt,
                            isWarning = (m?.pendingWithdrawals ?: 0) > 10,
                            onClick = onNavigateToTransactions,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Section: Trading & Bot Metrics
                item {
                    Text(
                        text = "TRADING & BOT ENGINE",
                        color = TextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AdminStatCard(
                            title = "Active Trades",
                            value = m?.activeTrades?.toString(),
                            icon = Icons.Default.Analytics,
                            onClick = onNavigateToTrading,
                            modifier = Modifier.weight(1f)
                        )
                        AdminStatCard(
                            title = "Active Bots",
                            value = m?.activeBots?.toString(),
                            icon = Icons.Default.SmartToy,
                            subValue = m?.let { "${it.botErrors} in error" },
                            isWarning = (m?.botErrors ?: 0) > 0,
                            onClick = onNavigateToBots,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Section: Support Metrics
                item {
                    Text(
                        text = "SUPPORT DESK",
                        color = TextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AdminStatCard(
                            title = "Open Tickets",
                            value = m?.openSupportTickets?.toString(),
                            icon = Icons.Default.SupportAgent,
                            onClick = onNavigateToSupport,
                            modifier = Modifier.weight(1f)
                        )
                        AdminStatCard(
                            title = "Pending Requests",
                            value = m?.pendingSupportRequests?.toString(),
                            icon = Icons.AutoMirrored.Filled.Assignment,
                            onClick = onNavigateToSupport,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminNavTile(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceDarkCard)
            .border(1.dp, SurfaceBorder, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(ElectricLimeSurface),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = ElectricLime,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = title,
            color = TextPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
