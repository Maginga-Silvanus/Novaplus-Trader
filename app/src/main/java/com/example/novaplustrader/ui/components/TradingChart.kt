package com.example.novaplustrader.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.novaplustrader.ui.theme.FinancialGreen
import com.example.novaplustrader.ui.theme.FinancialRed

@Composable
fun TradingChart(
    dataPoints: List<Float>,
    isPositive: Boolean,
    modifier: Modifier = Modifier,
    lineColor: Color = if (isPositive) FinancialGreen else FinancialRed
) {
    if (dataPoints.size < 2) return

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
    ) {
        val min = dataPoints.minOrNull() ?: 0f
        val max = dataPoints.maxOrNull() ?: 1f
        val range = if (max - min == 0f) 1f else max - min

        val path = Path()
        val stepX = size.width / (dataPoints.size - 1)

        dataPoints.forEachIndexed { index, point ->
            val x = index * stepX
            val y = size.height - ((point - min) / range * size.height)
            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }

        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 2.dp.toPx())
        )
    }
}
