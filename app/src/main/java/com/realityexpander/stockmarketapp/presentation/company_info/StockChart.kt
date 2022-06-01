package com.realityexpander.stockmarketapp.presentation.company_info

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.realityexpander.stockmarketapp.domain.model.IntradayInfo
import kotlin.math.roundToInt

@Composable
fun StockChart(
    modifier: Modifier = Modifier,
    infos: List<IntradayInfo> = emptyList(),
    graphColor: Color = Color.Cyan
) {

    val spacing = 100f
    val transparentGraphColor = remember {
        graphColor.copy(alpha = 0.5f)
    }
    val upperPrice = remember(infos) {
        infos.maxOfOrNull { it.close }?.roundToInt() ?: 0
    }
    val lowerPrice = remember(infos) {
        infos.minOfOrNull { it.close }?.roundToInt() ?: 0
    }
    val pixelDensity = LocalDensity.current
    val textPaint = remember {
        // use XML paint here because Compose Paint doesn't support text
        Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = pixelDensity.run {
                12.sp.toPx()
            }
            textAlign = Paint.Align.CENTER
        }
    }
    Canvas(modifier = modifier) {
        // X Axis (hour)
        val spacePerHour = (size.width - spacing) / infos.size
        (0 until infos.size step 2).forEach { i ->
            val info = infos[i]
            val hour = info.datetime.hour
            drawContext.canvas.nativeCanvas.apply {
                drawText(
                    hour.toString(),
                    (i * spacePerHour) + spacing,
                    size.height - 5,
                    textPaint
                )
            }
        }

        // Y Axis (price)
        val priceStep = (upperPrice - lowerPrice) / 5f
        (0..5).forEach { i ->
            drawContext.canvas.nativeCanvas.apply {
                drawText(
                    ((i.toFloat() * priceStep) + lowerPrice).roundToDecimalPlaces(1).toString(),
                    30f,
                    size.height - spacing - (i * size.height / 5f),
                    textPaint
                )
            }
        }


        // Graph

        var lastX = 0f;
        fun Path.drawLineGraph(
            i: Int,
            height: Float
        ) {
            val info = infos[i]
            val leftRatio = (info.close.toFloat() - lowerPrice) / (upperPrice - lowerPrice)
            val x = i * spacePerHour + spacing
            val y = height - spacing - (leftRatio * height)
            if (i == 0) {
                moveTo(x, y)
            } else {
                lineTo(x, y)
            }
        }

        fun Path.drawBezierGraph(i: Int, height: Float) {
            val info = infos[i]
            val nextInfo = infos.getOrNull(i + 1) ?: infos.last()
            val leftRatio = (info.close - lowerPrice) / (upperPrice - lowerPrice)
            val rightRatio = (nextInfo.close - lowerPrice) / (upperPrice - lowerPrice)
            val x1 = spacing + i * spacePerHour
            val y1 = height - spacing - (leftRatio * height).toFloat()
            val x2 = spacing + (i + 1) * spacePerHour
            val y2 = height - spacing - (rightRatio * height).toFloat()
            if (i == 0) {
                moveTo(x1, y1)
            }
            lastX = (x1 + x2) / 2f
            quadraticBezierTo(
                x1, y1, lastX, (y1 + y2) / 2f
            )
        }

        // Setup graph curve
        val strokePath = Path().apply {
            val height = size.height
            val drawMode = GraphLineMode.Line

            for (i in infos.indices) {
                when (drawMode) {
                    GraphLineMode.Line -> drawLineGraph(i, height)
                    GraphLineMode.Bezier -> drawBezierGraph(i, height)
                }
            }
        }

        // Setup the fill under the curve
        val fillPath = android.graphics.Path(strokePath.asAndroidPath())
            .asComposePath()
            .apply {
                lineTo(lastX, size.height - spacing)
                lineTo(spacing, size.height - spacing)
                close()
            }

        // Draw the fill
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    transparentGraphColor,
                    Color.Transparent,
                    transparentGraphColor,
                ),
                endY = size.height - spacing
            )
        )

        // Draw the stroke
        drawPath(
            path = strokePath,
            color = graphColor,
            style = Stroke(
                width = 3.dp.toPx(),
                cap = StrokeCap.Round,
            )
        )
    }
}

enum class GraphLineMode {
    Line,
    Bezier
}

// Round up to the nearest integer
private fun Double.roundUpInt(): Int {
    return (this + 0.5).roundToInt()
}

// Round to specific number of decimal places
private fun Float.roundToDecimalPlaces(decimals: Int): Double {
    var multiplier = 1.0
    repeat(decimals) { multiplier *= 10 }
    return kotlin.math.round(this * multiplier) / multiplier
}
