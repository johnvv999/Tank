package com.rcdriving.tankrtk

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import kotlinx.coroutines.delay

// ------------------------------------------------------------
// SPIN-IN-PLACE CONTROLS — moved out of the joystick (which used
// to have dedicated hard-left/hard-right stops for this) and into
// their own momentary buttons around it. Press and hold to spin,
// release to stop — same start/stop behavior the joystick itself
// uses, just triggered by a button instead of a stick position.
//
// Uses Modifier.clickable + interactionSource's pressed state
// (rather than a raw pointerInput/detectTapGestures) — the standard
// Compose pattern for a "press and hold" control, and it also gives
// the button real click/accessibility semantics (so it's a genuine
// clickable target, not just something that happens to react to
// touch coordinates).
//
// Uses the user-supplied curved-arrow artwork (res/drawable/
// spin_clockwise.png, spin_counterclockwise.png) instead of a
// hand-drawn Canvas icon.
//
// clockwise = true  -> left tread forward, right tread backward
// clockwise = false -> left tread backward, right tread forward
// (matches TankViewModel's arcade mix: left = throttle+steer,
// right = throttle-steer, with throttle = 0 and steer = ±1 — both
// treads always end up spinning in opposite directions.)
// ------------------------------------------------------------
@Composable
fun SpinArrowButton(
    clockwise: Boolean,
    onPressStart: () -> Unit,
    onPressEnd: () -> Unit,
    size: Dp,
    modifier: Modifier = Modifier
) {
    val drawableId = if (clockwise) R.drawable.spin_clockwise else R.drawable.spin_counterclockwise

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // The firmware has a 500ms safety watchdog that stops the motors if
    // no command arrives — the joystick naturally keeps sending fresh
    // commands because a held finger almost always jitters slightly,
    // but a held button press has no such natural repeat. Without an
    // explicit re-send loop here, onPressStart() would fire exactly
    // once and the spin would stop on its own after 500ms even while
    // still held down. Looping well under that timeout keeps the
    // watchdog fed for as long as the button is actually depressed.
    LaunchedEffect(isPressed) {
        if (isPressed) {
            while (true) {
                onPressStart()
                delay(200)
            }
        } else {
            onPressEnd()
        }
    }

    Image(
        painter = painterResource(id = drawableId),
        contentDescription = if (clockwise) "Spin clockwise" else "Spin counterclockwise",
        modifier = modifier
            .size(size)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {}
            )
    )
}
