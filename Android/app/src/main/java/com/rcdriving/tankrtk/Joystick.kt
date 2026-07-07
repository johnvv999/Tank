package com.rcdriving.tankrtk

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp

@Composable
fun Joystick(
    size: Dp,
    onMove: (Float, Float) -> Unit
) {
    val radius = with(LocalDensity.current) { size.toPx() / 2 }
    val travel = radius * 0.35f
    var handleX by remember { mutableStateOf(0f) }
    var handleY by remember { mutableStateOf(0f) }

    Canvas(
        modifier = Modifier
            .size(size)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDrag = { change, dragAmount ->
                        change.consume()
                        handleX = (handleX + dragAmount.x).coerceIn(-travel, travel)
                        handleY = (handleY + dragAmount.y).coerceIn(-travel, travel)
                        val normX = handleX / travel
                        val normY = -handleY / travel
                        onMove(normX, normY)
                    },
                    onDragEnd = {
                        handleX = 0f
                        handleY = 0f
                        onMove(0f, 0f)
                    }
                )
            }
    ) {
        val knobRadius = radius * 0.9f
        val knobCenter = Offset(center.x + handleX, center.y + handleY)
        val highlightOffset = Offset(knobCenter.x - knobRadius * 0.35f, knobCenter.y - knobRadius * 0.35f)

        // Glossy black dome
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF55575A),
                    Color(0xFF1A1B1C),
                    Color(0xFF000000)
                ),
                center = highlightOffset,
                radius = knobRadius * 1.7f
            ),
            radius = knobRadius,
            center = knobCenter
        )

        // Specular highlight (glossy reflection)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.45f),
                    Color.White.copy(alpha = 0.0f)
                ),
                center = highlightOffset,
                radius = knobRadius * 0.6f
            ),
            radius = knobRadius * 0.5f,
            center = highlightOffset
        )
    }
}
