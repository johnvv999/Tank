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
    var handleX by remember { mutableStateOf(0f) }
    var handleY by remember { mutableStateOf(0f) }

    Canvas(
        modifier = Modifier
            .size(size)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDrag = { change, dragAmount ->
                        change.consume()
                        handleX = (handleX + dragAmount.x).coerceIn(-radius, radius)
                        handleY = (handleY + dragAmount.y).coerceIn(-radius, radius)
                        val normX = handleX / radius
                        val normY = -handleY / radius
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
        drawCircle(color = Color.DarkGray, radius = radius, center = center)
        drawCircle(
            color = Color.Green,
            radius = radius / 4,
            center = Offset(center.x + handleX, center.y + handleY)
        )
    }
}