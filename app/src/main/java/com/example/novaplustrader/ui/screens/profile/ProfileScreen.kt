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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Settings
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
    modifier: Modifier = Modifier
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
                            initials = p?.displayName?.take(2) ?: "LL",
                            size = 68.dp,
                            hasOnlineBadge = true
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = p?.fullName ?: "Leonard Lopez",
                            color = TextPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = p?.email ?: "leonard.lopez@novapay.io",
                            color = TextMuted,
                            fontSize = 13.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatusBadge(status = TransactionStatus.COMPLETED)
                            Text(
                                text = "ID: ${p?.accountId ?: "#NV-92841"}",
                                color = ElectricLime,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = p?.createdAt ?: "Member since Jan 2024",
                            color = TextMuted,
                            fontSize = 11.sp
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
