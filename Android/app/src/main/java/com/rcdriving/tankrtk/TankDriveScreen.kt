package com.rcdriving.tankrtk

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TankDriveScreen(
    viewModel: TankViewModel,
    connected: Boolean,
    onMain: () -> Unit,
    onRecord: () -> Unit,
    onSettings: () -> Unit
) {
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
                SpeedDisplay(viewModel.speedCurrent)

                Spacer(modifier = Modifier.height(12.dp))

                TurboButton(
                    active = viewModel.turboActive,
                    onClick = { viewModel.activateTurbo() }
                )
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
                val buttonSpacing = (joystickSize * 0.12f).coerceAtMost(16.dp)

                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(buttonSpacing),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        MetalCircleButton3D(
                            text = "+",
                            onClick = { viewModel.increaseSpeed() },
                            modifier = Modifier.size(buttonSize)
                        )

                        MetalCircleButton3D(
                            text = "-",
                            onClick = { viewModel.decreaseSpeed() },
                            modifier = Modifier.size(buttonSize)
                        )
                    }

                    MetalJoystickBase(size = joystickSize) {
                        Joystick(
                            size = joystickSize * 0.5f,
                            onMove = { normX, normY ->
                                if (normX != 0f || normY != 0f) {
                                    viewModel.updateDirection(kotlin.math.atan2(normY, normX))
                                }
                            }
                        )
                    }
                }
            }
        }

        CornerAccent(
            modifier = Modifier.align(Alignment.BottomStart),
            mirrored = false
        )

        CornerAccent(
            modifier = Modifier.align(Alignment.BottomEnd),
            mirrored = true
        )
    }
}
