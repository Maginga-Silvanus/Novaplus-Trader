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
import androidx.compose.material.icons.filled.SmartToy
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
import com.example.novaplustrader.ui.screens.admin.models.AdminBotModel
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
fun AdminBotsScreen(
    viewModel: AdminBotsViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    val statusFilterOptions = listOf("ALL", "ACTIVE", "PAUSED", "STOPPED", "ERROR")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        AdminHeaderBar(
            title = "Trading Bot Cluster",
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
                placeholder = "Search bot ID, owner or strategy..."
            )
        }

        AdminFilterChipBar(
            options = statusFilterOptions,
            selectedOption = uiState.selectedStatusFilter,
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

        if (uiState.isLoading && uiState.bots.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                LoadingState()
            }
        } else if (uiState.bots.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                EmptyState(
                    title = "No Trading Bots Found",
                    message = "No bot instances match the current search or status filter."
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.bots, key = { it.botId }) { bot ->
                    AdminBotCard(
                        bot = bot,
                        onCommand = { cmd -> viewModel.initiateBotCommand(bot, cmd) }
                    )
                }
            }
        }

        // Bot Command Confirmation Sheet
        if (uiState.selectedBotForCommand != null && uiState.activeCommand != null) {
            val bot = uiState.selectedBotForCommand!!
            val cmd = uiState.activeCommand!!

            ConfirmationSheet(
                title = "Confirm Bot Command: $cmd",
                confirmButtonText = "Execute Command",
                isLoading = uiState.isActionInProgress,
                details = listOf(
                    Pair("Bot Instance ID", bot.botId),
                    Pair("Owner", bot.ownerName),
                    Pair("Strategy", bot.strategy),
                    Pair("Target Command", cmd),
                    Pair("Notice", "Command is dispatched to server bot execution engine")
                ),
                onConfirm = viewModel::confirmBotCommand,
                onDismiss = viewModel::dismissCommandConfirmation
            )
        }
    }
}

@Composable
private fun AdminBotCard(
    bot: AdminBotModel,
    onCommand: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isError = bot.status.equals("ERROR", ignoreCase = true)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(SurfaceDarkCard)
            .border(1.dp, if (isError) FinancialRed else SurfaceBorder, RoundedCornerShape(18.dp))
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
                        .background(if (isError) FinancialRedGlow else ElectricLimeSurface),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isError) Icons.Default.Warning else Icons.Default.SmartToy,
                        contentDescription = bot.strategy,
                        tint = if (isError) FinancialRed else ElectricLime,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "${bot.strategy} (${bot.pair})",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "ID: ${bot.botId} • Owner: ${bot.ownerName}",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isError) FinancialRedGlow else ElectricLimeSurface)
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = bot.status.uppercase(),
                    color = if (isError) FinancialRed else FinancialGreen,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (!bot.errorState.isNullOrBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(FinancialRedGlow)
                    .padding(10.dp)
            ) {
                Text(
                    text = "Error State: ${bot.errorState}",
                    color = FinancialRed,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Allocation: ${Formatters.formatCurrency(bot.allocation, Currency.USD)}",
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Risk: ${bot.riskLevel} • Active: ${bot.lastActivity}",
                color = TextMuted,
                fontSize = 11.sp
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (bot.status.equals("ACTIVE", ignoreCase = true)) {
                SecondaryButton(
                    text = "Pause Bot",
                    onClick = { onCommand("PAUSE") },
                    modifier = Modifier.weight(1f)
                )
            } else {
                SecondaryButton(
                    text = "Resume Bot",
                    onClick = { onCommand("RESUME") },
                    modifier = Modifier.weight(1f)
                )
            }

            SecondaryButton(
                text = "Stop Bot",
                onClick = { onCommand("STOP") },
                modifier = Modifier.weight(1f)
            )
        }
    }
}
