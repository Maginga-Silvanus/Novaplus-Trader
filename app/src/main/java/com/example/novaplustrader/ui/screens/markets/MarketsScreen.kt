package com.example.novaplustrader.ui.screens.markets

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.novaplustrader.ui.components.AccessibleDropdownSelector
import com.example.novaplustrader.domain.model.trading.MarketRanking
import com.example.novaplustrader.domain.model.trading.MarketRankings
import com.example.novaplustrader.domain.model.trading.MarketSummary
import com.example.novaplustrader.ui.screens.trading.TradingViewModel
import com.example.novaplustrader.ui.screens.trading.models.TradingAsset
import com.example.novaplustrader.ui.theme.BackgroundDark
import com.example.novaplustrader.ui.theme.ElectricLime
import com.example.novaplustrader.ui.theme.ElectricLimeSurface
import com.example.novaplustrader.ui.theme.FinancialGreen
import com.example.novaplustrader.ui.theme.FinancialRed
import com.example.novaplustrader.ui.theme.SurfaceBorder
import com.example.novaplustrader.ui.theme.SurfaceDarkCard
import com.example.novaplustrader.ui.theme.TextMuted
import com.example.novaplustrader.ui.theme.TextPrimary
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketsScreen(
    tradingViewModel: TradingViewModel,
    onNavigateToTrade: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by tradingViewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedRanking by remember { mutableStateOf(MarketRanking.HOT) }
    val rankings = MarketRanking.entries
    val filteredMarkets = remember(state.marketSummaries, searchQuery, selectedRanking) {
        val searched = state.marketSummaries.filter { market ->
            searchQuery.isBlank() ||
                market.name.contains(searchQuery, ignoreCase = true) ||
                market.baseAsset.contains(searchQuery, ignoreCase = true) ||
                market.pair.contains(searchQuery, ignoreCase = true)
        }
        MarketRankings.sort(searched, selectedRanking)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Spot Crypto Markets", color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text("Binance · live supported USDT pairs", color = TextMuted, fontSize = 12.sp)
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(ElectricLimeSurface)
                    .clickable { onNavigateToTrade() }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ShowChart,
                        contentDescription = "Trade",
                        tint = ElectricLime,
                        modifier = Modifier.height(16.dp)
                    )
                    Text("TRADE", color = ElectricLime, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp, start = 16.dp, end = 16.dp)
        ) {
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    placeholder = { Text("Search symbol or asset...", color = TextMuted) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SurfaceDarkCard,
                        unfocusedContainerColor = SurfaceDarkCard,
                        focusedBorderColor = ElectricLime,
                        unfocusedBorderColor = SurfaceBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    singleLine = true
                )
            }
            item {
                Spacer(Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "MARKET RANKING",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    AccessibleDropdownSelector(
                        label = "Market ranking",
                        options = rankings,
                        selectedOption = selectedRanking,
                        optionLabel = { it.label },
                        onOptionSelected = { selectedRanking = it },
                        modifier = Modifier.width(160.dp)
                    )
                }
                Spacer(Modifier.height(16.dp))
            }
            if (filteredMarkets.isEmpty()) {
                item {
                    Text(
                        text = if (state.marketIsLoading) "Loading live market data…" else "No supported market pairs match this search.",
                        color = TextMuted,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(vertical = 18.dp)
                    )
                }
            } else {
                items(filteredMarkets, key = { it.symbol }) { market ->
                    MarketRow(
                        market = market,
                        onClick = {
                            tradingViewModel.selectAsset(market.toTradingAsset())
                            onNavigateToTrade()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun MarketRow(market: MarketSummary, onClick: () -> Unit) {
    val positive = market.priceChangePercent24h >= 0.0
    val color = if (positive) FinancialGreen else FinancialRed
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceDarkCard)
            .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(market.pair, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text(market.name, color = TextMuted, fontSize = 11.sp)
            Text(
                "Vol ${compactVolume(market.quoteVolume24h)} USDT",
                color = TextMuted,
                fontSize = 10.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("${market.lastPrice.priceString()} USDT", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(
                String.format(Locale.US, "%+.2f%%", market.priceChangePercent24h),
                color = color,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

private fun MarketSummary.toTradingAsset() = TradingAsset(
    symbol = pair,
    name = name,
    currentPrice = lastPrice,
    change24hPercentage = priceChangePercent24h,
    high24h = highPrice24h,
    low24h = lowPrice24h,
    volume24h = compactVolume(quoteVolume24h),
    candleDataMap = emptyMap()
)

private fun Double.priceString(): String = String.format(Locale.US, "%.8f", this).trimEnd('0').trimEnd('.')

private fun compactVolume(volume: Double): String = when {
    volume >= 1_000_000_000 -> String.format(Locale.US, "%.2fB", volume / 1_000_000_000)
    volume >= 1_000_000 -> String.format(Locale.US, "%.2fM", volume / 1_000_000)
    volume >= 1_000 -> String.format(Locale.US, "%.2fK", volume / 1_000)
    else -> String.format(Locale.US, "%.2f", volume)
}
