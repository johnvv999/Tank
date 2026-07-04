package com.rcdriving.tankrtk

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun TankScreen(
    viewModel: TankViewModel,
    onJoystickMove: (Float, Float) -> Unit,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF202020))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text("Tank RTK Controller", color = Color.White)

        Spacer(Modifier.height(20.dp))

        RtkOverlay(viewModel)

        Spacer(Modifier.height(20.dp))

        Joystick(
            modifier = Modifier.size(240.dp),
            onMove = onJoystickMove
        )

        Spacer(Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            MotorBar("Left", viewModel.leftMotor.collectAsState().value)
            MotorBar("Right", viewModel.rightMotor.collectAsState().value)
        }

        Spacer(Modifier.height(20.dp))

        Row {
            Button(onClick = onConnect) { Text("Connect") }
            Spacer(Modifier.width(16.dp))
            Button(onClick = onDisconnect) { Text("Disconnect") }
        }
    }
}

@Composable
fun MotorBar(label: String, value: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = Color.White)
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(150.dp)
                .background(Color.DarkGray)
        ) {
            val pct = (value + 100) / 200f
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(pct)
                    .background(if (value >= 0) Color.Green else Color.Red)
                    .align(Alignment.BottomCenter)
            )
        }
        Text("$value", color = Color.White)
    }
}
