package com.example.novaplustrader.ui.screens.gaming

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
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
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
import com.example.novaplustrader.domain.model.gaming.GamingEligibilityStatus
import com.example.novaplustrader.ui.components.IconActionButton
import com.example.novaplustrader.ui.components.ProfileMenuItem
import com.example.novaplustrader.ui.components.SecondaryButton
import com.example.novaplustrader.ui.theme.BackgroundDark
import com.example.novaplustrader.ui.theme.ElectricLime
import com.example.novaplustrader.ui.theme.FinancialGreen
import com.example.novaplustrader.ui.theme.FinancialRed
import com.example.novaplustrader.ui.theme.SurfaceBorder
import com.example.novaplustrader.ui.theme.SurfaceDarkCard
import com.example.novaplustrader.ui.theme.SurfaceDarkElevated
import com.example.novaplustrader.ui.theme.TextMuted
import com.example.novaplustrader.ui.theme.TextPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResponsibleGamingScreen(
    viewModel: ResponsibleGamingViewModel,
    onBackClick: () -> Unit,
    onNavigateToSelfExclusion: () -> Unit,
    onNavigateToActivity: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val prof = uiState.profile

    val statusColor = when (prof.eligibilityStatus) {
        GamingEligibilityStatus.NORMAL -> FinancialGreen
        GamingEligibilityStatus.SELF_EXCLUDED, GamingEligibilityStatus.PERMANENTLY_BLOCKED -> FinancialRed
        else -> ElectricLime
    }

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
                    text = "Responsible Use & Controls",
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
                // Eligibility Status Card
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(SurfaceDarkCard)
                            .border(1.dp, statusColor.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Eligibility",
                            tint = statusColor,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Text(
                            text = prof.eligibilityStatus.label.uppercase(),
                            color = statusColor,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        if (prof.activeExclusion != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Active Period: ${prof.activeExclusion.period.label}",
                                color = TextMuted,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                // Reality Check Timer Card
                item {
                    Row(
                        modifier = Modifier
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
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = "Timer",
                                tint = ElectricLime
                            )
                            Column {
                                Text(
                                    text = "Current Session Duration",
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = uiState.sessionDurationFormatted,
                                    color = TextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(ElectricLime.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "REALITY CHECK ON",
                                color = ElectricLime,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Controls Section
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "RESPONSIBLE GAMING CONTROLS",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        ProfileMenuItem(
                            title = "Self-Exclusion Lock",
                            subtitle = "Lock access to Casino Spin & Sportsbook for a set period",
                            icon = Icons.Default.Lock,
                            onClick = onNavigateToSelfExclusion
                        )

                        ProfileMenuItem(
                            title = "Demo Activity Logs",
                            subtitle = "View session history, demo stakes, and results",
                            icon = Icons.Default.History,
                            onClick = onNavigateToActivity
                        )
                    }
                }

                // Demo Notice
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(SurfaceDarkElevated)
                            .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
                            .padding(14.dp)
                    ) {
                        Text(
                            text = "DEMO FEATURE DISCLAIMER",
                            color = ElectricLime,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Casino Spin and Sports Betting features operate in Demo Mode using Demo Credits only. No real money wagering or cash settlement occurs.",
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
