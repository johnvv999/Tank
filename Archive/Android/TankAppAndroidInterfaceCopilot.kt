@Composable
fun TankDriveScreen(
    leftSpeed: Int,
    rightSpeed: Int,
    onJoystickMove: (Float, Float) -> Unit,
    onStop: () -> Unit,
    onTrim: () -> Unit,
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

        // Top Title Bar
        Text(
            text = "RC Tank Control",
            color = Color.White,
            fontSize = 26.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            textAlign = TextAlign.Center
        )

        // Motor Indicators
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            MotorBox("LEFT MOTOR", leftSpeed)
            MotorBox("RIGHT MOTOR", rightSpeed)
        }

        Spacer(Modifier.height(20.dp))

        // Joystick
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

        Spacer(Modifier.height(20.dp))

        // Trim / Stop / Speed Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            SmallButton("TRIM", onTrim)
            StopButton(onStop)
            SmallButton("SPEED", onSpeed)
        }

        Spacer(Modifier.height(20.dp))

        // Turbo / Turtle Toggle
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
s