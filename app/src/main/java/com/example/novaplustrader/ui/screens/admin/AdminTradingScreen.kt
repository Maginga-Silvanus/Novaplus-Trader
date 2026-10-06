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
import androidx.compose.material.icons.filled.Analytics
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
import com.example.novaplustrader.ui.screens.admin.models.AdminTradeModel
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
import com.example.novaplustrader.ui.util.Formatters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminTradingScreen(
    viewModel: AdminTradingViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    val statusFilterOptions = listOf("ALL", "ACTIVE", "PENDING", "COMPLETED", "FAILED", "CANCELLED")
    val pairFilterOptions = listOf("ALL", "BTC/USD", "ETH/USD", "EUR/USD")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        AdminHeaderBar(
            title = "Trading Desk Operations",
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
                placeholder = "Search trade ID, pair or user..."
            )
        }

        AdminFilterChipBar(
            options = statusFilterOptions,
            selectedOption = uiState.selectedStatusFilter,
            onOptionSelected = viewModel::onStatusFilterSelected
        )

        AdminFilterChipBar(
            options = pairFilterOptions,
            selectedOption = uiState.selectedPairFilter,
            onOptionSelected = viewModel::onPairFilterSelected
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

        if (uiState.isLoading && uiState.trades.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                LoadingState()
            }
        } else if (uiState.trades.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                EmptyState(
                    title = "No Trades Found",
                    message = "No financial trades match the current search or status filter."
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.trades, key = { it.tradeId }) { trade ->
                    AdminTradeCard(
                        trade = trade,
                        onRequestCancellation = { viewModel.initiateTradeCancellation(trade) }
                    )
                }
            }
        }

        // Trade Cancellation Confirmation Sheet
        if (uiState.selectedTradeForCancellation != null) {
            val trd = uiState.selectedTradeForCancellation!!

            ConfirmationSheet(
                title = "Request Trade Cancellation",
                confirmButtonText = "Submit Cancellation Command",
                isLoading = uiState.isActionInProgress,
                details = listOf(
                    Pair("Trade ID", trd.tradeId),
                    Pair("User ID", trd.userId),
                    Pair("Pair", trd.assetPair),
                    Pair("Direction", trd.direction),
                    Pair("Amount", Formatters.formatCurrency(trd.amount, Currency.USD)),
                    Pair("Notice", "Cancellation is sent to backend trading settlement engine")
                ),
                onConfirm = viewModel::confirmTradeCancellationRequest,
                onDismiss = viewModel::dismissCancellation
            )
        }
    }
}

@Composable
private fun AdminTradeCard(
    trade: AdminTradeModel,
    onRequestCancellation: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isBuy = trade.direction.equals("BUY", ignoreCase = true)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(SurfaceDarkCard)
            .border(1.dp, SurfaceBorder, RoundedCornerShape(18.dp))
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
                        .background(if (isBuy) FinancialGreenGlow else FinancialRedGlow),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Analytics,
                        contentDescription = trade.assetPair,
                        tint = if (isBuy) FinancialGreen else FinancialRed,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "${trade.assetPair} • ${trade.direction}",
                        color = if (isBuy) FinancialGreen else FinancialRed,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Trade ID: ${trade.tradeId} • User: ${trade.userId}",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(ElectricLimeSurface)
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = trade.status.uppercase(),
                    color = ElectricLime,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Amount: ${Formatters.formatCurrency(trade.amount, Currency.USD)}",
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Duration: ${trade.duration} • Time: ${trade.timestamp}",
                color = TextMuted,
                fontSize = 12.sp
            )
        }

        if (trade.status.equals("ACTIVE", ignoreCase = true) || trade.status.equals("PENDING", ignoreCase = true)) {
            SecondaryButton(
                text = "Request Trade Cancellation",
                onClick = onRequestCancellation
            )
        }
    }
}
