package com.rcdriving.tankrtk

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TankDriveScreen(
    leftSpeed: Int,
    rightSpeed: Int,
    connectionStatus: ConnectionStatus,
    speedPercent: Int,
    onJoystickMove: (Float, Float) -> Unit,
    onStop: () -> Unit,
    turboEnabled: Boolean,
    onToggleTurbo: () -> Unit,
    onIncreaseSpeed: () -> Unit,
    onDecreaseSpeed: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A2F0A))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ConnectionStatusIndicator(connectionStatus, showLabel = true)
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            MotorBox("LEFT MOTOR", leftSpeed)
            MotorBox("RIGHT MOTOR", rightSpeed)
        }

        Spacer(Modifier.height(20.dp))

        Box(
            modifier = Modifier.fillMaxWidth().weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Joystick(size = 260.dp, onMove = onJoystickMove)
        }

        Spacer(Modifier.height(20.dp))

        Box(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    TinyButton("+", onIncreaseSpeed)
                    Spacer(Modifier.height(4.dp))
                    Text("$speedPercent%", color = Color.White, fontSize = 14.sp)
                    Spacer(Modifier.height(4.dp))
                    TinyButton("−", onDecreaseSpeed)
                }

                Spacer(Modifier.width(16.dp))

                Button(
                    onClick = onToggleTurbo,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (turboEnabled) Color.Blue else Color.DarkGray
                    ),
                    modifier = Modifier.width(140.dp)
                ) {
                    Text(if (turboEnabled) "TURBO" else "TURTLE", color = Color.White)
                }
            }

            StopOctagonButton(
                onClick = onStop,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-8).dp, y = (-30).dp)
                    .size(100.dp)
            )
        }

        Spacer(Modifier.height(24.dp))
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
fun TinyButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
        modifier = Modifier.size(64.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(text, fontSize = 24.sp)
    }
}

private val OctagonShape = GenericShape { size, _ ->
    val w = size.width
    val h = size.height
    val cut = minOf(w, h) * 0.293f
    moveTo(cut, 0f)
    lineTo(w - cut, 0f)
    lineTo(w, cut)
    lineTo(w, h - cut)
    lineTo(w - cut, h)
    lineTo(cut, h)
    lineTo(0f, h - cut)
    lineTo(0f, cut)
    close()
}

@Composable
fun StopOctagonButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(OctagonShape)
            .background(Color.Red)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text("STOP", color = Color.White, fontSize = 15.sp, textAlign = TextAlign.Center)
    }
}
