package com.example.novaplustrader.ui.screens.admin

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
import androidx.compose.material.icons.filled.CloudCircle
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
import com.example.novaplustrader.domain.model.monitoring.SystemHealthModel
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
fun SystemHealthScreen(
    viewModel: SystemHealthViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        AdminHeaderBar(
            title = "Production System Health",
            onBackClick = onBackClick
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SUBSYSTEM HEALTH MONITOR (10 SERVICES)",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Refreshed: ${uiState.lastRefreshedTime}",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }
            }

            if (uiState.services.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Not Connected / Unknown Status (Backend Unreachable)",
                            color = FinancialRed,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                items(uiState.services, key = { it.serviceId }) { health ->
                    SystemHealthCard(health = health)
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                PrimaryButton(
                    text = "Verify & Refresh Subsystems",
                    onClick = viewModel::refreshAll,
                    isLoading = uiState.isRefreshing
                )
            }
        }
    }
}

@Composable
private fun SystemHealthCard(
    health: SystemHealthModel,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when (health.status) {
        HealthStatus.OPERATIONAL -> Pair(FinancialGreenGlow, FinancialGreen)
        HealthStatus.DEGRADED -> Pair(ElectricLimeSurface, ElectricLime)
        HealthStatus.OUTAGE, HealthStatus.UNKNOWN, HealthStatus.MAINTENANCE -> Pair(FinancialRedGlow, FinancialRed)
    }

    val statusText = if (health.isStale) "${health.status.label.uppercase()} (STALE)" else health.status.label.uppercase()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceDarkCard)
            .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(bgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CloudCircle,
                    contentDescription = health.serviceName,
                    tint = textColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = health.serviceName,
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Latency: ${health.latencyMs} ms • Checked: ${health.lastChecked}",
                    color = TextMuted,
                    fontSize = 11.sp
                )
                if (!health.message.isNullOrBlank()) {
                    Text(
                        text = health.message!!,
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(bgColor)
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = statusText,
                color = textColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
