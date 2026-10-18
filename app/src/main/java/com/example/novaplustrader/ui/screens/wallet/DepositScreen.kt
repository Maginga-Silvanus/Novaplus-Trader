package com.example.novaplustrader.ui.screens.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.CurrencyBitcoin
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.novaplustrader.ui.components.IconActionButton
import com.example.novaplustrader.ui.model.Currency
import com.example.novaplustrader.ui.screens.wallet.models.DepositMethod
import com.example.novaplustrader.ui.theme.BackgroundDark
import com.example.novaplustrader.ui.theme.ElectricLime
import com.example.novaplustrader.ui.theme.SurfaceBorder
import com.example.novaplustrader.ui.theme.SurfaceDarkCard
import com.example.novaplustrader.ui.theme.TextMuted
import com.example.novaplustrader.ui.theme.TextPrimary
import com.example.novaplustrader.ui.util.Formatters

@Composable
fun DepositScreen(
    viewModel: WalletViewModel,
    onBackClick: () -> Unit,
    onMpesaClick: () -> Unit,
    onPaybillClick: () -> Unit,
    onBankClick: () -> Unit,
    onCardClick: () -> Unit,
    onCryptoClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val methods = listOf(
        Triple(DepositMethod.MPESA, Icons.Default.PhoneAndroid, onMpesaClick),
        Triple(DepositMethod.PAYBILL, Icons.Default.ReceiptLong, onPaybillClick),
        Triple(DepositMethod.BANK, Icons.Default.AccountBalance, onBankClick),
        Triple(DepositMethod.CARD, Icons.Default.CreditCard, onCardClick),
        Triple(DepositMethod.CRYPTO, Icons.Default.CurrencyBitcoin, onCryptoClick)
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconActionButton(
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                onClick = onBackClick
            )
            Column {
                Text("Deposit", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(
                    Formatters.formatCurrency(
                        if (state.selectedCurrency == Currency.USD) state.usdBalance else state.kesBalance,
                        state.selectedCurrency
                    ),
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    "CHOOSE A DEPOSIT METHOD",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }
            methods.forEach { (method, icon, onClick) ->
                item(key = method.name) {
                    DepositMethodCard(
                        title = method.title,
                        description = when (method) {
                            DepositMethod.MPESA -> "Continue to M-Pesa mobile deposit"
                            DepositMethod.PAYBILL -> "Continue to Paybill deposit"
                            DepositMethod.BANK -> "Continue to bank transfer"
                            DepositMethod.CARD -> "Continue to card deposit"
                            DepositMethod.CRYPTO -> "Continue to crypto deposit"
                        },
                        icon = icon,
                        onClick = {
                            viewModel.selectDepositMethod(method)
                            onClick()
                        }
                    )
                }
            }
            item {
                Text(
                    "A submitted request remains pending until its payment provider confirms it.",
                    color = TextMuted,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 8.dp, bottom = 20.dp)
                )
            }
        }
    }
}

@Composable
private fun DepositMethodCard(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceDarkCard)
            .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = ElectricLime)
        Column {
            Text(title, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(3.dp))
            Text(description, color = TextMuted, fontSize = 12.sp)
        }
    }
}
