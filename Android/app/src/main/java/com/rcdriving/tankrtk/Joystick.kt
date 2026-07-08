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
import kotlin.math.sqrt

// ------------------------------------------------------------
// The stick moves freely and continuously rather than snapping to
// fixed positions — output is directly proportional to wherever the
// finger actually is, matching normX (right = +) / normY (forward =
// +), the convention TankViewModel.updateJoystick expects.
//
// This used to snap to one of 6 (originally 8) fixed stops with
// hand-tuned diagonal ratios to avoid an accidental spin-in-place.
// That's no longer needed: TankViewModel's mixing itself now caps
// steering by the current throttle magnitude, so a spin can never
// happen from joystick position alone regardless of where the stick
// is — spinning in place is only reachable via the dedicated
// spin-arrow buttons (see SpinArrowButtons.kt). That means the stick
// itself is free to just report the raw, continuous position.
// ------------------------------------------------------------

// Fraction of full travel the stick must move before it reports
// anything at all. Below this it snaps back to center (stopped) —
// this is just a small dead zone around center, not a discrete-
// position system.
private const val DEAD_ZONE = 0.1f

@Composable
fun Joystick(
    size: Dp,
    onMove: (Float, Float) -> Unit
) {
    val radius = with(LocalDensity.current) { size.toPx() / 2 }
    val travel = radius * 0.35f

    // Raw finger position, clamped to the circular travel boundary.
    // This is both the value reported via onMove AND what's drawn —
    // no separate "snapped" handle position anymore.
    var rawX by remember { mutableStateOf(0f) }
    var rawY by remember { mutableStateOf(0f) }

    Canvas(
        modifier = Modifier
            .size(size)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDrag = { change, dragAmount ->
                        change.consume()

                        rawX += dragAmount.x
                        rawY += dragAmount.y

                        // Clamp to a circular travel boundary (radius = travel)
                        // rather than clamping each axis independently, which
                        // would make the reachable area a square/diamond and
                        // let diagonal drags reach full magnitude with less
                        // finger travel than straight drags.
                        val rawMag = sqrt(rawX * rawX + rawY * rawY)
                        if (rawMag > travel) {
                            val scale = travel / rawMag
                            rawX *= scale
                            rawY *= scale
                        }

                        val normX = rawX / travel
                        val normY = -rawY / travel
                        val magnitude = sqrt(normX * normX + normY * normY)

                        if (magnitude < DEAD_ZONE) {
                            onMove(0f, 0f)
                        } else {
                            onMove(normX, normY)
                        }
                    },
                    onDragEnd = {
                        rawX = 0f
                        rawY = 0f
                        onMove(0f, 0f)
                    }
                )
            }
    ) {
        val knobRadius = radius * 0.9f
        val knobCenter = Offset(center.x + rawX, center.y + rawY)
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
