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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
fun IncidentDetailsScreen(
    incidentId: String,
    viewModel: IncidentViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(incidentId) {
        viewModel.loadIncidentDetails(incidentId)
    }

    val incident = uiState.selectedIncident

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        AdminHeaderBar(
            title = "Incident Details: $incidentId",
            onBackClick = onBackClick
        )

        if (incident == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(40.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Incident not found or loading...",
                    color = TextMuted,
                    fontSize = 15.sp
                )
            }
        } else {
            val (sevBg, sevText) = when (incident.severity) {
                IncidentSeverity.CRITICAL, IncidentSeverity.HIGH -> Pair(FinancialRedGlow, FinancialRed)
                IncidentSeverity.MEDIUM -> Pair(ElectricLimeSurface, ElectricLime)
                IncidentSeverity.LOW -> Pair(FinancialGreenGlow, FinancialGreen)
            }

            val (statBg, statText) = when (incident.status) {
                IncidentStatus.RESOLVED -> Pair(FinancialGreenGlow, FinancialGreen)
                IncidentStatus.INVESTIGATING, IncidentStatus.OPEN -> Pair(FinancialRedGlow, FinancialRed)
                else -> Pair(ElectricLimeSurface, ElectricLime)
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(SurfaceDarkCard)
                            .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = incident.affectedService.uppercase(),
                                color = TextMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(sevBg)
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = incident.severity.label.uppercase(),
                                        color = sevText,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(statBg)
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = incident.status.label.uppercase(),
                                        color = statText,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Text(
                            text = incident.title,
                            color = TextPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = incident.description,
                            color = TextMuted,
                            fontSize = 14.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Started: ${incident.startTime} • Last Updated: ${incident.lastUpdatedTime}",
                            color = TextMuted,
                            fontSize = 12.sp
                        )

                        if (!incident.resolutionSummary.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(BackgroundDark)
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = "RESOLUTION SUMMARY",
                                    color = FinancialGreen,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = incident.resolutionSummary!!,
                                    color = TextPrimary,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    PrimaryButton(
                        text = "Mark Incident Resolved",
                        onClick = {
                            viewModel.updateStatus(incident.incidentId, "RESOLVED", "Resolved by system administrator intervention.")
                        }
                    )
                }
            }
        }
    }
}
