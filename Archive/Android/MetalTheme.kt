package com.rcdriving.tankrtk

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Satin gunmetal palette (A1)
private val GunmetalColors = darkColorScheme(
    primary = Color(0xFF4A90E2),      // Soft blue accent
    secondary = Color(0xFF00FF6A),    // Green LED / status
    background = Color(0xFF2B2F33),   // Satin gunmetal base
    surface = Color(0xFF1F2226),      // Darker gunmetal panels
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White
)

@Composable
fun MetalTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = GunmetalColors,
        typography = MaterialTheme.typography,
        content = content
    )
}
