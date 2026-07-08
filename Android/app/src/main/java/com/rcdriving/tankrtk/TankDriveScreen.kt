package com.rcdriving.tankrtk

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun TankDriveScreen(
    viewModel: TankViewModel,
    connected: Boolean,
    onMain: () -> Unit,
    onRecord: () -> Unit,
    onSettings: () -> Unit
) {
    // Full device screen height (not just this row's local height) — used
    // to shift the joystick (and now the spin arrows above it) up by a
    // fixed fraction of the whole screen. Raised from 0.10 to 0.16 to
    // move the whole joystick/arrows block further up the screen.
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val joystickUpwardOffset = screenHeight * 0.16f

    // How far outside the joystick's rim each arrow sits horizontally
    // (measured at the height where the arrow overlaps the joystick —
    // see overlapDepth/chordHalfWidth below), so the arrow traces just
    // outside the joystick's curve instead of touching/covering it.
    val arrowJoystickGap = 10.dp

    // Flash the current speed setting on the main display for 1 second
    // after every +/- press, even if the joystick is centered (where the
    // display would otherwise just show 0). Re-keying on
    // speedChangeTrigger means a fresh press restarts the 1-second
    // window instead of letting an in-flight one cut it short.
    LaunchedEffect(viewModel.speedChangeTrigger) {
        if (viewModel.speedChangeTrigger > 0) {
            viewModel.showSpeedSetting = true
            delay(1000)
            viewModel.showSpeedSetting = false
        }
    }

    MetalPanel(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            TopBar(
                connected = connected,
                selectedTab = TopTab.MAIN,
                signalStrength = viewModel.signalStrength,
                onMain = onMain,
                onRecord = onRecord,
                onSettings = onSettings
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ------------------------------------------------------------
            // SPEED + TURBO (centered)
            // ------------------------------------------------------------
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SpeedDisplay(viewModel.speedPercentDisplay)

                Spacer(modifier = Modifier.height(12.dp))

                LastCommandDisplay(command = viewModel.lastCommand)
            }

            // ------------------------------------------------------------
            // SPEED +/- STACK (left) + JOYSTICK (right)
            // Sized relative to whatever vertical space is actually left,
            // so it always fits regardless of screen size / orientation
            // (fixes squashing on shorter landscape phone screens).
            // ------------------------------------------------------------
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 40.dp, vertical = 12.dp)
            ) {
                val joystickSize = maxHeight.coerceAtMost(200.dp)
                val buttonSize = (joystickSize * 0.3f).coerceAtMost(64.dp)
                // Flat fixed gap instead of the old (joystickSize * 0.12f)
                // formula — that value was almost always well under the
                // coerceAtMost cap, so raising the cap alone did nothing;
                // the cap was never actually the binding constraint.
                val buttonSpacing = 32.dp

                // Joystick widget made 50% larger (buttons stay the same size).
                // NOTE: joystickSize is often already pinned to maxHeight on
                // typical phone screens, so clamping this to maxHeight (as an
                // earlier version did) silently cancelled the entire increase.
                // It's allowed to exceed the row's available height here so
                // the enlargement is actually visible; ultimate ceiling is
                // 200dp * 1.5 = 300dp via joystickSize's own cap above.
                val joystickDisplaySize = joystickSize * 1.5f

                // +/- buttons made 100% larger (double size). NOTE: an
                // earlier cap tied to maxHeight silently suppressed most of
                // this growth (same issue the joystick sizing had) — no
                // longer clamped here, and requiredSize (below) keeps the
                // buttons perfectly round even once they exceed their old
                // layout slot.
                val buttonDisplaySize = buttonSize * 2f

                // To read as concentric rings hugging the joystick's rim,
                // the arrows need to dip DOWN past the joystick's topmost
                // point and sit right along its curve, not float above it.
                // overlapDepth is how far below that top point the arrow's
                // bottom edge sits; at that height the joystick's circle
                // has a certain half-width (basic circle geometry — a
                // chord at distance (radius - overlapDepth) from center),
                // and placing the arrow just outside that half-width is
                // what makes it trace the same curve outward, concentric
                // with the joystick instead of just floating nearby.
                val joystickRadius = joystickDisplaySize / 2
                val joystickTopY = -(joystickUpwardOffset + joystickDisplaySize)
                val overlapDepth = joystickRadius * 0.3f
                val heightFromCenter = joystickRadius - overlapDepth
                val chordHalfWidthValue = kotlin.math.sqrt(
                    joystickRadius.value * joystickRadius.value - heightFromCenter.value * heightFromCenter.value
                )
                val chordHalfWidth = chordHalfWidthValue.dp

                val arrowBottomY = joystickTopY + overlapDepth
                val arrowXOffset = chordHalfWidth + arrowJoystickGap

                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    // Bottom-align both columns so the +/- stack and the
                    // joystick share a common bottom edge by default; the
                    // upward offset below is then applied identically to
                    // both, keeping that shared bottom edge in place.
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column(
                        modifier = Modifier.offset(y = -joystickUpwardOffset),
                        verticalArrangement = Arrangement.spacedBy(buttonSpacing),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        MetalCircleButton3D(
                            text = "+",
                            onClick = { viewModel.increaseSpeed() },
                            modifier = Modifier.requiredSize(buttonDisplaySize)
                        )

                        MetalCircleButton3D(
                            text = "-",
                            onClick = { viewModel.decreaseSpeed() },
                            modifier = Modifier.requiredSize(buttonDisplaySize)
                        )
                    }

                    // Spin arrows + joystick, all sharing one center point.
                    // Each is independently positioned via BottomCenter +
                    // an explicit offset (rather than being stacked in an
                    // auto-sized Column) so the joystick's bottom edge is
                    // computed exactly the same way as the +/- stack's —
                    // guaranteed to line up — while the arrows orbit that
                    // same center at a fixed radius.
                    Box(
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        MetalJoystickBase(
                            size = joystickDisplaySize,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .offset(y = -joystickUpwardOffset)
                        ) {
                            Joystick(
                                size = joystickDisplaySize * 0.5f,
                                onMove = { normX, normY ->
                                    viewModel.updateJoystick(normX, normY)
                                }
                            )
                        }

                        SpinArrowButton(
                            clockwise = false,
                            onPressStart = { viewModel.startSpin(clockwise = false) },
                            onPressEnd = { viewModel.stopSpin() },
                            size = buttonDisplaySize,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .offset(x = -arrowXOffset, y = arrowBottomY)
                        )

                        SpinArrowButton(
                            clockwise = true,
                            onPressStart = { viewModel.startSpin(clockwise = true) },
                            onPressEnd = { viewModel.stopSpin() },
                            size = buttonDisplaySize,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .offset(x = arrowXOffset, y = arrowBottomY)
                        )
                    }
                }
            }
        }
    }
}
