package com.example.novaplustrader.ui.screens.trading.components

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.novaplustrader.domain.model.trading.MarketCandle
import com.example.novaplustrader.ui.theme.ElectricLime
import com.example.novaplustrader.ui.theme.FinancialGreen
import com.example.novaplustrader.ui.theme.FinancialRed
import com.example.novaplustrader.ui.theme.SurfaceBorder
import com.example.novaplustrader.ui.theme.SurfaceDarkCard
import com.example.novaplustrader.ui.theme.TextMuted
import java.util.Locale
import kotlin.math.abs

@Composable
fun EnhancedTradingChart(
    candles: List<MarketCandle>,
    currentPrice: Double,
    chartType: String = "Candlestick",
    modifier: Modifier = Modifier
) {
    val orderedCandles = candles.sortedBy { it.openTimeMs }.takeLast(120)
    var scale by remember { mutableStateOf(1.0f) }
    var offsetX by remember { mutableStateOf(0f) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(420.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceDarkCard)
            .border(1.dp, SurfaceBorder, RoundedCornerShape(20.dp))
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    if (zoom != 1f || abs(pan.x) >= abs(pan.y)) {
                        val newScale = (scale * zoom).coerceIn(1.0f, 20.0f)
                        scale = newScale
                        val axisWidth = 58.dp.toPx()
                        val maxOffsetX = 0f
                        val minOffsetX = -((size.width - axisWidth) * (scale - 1f)).coerceAtLeast(0f)
                        offsetX = (offsetX + pan.x).coerceIn(minOffsetX, maxOffsetX)
                    }
                }
            }
            .padding(10.dp)
    ) {
        if (orderedCandles.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Waiting for market candles…", color = TextMuted, fontSize = 12.sp)
            }
            return@Box
        }

        Canvas(Modifier.fillMaxSize()) {
            val axisWidth = 58.dp.toPx()
            val chartLeft = 4.dp.toPx() + offsetX
            val chartRight = (size.width - axisWidth) * scale + offsetX
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
            val slotWidth = ((size.width - axisWidth - 4.dp.toPx()) / orderedCandles.size) * scale
            val bodyWidth = (slotWidth * 0.62f).coerceAtLeast(1.dp.toPx()).coerceAtMost(24.dp.toPx())

            val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = TextMuted.toArgb()
                textSize = 9.sp.toPx()
                textAlign = Paint.Align.RIGHT
            }
            val nativeCanvas = drawContext.canvas.nativeCanvas

            for (line in 0..4) {
                val ratio = line / 4f
                val y = chartTop + candleAreaHeight * ratio
                drawLine(
                    color = ElectricLime.copy(alpha = 0.25f),
                    start = Offset(chartLeft.coerceAtLeast(0f), y),
                    end = Offset(chartRight.coerceAtMost(size.width), y),
                    strokeWidth = 1.dp.toPx()
                )
                val price = maxPrice - priceRange * ratio
                nativeCanvas.drawText(
                    String.format(Locale.US, "%.2f", price),
                    size.width - 2.dp.toPx(),
                    y + labelPaint.textSize / 3f,
                    labelPaint
                )
            }

            for (vLine in 0..4) {
                val vx = chartLeft + (chartRight - chartLeft) * (vLine / 4f)
                if (vx in chartLeft..chartRight) {
                    drawLine(
                        color = SurfaceBorder.copy(alpha = 0.35f),
                        start = Offset(vx, chartTop),
                        end = Offset(vx, candleAreaBottom),
                        strokeWidth = 1.dp.toPx()
                    )
                }
            }

            fun priceY(price: Double): Float =
                candleAreaBottom - ((price - minPrice) / priceRange).toFloat() * candleAreaHeight

            when (chartType.lowercase(Locale.US)) {
                "line" -> {
                    val path = Path()
                    orderedCandles.forEachIndexed { index, candle ->
                        val x = chartLeft + slotWidth * (index + 0.5f)
                        val y = priceY(candle.closePrice)
                        if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }
                    drawPath(path = path, color = ElectricLime, style = Stroke(width = 2.dp.toPx()))
                }
                "area" -> {
                    val path = Path()
                    val fillPath = Path()
                    var firstX = 0f
                    var lastX = 0f
                    orderedCandles.forEachIndexed { index, candle ->
                        val x = chartLeft + slotWidth * (index + 0.5f)
                        val y = priceY(candle.closePrice)
                        if (index == 0) {
                            firstX = x
                            path.moveTo(x, y)
                            fillPath.moveTo(x, candleAreaBottom)
                            fillPath.lineTo(x, y)
                        } else {
                            lastX = x
                            path.lineTo(x, y)
                            fillPath.lineTo(x, y)
                        }
                    }
                    if (orderedCandles.isNotEmpty()) {
                        fillPath.lineTo(lastX, candleAreaBottom)
                        fillPath.lineTo(firstX, candleAreaBottom)
                        fillPath.close()
                        drawPath(path = fillPath, color = ElectricLime.copy(alpha = 0.2f))
                    }
                    drawPath(path = path, color = ElectricLime, style = Stroke(width = 2.dp.toPx()))
                }
                "bar" -> {
                    orderedCandles.forEachIndexed { index, candle ->
                        val x = chartLeft + slotWidth * (index + 0.5f)
                        if (x in -50f..(size.width + 50f)) {
                            val rising = candle.closePrice >= candle.openPrice
                            val color = if (rising) FinancialGreen else FinancialRed
                            val highY = priceY(candle.highPrice)
                            val lowY = priceY(candle.lowPrice)
                            val openY = priceY(candle.openPrice)
                            val closeY = priceY(candle.closePrice)

                            // High-Low vertical bar
                            drawLine(color = color, start = Offset(x, highY), end = Offset(x, lowY), strokeWidth = 1.5.dp.toPx())
                            // Open tick (left)
                            drawLine(color = color, start = Offset(x - 3.dp.toPx(), openY), end = Offset(x, openY), strokeWidth = 1.5.dp.toPx())
                            // Close tick (right)
                            drawLine(color = color, start = Offset(x, closeY), end = Offset(x + 3.dp.toPx(), closeY), strokeWidth = 1.5.dp.toPx())

                            // Volume Bar
                            val volumeHeight = ((candle.baseVolume / maxVolume).toFloat() * (chartBottom - volumeTop - 4.dp.toPx()))
                                .coerceAtLeast(1.dp.toPx())
                            drawRect(
                                color = color.copy(alpha = 0.5f),
                                topLeft = Offset(x - bodyWidth / 2f, chartBottom - volumeHeight),
                                size = androidx.compose.ui.geometry.Size(bodyWidth, volumeHeight)
                            )
                        }
                    }
                }
                else -> {
                    // Candlestick
                    orderedCandles.forEachIndexed { index, candle ->
                        val x = chartLeft + slotWidth * (index + 0.5f)
                        if (x in -50f..(size.width + 50f)) {
                            val rising = candle.closePrice >= candle.openPrice
                            val color = if (rising) FinancialGreen else FinancialRed

                            drawLine(
                                color = color,
                                start = Offset(x, priceY(candle.highPrice)),
                                end = Offset(x, priceY(candle.lowPrice)),
                                strokeWidth = 1.8.dp.toPx()
                            )

                            val bodyTop = priceY(maxOf(candle.openPrice, candle.closePrice))
                            val bodyBottom = priceY(minOf(candle.openPrice, candle.closePrice))
                            drawRect(
                                color = color,
                                topLeft = Offset(x - bodyWidth / 2f, bodyTop),
                                size = androidx.compose.ui.geometry.Size(bodyWidth, (bodyBottom - bodyTop).coerceAtLeast(2.dp.toPx()))
                            )

                            val volumeHeight = ((candle.baseVolume / maxVolume).toFloat() * (chartBottom - volumeTop - 4.dp.toPx()))
                                .coerceAtLeast(1.dp.toPx())
                            drawRect(
                                color = color.copy(alpha = if (candle.isFinal) 0.35f else 0.75f),
                                topLeft = Offset(x - bodyWidth / 2f, chartBottom - volumeHeight),
                                size = androidx.compose.ui.geometry.Size(bodyWidth, volumeHeight)
                            )
                        }
                    }
                }
            }
        }

        Column(Modifier.align(Alignment.TopEnd)) {
            Text(
                text = if (currentPrice > 0.0) String.format(Locale.US, "$%.2f USDT", currentPrice) else "Waiting for quote",
                color = ElectricLime,
                fontSize = 10.sp
            )
            Text(
                "$chartType · ZOOM 1.0x-20.0x · BOUNDED",
                color = TextMuted,
                fontSize = 8.sp
            )
        }
    }
}
