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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight

@Composable
fun SpeedDisplay(speed: Int) {

    // Metallic frame gradient (matches reference image)
    val frameMetal = Brush.linearGradient(
        colors = listOf(
            Color(0xFF3A3E42),
            Color(0xFF2B2F33),
            Color(0xFF1F2226)
        )
    )

    // Inner glow panel (green text area)
    val innerGlow = Brush.linearGradient(
        colors = listOf(
            Color(0xFF0A0F0A),
            Color(0xFF1A2F1A)
        )
    )

    Box(
        modifier = Modifier
            .width(260.dp)
            .height(70.dp)
            .background(frameMetal, RoundedCornerShape(12.dp))
            .border(2.dp, Color(0xFF4A4F52), RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp)
                .background(innerGlow, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Speed ${speed}%",
                color = Color(0xFF00FF6A),
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
