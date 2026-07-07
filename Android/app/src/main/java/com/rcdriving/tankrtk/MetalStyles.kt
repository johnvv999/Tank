package com.rcdriving.tankrtk

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

// ------------------------------------------------------------
//  GLOBAL METALLIC BACKGROUND — brushed aluminum plate
//  (light diagonal sheen, matches reference image)
// ------------------------------------------------------------
val satinGunmetal = Brush.linearGradient(
    colors = listOf(
        Color(0xFF9A9DA1),
        Color(0xFF7D8083),
        Color(0xFF5C5F63),
        Color(0xFF404346),
        Color(0xFF5C5F63),
        Color(0xFF7D8083),
        Color(0xFF9A9DA1)
    )
)

// ------------------------------------------------------------
//  METALLIC FRAME (used for Speed + Turbo panels)
// ------------------------------------------------------------
val metalFrame = Brush.linearGradient(
    colors = listOf(
        Color(0xFF6A6E72),
        Color(0xFF4A4D50),
        Color(0xFF2E3134)
    )
)

// ------------------------------------------------------------
//  CHROME BUTTON FACE (for + / - and joystick base)
// ------------------------------------------------------------
val buttonMetal = Brush.linearGradient(
    colors = listOf(
        Color(0xFFEAECEE),
        Color(0xFFC3C6C8),
        Color(0xFF8E9092),
        Color(0xFF6A6C6E)
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
//  CHROME CIRCLE BUTTON (+ / -)
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
            .border(2.dp, Color(0xFF9A9C9E), CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color(0xFF2A2C2E),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
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
    size: Dp = 220.dp,
    content: @Composable BoxScope.() -> Unit = {}
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(buttonMetal)
            .border(3.dp, Color(0xFF9A9C9E), CircleShape),
        contentAlignment = Alignment.Center
    ) {

        // Blue LED halo ring
        Box(
            modifier = Modifier
                .size(size)
                .background(blueHalo)
        )

        // Blue LED accent dots around the base ring
        Canvas(modifier = Modifier.size(size)) {
            val dotRadius = size.toPx() * 0.055f
            val ringRadius = size.toPx() * 0.5f * 0.78f
            val angles = listOf(45f, 135f, 225f, 315f)
            for (angleDeg in angles) {
                val rad = Math.toRadians(angleDeg.toDouble())
                val dotCenter = Offset(
                    center.x + (ringRadius * cos(rad)).toFloat(),
                    center.y + (ringRadius * sin(rad)).toFloat()
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF8FC7FF),
                            Color(0xFF4A90E2).copy(alpha = 0.6f),
                            Color(0xFF4A90E2).copy(alpha = 0.0f)
                        ),
                        center = dotCenter,
                        radius = dotRadius * 2.2f
                    ),
                    radius = dotRadius * 2.2f,
                    center = dotCenter
                )
                drawCircle(
                    color = Color(0xFFDFF0FF),
                    radius = dotRadius,
                    center = dotCenter
                )
            }
        }

        content()
    }
}

// ------------------------------------------------------------
//  DIAGONAL CORNER ACCENT (bottom corners of the main panel)
// ------------------------------------------------------------
@Composable
fun CornerAccent(
    modifier: Modifier = Modifier,
    mirrored: Boolean = false
) {
    Canvas(modifier = modifier.size(56.dp)) {
        val w = size.width
        val h = size.height
        val lineColor = Color(0xFF3A3D40)

        val start = if (!mirrored) Offset(0f, h) else Offset(w, h)
        val end = if (!mirrored) Offset(w, 0f) else Offset(0f, 0f)
        drawLine(color = lineColor, start = start, end = end, strokeWidth = 3.dp.toPx())

        val screwCenter = if (!mirrored) {
            Offset(w * 0.3f, h * 0.8f)
        } else {
            Offset(w * 0.7f, h * 0.8f)
        }
        drawCircle(color = Color(0xFF2A2C2E), radius = 6.dp.toPx(), center = screwCenter)
        drawCircle(
            color = Color(0xFF7A7D80),
            radius = 6.dp.toPx(),
            center = screwCenter,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
        )
    }
}
