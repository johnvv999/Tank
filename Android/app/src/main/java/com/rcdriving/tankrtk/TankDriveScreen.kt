package com.rcdriving.tankrtk

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun TankDriveScreen(
    viewModel: TankViewModel,
    connected: Boolean,
    onMain: () -> Unit,
    onRecord: () -> Unit,
    onSettings: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {

        // ------------------------------------------------------------
        // TOP BAR
        // ------------------------------------------------------------
        TopBar(
            connected = connected,
            signalStrength = viewModel.signalStrength,
            onMain = onMain,
            onRecord = onRecord,
            onSettings = onSettings
        )

        // ------------------------------------------------------------
        // MAIN DRIVE UI
        // ------------------------------------------------------------
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            SpeedDisplay(viewModel.speedCurrent)

            Spacer(modifier = Modifier.height(20.dp))

            TurboButton(
                active = viewModel.turboActive,
                onClick = { viewModel.activateTurbo() }
            )

            Spacer(modifier = Modifier.height(20.dp))

            JoystickControl(
                angle = viewModel.angle,
                onAngleChanged = { viewModel.updateDirection(it) }
            )
        }
    }
}
