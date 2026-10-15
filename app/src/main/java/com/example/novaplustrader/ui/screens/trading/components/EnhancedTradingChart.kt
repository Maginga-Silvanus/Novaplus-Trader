package com.example.novaplustrader.ui.screens.trading.components

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.novaplustrader.domain.model.trading.MarketCandle
import com.example.novaplustrader.ui.theme.BackgroundDark
import com.example.novaplustrader.ui.theme.ElectricLime
import com.example.novaplustrader.ui.theme.FinancialGreen
import com.example.novaplustrader.ui.theme.FinancialRed
import com.example.novaplustrader.ui.theme.SurfaceBorder
import com.example.novaplustrader.ui.theme.SurfaceDarkCard
import com.example.novaplustrader.ui.theme.TextMuted
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Composable
fun EnhancedTradingChart(
    candles: List<MarketCandle>,
    currentPrice: Double,
    modifier: Modifier = Modifier
) {
    val orderedCandles = candles.sortedBy { it.openTimeMs }.takeLast(80)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(260.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceDarkCard)
            .border(1.dp, SurfaceBorder, RoundedCornerShape(20.dp))
            .padding(10.dp)
    ) {
        if (orderedCandles.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Waiting for Binance OHLC candles…", color = TextMuted, fontSize = 12.sp)
            }
            return@Box
        }

        Canvas(Modifier.fillMaxSize()) {
            val axisWidth = 58.dp.toPx()
            val chartLeft = 4.dp.toPx()
            val chartRight = size.width - axisWidth
            val chartTop = 10.dp.toPx()
            val chartBottom = size.height - 35.dp.toPx()
            val volumeTop = chartTop + (chartBottom - chartTop) * 0.78f
            val candleAreaBottom = volumeTop - 7.dp.toPx()
            val candleAreaHeight = candleAreaBottom - chartTop
            val highs = orderedCandles.map { it.highPrice }
            val lows = orderedCandles.map { it.lowPrice }
            val low = lows.minOrNull() ?: 0.0
            val high = highs.maxOrNull() ?: 1.0
            val padding = ((high - low) * 0.06).coerceAtLeast(high * 0.0001)
            val minPrice = (low - padding).coerceAtLeast(0.0)
            val maxPrice = high + padding
            val priceRange = (maxPrice - minPrice).coerceAtLeast(1e-12)
            val maxVolume = orderedCandles.maxOfOrNull { it.baseVolume }?.coerceAtLeast(1e-12) ?: 1.0
            val slotWidth = (chartRight - chartLeft) / orderedCandles.size
            val bodyWidth = (slotWidth * 0.62f).coerceAtLeast(1.dp.toPx()).coerceAtMost(12.dp.toPx())

            val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = SurfaceBorder.copy(alpha = 0.7f).toArgb()
                strokeWidth = 1.dp.toPx()
            }
            val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = TextMuted.toArgb()
                textSize = 9.sp.toPx()
                textAlign = Paint.Align.RIGHT
            }
            val timePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = TextMuted.toArgb()
                textSize = 9.sp.toPx()
            }
            val timeFormat = SimpleDateFormat("MM-dd HH:mm", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val nativeCanvas = drawContext.canvas.nativeCanvas

            for (line in 0..3) {
                val ratio = line / 3f
                val y = chartTop + candleAreaHeight * ratio
                drawLine(
                    color = SurfaceBorder.copy(alpha = 0.55f),
                    start = Offset(chartLeft, y),
                    end = Offset(chartRight, y),
                    strokeWidth = 1.dp.toPx()
                )
                val price = maxPrice - priceRange * ratio
                nativeCanvas.drawText(
                    String.format(Locale.US, "%.6g", price),
                    size.width - 2.dp.toPx(),
                    y + labelPaint.textSize / 3f,
                    labelPaint
                )
            }

            drawLine(
                color = SurfaceBorder.copy(alpha = 0.8f),
                start = Offset(chartLeft, volumeTop),
                end = Offset(chartRight, volumeTop),
                strokeWidth = 1.dp.toPx()
            )

            orderedCandles.forEachIndexed { index, candle ->
                val x = chartLeft + slotWidth * (index + 0.5f)
                fun priceY(price: Double): Float =
                    candleAreaBottom - ((price - minPrice) / priceRange).toFloat() * candleAreaHeight

                val rising = candle.closePrice >= candle.openPrice
                val color = if (rising) FinancialGreen else FinancialRed
                drawLine(
                    color = color,
                    start = Offset(x, priceY(candle.highPrice)),
                    end = Offset(x, priceY(candle.lowPrice)),
                    strokeWidth = 1.2.dp.toPx()
                )
                val bodyTop = priceY(maxOf(candle.openPrice, candle.closePrice))
                val bodyBottom = priceY(minOf(candle.openPrice, candle.closePrice))
                drawRect(
                    color = color,
                    topLeft = Offset(x - bodyWidth / 2f, bodyTop),
                    size = androidx.compose.ui.geometry.Size(bodyWidth, (bodyBottom - bodyTop).coerceAtLeast(1.5.dp.toPx()))
                )
                val volumeHeight = ((candle.baseVolume / maxVolume).toFloat() * (chartBottom - volumeTop - 4.dp.toPx()))
                    .coerceAtLeast(1.dp.toPx())
                drawRect(
                    color = color.copy(alpha = if (candle.isFinal) 0.4f else 0.75f),
                    topLeft = Offset(x - bodyWidth / 2f, chartBottom - volumeHeight),
                    size = androidx.compose.ui.geometry.Size(bodyWidth, volumeHeight)
                )
            }

            nativeCanvas.drawText(
                timeFormat.format(Date(orderedCandles.first().openTimeMs)),
                chartLeft,
                size.height - 3.dp.toPx(),
                timePaint
            )
            val lastTime = timeFormat.format(Date(orderedCandles.last().openTimeMs))
            nativeCanvas.drawText(
                lastTime,
                chartRight - timePaint.measureText(lastTime),
                size.height - 3.dp.toPx(),
                timePaint
            )
            drawLine(
                color = ElectricLime.copy(alpha = 0.7f),
                start = Offset(chartLeft, volumeTop),
                end = Offset(chartRight, volumeTop),
                strokeWidth = 1.dp.toPx()
            )
        }

        Column(Modifier.align(Alignment.TopEnd)) {
            Text(
                text = if (currentPrice > 0.0) String.format(Locale.US, "%.8f USDT", currentPrice) else "Waiting for quote",
                color = ElectricLime,
                fontSize = 10.sp
            )
            Text(
                if (orderedCandles.last().isFinal) "CLOSED · OHLC · BASE VOL" else "FORMING · OHLC · BASE VOL",
                color = TextMuted,
                fontSize = 8.sp
            )
        }
        Row(
            modifier = Modifier.align(Alignment.BottomStart),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("VOL", color = TextMuted, fontSize = 8.sp)
        }
    }
}
