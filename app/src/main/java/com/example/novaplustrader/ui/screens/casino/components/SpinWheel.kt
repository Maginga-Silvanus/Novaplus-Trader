package com.example.novaplustrader.ui.screens.casino.components

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.novaplustrader.ui.screens.casino.models.CasinoSegment
import com.example.novaplustrader.ui.theme.BackgroundDark
import com.example.novaplustrader.ui.theme.ElectricLime
import com.example.novaplustrader.ui.theme.ElectricLimeGlow
import com.example.novaplustrader.ui.theme.SurfaceBorder
import com.example.novaplustrader.ui.theme.SurfaceDarkCard
import com.example.novaplustrader.ui.theme.SurfaceDarkElevated
import com.example.novaplustrader.ui.theme.TextPrimary
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SpinWheel(
    rotationAngle: Float,
    segments: List<CasinoSegment>,
    isSpinning: Boolean,
    modifier: Modifier = Modifier
) {
    val pointerShape = GenericShape { size, _ ->
        moveTo(size.width / 2f, size.height)
        lineTo(0f, 0f)
        lineTo(size.width, 0f)
        close()
    }

    Box(
        modifier = modifier
            .size(260.dp)
            .semantics {
                contentDescription = if (isSpinning) "Wheel spinning" else "Demo Spin Wheel"
            },
        contentAlignment = Alignment.Center
    ) {
        // Outer Wheel Outer Ring with Shadow
        Box(
            modifier = Modifier
                .fillMaxSize()
                .shadow(12.dp, CircleShape, ambientColor = ElectricLimeGlow, spotColor = ElectricLimeGlow)
                .clip(CircleShape)
                .background(SurfaceDarkCard)
                .border(3.dp, ElectricLime.copy(alpha = 0.6f), CircleShape)
                .padding(8.dp)
        ) {
            // Rotating Wheel Canvas
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .rotate(rotationAngle)
            ) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val radius = size.width / 2f
                val sweepAngle = 360f / segments.size

                segments.forEachIndexed { index, segment ->
                    val startAngle = index * sweepAngle - 90f

                    // Draw Segment Arc
                    val segmentColor = if (index % 2 == 0) SurfaceDarkElevated else SurfaceDarkCard
                    drawArc(
                        color = segmentColor,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = true,
                        size = Size(radius * 2f, radius * 2f),
                        topLeft = Offset(0f, 0f)
                    )

                    // Draw Segment Border Line
                    val lineAngleRad = Math.toRadians(startAngle.toDouble())
                    val lineEnd = Offset(
                        x = (center.x + radius * cos(lineAngleRad)).toFloat(),
                        y = (center.y + radius * sin(lineAngleRad)).toFloat()
                    )
                    drawLine(
                        color = SurfaceBorder,
                        start = center,
                        end = lineEnd,
                        strokeWidth = 2.dp.toPx()
                    )

                    // Render Multiplier Text Label
                    val textAngleRad = Math.toRadians((startAngle + sweepAngle / 2f).toDouble())
                    val textRadius = radius * 0.68f
                    val textX = (center.x + textRadius * cos(textAngleRad)).toFloat()
                    val textY = (center.y + textRadius * sin(textAngleRad)).toFloat()

                    drawContext.canvas.nativeCanvas.apply {
                        val paint = Paint().apply {
                            color = if (segment.multiplier > 0) android.graphics.Color.WHITE else android.graphics.Color.GRAY
                            textSize = 34f
                            isFakeBoldText = true
                            textAlign = Paint.Align.CENTER
                        }
                        drawText(segment.label, textX, textY + 12f, paint)
                    }
                }
            }
        }

        // Center Hub
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(BackgroundDark)
                .border(2.dp, ElectricLime, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "NOVA",
                color = ElectricLime,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Top Pointer Marker Indicator
        Box(
            modifier = Modifier
                .size(24.dp, 20.dp)
                .align(Alignment.TopCenter)
                .clip(pointerShape)
                .background(ElectricLime)
        )
    }
}
