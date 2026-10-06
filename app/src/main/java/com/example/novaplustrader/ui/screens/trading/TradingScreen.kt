package com.example.novaplustrader.ui.screens.trading

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.text.KeyboardOptions
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
import com.example.novaplustrader.ui.components.PrimaryButton
import com.example.novaplustrader.ui.components.SuccessAnimation
import com.example.novaplustrader.ui.screens.trading.components.AssetSelectorSheet
import com.example.novaplustrader.ui.screens.trading.components.BuySellSegmentedControl
import com.example.novaplustrader.ui.screens.trading.components.EnhancedTradingChart
import com.example.novaplustrader.ui.screens.trading.components.PercentageShortcuts
import com.example.novaplustrader.ui.screens.trading.components.TimeframeSelector
import com.example.novaplustrader.ui.screens.trading.components.TradeDurationSelector
import com.example.novaplustrader.ui.screens.trading.components.TradingHeaderBar
import com.example.novaplustrader.ui.screens.trading.models.TradeDirection
import com.example.novaplustrader.ui.theme.BackgroundDark
import com.example.novaplustrader.ui.theme.ElectricLime
import com.example.novaplustrader.ui.theme.FinancialGreen
import com.example.novaplustrader.ui.theme.FinancialRed
import com.example.novaplustrader.ui.theme.OverlayDark
import com.example.novaplustrader.ui.theme.SurfaceBorder
import com.example.novaplustrader.ui.theme.SurfaceDarkCard
import com.example.novaplustrader.ui.theme.TextMuted
import com.example.novaplustrader.ui.theme.TextPrimary
import com.example.novaplustrader.ui.util.Formatters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TradingScreen(
    viewModel: TradingViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val asset = uiState.selectedAsset
    val chartPoints = asset.chartDataMap[uiState.selectedTimeframe] ?: emptyList()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TradingHeaderBar(
                assetSymbol = asset.symbol,
                onBackClick = onBackClick,
                onAssetSelectorClick = { viewModel.openAssetPicker() },
                onMoreOptionsClick = { /* More options handler */ }
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 100.dp, start = 16.dp, end = 16.dp)
            ) {
                // 1. Asset Price & 24h Stats Header
                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = Formatters.formatCurrency(asset.currentPrice, uiState.currency),
                                    color = TextPrimary,
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = (-0.5).sp
                                )
                                Text(
                                    text = "${asset.name} Live Price",
                                    color = TextMuted,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Normal
                                )
                            }

                            val changeColor = if (asset.change24hPercentage >= 0) FinancialGreen else FinancialRed
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(changeColor.copy(alpha = 0.15f))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = Formatters.formatPercentage(asset.change24hPercentage),
                                    color = changeColor,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // 24h High, Low, Volume Stats Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(SurfaceDarkCard)
                                .border(1.dp, SurfaceBorder, RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(text = "24h High", color = TextMuted, fontSize = 10.sp)
                                Text(
                                    text = Formatters.formatCurrency(asset.high24h, uiState.currency),
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Column {
                                Text(text = "24h Low", color = TextMuted, fontSize = 10.sp)
                                Text(
                                    text = Formatters.formatCurrency(asset.low24h, uiState.currency),
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Column {
                                Text(text = "24h Volume", color = TextMuted, fontSize = 10.sp)
                                Text(
                                    text = asset.volume24h,
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // 2. Timeframe Selector & Chart
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    TimeframeSelector(
                        selectedTimeframe = uiState.selectedTimeframe,
                        onTimeframeSelected = { viewModel.selectTimeframe(it) }
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    EnhancedTradingChart(
                        dataPoints = chartPoints,
                        currentPrice = asset.currentPrice,
                        isPositiveChange = asset.change24hPercentage >= 0
                    )
                }

                // 3. BUY / SELL Direction Control
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                    BuySellSegmentedControl(
                        selectedDirection = uiState.selectedDirection,
                        onDirectionSelected = { viewModel.selectDirection(it) }
                    )
                }

                // 4. Order Entry: Balance, Amount, Percentage Shortcuts
                item {
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ORDER AMOUNT",
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
                        value = uiState.tradeAmountString,
                        onValueChange = { viewModel.updateTradeAmount(it) },
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

                // 5. Trade Duration
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    TradeDurationSelector(
                        selectedDuration = uiState.selectedDuration,
                        onDurationSelected = { viewModel.selectDuration(it) }
                    )
                }

                // 6. Trade Summary Card & Submit Button
                item {
                    Spacer(modifier = Modifier.height(20.dp))

                    val tradeAmount = uiState.tradeAmountString.toDoubleOrNull() ?: 0.0
                    val payoutAmount = tradeAmount * (1 + (asset.payoutPercentage / 100.0))
                    val balanceAfter = (uiState.availableBalance - tradeAmount).coerceAtLeast(0.0)

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(SurfaceDarkCard)
                            .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Direction & Pair", color = TextMuted, fontSize = 12.sp)
                            val dirColor = if (uiState.selectedDirection == TradeDirection.BUY) FinancialGreen else FinancialRed
                            Text(
                                text = "${uiState.selectedDirection.label} ${asset.symbol}",
                                color = dirColor,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Est. Return (${asset.payoutPercentage}%)", color = TextMuted, fontSize = 12.sp)
                            Text(
                                text = "+${Formatters.formatCurrency(payoutAmount, uiState.currency)}",
                                color = FinancialGreen,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Balance After Trade", color = TextMuted, fontSize = 12.sp)
                            Text(
                                text = Formatters.formatCurrency(balanceAfter, uiState.currency),
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val buttonColor = if (uiState.selectedDirection == TradeDirection.BUY) FinancialGreen else FinancialRed
                    PrimaryButton(
                        text = "EXECUTE ${uiState.selectedDirection.label} (${asset.symbol})",
                        onClick = { viewModel.openConfirmSheet() },
                        buttonColor = buttonColor
                    )
                }
            }
        }

        // Modals & Sheets
        if (uiState.isAssetPickerOpen) {
            AssetSelectorSheet(
                assets = uiState.availableAssets,
                selectedAsset = asset,
                onAssetSelected = { viewModel.selectAsset(it) },
                onDismiss = { viewModel.closeAssetPicker() }
            )
        }

        if (uiState.isConfirmSheetOpen) {
            val tradeAmount = uiState.tradeAmountString.toDoubleOrNull() ?: 0.0
            val payoutAmount = tradeAmount * (1 + (asset.payoutPercentage / 100.0))
            val balanceAfter = (uiState.availableBalance - tradeAmount).coerceAtLeast(0.0)

            ConfirmationSheet(
                title = "Confirm Binary Trade",
                details = listOf(
                    "Asset Pair" to asset.symbol,
                    "Direction" to uiState.selectedDirection.label,
                    "Trade Amount" to Formatters.formatCurrency(tradeAmount, uiState.currency),
                    "Entry Price" to Formatters.formatCurrency(asset.currentPrice, uiState.currency),
                    "Duration" to uiState.selectedDuration.label,
                    "Potential Return" to "+${Formatters.formatCurrency(payoutAmount, uiState.currency)}",
                    "Balance After" to Formatters.formatCurrency(balanceAfter, uiState.currency)
                ),
                confirmButtonText = "Confirm Trade",
                isLoading = uiState.isLoading,
                onConfirm = { viewModel.executeTrade() },
                onDismiss = { viewModel.closeConfirmSheet() }
            )
        }

        AnimatedVisibility(
            visible = uiState.isSuccessDialogOpen,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(OverlayDark)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                SuccessAnimation(
                    title = "Trade Order Executed!",
                    subtitle = "Your ${uiState.selectedDirection.label} position on ${asset.symbol} is active for ${uiState.selectedDuration.label}.",
                    onDoneClick = { viewModel.dismissSuccessDialog() }
                )
            }
        }
    }
}
