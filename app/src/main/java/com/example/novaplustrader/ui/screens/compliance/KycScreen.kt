package com.example.novaplustrader.ui.screens.compliance

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Shield
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
import com.example.novaplustrader.domain.model.compliance.KycStatus
import com.example.novaplustrader.ui.components.IconActionButton
import com.example.novaplustrader.ui.components.PrimaryButton
import com.example.novaplustrader.ui.theme.BackgroundDark
import com.example.novaplustrader.ui.theme.ElectricLime
import com.example.novaplustrader.ui.theme.ElectricLimeSurface
import com.example.novaplustrader.ui.theme.FinancialGreen
import com.example.novaplustrader.ui.theme.FinancialRed
import com.example.novaplustrader.ui.theme.SurfaceBorder
import com.example.novaplustrader.ui.theme.SurfaceDarkCard
import com.example.novaplustrader.ui.theme.SurfaceDarkElevated
import com.example.novaplustrader.ui.theme.TextMuted
import com.example.novaplustrader.ui.theme.TextPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KycScreen(
    viewModel: KycViewModel,
    onBackClick: () -> Unit,
    onNavigateToUpload: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    val statusColor = when (uiState.kycStatus) {
        KycStatus.VERIFIED -> FinancialGreen
        KycStatus.UNDER_REVIEW, KycStatus.SUBMITTED, KycStatus.IN_PROGRESS -> ElectricLime
        KycStatus.REJECTED -> FinancialRed
        else -> TextMuted
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
                    text = "Identity & KYC Verification",
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
                // Verification Status Card
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
                            contentDescription = "KYC Status",
                            tint = statusColor,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Text(
                            text = uiState.kycStatus.label.uppercase(),
                            color = statusColor,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Risk Level: ${uiState.riskStatus.label}",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }
                }

                // Requirements Checklist
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "VERIFICATION REQUIREMENTS",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                items(uiState.requirements) { req ->
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
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = req.title,
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = req.description,
                                color = TextMuted,
                                fontSize = 12.sp
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Requirement Met",
                            tint = FinancialGreen,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }

                // Upload Documents Button
                item {
                    Spacer(modifier = Modifier.height(16.dp))

                    PrimaryButton(
                        text = if (uiState.kycStatus == KycStatus.VERIFIED) "UPDATE IDENTITY DOCUMENTS" else "UPLOAD KYC DOCUMENTS",
                        onClick = onNavigateToUpload
                    )
                }

                // Data Privacy Compliance Notice
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
                            text = "REGULATORY COMPLIANCE NOTICE",
                            color = ElectricLime,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Identity documents are processed through encrypted backend connections strictly for Anti-Money Laundering (AML) and Know Your Customer (KYC) compliance. Documents are never stored in plaintext.",
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
