package com.rcdriving.tankrtk

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TankDriveScreen(
    leftSpeed: Int,
    rightSpeed: Int,
    speedLevel: Int,
    trimOffset: Int,
    onJoystickMove: (Float, Float) -> Unit,
    onStop: () -> Unit,
    onTrimLeft: () -> Unit,
    onTrimRight: () -> Unit,
    onSpeed: () -> Unit,
    turboEnabled: Boolean,
    onTurboToggle: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A2F0A))
            .padding(16.dp)
    ) {
        Text(
            text = "RC Tank Control",
            color = Color.White,
            fontSize = 26.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            textAlign = TextAlign.Center
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            MotorBox("LEFT MOTOR", leftSpeed)
            MotorBox("RIGHT MOTOR", rightSpeed)
        }

        Spacer(Modifier.height(20.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Joystick(
                size = 260.dp,
                onMove = onJoystickMove
            )
        }

        Spacer(Modifier.height(12.dp))

        Text(
            text = "Trim: $trimOffset   |   Speed: $speedLevel/4",
            color = Color.White,
            fontSize = 14.sp,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            SmallButton("◀ TRIM", onTrimLeft)
            StopButton(onStop)
            SmallButton("TRIM ▶", onTrimRight)
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            SmallButton("SPEED $speedLevel/4", onSpeed)
        }

        Spacer(Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ModeToggle("TURBO", turboEnabled) { onTurboToggle(true) }
            ModeToggle("TURTLE", !turboEnabled) { onTurboToggle(false) }
        }
    }
}

@Composable
fun MotorBox(label: String, speed: Int) {
    Column(
        modifier = Modifier
            .width(150.dp)
            .background(Color(0xFF145214), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Text(label, color = Color.White, fontSize = 16.sp)
        Text("Speed: $speed", color = Color.White, fontSize = 20.sp)
    }
}

@Composable
fun SmallButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
        modifier = Modifier.width(90.dp)
    ) {
        Text(text)
    }
}

@Composable
fun StopButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
        modifier = Modifier.width(120.dp)
    ) {
        Text("STOP", color = Color.White, fontSize = 20.sp)
    }
}

@Composable
fun ModeToggle(text: String, active: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (active) Color.Blue else Color.DarkGray
        ),
        modifier = Modifier.width(120.dp)
    ) {
        Text(text, color = Color.White)
    }
}