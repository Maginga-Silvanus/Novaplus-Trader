package com.example.novaplustrader.ui.screens.settings

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.novaplustrader.ui.components.SecurityCard
import com.example.novaplustrader.ui.screens.notifications.NotificationViewModel
import com.example.novaplustrader.ui.theme.BackgroundDark
import com.example.novaplustrader.ui.theme.ElectricLime
import com.example.novaplustrader.ui.theme.SurfaceBorder
import com.example.novaplustrader.ui.theme.SurfaceDarkElevated
import com.example.novaplustrader.ui.theme.TextMuted
import com.example.novaplustrader.ui.theme.TextPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
    viewModel: NotificationViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val prefs = uiState.preferences

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
                    text = "Notification Preferences",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Box(modifier = Modifier.padding(start = 24.dp))
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 100.dp, start = 16.dp, end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "ALERT CATEGORIES",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                item {
                    SecurityCard(
                        title = "Wallet & Transactions",
                        subtitle = "Deposit, withdrawal, and transfer confirmations",
                        isEnabled = prefs.allowTransactions,
                        onToggle = { enabled ->
                            viewModel.updatePreferences(prefs.copy(allowTransactions = enabled))
                        }
                    )
                }

                item {
                    SecurityCard(
                        title = "Trading Execution",
                        subtitle = "Option trade settlements and market events",
                        isEnabled = prefs.allowTrading,
                        onToggle = { enabled ->
                            viewModel.updatePreferences(prefs.copy(allowTrading = enabled))
                        }
                    )
                }

                item {
                    SecurityCard(
                        title = "Trading Bot Alerts",
                        subtitle = "Automated bot strategy triggers and status events",
                        isEnabled = prefs.allowTradingBot,
                        onToggle = { enabled ->
                            viewModel.updatePreferences(prefs.copy(allowTradingBot = enabled))
                        }
                    )
                }

                item {
                    SecurityCard(
                        title = "Security & Account",
                        subtitle = "New logins, security alerts, and credential events",
                        isEnabled = prefs.allowSecurity,
                        onToggle = { enabled ->
                            viewModel.updatePreferences(prefs.copy(allowSecurity = enabled))
                        }
                    )
                }

                item {
                    SecurityCard(
                        title = "System News",
                        subtitle = "Maintenance windows, upgrades, and platform news",
                        isEnabled = prefs.allowSystem,
                        onToggle = { enabled ->
                            viewModel.updatePreferences(prefs.copy(allowSystem = enabled))
                        }
                    )
                }

                item {
                    SecurityCard(
                        title = "Demo Games",
                        subtitle = "Simulated spins and sportsbook demo notifications",
                        isEnabled = prefs.allowDemoGames,
                        onToggle = { enabled ->
                            viewModel.updatePreferences(prefs.copy(allowDemoGames = enabled))
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(SurfaceDarkElevated)
                            .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
                            .padding(14.dp)
                    ) {
                        Text(
                            text = "SECURITY NOTICE",
                            color = ElectricLime,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Critical security alerts regarding unauthorized access or credential changes will be dispatched regardless of preference settings to protect your account.",
                            color = TextMuted,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
}
