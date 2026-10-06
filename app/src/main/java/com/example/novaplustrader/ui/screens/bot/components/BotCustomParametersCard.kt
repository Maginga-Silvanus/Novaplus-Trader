package com.example.novaplustrader.ui.screens.bot.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.novaplustrader.ui.screens.bot.models.BotConfiguration
import com.example.novaplustrader.ui.theme.ElectricLime
import com.example.novaplustrader.ui.theme.SurfaceBorder
import com.example.novaplustrader.ui.theme.SurfaceDarkCard
import com.example.novaplustrader.ui.theme.TextMuted
import com.example.novaplustrader.ui.theme.TextPrimary

@Composable
fun BotCustomParametersCard(
    config: BotConfiguration,
    onParametersChanged: (entry: Double, stopLoss: Double, takeProfit: Double, maxTrades: Int, durationHours: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var entryText by remember(config.entryThresholdPercent) { mutableStateOf(config.entryThresholdPercent.toString()) }
    var stopLossText by remember(config.stopLossPercent) { mutableStateOf(config.stopLossPercent.toString()) }
    var takeProfitText by remember(config.takeProfitPercent) { mutableStateOf(config.takeProfitPercent.toString()) }
    var maxTradesText by remember(config.maxOpenTrades) { mutableStateOf(config.maxOpenTrades.toString()) }
    var durationText by remember(config.maxSessionDurationHours) { mutableStateOf(config.maxSessionDurationHours.toString()) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceDarkCard)
            .border(1.dp, SurfaceBorder, RoundedCornerShape(20.dp))
            .padding(18.dp)
    ) {
        Text(
            text = "CUSTOM PARAMETERS",
            color = TextMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = entryText,
                onValueChange = {
                    entryText = it
                    onParametersChanged(
                        it.toDoubleOrNull() ?: config.entryThresholdPercent,
                        stopLossText.toDoubleOrNull() ?: config.stopLossPercent,
                        takeProfitText.toDoubleOrNull() ?: config.takeProfitPercent,
                        maxTradesText.toIntOrNull() ?: config.maxOpenTrades,
                        durationText.toIntOrNull() ?: config.maxSessionDurationHours
                    )
                },
                label = { Text("Entry %", fontSize = 11.sp, color = TextMuted) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricLime,
                    unfocusedBorderColor = SurfaceBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            OutlinedTextField(
                value = stopLossText,
                onValueChange = {
                    stopLossText = it
                    onParametersChanged(
                        entryText.toDoubleOrNull() ?: config.entryThresholdPercent,
                        it.toDoubleOrNull() ?: config.stopLossPercent,
                        takeProfitText.toDoubleOrNull() ?: config.takeProfitPercent,
                        maxTradesText.toIntOrNull() ?: config.maxOpenTrades,
                        durationText.toIntOrNull() ?: config.maxSessionDurationHours
                    )
                },
                label = { Text("Stop Loss %", fontSize = 11.sp, color = TextMuted) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricLime,
                    unfocusedBorderColor = SurfaceBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            OutlinedTextField(
                value = takeProfitText,
                onValueChange = {
                    takeProfitText = it
                    onParametersChanged(
                        entryText.toDoubleOrNull() ?: config.entryThresholdPercent,
                        stopLossText.toDoubleOrNull() ?: config.stopLossPercent,
                        it.toDoubleOrNull() ?: config.takeProfitPercent,
                        maxTradesText.toIntOrNull() ?: config.maxOpenTrades,
                        durationText.toIntOrNull() ?: config.maxSessionDurationHours
                    )
                },
                label = { Text("Take Profit %", fontSize = 11.sp, color = TextMuted) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricLime,
                    unfocusedBorderColor = SurfaceBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = maxTradesText,
                onValueChange = {
                    maxTradesText = it
                    onParametersChanged(
                        entryText.toDoubleOrNull() ?: config.entryThresholdPercent,
                        stopLossText.toDoubleOrNull() ?: config.stopLossPercent,
                        takeProfitText.toDoubleOrNull() ?: config.takeProfitPercent,
                        it.toIntOrNull() ?: config.maxOpenTrades,
                        durationText.toIntOrNull() ?: config.maxSessionDurationHours
                    )
                },
                label = { Text("Max Trades", fontSize = 11.sp, color = TextMuted) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricLime,
                    unfocusedBorderColor = SurfaceBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            OutlinedTextField(
                value = durationText,
                onValueChange = {
                    durationText = it
                    onParametersChanged(
                        entryText.toDoubleOrNull() ?: config.entryThresholdPercent,
                        stopLossText.toDoubleOrNull() ?: config.stopLossPercent,
                        takeProfitText.toDoubleOrNull() ?: config.takeProfitPercent,
                        maxTradesText.toIntOrNull() ?: config.maxOpenTrades,
                        it.toIntOrNull() ?: config.maxSessionDurationHours
                    )
                },
                label = { Text("Max Duration (h)", fontSize = 11.sp, color = TextMuted) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricLime,
                    unfocusedBorderColor = SurfaceBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
        }
    }
}
