package com.rcdriving.tankrtk

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val TankColors = darkColorScheme(
    primary = Color(0xFF3D7BFF),
    secondary = Color(0xFF00FF4A),
    background = Color(0xFF20252B),
    surface = Color(0xFF15191F),
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White
)

@Composable
fun TankTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = TankColors,
        typography = MaterialTheme.typography,
        content = content
    )
}
