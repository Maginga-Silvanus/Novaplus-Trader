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
import com.example.novaplustrader.ui.screens.trading.components.TradingHeaderBar
import com.example.novaplustrader.ui.screens.trading.models.TradeDirection
import com.example.novaplustrader.ui.model.Currency
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
    val candles = asset.candleDataMap[uiState.selectedTimeframe].orEmpty()

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
                item {
                    Text(
                        text = when (uiState.walletMode) {
                            "paper" -> "PAPER TRADING · TEST FUNDS ONLY · No exchange orders or on-chain assets."
                            else -> "Trading is unavailable until the secure backend is configured."
                        },
                        color = if (uiState.walletMode == "paper") ElectricLime else FinancialRed,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 8.dp, bottom = 12.dp)
                    )
                }
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
                                    text = if (asset.currentPrice > 0.0) {
                                        String.format("%.8f USDT", asset.currentPrice)
                                    } else {
                                        "Loading live price"
                                    },
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
                                    text = if (uiState.marketDataTimestamp != null) {
                                        Formatters.formatPercentage(asset.change24hPercentage)
                                    } else {
                                        "--"
                                    },
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
                                    text = if (asset.high24h > 0.0) String.format("%.8f USDT", asset.high24h) else "--",
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Column {
                                Text(text = "24h Low", color = TextMuted, fontSize = 10.sp)
                                Text(
                                    text = if (asset.low24h > 0.0) String.format("%.8f USDT", asset.low24h) else "--",
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
                        candles = candles,
                        currentPrice = asset.currentPrice
                    )
                    Text(
                        text = when {
                            uiState.isMarketPriceStale && uiState.marketDataTimestamp == null ->
                                "Waiting for fresh market data · ${uiState.marketConnectionState.label}"
                            uiState.isMarketPriceStale -> "Market feed: ${uiState.marketConnectionState.label}"
                            uiState.marketDataTimestamp != null ->
                                "Live exchange data · updated ${uiState.marketDataTimestamp}"
                            else -> "Connecting to live market data…"
                        },
                        color = if (uiState.isMarketPriceStale) FinancialRed else TextMuted,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 8.dp, start = 4.dp)
                    )
                    uiState.marketDataError?.let { error ->
                        Text(
                            text = error,
                            color = FinancialRed,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                        )
                    }
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
                            text = if (uiState.selectedDirection == TradeDirection.BUY) {
                                "TEST USD available: ${Formatters.formatCurrency(uiState.availableBalance, uiState.currency)}"
                            } else {
                                val assetCode = asset.symbol.substringBefore("/")
                                "Available: ${String.format("%.8f", uiState.assetBalances[assetCode] ?: 0.0)} $assetCode"
                            },
                            color = ElectricLime,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (uiState.selectedDirection == TradeDirection.BUY) {
                        Text(
                            text = "Main User Wallet · TEST FUNDS (PAPER MONEY)",
                            color = TextMuted,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(top = 4.dp, start = 4.dp)
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
                                text = if (uiState.selectedDirection == TradeDirection.BUY) {
                                    "${uiState.currency.symbol} "
                                } else {
                                    "${asset.symbol.substringBefore("/")} "
                                },
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
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                    Text(
                        text = if (uiState.selectedDirection == TradeDirection.BUY) {
                            "Enter the TEST USD amount to spend. Simulated fee: ${String.format("%.2f", uiState.paperTradeFeeBps / 100.0)}%."
                        } else {
                            "Enter the ${asset.symbol.substringBefore("/")} quantity to sell from your paper holdings."
                        },
                        color = TextMuted,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 6.dp, start = 4.dp)
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

                // 5. Spot order summary and submit
                item {
                    Spacer(modifier = Modifier.height(20.dp))

                    val tradeAmount = uiState.tradeAmountString.toDoubleOrNull() ?: 0.0
                    val isBuy = uiState.selectedDirection == TradeDirection.BUY
                    val assetQuantity = if (isBuy && asset.currentPrice > 0) tradeAmount / asset.currentPrice else tradeAmount
                    val grossUsd = if (isBuy) tradeAmount else tradeAmount * asset.currentPrice
                    val feeUsd = kotlin.math.ceil(grossUsd * uiState.paperTradeFeeBps / 10_000.0 * 100.0) / 100.0
                    val balanceAfter = when (uiState.selectedDirection) {
                        TradeDirection.BUY -> (uiState.availableBalance - grossUsd - feeUsd).coerceAtLeast(0.0)
                        TradeDirection.SELL -> uiState.availableBalance + grossUsd - feeUsd
                    }

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
                            Text(text = if (isBuy) "Estimated ${asset.symbol.substringBefore("/")} quantity" else "Quantity sold", color = TextMuted, fontSize = 12.sp)
                            Text(
                                text = "${String.format("%.8f", assetQuantity)} ${asset.symbol.substringBefore("/")}",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Estimated paper fee", color = TextMuted, fontSize = 12.sp)
                            Text(text = Formatters.formatCurrency(feeUsd, uiState.currency), color = TextPrimary, fontSize = 13.sp)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "TEST USD balance after order", color = TextMuted, fontSize = 12.sp)
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
                        text = "PAPER ${uiState.selectedDirection.label} (${asset.symbol})",
                        onClick = { viewModel.openConfirmSheet() },
                        buttonColor = buttonColor
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Recent crypto trade requests",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (uiState.recentTradeRequests.isEmpty()) {
                        Text(
                            text = "No trade requests yet",
                            color = TextMuted,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
                        )
                    } else {
                        Column(
                            modifier = Modifier.padding(top = 8.dp, bottom = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            uiState.recentTradeRequests.take(10).forEach { request ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${request.direction} ${request.asset} · ${String.format("%.8f", request.quantity)} ${request.asset}",
                                        color = TextPrimary,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = request.status,
                                        color = if (request.status == "FILLED_TEST") FinancialGreen else TextMuted,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                request.message?.let { message ->
                                    Text(text = message, color = TextMuted, fontSize = 10.sp)
                                }
                            }
                        }
                    }
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
            val isBuy = uiState.selectedDirection == TradeDirection.BUY
            val assetQuantity = if (isBuy && asset.currentPrice > 0) tradeAmount / asset.currentPrice else tradeAmount
            val grossUsd = if (isBuy) tradeAmount else tradeAmount * asset.currentPrice
            val feeUsd = kotlin.math.ceil(grossUsd * uiState.paperTradeFeeBps / 10_000.0 * 100.0) / 100.0
            val balanceAfter = when (uiState.selectedDirection) {
                TradeDirection.BUY -> (uiState.availableBalance - grossUsd - feeUsd).coerceAtLeast(0.0)
                TradeDirection.SELL -> uiState.availableBalance + grossUsd - feeUsd
            }

            ConfirmationSheet(
                title = "Confirm SIMULATED TRADE",
                details = listOf(
                    "Asset Pair" to asset.symbol,
                    "Direction" to uiState.selectedDirection.label,
                    "Order Value" to Formatters.formatCurrency(grossUsd, uiState.currency),
                    "Exchange Rate" to "1 ${asset.symbol.substringBefore("/")} = ${String.format("%.8f USDT", asset.currentPrice)}",
                    "Estimated Quantity" to "${String.format("%.8f", assetQuantity)} ${asset.symbol.substringBefore("/")}",
                    "Estimated TEST fee" to Formatters.formatCurrency(feeUsd, uiState.currency),
                    if (isBuy) "Estimated Total TEST Cost" to Formatters.formatCurrency(grossUsd + feeUsd, uiState.currency)
                    else "Estimated Net TEST Proceeds" to Formatters.formatCurrency(grossUsd - feeUsd, uiState.currency),
                    "TEST USD Balance After" to Formatters.formatCurrency(balanceAfter, uiState.currency)
                ),
                confirmButtonText = "Confirm PAPER TRADE",
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
                    title = "SIMULATED TRADE RECEIPT",
                    subtitle = uiState.tradeSuccessMessage
                        ?: "Paper order for ${asset.symbol} completed. No real funds or assets were exchanged.",
                    onDoneClick = { viewModel.dismissSuccessDialog() }
                )
            }
        }
    }
}
