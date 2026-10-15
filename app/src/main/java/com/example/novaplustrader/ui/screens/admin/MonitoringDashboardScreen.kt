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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Refresh
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
import com.example.novaplustrader.domain.model.monitoring.HealthStatus
import com.example.novaplustrader.domain.model.monitoring.IncidentSeverity
import com.example.novaplustrader.domain.model.monitoring.IncidentStatus
import com.example.novaplustrader.ui.components.PrimaryButton
import com.example.novaplustrader.ui.screens.admin.components.AdminHeaderBar
import com.example.novaplustrader.ui.theme.BackgroundDark
import com.example.novaplustrader.ui.theme.ElectricLime
import com.example.novaplustrader.ui.theme.ElectricLimeSurface
import com.example.novaplustrader.ui.theme.FinancialGreen
import com.example.novaplustrader.ui.theme.FinancialGreenGlow
import com.example.novaplustrader.ui.theme.FinancialRed
import com.example.novaplustrader.ui.theme.FinancialRedGlow
import com.example.novaplustrader.ui.theme.SurfaceBorder
import com.example.novaplustrader.ui.theme.SurfaceDarkCard
import com.example.novaplustrader.ui.theme.TextMuted
import com.example.novaplustrader.ui.theme.TextPrimary

@Composable
fun MonitoringDashboardScreen(
    systemHealthViewModel: SystemHealthViewModel,
    incidentViewModel: IncidentViewModel,
    onNavigateToSystemHealth: () -> Unit,
    onNavigateToIncidentDetails: (String) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val healthUiState by systemHealthViewModel.uiState.collectAsState()
    val incidentUiState by incidentViewModel.uiState.collectAsState()

    val marketRel = healthUiState.marketTradingReliability
    val alerts = healthUiState.financialAlerts
    val bots = healthUiState.botReliabilityList
    val incidents = incidentUiState.incidents

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        AdminHeaderBar(
            title = "Production Monitoring Dashboard",
            onBackClick = onBackClick
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // System Health Subsystem Banner
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceDarkCard)
                        .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
                        .clickable { onNavigateToSystemHealth() }
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(ElectricLimeSurface),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudDone,
                                contentDescription = "System Health",
                                tint = ElectricLime,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Subsystem Status (10 Tracked)",
                                color = TextMuted,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            val operationalCount = healthUiState.services.count { it.status == HealthStatus.OPERATIONAL }
                            Text(
                                text = "$operationalCount / 10 Operational",
                                color = if (operationalCount == 10) FinancialGreen else FinancialRed,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "View Health",
                        tint = TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Market Data & Trading Reliability Card
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceDarkCard)
                        .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "MARKET DATA & TRADING RELIABILITY",
                            color = TextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        val isTradeAvailable = marketRel.tradingBackendAvailable && !marketRel.isStalePriceFeeds
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isTradeAvailable) FinancialGreenGlow else FinancialRedGlow)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = if (isTradeAvailable) "TRADING ENABLED" else "TRADING BLOCKED",
                                color = if (isTradeAvailable) FinancialGreen else FinancialRed,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ReliabilityStatItem("WebSocket", if (marketRel.websocketConnected) "Connected" else "Disconnected", marketRel.websocketConnected)
                        ReliabilityStatItem("Price Feeds", if (marketRel.isStalePriceFeeds) "Stale" else "Fresh", !marketRel.isStalePriceFeeds)
                        ReliabilityStatItem("Execution Delay", "${marketRel.executionDelayMs} ms", marketRel.executionDelayMs < 300)
                    }
                    Text(
                        text = "Last Market Update: ${marketRel.lastMarketDataUpdate} • Rejection Rate: ${marketRel.orderRejectionRate}%",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }

            // Financial Reliability & Ledger Alerts
            if (alerts.isNotEmpty()) {
                item {
                    Text(
                        text = "FINANCIAL & LEDGER RECONCILIATION ALERTS",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
                items(alerts, key = { it.alertId }) { alert ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(SurfaceDarkCard)
                            .border(1.dp, SurfaceBorder, RoundedCornerShape(14.dp))
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = alert.title,
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = alert.description,
                                color = TextMuted,
                                fontSize = 12.sp
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(FinancialRedGlow)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = alert.severity.label.uppercase(),
                                color = FinancialRed,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Active Incidents Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ACTIVE INCIDENTS (${incidents.size})",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }

            if (incidents.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(SurfaceDarkCard)
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No active production incidents reported.",
                            color = FinancialGreen,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                items(incidents, key = { it.incidentId }) { inc ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(SurfaceDarkCard)
                            .border(1.dp, SurfaceBorder, RoundedCornerShape(14.dp))
                            .clickable { onNavigateToIncidentDetails(inc.incidentId) }
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = inc.title,
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Service: ${inc.affectedService} • Updated: ${inc.lastUpdatedTime}",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (inc.status == IncidentStatus.RESOLVED) FinancialGreenGlow else FinancialRedGlow)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = inc.status.label.uppercase(),
                                color = if (inc.status == IncidentStatus.RESOLVED) FinancialGreen else FinancialRed,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(10.dp))
                PrimaryButton(
                    text = "Refresh Monitoring Dashboards",
                    onClick = {
                        systemHealthViewModel.refreshAll()
                        incidentViewModel.loadIncidents()
                    },
                    isLoading = healthUiState.isRefreshing || incidentUiState.isLoading
                )
            }
        }
    }
}

@Composable
private fun ReliabilityStatItem(
    label: String,
    value: String,
    isGood: Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            color = TextMuted,
            fontSize = 11.sp
        )
        Text(
            text = value,
            color = if (isGood) FinancialGreen else FinancialRed,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
