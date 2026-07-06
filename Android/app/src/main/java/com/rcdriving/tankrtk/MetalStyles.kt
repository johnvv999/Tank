package com.rcdriving.tankrtk

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight

// ------------------------------------------------------------
//  GLOBAL METALLIC BACKGROUND (matches reference image)
// ------------------------------------------------------------
val satinGunmetal = Brush.linearGradient(
    colors = listOf(
        Color(0xFF2C2F33),
        Color(0xFF1F2124),
        Color(0xFF2C2F33)
    )
)

// ------------------------------------------------------------
//  METALLIC FRAME (used for Speed + Turbo panels)
// ------------------------------------------------------------
val metalFrame = Brush.linearGradient(
    colors = listOf(
        Color(0xFF3A3E42),
        Color(0xFF2B2F33),
        Color(0xFF1F2226)
    )
)

// ------------------------------------------------------------
//  METALLIC BUTTON BEVEL (for + / - buttons)
// ------------------------------------------------------------
val buttonMetal = Brush.linearGradient(
    colors = listOf(
        Color(0xFF4A4F52),
        Color(0xFF2F3336),
        Color(0xFF1C1F21)
    )
)

// ------------------------------------------------------------
//  BLUE LED HALO (joystick ring)
// ------------------------------------------------------------
val blueHalo = Brush.radialGradient(
    colors = listOf(
        Color(0xFF4A90E2).copy(alpha = 0.9f),
        Color(0xFF003366).copy(alpha = 0.0f)
    )
)

// ------------------------------------------------------------
//  METAL PANEL CONTAINER
// ------------------------------------------------------------
@Composable
fun MetalPanel(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .background(satinGunmetal),
        content = content
    )
}

// ------------------------------------------------------------
//  METALLIC CIRCLE BUTTON (+ / -)
// ------------------------------------------------------------
@Composable
fun MetalCircleButton3D(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(buttonMetal)
            .border(2.dp, Color(0xFF4A4F52), CircleShape)
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2F3336)
            )
        ) {
            Text(
                text = text,
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ------------------------------------------------------------
//  ⭐ RESTORED METALLIC RECTANGULAR BUTTON (Option A)
// ------------------------------------------------------------
@Composable
fun MetalButton3D(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bevel = Brush.linearGradient(
        colors = listOf(
            Color(0xFF4A4F52),   // top bevel
            Color(0xFF2F3336),   // mid metal
            Color(0xFF1C1F21)    // bottom shadow
        )
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(bevel)
            .border(2.dp, Color(0xFF4A4F52), RoundedCornerShape(14.dp))
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(14.dp)),
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
}

// ------------------------------------------------------------
//  METALLIC JOYSTICK BASE + BLUE LED RING
// ------------------------------------------------------------
@Composable
fun MetalJoystickBase(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit = {}
) {
    Box(
        modifier = modifier
            .size(220.dp)
            .clip(CircleShape)
            .background(buttonMetal)
            .border(3.dp, Color(0xFF4A4F52), CircleShape),
        contentAlignment = Alignment.Center
    ) {

        // Blue LED halo ring
        Box(
            modifier = Modifier
                .size(220.dp)
                .background(blueHalo)
        )

        content()
    }
}
