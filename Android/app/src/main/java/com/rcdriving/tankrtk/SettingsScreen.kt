package com.rcdriving.tankrtk

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SettingsScreen(
    speedLevel: Int,
    trimOffset: Int,
    connectionStatus: ConnectionStatus,
    onTrimLeft: () -> Unit,
    onTrimRight: () -> Unit,
    onSpeed: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A2F0A))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Settings",
            color = Color.White,
            fontSize = 26.sp,
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            textAlign = TextAlign.Center
        )

        ConnectionStatusIndicator(connectionStatus, showLabel = true)

        Spacer(Modifier.height(32.dp))

        Text("Speed Level: $speedLevel/4", color = Color.White, fontSize = 20.sp)
        Spacer(Modifier.height(8.dp))
        SmallButton("CYCLE SPEED", onSpeed)

        Spacer(Modifier.height(32.dp))

        Text("Trim Offset: $trimOffset", color = Color.White, fontSize = 20.sp)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            SmallButton("◀ TRIM", onTrimLeft)
            SmallButton("TRIM ▶", onTrimRight)
        }
    }
}

@Composable
fun ConnectionStatusIndicator(status: ConnectionStatus, showLabel: Boolean = false) {
    val (color, label) = when (status) {
        ConnectionStatus.CONNECTED -> Color.Green to "Connected"
        ConnectionStatus.CONNECTING -> Color.Yellow to "Connecting..."
        ConnectionStatus.FAILED -> Color.Red to "Connection Failed"
        ConnectionStatus.DISCONNECTED -> Color.Gray to "Disconnected"
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(14.dp)
                .background(color, shape = androidx.compose.foundation.shape.CircleShape)
        )
        if (showLabel) {
            Spacer(Modifier.width(8.dp))
            Text(label, color = Color.White, fontSize = 16.sp)
        }
    }
}
