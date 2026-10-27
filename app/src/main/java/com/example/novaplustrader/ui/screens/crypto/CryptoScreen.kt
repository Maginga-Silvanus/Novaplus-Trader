package com.example.novaplustrader.ui.screens.crypto

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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CurrencyBitcoin
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.novaplustrader.data.repository.MainUserWalletRepositoryImpl
import com.example.novaplustrader.data.repository.MainUserWalletSnapshot
import com.example.novaplustrader.ui.components.AccessibleDropdownSelector
import com.example.novaplustrader.ui.model.Currency
import com.example.novaplustrader.ui.theme.BackgroundDark
import com.example.novaplustrader.ui.theme.ElectricLime
import com.example.novaplustrader.ui.theme.ElectricLimeSurface
import com.example.novaplustrader.ui.theme.FinancialGreen
import com.example.novaplustrader.ui.theme.FinancialRed
import com.example.novaplustrader.ui.theme.SurfaceBorder
import com.example.novaplustrader.ui.theme.SurfaceDarkCard
import com.example.novaplustrader.ui.theme.TextMuted
import com.example.novaplustrader.ui.theme.TextPrimary
import com.example.novaplustrader.ui.util.Formatters
import com.google.firebase.auth.FirebaseAuth
import java.util.Locale

data class CryptoTokenItem(
    val symbol: String,
    val name: String,
    val priceUsd: Double,
    val change24h: Double,
    val iconEmoji: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CryptoScreen(
    onNavigateToReceive: () -> Unit,
    onNavigateToSend: () -> Unit,
    onNavigateToTrade: () -> Unit,
    onNavigateToBuyCrypto: () -> Unit,
    modifier: Modifier = Modifier
) {
    val auth = remember { FirebaseAuth.getInstance() }
    val userId = auth.currentUser?.uid.orEmpty()
    val mainWalletRepo = remember { MainUserWalletRepositoryImpl() }
    val walletSnapshot by mainWalletRepo.observeWallet(userId).collectAsState(
        initial = MainUserWalletSnapshot()
    )

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("Today") }
    val timeFilters = listOf("Today", "This week", "This month", "This year")

    val tokens = listOf(
        CryptoTokenItem("BTC", "Bitcoin", 65420.00, 3.42, "₿"),
        CryptoTokenItem("ETH", "Ethereum", 3480.50, 2.15, "Ξ"),
        CryptoTokenItem("SOL", "Solana", 182.40, 6.80, "◎"),
        CryptoTokenItem("USDC", "USD Coin", 1.00, 0.01, "$"),
        CryptoTokenItem("BNB", "BNB", 590.20, -0.85, "B"),
        CryptoTokenItem("ADA", "Cardano", 0.45, 1.20, "₳"),
        CryptoTokenItem("XRP", "XRP", 0.58, 4.10, "✕")
    )

    val filteredTokens = remember(searchQuery, tokens) {
        tokens.filter {
            searchQuery.isBlank() || it.name.contains(searchQuery, ignoreCase = true) || it.symbol.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Crypto & Market Overview", color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text("Single Main Wallet Architecture", color = TextMuted, fontSize = 12.sp)
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(ElectricLimeSurface)
                    .clickable { onNavigateToTrade() }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text("SPOT HUB", color = ElectricLime, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp, start = 16.dp, end = 16.dp)
        ) {
            // Main Wallet Balance Card
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(SurfaceDarkCard)
                        .border(1.dp, SurfaceBorder, RoundedCornerShape(22.dp))
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.CurrencyBitcoin, contentDescription = "Wallet", tint = ElectricLime, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Main Wallet Balance", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            "Single User Wallet",
                            color = ElectricLime,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text("Total Available Balance", color = TextMuted, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = Formatters.formatCurrency(walletSnapshot.usdBalance, Currency.USD),
                        color = TextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Unified balance used across Trading, Casino, Sports, Send & Receive.",
                        color = TextMuted,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Quick Actions: Receive, Transfer, Exchange, Purchase
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        CryptoQuickActionButton(
                            label = "Receive",
                            icon = Icons.Default.Download,
                            onClick = onNavigateToReceive
                        )
                        CryptoQuickActionButton(
                            label = "Transfer",
                            icon = Icons.AutoMirrored.Filled.Send,
                            onClick = onNavigateToSend
                        )
                        CryptoQuickActionButton(
                            label = "Exchange",
                            icon = Icons.Default.SwapHoriz,
                            onClick = onNavigateToTrade
                        )
                        CryptoQuickActionButton(
                            label = "Purchase",
                            icon = Icons.Default.CurrencyBitcoin,
                            onClick = onNavigateToBuyCrypto
                        )
                    }
                }
            }

            // Search Token / Discover New Assets
            item {
                Spacer(modifier = Modifier.height(20.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    placeholder = { Text("Search token or discover new assets...", color = TextMuted) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = TextMuted) },
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

            // Trending Tokens & Clickable Time Filter
            item {
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TRENDING TOKENS",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    AccessibleDropdownSelector(
                        label = "Trending time range",
                        options = timeFilters,
                        selectedOption = selectedFilter,
                        optionLabel = { it },
                        onOptionSelected = { selectedFilter = it },
                        modifier = Modifier.width(150.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Spacer(modifier = Modifier.height(12.dp))
            }

            // Token Cards (6 to 7 tokens)
            items(filteredTokens, key = { it.symbol }) { token ->
                val isPositive = token.change24h >= 0.0
                val changeColor = if (isPositive) FinancialGreen else FinancialRed

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceDarkCard)
                        .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
                        .clickable { onNavigateToTrade() }
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(ElectricLimeSurface),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = token.iconEmoji, color = ElectricLime, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }

                        Column {
                            Text(text = token.name, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Text(text = token.symbol, color = TextMuted, fontSize = 11.sp)
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "$${String.format(Locale.US, "%.2f", token.priceUsd)}", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = String.format(Locale.US, "%+.2f%%", token.change24h),
                            color = changeColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CryptoQuickActionButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(ElectricLimeSurface)
                .border(1.dp, ElectricLime.copy(alpha = 0.4f), RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = ElectricLime, modifier = Modifier.size(22.dp))
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = label, color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}
