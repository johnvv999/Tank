package com.rcdriving.tankrtk

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TurboButton(active: Boolean, onClick: () -> Unit) {

    // Outer metallic frame (matches reference image)
    val turboFrame = Brush.linearGradient(
        colors = listOf(
            Color(0xFF3A3E42),
            Color(0xFF2B2F33),
            Color(0xFF1F2226)
        )
    )

    // Inner glowing blue panel (brighter when active)
    val turboGlow = if (active) {
        Brush.radialGradient(
            colors = listOf(
                Color(0xFF6AB4FF),
                Color(0xFF0055AA)
            )
        )
    } else {
        Brush.radialGradient(
            colors = listOf(
                Color(0xFF4A90E2),
                Color(0xFF003366)
            )
        )
    }

    Box(
        modifier = Modifier
            .width(200.dp)
            .height(70.dp)
            .background(turboFrame, RoundedCornerShape(14.dp))
            .border(2.dp, if (active) Color(0xFF9AD0FF) else Color(0xFF4A4F52), RoundedCornerShape(14.dp)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp)
                .background(turboGlow, RoundedCornerShape(10.dp))
                .clickableNoRipple { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "TURBO",
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// No‑ripple clickable modifier (keeps metallic look clean)
fun Modifier.clickableNoRipple(onClick: () -> Unit): Modifier =
    this.clickable(
        indication = null,
        interactionSource = MutableInteractionSource(),
        onClick = onClick
    )
