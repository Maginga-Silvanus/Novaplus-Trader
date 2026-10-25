package com.example.novaplustrader.ui.screens.profile

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SportsSoccer
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
import com.example.novaplustrader.ui.components.ConfirmationSheet
import com.example.novaplustrader.ui.components.ProfileMenuItem
import com.example.novaplustrader.ui.components.SecondaryButton
import com.example.novaplustrader.ui.components.StatusBadge
import com.example.novaplustrader.ui.components.UserAvatar
import com.example.novaplustrader.ui.model.TransactionStatus
import com.example.novaplustrader.ui.theme.BackgroundDark
import com.example.novaplustrader.ui.theme.ElectricLime
import com.example.novaplustrader.ui.theme.SurfaceBorder
import com.example.novaplustrader.ui.theme.SurfaceDarkCard
import com.example.novaplustrader.ui.theme.TextMuted
import com.example.novaplustrader.ui.theme.TextPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onNavigateToEditProfile: () -> Unit,
    onNavigateToSecurity: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToPrivacy: () -> Unit,
    onLoggedOut: () -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToKyc: () -> Unit = {},
    onNavigateToResponsibleGaming: () -> Unit = {},
    onNavigateToTrade: () -> Unit = {},
    onNavigateToTradingBot: () -> Unit = {},
    onNavigateToCasinoSpin: () -> Unit = {},
    onNavigateToBetting: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val p = uiState.profile

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar Title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Profile",
                    color = TextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 100.dp, start = 16.dp, end = 16.dp)
            ) {
                // Header Card
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(SurfaceDarkCard)
                            .border(1.dp, SurfaceBorder, RoundedCornerShape(24.dp))
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        UserAvatar(
                            initials = p?.displayName?.take(2)?.ifBlank { null } ?: "NT",
                            size = 68.dp,
                            hasOnlineBadge = true
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = p?.fullName?.ifBlank { null } ?: "Nova Trader",
                                color = TextPrimary,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (p?.verificationStatus.equals("VERIFIED", ignoreCase = true)) {
                                androidx.compose.material3.Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Verified Account",
                                    tint = com.example.novaplustrader.ui.theme.FinancialGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Text(
                            text = p?.email?.ifBlank { null } ?: "No signed-in email",
                            color = TextMuted,
                            fontSize = 13.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatusBadge(
                                status = if (p?.verificationStatus.equals("VERIFIED", ignoreCase = true)) {
                                    TransactionStatus.COMPLETED
                                } else {
                                    TransactionStatus.PENDING
                                }
                            )
                            Text(
                                text = p?.accountId?.takeIf { it.isNotBlank() }?.let { "ID: $it" }
                                    ?: "Account ID unavailable",
                                color = ElectricLime,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = p?.createdAt?.ifBlank { null }?.let { "Member since $it" }
                                ?: "Membership date unavailable",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                // Featured Modes Section
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "FEATURED MODES & ENTERTAINMENT",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        ProfileMenuItem(
                            title = "Binary Options Trading",
                            subtitle = "Live price options trading with real-time charts",
                            icon = Icons.AutoMirrored.Filled.ShowChart,
                            onClick = onNavigateToTrade
                        )

                        ProfileMenuItem(
                            title = "Trading Bot Engine",
                            subtitle = "Automated algorithmic trading strategy execution",
                            icon = Icons.Default.Android,
                            onClick = onNavigateToTradingBot
                        )

                        ProfileMenuItem(
                            title = "Casino Spin Demo",
                            subtitle = "Minigame wheel spin using Demo Credits",
                            icon = Icons.Default.Casino,
                            onClick = onNavigateToCasinoSpin
                        )

                        ProfileMenuItem(
                            title = "Sportsbook Demo",
                            subtitle = "Sports odds & bet slips with Demo Credits",
                            icon = Icons.Default.SportsSoccer,
                            onClick = onNavigateToBetting
                        )
                    }
                }

                // Menu Items Section
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "ACCOUNT MANAGEMENT",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        ProfileMenuItem(
                            title = "Identity & KYC Verification",
                            subtitle = "Government ID details and compliance status",
                            icon = Icons.Default.Badge,
                            onClick = onNavigateToKyc
                        )

                        ProfileMenuItem(
                            title = "Responsible Use & Controls",
                            subtitle = "Self-exclusion, session limits, and reality check timers",
                            icon = Icons.Default.Shield,
                            onClick = onNavigateToResponsibleGaming
                        )

                        ProfileMenuItem(
                            title = "Edit Profile",
                            subtitle = "Full name, display name, and contact details",
                            icon = Icons.Default.Edit,
                            onClick = onNavigateToEditProfile
                        )

                        ProfileMenuItem(
                            title = "Security & Biometrics",
                            subtitle = "Password, PIN, fingerprint, active sessions",
                            icon = Icons.Default.Lock,
                            onClick = onNavigateToSecurity
                        )

                        ProfileMenuItem(
                            title = "App Preferences & Settings",
                            subtitle = "Currency, notifications, theme, and language",
                            icon = Icons.Default.Settings,
                            onClick = onNavigateToSettings
                        )

                        ProfileMenuItem(
                            title = "Privacy & Data Settings",
                            subtitle = "Data permissions, analytics, and marketing",
                            icon = Icons.Default.PrivacyTip,
                            onClick = onNavigateToPrivacy
                        )
                    }
                }

                // Logout Button
                item {
                    Spacer(modifier = Modifier.height(28.dp))

                    SecondaryButton(
                        text = "LOG OUT OF ACCOUNT",
                        onClick = { viewModel.openLogoutConfirmSheet() }
                    )
                }
            }
        }

        // Logout Confirmation Sheet
        if (uiState.isLogoutConfirmOpen) {
            ConfirmationSheet(
                title = "Log Out of NovaPlus Trader?",
                details = listOf(
                    "User Email" to (p?.email ?: ""),
                    "Account ID" to (p?.accountId ?: ""),
                    "Notice" to "You will be required to re-authenticate to access your account."
                ),
                confirmButtonText = "Log Out Now",
                isLoading = uiState.isLoading,
                onConfirm = {
                    viewModel.confirmLogout(onLoggedOut)
                },
                onDismiss = { viewModel.closeLogoutConfirmSheet() }
            )
        }
    }
}
