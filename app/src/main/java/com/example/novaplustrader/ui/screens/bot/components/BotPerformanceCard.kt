package com.example.novaplustrader.ui.screens.bot.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.novaplustrader.ui.model.Currency
import com.example.novaplustrader.ui.screens.bot.models.BotPerformance
import com.example.novaplustrader.ui.theme.ElectricLime
import com.example.novaplustrader.ui.theme.ElectricLimeSurface
import com.example.novaplustrader.ui.theme.FinancialGreen
import com.example.novaplustrader.ui.theme.FinancialRed
import com.example.novaplustrader.ui.theme.SurfaceBorder
import com.example.novaplustrader.ui.theme.SurfaceDarkCard
import com.example.novaplustrader.ui.theme.TextMuted
import com.example.novaplustrader.ui.theme.TextPrimary
import com.example.novaplustrader.ui.util.Formatters

@Composable
fun BotPerformanceCard(
    performance: BotPerformance,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceDarkCard)
            .border(1.dp, SurfaceBorder, RoundedCornerShape(20.dp))
            .padding(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "BOT PERFORMANCE",
                color = TextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            if (performance.isMockData) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(ElectricLimeSurface)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "PREVIEW DATA",
                        color = ElectricLime,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "SESSION P/L", color = TextMuted, fontSize = 10.sp)
                val plColor = if (performance.sessionProfitLoss >= 0) FinancialGreen else FinancialRed
                val prefix = if (performance.sessionProfitLoss >= 0) "+" else ""
                Text(
                    text = "$prefix${Formatters.formatCurrency(performance.sessionProfitLoss, Currency.USD)}",
                    color = plColor,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(text = "WIN RATE", color = TextMuted, fontSize = 10.sp)
                Text(
                    text = "${performance.winRatePercentage}%",
                    color = ElectricLime,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = "Total Trades", color = TextMuted, fontSize = 11.sp)
                Text(text = "${performance.totalTrades}", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
            Column {
                Text(text = "Winning", color = TextMuted, fontSize = 11.sp)
                Text(text = "${performance.winningTrades}", color = FinancialGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
            Column {
                Text(text = "Losing", color = TextMuted, fontSize = 11.sp)
                Text(text = "${performance.losingTrades}", color = FinancialRed, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
            Column {
                Text(text = "Exposure", color = TextMuted, fontSize = 11.sp)
                Text(text = Formatters.formatCurrency(performance.currentExposure, Currency.USD), color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
