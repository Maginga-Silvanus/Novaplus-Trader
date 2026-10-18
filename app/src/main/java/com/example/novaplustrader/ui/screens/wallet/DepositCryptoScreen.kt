package com.example.novaplustrader.ui.screens.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.novaplustrader.ui.components.IconActionButton
import com.example.novaplustrader.ui.components.PrimaryButton
import com.example.novaplustrader.ui.theme.BackgroundDark
import com.example.novaplustrader.ui.theme.ElectricLime
import com.example.novaplustrader.ui.theme.ElectricLimeSurface
import com.example.novaplustrader.ui.theme.SurfaceBorder
import com.example.novaplustrader.ui.theme.SurfaceDarkCard
import com.example.novaplustrader.ui.theme.TextMuted
import com.example.novaplustrader.ui.theme.TextPrimary

@Composable
fun DepositCryptoScreen(
    viewModel: WalletViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var amount by remember { mutableStateOf("") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconActionButton(icon = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", onClick = onBackClick)
                Text("Crypto Deposit", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.size(40.dp))
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(text = "Select Cryptocurrency Asset", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)

                val cryptoAssets = listOf("BTC", "ETH", "USDT", "USDC")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    cryptoAssets.forEach { asset ->
                        val isSelected = asset == uiState.selectedCryptoAsset
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) ElectricLimeSurface else SurfaceDarkCard)
                                .border(1.dp, if (isSelected) ElectricLime else SurfaceBorder, RoundedCornerShape(10.dp))
                                .clickable { viewModel.selectCryptoAsset(asset) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = asset, color = if (isSelected) ElectricLime else TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                val address = when (uiState.selectedCryptoAsset) {
                    "BTC" -> "bc1q9x2m48k5p7q0v3r1n8w6t2y4u9s8d3f7g1"
                    "ETH" -> "0x71C7656EC7ab88b098defB751B7401B5f6d8976F"
                    "USDT" -> "TX8m24N9v8q1W5x3P7z9R2k4M6y8u1D3f7"
                    else -> "0x89205A3A3b2A6AD3D042700D3e5423f01B5f6d8"
                }

                Text(text = "Vault Deposit Address (${uiState.selectedCryptoAsset}) [Working]", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(SurfaceDarkCard)
                        .border(1.dp, SurfaceBorder, RoundedCornerShape(10.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = address.take(16) + "..." + address.takeLast(6),
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "COPY",
                        color = ElectricLime,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text("Deposit Amount", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("0.00", color = TextMuted) },
                    shape = RoundedCornerShape(12.dp),
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

                Spacer(modifier = Modifier.height(20.dp))
                PrimaryButton(
                    text = "CONFIRM CRYPTO DEPOSIT",
                    onClick = {
                        viewModel.updateDepositAmount(amount)
                        viewModel.selectDepositMethod(com.example.novaplustrader.ui.screens.wallet.models.DepositMethod.CRYPTO)
                        viewModel.openDepositConfirmSheet()
                    }
                )
            }
        }
    }
}
