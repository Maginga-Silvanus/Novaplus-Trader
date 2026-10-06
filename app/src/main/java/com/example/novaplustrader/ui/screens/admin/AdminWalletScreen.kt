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
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Warning
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
import com.example.novaplustrader.ui.components.ConfirmationSheet
import com.example.novaplustrader.ui.components.EmptyState
import com.example.novaplustrader.ui.components.LoadingState
import com.example.novaplustrader.ui.components.SecondaryButton
import com.example.novaplustrader.ui.model.Currency
import com.example.novaplustrader.ui.screens.admin.components.AdminFilterChipBar
import com.example.novaplustrader.ui.screens.admin.components.AdminHeaderBar
import com.example.novaplustrader.ui.screens.admin.components.AdminSearchBar
import com.example.novaplustrader.ui.screens.admin.models.AdminWalletModel
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
import com.example.novaplustrader.ui.util.Formatters

private fun formatWalletAmount(amount: Double, currencyStr: String): String {
    val currencyEnum = try { Currency.valueOf(currencyStr.uppercase()) } catch (e: Exception) { Currency.USD }
    return Formatters.formatCurrency(amount, currencyEnum)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminWalletScreen(
    viewModel: AdminWalletViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    val currencyFilterOptions = listOf("ALL", "USD", "KES", "EUR")
    val statusFilterOptions = listOf("ALL", "ACTIVE", "HOLD", "LOCKED")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        AdminHeaderBar(
            title = "Wallet & Ledger Control",
            onBackClick = onBackClick
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            AdminSearchBar(
                query = uiState.searchQuery,
                onQueryChange = viewModel::onSearchQueryChange,
                placeholder = "Search wallet ID, user or owner..."
            )
        }

        AdminFilterChipBar(
            options = currencyFilterOptions,
            selectedOption = uiState.currencyFilter,
            onOptionSelected = viewModel::onCurrencyFilterSelected
        )

        AdminFilterChipBar(
            options = statusFilterOptions,
            selectedOption = uiState.statusFilter,
            onOptionSelected = viewModel::onStatusFilterSelected
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (uiState.actionSuccessMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(ElectricLimeSurface)
                    .border(1.dp, ElectricLime, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = uiState.actionSuccessMessage!!,
                    color = ElectricLime,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        if (uiState.isLoading && uiState.wallets.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                LoadingState()
            }
        } else if (uiState.wallets.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                EmptyState(
                    title = "No Wallets Found",
                    message = "No wallet accounts match the active search or filter rules."
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.wallets, key = { it.walletId }) { wallet ->
                    AdminWalletCard(
                        wallet = wallet,
                        onRiskHoldToggle = { viewModel.initiateRiskHoldToggle(wallet) },
                        onRequestAdjustment = { viewModel.initiateLedgerAdjustmentRequest(wallet) }
                    )
                }
            }
        }

        // Risk Hold Confirmation
        if (uiState.showRiskHoldConfirmation && uiState.selectedWalletForAction != null) {
            val w = uiState.selectedWalletForAction!!
            val newHoldState = !w.riskHold

            ConfirmationSheet(
                title = if (newHoldState) "Apply Risk Hold" else "Release Risk Hold",
                confirmButtonText = "Submit Hold Command",
                isLoading = uiState.isActionInProgress,
                details = listOf(
                    Pair("Wallet ID", w.walletId),
                    Pair("Owner", w.userName),
                    Pair("Currency", w.currency),
                    Pair("Target Status", if (newHoldState) "HOLD" else "ACTIVE")
                ),
                onConfirm = viewModel::confirmRiskHoldToggle,
                onDismiss = viewModel::dismissConfirmations
            )
        }

        // Ledger Adjustment Confirmation
        if (uiState.showLedgerAdjustmentConfirmation && uiState.selectedWalletForAction != null) {
            val w = uiState.selectedWalletForAction!!

            ConfirmationSheet(
                title = "Backend Ledger Adjustment Request",
                confirmButtonText = "Submit Ledger Request",
                isLoading = uiState.isActionInProgress,
                details = listOf(
                    Pair("Wallet ID", w.walletId),
                    Pair("Owner", w.userName),
                    Pair("Currency", w.currency),
                    Pair("Current Available", formatWalletAmount(w.availableBalance, w.currency)),
                    Pair("Notice", "Adjustment is processed authoritatively by backend ledger service")
                ),
                onConfirm = viewModel::confirmLedgerAdjustmentRequest,
                onDismiss = viewModel::dismissConfirmations
            )
        }
    }
}

@Composable
private fun AdminWalletCard(
    wallet: AdminWalletModel,
    onRiskHoldToggle: () -> Unit,
    onRequestAdjustment: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(SurfaceDarkCard)
            .border(1.dp, if (wallet.riskHold) FinancialRed else SurfaceBorder, RoundedCornerShape(18.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (wallet.riskHold) FinancialRedGlow else ElectricLimeSurface),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (wallet.riskHold) Icons.Default.Warning else Icons.Default.AccountBalanceWallet,
                        contentDescription = wallet.currency,
                        tint = if (wallet.riskHold) FinancialRed else ElectricLime,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "${wallet.userName} (${wallet.currency})",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "ID: ${wallet.walletId} • User: ${wallet.userId}",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (wallet.riskHold) FinancialRedGlow else ElectricLimeSurface)
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = wallet.status.uppercase(),
                    color = if (wallet.riskHold) FinancialRed else FinancialGreen,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceDarkElevated)
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = "Available Balance", color = TextMuted, fontSize = 11.sp)
                Text(
                    text = formatWalletAmount(wallet.availableBalance, wallet.currency),
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(text = "Pending Balance", color = TextMuted, fontSize = 11.sp)
                Text(
                    text = formatWalletAmount(wallet.pendingBalance, wallet.currency),
                    color = ElectricLime,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Text(
            text = "Last Transaction: ${wallet.lastTransaction}",
            color = TextMuted,
            fontSize = 11.sp
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SecondaryButton(
                text = if (wallet.riskHold) "Release Hold" else "Set Risk Hold",
                onClick = onRiskHoldToggle,
                modifier = Modifier.weight(1f)
            )
            SecondaryButton(
                text = "Request Ledger Adj",
                onClick = onRequestAdjustment,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
