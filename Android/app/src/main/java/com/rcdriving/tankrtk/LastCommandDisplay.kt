package com.rcdriving.tankrtk

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Replaces the old TurboButton slot — shows the last L/R command
// actually sent to the Arduino, so you can see at a glance what's
// being transmitted (handy for debugging the drive/steering mix).
@Composable
fun LastCommandDisplay(command: String) {

    // Metallic frame gradient (matches SpeedDisplay / TurboButton)
    val frameMetal = Brush.linearGradient(
        colors = listOf(
            Color(0xFF3A3E42),
            Color(0xFF2B2F33),
            Color(0xFF1F2226)
        )
    )

    // Inner glow panel (amber readout, distinct from the green speed display)
    val innerGlow = Brush.linearGradient(
        colors = listOf(
            Color(0xFF0F0A00),
            Color(0xFF2F220A)
        )
    )

    Box(
        modifier = Modifier
            .width(200.dp)
            .height(70.dp)
            .background(frameMetal, RoundedCornerShape(14.dp))
            .border(2.dp, Color(0xFF4A4F52), RoundedCornerShape(14.dp)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp)
                .background(innerGlow, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "LAST CMD",
                    color = Color(0xFFB07A2A),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = command,
                    color = Color(0xFFFFB030),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
