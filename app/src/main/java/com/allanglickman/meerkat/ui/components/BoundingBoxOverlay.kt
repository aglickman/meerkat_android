package com.allanglickman.meerkat.ui.components

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import com.allanglickman.meerkat.detection.DetectedObject
import android.graphics.Color as AndroidColor

@Composable
fun BoundingBoxOverlay(
    detections: List<DetectedObject>,
    imageWidth: Int,
    imageHeight: Int,
    modifier: Modifier = Modifier,
) {
    val labelPaint = remember {
        Paint().apply {
            color = AndroidColor.GREEN
            textSize = 36f
            isAntiAlias = true
        }
    }

    Canvas(modifier = modifier) {
        if (imageWidth <= 0 || imageHeight <= 0) return@Canvas
        val scaleX = size.width / imageWidth.toFloat()
        val scaleY = size.height / imageHeight.toFloat()

        detections.forEach { detection ->
            val box = detection.boundingBox
            val topLeft = Offset(box.left * scaleX, box.top * scaleY)
            val boxSize = Size(box.width() * scaleX, box.height() * scaleY)

            drawRect(
                color = Color.Green,
                topLeft = topLeft,
                size = boxSize,
                style = Stroke(width = 4.dp.toPx()),
            )

            val label = buildString {
                append(detection.label)
                append(' ')
                append((detection.confidence * 100).toInt())
                append('%')
                detection.trackingId?.let { append(" #").append(it) }
            }
            drawContext.canvas.nativeCanvas.drawText(
                label,
                topLeft.x,
                (topLeft.y - 8.dp.toPx()).coerceAtLeast(labelPaint.textSize),
                labelPaint,
            )
        }
    }
}
