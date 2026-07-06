package com.rcdriving.tankrtk.ui.screens  // <-- change if your folder path differs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TankDriveScreen(
    modifier: Modifier = Modifier,
    onSpeedChange: (Int) -> Unit = {},
    onModeChange: (Boolean) -> Unit = {}, // true = TURBO, false = TURTLE
) {
    val speedState = remember { mutableIntStateOf(0) }
    val isTurboState = remember { mutableStateOf(true) }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color(0xFF20252B)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            TopBar()

            Spacer(modifier = Modifier.height(12.dp))

            SpeedWindow(speed = speedState.intValue)

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                LeftControls(
                    isTurbo = isTurboState.value,
                    onTurboToggle = {
                        isTurboState.value = !isTurboState.value
                        onModeChange(isTurboState.value)
                    },
                    onSpeedChange = { delta ->
                        val newSpeed = (speedState.intValue + delta).coerceIn(0, 100)
                        speedState.intValue = newSpeed
                        onSpeedChange(newSpeed)
                    }
                )

                RightJoystick()
            }
        }
    }
}

@Composable
private fun TopBar() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF00FF4A))
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "CONNECTED",
                color = Color(0xFF00FF4A),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Row {
            TabChip(text = "MAIN", selected = true)
            Spacer(modifier = Modifier.width(8.dp))
            TabChip(text = "RECORD", selected = false)
            Spacer(modifier = Modifier.width(8.dp))
            TabChip(text = "SETTINGS", selected = false)
        }
    }
}

@Composable
private fun TabChip(text: String, selected: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = text,
            color = if (selected) Color(0xFFB0C4FF) else Color(0xFF808890),
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
        if (selected) {
            Spacer(modifier = Modifier.height(2.dp))
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(2.dp)
                    .background(Color(0xFF3D7BFF))
            )
        }
    }
}

@Composable
private fun SpeedWindow(speed: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth(0.6f)
            .height(60.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF15191F),
                        Color(0xFF252A33)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Speed $speed%",
            color = Color(0xFF00FF4A),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun LeftControls(
    isTurbo: Boolean,
    onTurboToggle: () -> Unit,
    onSpeedChange: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(120.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        TurboButton(
            isTurbo = isTurbo,
            onClick = onTurboToggle
        )

        Spacer(modifier = Modifier.height(16.dp))

        IconButtonCircle(
            text = "+",
            onClick = { onSpeedChange(+5) }
        )

        Spacer(modifier = Modifier.height(24.dp))

        IconButtonCircle(
            text = "-",
            onClick = { onSpeedChange(-5) }
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun TurboButton(
    isTurbo: Boolean,
    onClick: () -> Unit
) {
    val label = if (isTurbo) "TURBO" else "TURTLE"
    val color = if (isTurbo) Color(0xFF3D7BFF) else Color(0xFFFFA63D)

    Button(
        onClick = onClick,
        modifier = Modifier
            .width(110.dp)
            .height(32.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = color
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(
            text = label,
            color = Color.Black,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun IconButtonCircle(
    text: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.size(52.dp),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF3A3F48)
        )
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun RightJoystick() {
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(160.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(
            modifier = Modifier
                .size(140.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF1A1F26),
                            Color(0xFF0D1116)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF3D7BFF)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "JOYSTICK",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Preview(
    name = "Tank Drive Screen Preview",
    showBackground = true,
    backgroundColor = 0xFF20252B,
    widthDp = 420,
    heightDp = 800
)
@Composable
fun TankDriveScreenPreview() {
    TankDriveScreen()
}
