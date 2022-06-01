package com.realityexpander.stockmarketapp.presentation.company_info

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.realityexpander.stockmarketapp.data.mapper.DateFormatterPattern
import com.realityexpander.stockmarketapp.domain.model.IntradayInfo
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

@Composable
fun StockChart(
    modifier: Modifier = Modifier,
    infos2: List<IntradayInfo> = emptyList(),
    graphColor: Color = Color.Cyan
) {
    val fmt = DateTimeFormatter.ofPattern(DateFormatterPattern)
    val infos = listOf<IntradayInfo>(
        IntradayInfo(LocalDateTime.parse("2020-05-31 01:00:00",fmt), close = 0.0, high = 0.0, low = 0.0, open = 0.0, volume = 100),
        IntradayInfo(LocalDateTime.parse("2020-05-31 02:00:00",fmt), close = 10.0, high = 0.0, low = 0.0, open = 0.0, volume = 100),
        IntradayInfo(LocalDateTime.parse("2020-05-31 03:00:00",fmt), close = 20.0, high = 0.0, low = 0.0, open = 0.0, volume = 100),
        IntradayInfo(LocalDateTime.parse("2020-05-31 04:00:00",fmt), close = 30.0, high = 0.0, low = 0.0, open = 0.0, volume = 100),
        IntradayInfo(LocalDateTime.parse("2020-05-31 05:00:00",fmt), close = 40.0, high = 0.0, low = 0.0, open = 0.0, volume = 100),
        IntradayInfo(LocalDateTime.parse("2020-05-31 06:00:00",fmt), close = 50.0, high = 0.0, low = 0.0, open = 0.0, volume = 100),
        IntradayInfo(LocalDateTime.parse("2020-05-31 07:00:00",fmt), close = 60.0, high = 0.0, low = 0.0, open = 0.0, volume = 100),
        IntradayInfo(LocalDateTime.parse("2020-05-31 08:00:00",fmt), close = 70.0, high = 0.0, low = 0.0, open = 0.0, volume = 100),
        IntradayInfo(LocalDateTime.parse("2020-05-31 09:00:00",fmt), close = 80.0, high = 0.0, low = 0.0, open = 0.0, volume = 100),
    )

    val spacing = 100f
    val transparentGraphColor = remember {
        graphColor.copy(alpha = 0.5f)
    }
    val upperPrice = remember(infos) {
        infos.maxOfOrNull { it.close }?.roundUpInt() ?: 0
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
    Canvas(modifier = modifier ) {
        // X Axis (hour)
        val spacePerHour = (size.width - spacing) / infos.size
        (0 until infos.size - 1 step 2).forEach { i->
            val info = infos[i]
            val hour = info.datetime.hour
            drawContext.canvas.nativeCanvas.apply {
                drawText(hour.toString(),
                    (i * spacePerHour) + spacing,
                    size.height - 5,
                    textPaint)
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
                    textPaint)
            }
        }

        // Graph
        val strokePath = Path().apply {
            val height = size.height
            var prevX: Float = 0f
            var prevY: Float = 0f
            for(i in infos.indices) {
                val info = infos[i]
                val nextInfo = infos.getOrNull(i + 1) ?: infos.last()
                val leftRatio = (info.close.toFloat() - lowerPrice) / (upperPrice - lowerPrice)
                //val rightRatio = (nextInfo.close.toFloat() - lowerValue) / (upperValue - lowerValue)
                val x = i * spacePerHour + spacing
                val y = height - spacing - (leftRatio * height)
                if(i == 0) {
                    moveTo(x, y)
                } else {
                    lineTo(x, y)
//                    quadraticBezierTo(
//                        x,
//                        y,
//                        prevX,
//                        (y - prevY) / 2f,
//                    )
                }
                prevX = x
                prevY = y
            }
        }

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
