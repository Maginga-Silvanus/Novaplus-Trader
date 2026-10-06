package com.example.novaplustrader.ui.screens.trading.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.novaplustrader.ui.model.Currency
import com.example.novaplustrader.ui.theme.BackgroundDark
import com.example.novaplustrader.ui.theme.ElectricLime
import com.example.novaplustrader.ui.theme.FinancialGreen
import com.example.novaplustrader.ui.theme.FinancialRed
import com.example.novaplustrader.ui.theme.SurfaceBorder
import com.example.novaplustrader.ui.theme.SurfaceDarkCard
import com.example.novaplustrader.ui.theme.TextMuted
import com.example.novaplustrader.ui.util.Formatters

@Composable
fun EnhancedTradingChart(
    dataPoints: List<Float>,
    currentPrice: Double,
    isPositiveChange: Boolean,
    modifier: Modifier = Modifier,
    lineColor: Color = if (isPositiveChange) FinancialGreen else FinancialRed
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceDarkCard)
            .border(1.dp, SurfaceBorder, RoundedCornerShape(20.dp))
            .padding(12.dp)
    ) {
        if (dataPoints.size < 2) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Loading chart data...",
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }
            return@Box
        }

        val minVal = dataPoints.minOrNull() ?: 0f
        val maxVal = dataPoints.maxOrNull() ?: 1f
        val range = if (maxVal - minVal == 0f) 1f else maxVal - minVal

        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // 1. Draw horizontal grid lines
            val gridColor = SurfaceBorder.copy(alpha = 0.5f)
            val pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)

            for (i in 1..3) {
                val y = height * (i / 4f)
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = pathEffect
                )
            }

            // 2. Draw price path & fill gradient
            val strokePath = Path()
            val fillPath = Path()
            val stepX = width / (dataPoints.size - 1)

            dataPoints.forEachIndexed { index, point ->
                val x = index * stepX
                val y = height - ((point - minVal) / range * (height - 20.dp.toPx())) - 10.dp.toPx()

                if (index == 0) {
                    strokePath.moveTo(x, y)
                    fillPath.moveTo(x, height)
                    fillPath.lineTo(x, y)
                } else {
                    strokePath.lineTo(x, y)
                    fillPath.lineTo(x, y)
                }

                if (index == dataPoints.size - 1) {
                    fillPath.lineTo(x, height)
                    fillPath.close()
                }
            }

            // Gradient area fill
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(lineColor.copy(alpha = 0.25f), Color.Transparent),
                    startY = 0f,
                    endY = height
                )
            )

            // Main stroke
            drawPath(
                path = strokePath,
                color = lineColor,
                style = Stroke(width = 2.5.dp.toPx())
            )

            // 3. Current Live Price Horizontal Line
            val lastPoint = dataPoints.last()
            val currentY = height - ((lastPoint - minVal) / range * (height - 20.dp.toPx())) - 10.dp.toPx()

            drawLine(
                color = ElectricLime,
                start = Offset(0f, currentY),
                end = Offset(width, currentY),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
            )

            drawCircle(
                color = ElectricLime,
                radius = 4.dp.toPx(),
                center = Offset(width, currentY)
            )
        }

        // Live Price Tag Badge Overlay
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .clip(RoundedCornerShape(8.dp))
                .background(ElectricLime)
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = Formatters.formatCurrency(currentPrice, Currency.USD),
                color = BackgroundDark,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Min & Max Price Labels
        Column(
            modifier = Modifier.align(Alignment.BottomStart)
        ) {
            Text(
                text = "HIGH: ${Formatters.formatCurrency(maxVal.toDouble(), Currency.USD)}",
                color = TextMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "LOW: ${Formatters.formatCurrency(minVal.toDouble(), Currency.USD)}",
                color = TextMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
