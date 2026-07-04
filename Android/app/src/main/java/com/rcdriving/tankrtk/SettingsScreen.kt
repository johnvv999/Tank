package com.rcdriving.tankrtk

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SettingsScreen(
    minSpeedPercent: Int,
    maxSpeedPercent: Int,
    trimOffset: Int,
    connectionStatus: ConnectionStatus,
    onMinSpeedChange: (Int) -> Unit,
    onMaxSpeedChange: (Int) -> Unit,
    onTrimLeft: () -> Unit,
    onTrimRight: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A2F0A))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ConnectionStatusIndicator(connectionStatus, showLabel = true)
        }

        Spacer(Modifier.height(4.dp))

        Text(
            text = "Access Point: $TANK_AP_SSID",
            color = Color.White,
            fontSize = 14.sp
        )

        Spacer(Modifier.height(24.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            Spacer(Modifier.weight(1f))
            Column(horizontalAlignment = Alignment.End) {
                SpeedField("Min Speed (%)", minSpeedPercent, onMinSpeedChange)
                Spacer(Modifier.height(12.dp))
                SpeedField("Max Speed (%)", maxSpeedPercent, onMaxSpeedChange)
            }
        }

        Spacer(Modifier.height(32.dp))

        Text("Trim", color = Color.White, fontSize = 16.sp)
        Spacer(Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SmallButton("−", onTrimLeft)
            Text("$trimOffset", color = Color.White, fontSize = 20.sp)
            SmallButton("+", onTrimRight)
        }
    }
}

@Composable
private fun SpeedField(label: String, value: Int, onValueChange: (Int) -> Unit) {
    var text by remember(value) { mutableStateOf(value.toString()) }
    Column(horizontalAlignment = Alignment.End) {
        Text(label, color = Color.White, fontSize = 14.sp)
        TextField(
            value = text,
            onValueChange = { new ->
                text = new
                new.toIntOrNull()?.let { onValueChange(it.coerceIn(0, 100)) }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.width(80.dp),
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF145214),
                unfocusedContainerColor = Color(0xFF145214),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )
    }
}

@Composable
fun ConnectionStatusIndicator(status: ConnectionStatus, showLabel: Boolean = false) {
    val (color, label) = when (status) {
        ConnectionStatus.CONNECTED -> Color(0xFF4CAF50) to "Connected"
        ConnectionStatus.CONNECTING -> Color.Yellow to "Connecting..."
        ConnectionStatus.FAILED -> Color.Red to "Connection Failed"
        ConnectionStatus.DISCONNECTED -> Color.Gray to "Disconnected"
    }
    if (showLabel) {
        Text(label, color = color, fontSize = 16.sp)
    }
}
