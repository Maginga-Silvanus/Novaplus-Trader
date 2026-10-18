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
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.novaplustrader.ui.components.EmptyState
import com.example.novaplustrader.ui.components.LoadingState
import com.example.novaplustrader.ui.components.SecondaryButton
import com.example.novaplustrader.ui.model.Currency
import com.example.novaplustrader.ui.screens.admin.components.AdminFilterChipBar
import com.example.novaplustrader.ui.screens.admin.components.AdminHeaderBar
import com.example.novaplustrader.ui.screens.admin.components.AdminSearchBar
import com.example.novaplustrader.ui.screens.admin.models.AdminTransactionModel
import com.example.novaplustrader.ui.theme.BackgroundDark
import com.example.novaplustrader.ui.theme.ElectricLime
import com.example.novaplustrader.ui.theme.ElectricLimeSurface
import com.example.novaplustrader.ui.theme.FinancialGreen
import com.example.novaplustrader.ui.theme.FinancialGreenGlow
import com.example.novaplustrader.ui.theme.FinancialRed
import com.example.novaplustrader.ui.theme.FinancialRedGlow
import com.example.novaplustrader.ui.theme.OverlayDark
import com.example.novaplustrader.ui.theme.SurfaceBorder
import com.example.novaplustrader.ui.theme.SurfaceDarkCard
import com.example.novaplustrader.ui.theme.SurfaceDarkElevated
import com.example.novaplustrader.ui.theme.TextMuted
import com.example.novaplustrader.ui.theme.TextPrimary
import com.example.novaplustrader.ui.util.Formatters

private fun formatTxAmount(amount: Double, currencyStr: String): String {
    val currencyEnum = try { Currency.valueOf(currencyStr.uppercase()) } catch (_: Exception) { Currency.USD }
    return Formatters.formatCurrency(amount, currencyEnum)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminTransactionsScreen(
    viewModel: AdminTransactionsViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    val typeFilterOptions = listOf("ALL", "DEPOSIT", "WITHDRAWAL", "TRANSFER", "TRADE", "FEE", "REFUND", "ADJUSTMENT")
    val statusFilterOptions = listOf("ALL", "SUBMITTED", "PENDING", "PROCESSING", "COMPLETED", "APPROVED", "FAILED", "CANCELLED", "REVERSED")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        AdminHeaderBar(
            title = "Transaction Audit Desk",
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
                placeholder = "Search Tx ID, user UID or name..."
            )
        }

        AdminFilterChipBar(
            options = typeFilterOptions,
            selectedOption = uiState.selectedTypeFilter,
            onOptionSelected = viewModel::onTypeFilterSelected
        )

        AdminFilterChipBar(
            options = statusFilterOptions,
            selectedOption = uiState.selectedStatusFilter,
            onOptionSelected = viewModel::onStatusFilterSelected
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (uiState.isLoading && uiState.transactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                LoadingState()
            }
        } else if (uiState.transactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                EmptyState(
                    title = "No Transactions Found",
                    message = "No audit transaction logs match the current search or status filter."
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(uiState.transactions, key = { it.transactionId }) { tx ->
                    AdminTransactionRow(
                        tx = tx,
                        onClick = { viewModel.selectTransactionDetails(tx) }
                    )
                }
            }
        }

        // Transaction Details Sheet
        if (uiState.selectedTransactionDetails != null) {
            val selectedTx = uiState.selectedTransactionDetails!!
            ModalBottomSheet(
                onDismissRequest = { viewModel.selectTransactionDetails(null) },
                containerColor = BackgroundDark,
                scrimColor = OverlayDark
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Transaction Audit Detail",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(SurfaceDarkElevated)
                            .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        TxDetailPair("Transaction ID", selectedTx.transactionId)
                        TxDetailPair("User ID", selectedTx.userId)
                        TxDetailPair("Owner Name", selectedTx.userName)
                        TxDetailPair("Type", selectedTx.type)
                        TxDetailPair("Status", selectedTx.status)
                        TxDetailPair("Amount", formatTxAmount(selectedTx.amount, selectedTx.currency))
                        selectedTx.providerReference?.let {
                            TxDetailPair("M-Pesa Transaction Code", it)
                        }
                        TxDetailPair("Backend Timestamp", selectedTx.timestamp)
                    }

                    if (selectedTx.type.equals("DEPOSIT", ignoreCase = true) &&
                        selectedTx.status.equals("PENDING", ignoreCase = true)
                    ) {
                        com.example.novaplustrader.ui.components.PrimaryButton(
                            text = "APPROVE DEPOSIT & CREDIT WALLET",
                            onClick = { viewModel.approveDeposit(selectedTx) },
                            isLoading = uiState.isLoading
                        )
                    }

                    SecondaryButton(
                        text = "Close",
                        onClick = { viewModel.selectTransactionDetails(null) }
                    )
                }
            }
        }
    }
}

@Composable
private fun AdminTransactionRow(
    tx: AdminTransactionModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceDarkCard)
            .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(ElectricLimeSurface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Receipt,
                    contentDescription = tx.type,
                    tint = ElectricLime,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = tx.type,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    TxStatusBadge(status = tx.status)
                }
                Text(
                    text = "${tx.userName} • ID: ${tx.transactionId}",
                    color = TextMuted,
                    fontSize = 12.sp
                )
                Text(
                    text = tx.timestamp,
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }
        }

        Text(
            text = formatTxAmount(tx.amount, tx.currency),
            color = TextPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun TxStatusBadge(status: String) {
    val (bgColor, textColor) = when (status.uppercase()) {
        "COMPLETED" -> Pair(FinancialGreenGlow, FinancialGreen)
        "FAILED", "CANCELLED", "REVERSED" -> Pair(FinancialRedGlow, FinancialRed)
        else -> Pair(ElectricLimeSurface, ElectricLime)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = status.uppercase(),
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun TxDetailPair(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = TextMuted, fontSize = 13.sp)
        Text(text = value, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}
