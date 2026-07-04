package com.rcdriving.tankrtk

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

@Composable
fun Joystick(
    modifier: Modifier = Modifier,
    onMove: (Float, Float) -> Unit
) {
    Box(
        modifier = modifier
            .size(200.dp)
            .background(Color.DarkGray)
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    val size = this.size
                    val cx = size.width / 2f
                    val cy = size.height / 2f
                    val dx = (change.position.x - cx) / cx
                    val dy = (cy - change.position.y) / cy
                    onMove(dx.coerceIn(-1f, 1f), dy.coerceIn(-1f, 1f))
                }
            }
    )
}
