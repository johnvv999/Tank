package com.rcdriving.tankrtk

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RecordScreen(
    viewModel: TankViewModel,
    connected: Boolean,
    onMain: () -> Unit,
    onSettings: () -> Unit,
    onRecord: () -> Unit,

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
            // TOP METALLIC TAB STRIP
            // ------------------------------------------------------------
            TopBar(
                connected = connected,
                selectedTab = TopTab.RECORD,
                signalStrength = viewModel.signalStrength,
                onMain = onMain,
                onRecord = {},
                onSettings = onSettings
            )

            Spacer(modifier = Modifier.height(30.dp))

            // ------------------------------------------------------------
            // RECORDING STATUS
            // ------------------------------------------------------------
            val isRecording = viewModel.isRecording

            Text(
                text = if (isRecording) "Recording…" else "Not Recording",
                color = if (isRecording) Color(0xFF00FF6A) else Color(0xFFFF4A4A),
                fontSize = 24.sp
            )

            Spacer(modifier = Modifier.height(40.dp))

            // ------------------------------------------------------------
            // RECORD CONTROL BUTTONS
            // ------------------------------------------------------------
            Row(
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MetalButton3D(
                    text = "START",
                    onClick = { viewModel.startRecording() },
                    modifier = Modifier.width(140.dp).height(60.dp)
                )

                MetalButton3D(
                    text = "STOP",
                    onClick = { viewModel.stopRecording() },
                    modifier = Modifier.width(140.dp).height(60.dp)
                )
            }
        }
    }
}
