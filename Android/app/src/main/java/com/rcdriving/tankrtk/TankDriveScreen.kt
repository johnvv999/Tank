package com.rcdriving.tankrtk

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp

@Composable
fun TankDriveScreen(
    viewModel: TankViewModel,
    connected: Boolean,
    onMain: () -> Unit,
    onRecord: () -> Unit,
    onSettings: () -> Unit
) {
    // Full device screen height (not just this row's local height) — used
    // to shift the joystick up by a fixed 10% of the whole screen.
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val joystickUpwardOffset = screenHeight * 0.10f

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

                    MetalJoystickBase(
                        size = joystickDisplaySize,
                        modifier = Modifier.offset(y = -joystickUpwardOffset)
                    ) {
                        Joystick(
                            size = joystickDisplaySize * 0.5f,
                            onMove = { normX, normY ->
                                viewModel.updateJoystick(normX, normY)
                            }
                        )
                    }
                }
            }
        }
    }
}
