package com.example.novaplustrader.ui.screens.security

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.novaplustrader.ui.components.ConfirmationSheet
import com.example.novaplustrader.ui.components.IconActionButton
import com.example.novaplustrader.ui.components.ProfileMenuItem
import com.example.novaplustrader.ui.components.SecondaryButton
import com.example.novaplustrader.ui.components.SecurityCard
import com.example.novaplustrader.ui.theme.BackgroundDark
import com.example.novaplustrader.ui.theme.TextMuted
import com.example.novaplustrader.ui.theme.TextPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityScreen(
    viewModel: SecurityViewModel,
    onBackClick: () -> Unit,
    onNavigateToActiveSessions: () -> Unit,
    onNavigateToSecurityActivity: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

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
                    text = "Security & Biometrics",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Box(modifier = Modifier.padding(start = 24.dp))
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 100.dp, start = 16.dp, end = 16.dp)
            ) {
                // Biometrics & PIN
                item {
                    Text(
                        text = "AUTHENTICATION & ACCESS",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        SecurityCard(
                            title = "Biometric Sign-In",
                            subtitle = "Use Fingerprint or Face ID for instant authorization",
                            isEnabled = uiState.isBiometricEnabled,
                            onToggle = { enabled ->
                                viewModel.toggleBiometric(enabled)
                            }
                        )

                        ProfileMenuItem(
                            title = "Transaction PIN",
                            subtitle = "4-digit security PIN for withdrawals and transfers",
                            icon = Icons.Default.Key,
                            onClick = { /* Security PIN configuration */ }
                        )

                        ProfileMenuItem(
                            title = "Change Password",
                            subtitle = "Update Firebase Authentication password",
                            icon = Icons.Default.Lock,
                            onClick = { /* Change password flow */ }
                        )

                        ProfileMenuItem(
                            title = "Two-Factor Authentication (2FA)",
                            subtitle = "Authenticator app / SMS verification (Coming soon)",
                            icon = Icons.Default.Shield,
                            onClick = { /* 2FA flow */ }
                        )
                    }
                }

                // Monitoring & Devices
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "SECURITY MONITORING & SESSIONS",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        ProfileMenuItem(
                            title = "Active Devices & Sessions",
                            subtitle = "${uiState.activeSessions.size} active sessions logged in",
                            icon = Icons.Default.Devices,
                            onClick = onNavigateToActiveSessions
                        )

                        ProfileMenuItem(
                            title = "Security Activity Log",
                            subtitle = "Recent logins, security changes, and verifications",
                            icon = Icons.Default.History,
                            onClick = onNavigateToSecurityActivity
                        )
                    }
                }

                // Protection Actions
                item {
                    Spacer(modifier = Modifier.height(24.dp))

                    SecondaryButton(
                        text = "SIGN OUT ALL OTHER DEVICES",
                        onClick = { viewModel.openSignOutOthersConfirmSheet() }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    SecondaryButton(
                        text = "REQUEST ACCOUNT DELETION",
                        onClick = { viewModel.openDeleteAccountConfirmSheet() }
                    )
                }
            }
        }

        // Sign Out Other Sessions Confirmation Sheet
        if (uiState.isSignOutOthersConfirmOpen) {
            ConfirmationSheet(
                title = "Sign Out Other Sessions?",
                details = listOf(
                    "Current Device" to "Pixel 9 Pro (Kept Logged In)",
                    "Sessions to Revoke" to "${uiState.activeSessions.size - 1} Devices",
                    "Action" to "Revokes authentication tokens on all other browsers and devices."
                ),
                confirmButtonText = "Sign Out Other Devices",
                isLoading = uiState.isLoading,
                onConfirm = { viewModel.confirmSignOutOthers() },
                onDismiss = { viewModel.closeSignOutOthersConfirmSheet() }
            )
        }

        // Delete Account Request Confirmation Sheet
        if (uiState.isDeleteAccountConfirmOpen) {
            ConfirmationSheet(
                title = "Request Account Deletion?",
                details = listOf(
                    "Notice" to "Account deletion requires compliance review.",
                    "Ledger Data" to "Financial ledger records must be retained per regulatory rules.",
                    "Consequence" to "Your trading access and profile credentials will be permanently closed."
                ),
                confirmButtonText = "Submit Deletion Request",
                isLoading = uiState.isLoading,
                onConfirm = { viewModel.confirmDeleteAccountRequest() },
                onDismiss = { viewModel.closeDeleteAccountConfirmSheet() }
            )
        }
    }
}
