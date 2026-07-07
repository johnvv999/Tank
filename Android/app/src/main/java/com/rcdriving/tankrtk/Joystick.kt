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
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.sqrt

// ------------------------------------------------------------
// The stick snaps to one of 8 fixed directions (plus center =
// stopped) rather than moving freely — a gated, detent feel.
// Angles in degrees, 0° = right, 90° = forward, matching the
// normX (right = +) / normY (forward = +) convention used by
// TankViewModel.updateJoystick.
//
// Pure left/right (dirY = 0) drive the two treads in opposite
// directions — e.g. right = left tread forward, right tread
// backward — which is exactly a spin-in-place, matching
// TankViewModel's arcade mix (left = throttle+steer, right =
// throttle-steer): left stop -> spins counterclockwise,
// right stop -> spins clockwise.
//
// The 4 diagonal stops deliberately do NOT use an equal 45°
// unit vector (dirX == dirY). With throttle == steer, the mix
// gives one tread = throttle+steer (full) and the other =
// throttle-steer = exactly 0 — i.e. one tread fully on, the
// other fully off, same as a pivot, not a gradual curve. Using
// a shallower angle (steer < throttle) keeps BOTH treads
// driving in the same direction, one just slower, which is
// what an actual graduated turn while moving looks like.
// ------------------------------------------------------------
private data class StickStop(val degrees: Float, val dirX: Float, val dirY: Float)

// Angle of the diagonal stops, measured from straight-forward/back (0°
// would be no turn at all, 45° would be the old equal-component pivot
// bug). Narrower angle = gentler, wider-radius turn (inner tread stays
// closer in speed to the outer one); wider angle = sharper, faster turn.
// Lowered from 30° to 15° because 30° made the inner tread run at only
// ~27% of the outer tread's speed — once the app/firmware PWM scaling
// bug was fixed and the inner tread actually had enough torque to move,
// that ratio turned out to spin the tank around too quickly during a
// forward turn. At 15°, the inner tread runs at ~58% of the outer
// tread's speed — still a real turn, just far less abrupt.
private const val DIAG_THROTTLE = 0.966f  // cos(15°) — dominant forward/back component
private const val DIAG_STEER    = 0.259f  // sin(15°) — gentle turn component (< throttle)

private val STOPS = listOf(
    StickStop(90f,   0f,             1f),             // forward
    StickStop(45f,   DIAG_STEER,     DIAG_THROTTLE),   // forward-right (graduated curve)
    StickStop(135f, -DIAG_STEER,     DIAG_THROTTLE),   // forward-left  (graduated curve)
    StickStop(270f,  0f,            -1f),              // backward
    StickStop(315f,  DIAG_STEER,    -DIAG_THROTTLE),   // backward-right (graduated curve)
    StickStop(225f, -DIAG_STEER,    -DIAG_THROTTLE),   // backward-left  (graduated curve)
    StickStop(0f,    1f,             0f),              // right — spin clockwise
    StickStop(180f, -1f,             0f)               // left — spin counterclockwise
)

// Fraction of full travel the stick must move before a direction
// engages. Below this it snaps back to center (stopped).
private const val DEAD_ZONE = 0.3f

private fun angularDistance(a: Float, b: Float): Float {
    val diff = abs(a - b) % 360f
    return if (diff > 180f) 360f - diff else diff
}

@Composable
fun Joystick(
    size: Dp,
    onMove: (Float, Float) -> Unit
) {
    val radius = with(LocalDensity.current) { size.toPx() / 2 }
    val travel = radius * 0.35f

    // Raw finger position — tracks the actual drag so we can measure
    // direction + magnitude, independent of the visually-snapped handle.
    var rawX by remember { mutableStateOf(0f) }
    var rawY by remember { mutableStateOf(0f) }

    // Visual handle position — always one of the 8 stop positions, or
    // center. This is what actually gets drawn.
    var handleX by remember { mutableStateOf(0f) }
    var handleY by remember { mutableStateOf(0f) }

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
                        // makes the reachable area a square/diamond — that
                        // made diagonal drags reach the dead zone / stops
                        // with less finger travel than straight drags.
                        val rawMag = sqrt(rawX * rawX + rawY * rawY)
                        if (rawMag > travel) {
                            val scale = travel / rawMag
                            rawX *= scale
                            rawY *= scale
                        }

                        val normX = rawX / travel
                        val normY = -rawY / travel
                        val magnitude = sqrt(normX * normX + normY * normY).coerceAtMost(1f)

                        if (magnitude < DEAD_ZONE) {
                            handleX = 0f
                            handleY = 0f
                            onMove(0f, 0f)
                        } else {
                            val angleDeg = ((Math.toDegrees(atan2(normY, normX).toDouble()) + 360.0) % 360.0).toFloat()
                            val stop = STOPS.minByOrNull { angularDistance(angleDeg, it.degrees) }!!

                            handleX = stop.dirX * travel
                            handleY = -stop.dirY * travel
                            onMove(stop.dirX, stop.dirY)
                        }
                    },
                    onDragEnd = {
                        rawX = 0f
                        rawY = 0f
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
