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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.example.novaplustrader.ui.components.ConfirmationSheet
import com.example.novaplustrader.ui.components.LoadingState
import com.example.novaplustrader.ui.components.PrimaryButton
import com.example.novaplustrader.ui.components.SecondaryButton
import com.example.novaplustrader.ui.components.UserAvatar
import com.example.novaplustrader.ui.screens.admin.components.AdminHeaderBar
import com.example.novaplustrader.ui.screens.admin.models.AdminUserStatus
import com.example.novaplustrader.ui.theme.BackgroundDark
import com.example.novaplustrader.ui.theme.ElectricLime
import com.example.novaplustrader.ui.theme.ElectricLimeSurface
import com.example.novaplustrader.ui.theme.FinancialGreen
import com.example.novaplustrader.ui.theme.FinancialRed
import com.example.novaplustrader.ui.theme.FinancialRedGlow
import com.example.novaplustrader.ui.theme.SurfaceBorder
import com.example.novaplustrader.ui.theme.SurfaceDarkCard
import com.example.novaplustrader.ui.theme.SurfaceDarkElevated
import com.example.novaplustrader.ui.theme.TextMuted
import com.example.novaplustrader.ui.theme.TextPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUserDetailsScreen(
    userId: String,
    viewModel: AdminUserDetailsViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val u = uiState.user

    LaunchedEffect(userId) {
        viewModel.loadUserDetails(userId)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        AdminHeaderBar(
            title = "User Details",
            onBackClick = onBackClick
        )

        if (uiState.isLoading && u == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                LoadingState()
            }
        } else if (u != null) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // User Profile Summary Card
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(SurfaceDarkCard)
                            .border(1.dp, SurfaceBorder, RoundedCornerShape(20.dp))
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        UserAvatar(
                            initials = u.fullName.take(2).uppercase(),
                            size = 64.dp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = u.fullName,
                            color = TextPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = u.email,
                            color = TextMuted,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(ElectricLimeSurface)
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "ROLE: ${u.role.label}",
                                    color = ElectricLime,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            val isSuspendedOrLocked = u.accountStatus == AdminUserStatus.SUSPENDED || u.accountStatus == AdminUserStatus.LOCKED
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSuspendedOrLocked) FinancialRedGlow else ElectricLimeSurface)
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "STATUS: ${u.accountStatus.label.uppercase()}",
                                    color = if (isSuspendedOrLocked) FinancialRed else FinancialGreen,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Feedback Messages
                if (uiState.actionSuccessMessage != null) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(ElectricLimeSurface)
                                .border(1.dp, ElectricLime, RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = uiState.actionSuccessMessage!!,
                                color = ElectricLime,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                if (uiState.errorMessage != null) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(FinancialRedGlow)
                                .border(1.dp, FinancialRed, RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = uiState.errorMessage!!,
                                color = FinancialRed,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // Operational Info Card
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(SurfaceDarkCard)
                            .border(1.dp, SurfaceBorder, RoundedCornerShape(20.dp))
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "ACCOUNT & VERIFICATION METRICS",
                            color = TextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        AdminInfoRow("User ID (UID)", u.uid)
                        AdminInfoRow("Verification Status", u.verificationStatus)
                        AdminInfoRow("Account Created", u.createdAt)
                        AdminInfoRow("Last Active Timestamp", u.lastActivity)
                    }
                }

                // Operational Status Summary Card
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(SurfaceDarkCard)
                            .border(1.dp, SurfaceBorder, RoundedCornerShape(20.dp))
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "OPERATIONAL SUBSYSTEM STATUS",
                            color = TextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        AdminSubsystemRow(
                            label = "Wallet & Ledger Status",
                            value = u.walletStatus,
                            icon = Icons.Default.AccountBalanceWallet
                        )
                        AdminSubsystemRow(
                            label = "Trading Permission",
                            value = u.tradingStatus,
                            icon = Icons.Default.Analytics
                        )
                        AdminSubsystemRow(
                            label = "Trading Bot Service",
                            value = u.botStatus,
                            icon = Icons.Default.SmartToy
                        )
                        AdminSubsystemRow(
                            label = "Support Desk Queue",
                            value = u.supportStatus,
                            icon = Icons.Default.SupportAgent
                        )
                        AdminSubsystemRow(
                            label = "Security Activity Summary",
                            value = u.securitySummary,
                            icon = Icons.Default.Security
                        )
                    }
                }

                // Controlled Actions Card
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(SurfaceDarkCard)
                            .border(1.dp, SurfaceBorder, RoundedCornerShape(20.dp))
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "PRIVILEGED OPERATIONAL ACTIONS",
                            color = TextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        if (u.accountStatus == AdminUserStatus.SUSPENDED) {
                            PrimaryButton(
                                text = "Restore Account",
                                onClick = { viewModel.initiateAction(UserActionType.RESTORE) }
                            )
                        } else {
                            SecondaryButton(
                                text = "Suspend Account",
                                onClick = { viewModel.initiateAction(UserActionType.SUSPEND) }
                            )
                        }

                        if (u.accountStatus == AdminUserStatus.LOCKED) {
                            SecondaryButton(
                                text = "Unlock Account",
                                onClick = { viewModel.initiateAction(UserActionType.RESTORE) }
                            )
                        } else {
                            SecondaryButton(
                                text = "Lock Account",
                                onClick = { viewModel.initiateAction(UserActionType.LOCK) }
                            )
                        }

                        SecondaryButton(
                            text = "Require Reauthentication",
                            onClick = { viewModel.initiateAction(UserActionType.REQUIRE_REAUTH) }
                        )
                    }
                }
            }
        }

        // Action Confirmation Sheet with backend verification
        if (uiState.activeConfirmation != UserActionType.NONE && u != null) {
            val actionName = when (uiState.activeConfirmation) {
                UserActionType.SUSPEND -> "Suspend User Account"
                UserActionType.RESTORE -> "Restore User Account"
                UserActionType.LOCK -> "Lock User Account"
                UserActionType.REQUIRE_REAUTH -> "Require User Reauthentication"
                UserActionType.NONE -> ""
            }

            ConfirmationSheet(
                title = "Confirm $actionName",
                confirmButtonText = "Submit Command",
                isLoading = uiState.isActionInProgress,
                details = listOf(
                    Pair("Target User", u.fullName),
                    Pair("User ID", u.uid),
                    Pair("Target Email", u.email),
                    Pair("Action", uiState.activeConfirmation.name),
                    Pair("Authority Level", "Backend Authorized")
                ),
                onConfirm = viewModel::confirmAction,
                onDismiss = viewModel::dismissConfirmation
            )
        }
    }
}

@Composable
private fun AdminInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = TextMuted, fontSize = 13.sp)
        Text(text = value, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun AdminSubsystemRow(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceDarkElevated)
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = ElectricLime,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(text = label, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }
        Text(text = value, color = ElectricLime, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}
