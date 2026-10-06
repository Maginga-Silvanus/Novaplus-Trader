package com.example.novaplustrader.ui.screens.bot

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.novaplustrader.ui.components.ConfirmationSheet
import com.example.novaplustrader.ui.components.IconActionButton
import com.example.novaplustrader.ui.components.PrimaryButton
import com.example.novaplustrader.ui.components.SecondaryButton
import com.example.novaplustrader.ui.screens.bot.components.BotActiveTradesList
import com.example.novaplustrader.ui.screens.bot.components.BotActivityHistoryList
import com.example.novaplustrader.ui.screens.bot.components.BotCustomParametersCard
import com.example.novaplustrader.ui.screens.bot.components.BotPerformanceCard
import com.example.novaplustrader.ui.screens.bot.components.BotRiskSelector
import com.example.novaplustrader.ui.screens.bot.components.BotStatusCard
import com.example.novaplustrader.ui.screens.bot.components.BotStrategySelector
import com.example.novaplustrader.ui.screens.bot.models.BotStatus
import com.example.novaplustrader.ui.screens.bot.models.BotStrategy
import com.example.novaplustrader.ui.screens.trading.components.AssetSelectorSheet
import com.example.novaplustrader.ui.screens.trading.components.PercentageShortcuts
import com.example.novaplustrader.ui.screens.trading.defaultAssetList
import com.example.novaplustrader.ui.theme.BackgroundDark
import com.example.novaplustrader.ui.theme.ElectricLime
import com.example.novaplustrader.ui.theme.FinancialGreen
import com.example.novaplustrader.ui.theme.FinancialRed
import com.example.novaplustrader.ui.theme.SurfaceBorder
import com.example.novaplustrader.ui.theme.SurfaceDarkCard
import com.example.novaplustrader.ui.theme.TextMuted
import com.example.novaplustrader.ui.theme.TextPrimary
import com.example.novaplustrader.ui.util.Formatters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TradingBotScreen(
    viewModel: TradingBotViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val config = uiState.configuration
    val isBotActive = uiState.botStatus == BotStatus.ACTIVE

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 1. Top Bar
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

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val statusDotColor = if (isBotActive) FinancialGreen else TextMuted
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(statusDotColor)
                    )
                    Text(
                        text = "Trading Bot",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconActionButton(
                    icon = Icons.Default.Info,
                    contentDescription = "Info",
                    onClick = { /* Open Bot Strategy Information */ }
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 100.dp, start = 16.dp, end = 16.dp)
            ) {
                // 2. Bot Status Card
                item {
                    BotStatusCard(
                        botStatus = uiState.botStatus,
                        config = config,
                        sessionDuration = uiState.sessionDurationFormatted
                    )
                }

                // 3. Start / Stop Control Button
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    if (isBotActive) {
                        SecondaryButton(
                            text = "STOP TRADING BOT",
                            onClick = { viewModel.onRequestStopBot() }
                        )
                    } else {
                        PrimaryButton(
                            text = "START TRADING BOT",
                            onClick = { viewModel.onRequestStartBot() },
                            buttonColor = ElectricLime,
                            isLoading = uiState.isLoading
                        )
                    }
                }

                // 4. Trading Pair Selector
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "TARGET ASSET PAIR",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(SurfaceDarkCard)
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = config.asset.symbol,
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${config.asset.name} • ${Formatters.formatCurrency(config.asset.currentPrice, uiState.currency)}",
                                color = TextMuted,
                                fontSize = 12.sp
                            )
                        }

                        SecondaryButton(
                            text = "Change Pair",
                            onClick = { viewModel.openAssetPicker() },
                            modifier = Modifier.padding(start = 12.dp)
                        )
                    }
                }

                // 5. Strategy Preset Selector
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                    BotStrategySelector(
                        selectedStrategy = config.strategy,
                        onStrategySelected = { viewModel.selectStrategy(it) }
                    )
                }

                // 6. Custom Parameters (if CUSTOM strategy)
                if (config.strategy == BotStrategy.CUSTOM) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        BotCustomParametersCard(
                            config = config,
                            onParametersChanged = { entry, stopLoss, takeProfit, maxTrades, duration ->
                                viewModel.updateCustomParameters(entry, stopLoss, takeProfit, maxTrades, duration)
                            }
                        )
                    }
                }

                // 7. Allocation Section
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "BOT ALLOCATION",
                            color = TextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Available: ${Formatters.formatCurrency(uiState.availableBalance, uiState.currency)}",
                            color = ElectricLime,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = config.allocatedAmount.toString(),
                        onValueChange = { viewModel.updateAllocatedAmount(it) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        prefix = {
                            Text(
                                text = "${uiState.currency.symbol} ",
                                color = ElectricLime,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = SurfaceDarkCard,
                            unfocusedContainerColor = SurfaceDarkCard,
                            focusedBorderColor = ElectricLime,
                            unfocusedBorderColor = SurfaceBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )

                    if (uiState.validationError != null) {
                        Text(
                            text = uiState.validationError ?: "",
                            color = FinancialRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    PercentageShortcuts(
                        selectedPercentage = uiState.selectedPercentage,
                        onPercentageSelected = { viewModel.applyPercentageShortcut(it) }
                    )
                }

                // 8. Risk Selector
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                    BotRiskSelector(
                        selectedRisk = config.riskLevel,
                        onRiskSelected = { viewModel.selectRiskLevel(it) }
                    )
                }

                // 9. Performance Card
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                    BotPerformanceCard(performance = uiState.performance)
                }

                // 10. Active Bot Positions
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                    BotActiveTradesList(trades = uiState.activeTrades)
                }

                // 11. Activity History
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                    BotActivityHistoryList(events = uiState.activityHistory)
                }
            }
        }

        // Asset Selector Sheet
        if (uiState.isAssetPickerOpen) {
            AssetSelectorSheet(
                assets = defaultAssetList,
                selectedAsset = config.asset,
                onAssetSelected = { viewModel.selectAsset(it) },
                onDismiss = { viewModel.closeAssetPicker() }
            )
        }

        // Start Bot Confirmation Sheet
        if (uiState.isStartConfirmSheetOpen) {
            ConfirmationSheet(
                title = "Start Nova Trading Bot?",
                details = listOf(
                    "Target Pair" to config.asset.symbol,
                    "Strategy" to config.strategy.title,
                    "Allocation" to Formatters.formatCurrency(config.allocatedAmount, uiState.currency),
                    "Risk Parameter" to config.riskLevel.title,
                    "Max Duration" to "${config.maxSessionDurationHours} Hours",
                    "Risk Disclaimer" to "Trading involves financial exposure"
                ),
                confirmButtonText = "Confirm & Start Bot",
                isLoading = uiState.isLoading,
                onConfirm = { viewModel.confirmStartBot() },
                onDismiss = { viewModel.closeStartConfirmSheet() }
            )
        }

        // Stop Bot Confirmation Sheet
        if (uiState.isStopConfirmSheetOpen) {
            ConfirmationSheet(
                title = "Stop Trading Bot?",
                details = listOf(
                    "Bot Name" to "Nova Bot",
                    "Active Pair" to config.asset.symbol,
                    "Current Session" to uiState.sessionDurationFormatted,
                    "Action" to "Close active session & return allocation"
                ),
                confirmButtonText = "Stop Bot",
                isLoading = uiState.isLoading,
                onConfirm = { viewModel.confirmStopBot() },
                onDismiss = { viewModel.closeStopConfirmSheet() }
            )
        }
    }
}
