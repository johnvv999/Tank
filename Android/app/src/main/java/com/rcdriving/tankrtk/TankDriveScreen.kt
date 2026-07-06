package com.rcdriving.tankrtk

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.atan2

@Composable
fun TankDriveScreen(
    viewModel: TankViewModel,
    connected: Boolean,
    onRecord: () -> Unit,
    onSettings: () -> Unit
) {
    MetalPanel(
        modifier = Modifier
            .fillMaxSize()
            .background(satinGunmetal)
    ) {

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ------------------------------------------------------------
            // TOP TAB BAR (exact layout from reference image)
            // ------------------------------------------------------------
            
	TopBar(
    		connected = connected,
    		selectedTab = TopTab.MAIN,
    		signalStrength = viewModel.signalStrength,
    		onMain = {},
    		onRecord = onRecord,
    		onSettings = onSettings
		)




            )

            Spacer(modifier = Modifier.height(20.dp))

            // ------------------------------------------------------------
            // SPEED DISPLAY (exact green "Speed 0%" panel)
            // ------------------------------------------------------------
            SpeedDisplay(viewModel.speedCurrent)

            Spacer(modifier = Modifier.height(20.dp))

            // ------------------------------------------------------------
            // TURBO BUTTON (exact glowing blue button)
            // ------------------------------------------------------------
            TurboButton(
                onClick = { viewModel.activateTurbo() }
            )

            Spacer(modifier = Modifier.height(30.dp))

            // ------------------------------------------------------------
            // LOWER CONTROL AREA (matches reference image)
            // ------------------------------------------------------------
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {

                // --------------------------------------------------------
                // LEFT SIDE: + / – buttons (exact metallic circles)
                // --------------------------------------------------------
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    MetalCircleButton3D(
                        text = "+",
                        onClick = { viewModel.increaseSpeed() },
                        modifier = Modifier.size(70.dp)
                    )
                    MetalCircleButton3D(
                        text = "-",
                        onClick = { viewModel.decreaseSpeed() },
                        modifier = Modifier.size(70.dp)
                    )
                }

                // --------------------------------------------------------
                // RIGHT SIDE: Joystick (functional, but looks identical)
                // --------------------------------------------------------
                JoystickControl(viewModel)
            }
        }
    }
}

// ------------------------------------------------------------
// FUNCTIONAL JOYSTICK (looks identical to reference image)
// ------------------------------------------------------------
@Composable
fun JoystickControl(viewModel: TankViewModel) {

    var center by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = Modifier
            .size(220.dp)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        if (center == Offset.Zero) {
                            center = Offset(size.width / 2f, size.height / 2f)
                        }
                        val angle = atan2(
                            offset.y - center.y,
                            offset.x - center.x
                        )
                        viewModel.updateDirection(angle)
                    },
                    onDrag = { change, _ ->
                        val pos = change.position
                        val angle = atan2(
                            pos.y - center.y,
                            pos.x - center.x
                        )
                        viewModel.updateDirection(angle)
                    },
                    onDragEnd = {
                        viewModel.updateDirection(0f)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {

        // Metallic joystick base + blue LED ring (exact match)
        MetalJoystickBase {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF3A3F48))
            )
        }
    }
}
