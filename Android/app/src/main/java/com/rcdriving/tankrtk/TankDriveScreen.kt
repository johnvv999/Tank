package com.rcdriving.tankrtk

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import kotlin.math.sqrt

// ── Design tokens ─────────────────────────────────────────────────────────────
private val BgDark        = Color(0xFF1A1D22)
private val BgMid         = Color(0xFF23272E)
private val BgLight       = Color(0xFF2D323B)
private val AccentBlue    = Color(0xFF2A6FFF)
private val AccentBlueLit = Color(0xFF5A9FFF)
private val GreenLed      = Color(0xFF00FF6A)
private val TextPrimary   = Color(0xFFE0E8FF)
private val TextMuted     = Color(0xFF606880)
private val RimLight      = Color(0xFF454C5A)
private val RimDark       = Color(0xFF12151A)

@Composable
fun TankDriveScreen(
    modifier: Modifier = Modifier,
    leftSpeed: Int = 0,
    rightSpeed: Int = 0,
    connectionStatus: ConnectionStatus = ConnectionStatus.DISCONNECTED,
    speedPercent: Int = 0,
    onJoystickMove: (Float, Float) -> Unit = { _, _ -> },
    onStop: () -> Unit = {},
    turboEnabled: Boolean = true,
    onToggleTurbo: () -> Unit = {},
    onIncreaseSpeed: () -> Unit = {},
    onDecreaseSpeed: () -> Unit = {},
    onSpeedChange: (Int) -> Unit = {},
    onModeChange: (Boolean) -> Unit = {},
) {
    val speedState    = remember { mutableIntStateOf(0) }
    val isTurboState  = remember { mutableStateOf(true) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF1C2028), Color(0xFF13161B))
                )
            )
    ) {
        // Brushed-metal texture overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to Color(0x08FFFFFF),
                            0.3f to Color(0x04FFFFFF),
                            0.6f to Color(0x06FFFFFF),
                            1.0f to Color(0x02FFFFFF)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            // Status bar
            ConnectionBar(connected = connectionStatus == ConnectionStatus.CONNECTED)

            Spacer(modifier = Modifier.height(14.dp))

            // Speed readout
            SpeedDisplay(speed = speedState.intValue)

            Spacer(modifier = Modifier.height(20.dp))

            // Main control row
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                // Left: +/- and TURBO
                LeftPanel(
                    isTurbo = isTurboState.value,
                    onTurboToggle = {
                        isTurboState.value = !isTurboState.value
                        onToggleTurbo()
                        onModeChange(isTurboState.value)
                    },
                    onSpeedChange = { delta ->
                        val newSpeed = (speedState.intValue + delta).coerceIn(0, 100)
                        speedState.intValue = newSpeed
                        onSpeedChange(newSpeed)
                        if (delta > 0) onIncreaseSpeed() else onDecreaseSpeed()
                    }
                )

                // Right: joystick
                JoystickPad(onMove = onJoystickMove)
            }
        }
    }
}

// ── Connection status bar ─────────────────────────────────────────────────────
@Composable
private fun ConnectionBar(connected: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        // Pulsing LED dot
        Box(
            modifier = Modifier
                .size(9.dp)
                .clip(CircleShape)
                .background(
                    if (connected)
                        Brush.radialGradient(listOf(GreenLed, Color(0xFF008040)))
                    else
                        Brush.radialGradient(listOf(Color(0xFFFF4040), Color(0xFF800000)))
                )
                .border(0.5.dp, Color(0x40FFFFFF), CircleShape)
        )
        Spacer(modifier = Modifier.width(7.dp))
        Text(
            text = if (connected) "CONNECTED" else "DISCONNECTED",
            color = if (connected) GreenLed else Color(0xFFFF6060),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
    }
}

// ── Speed display ─────────────────────────────────────────────────────────────
@Composable
private fun SpeedDisplay(speed: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth(0.55f)
            .height(52.dp)
            .shadow(6.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF0E1116), Color(0xFF1E232C))
                )
            )
            .border(
                1.dp,
                Brush.linearGradient(listOf(RimLight, RimDark, RimLight)),
                RoundedCornerShape(12.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        // Inset glow effect
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0x1400FF6A), Color.Transparent),
                        radius = 200f
                    )
                )
        )
        Text(
            text = "Speed  $speed%",
            color = GreenLed,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp,
            textAlign = TextAlign.Center
        )
    }
}

// ── Left panel: speed buttons + turbo ────────────────────────────────────────
@Composable
private fun LeftPanel(
    isTurbo: Boolean,
    onTurboToggle: () -> Unit,
    onSpeedChange: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(110.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        // TURBO / TURTLE pill
        TurboToggle(isTurbo = isTurbo, onClick = onTurboToggle)

        Spacer(modifier = Modifier.height(20.dp))

        MetalButton(label = "+") { onSpeedChange(+5) }

        Spacer(modifier = Modifier.height(18.dp))

        MetalButton(label = "−") { onSpeedChange(-5) }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun TurboToggle(isTurbo: Boolean, onClick: () -> Unit) {
    val accent = if (isTurbo) AccentBlue else Color(0xFF8B4513)
    val accentLit = if (isTurbo) AccentBlueLit else Color(0xFFD2691E)
    val label = if (isTurbo) "TURBO" else "TURTLE"

    Box(
        modifier = Modifier
            .width(105.dp)
            .height(36.dp)
            .shadow(8.dp, RoundedCornerShape(18.dp))
            .clip(RoundedCornerShape(18.dp))
            .background(Brush.linearGradient(listOf(accent, accentLit)))
            .border(1.dp, Color(0x60FFFFFF), RoundedCornerShape(18.dp))
            .pointerInput(Unit) { detectDragGestures { _, _ -> } }
            ,
        contentAlignment = Alignment.Center
    ) {
        // Highlight streak
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0x30FFFFFF), Color.Transparent)
                    )
                )
        )
       
        Button(
            onClick = onClick,
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues(0.dp),
            elevation = null
        ) {
            Text(
                text = label,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.5.sp
            )
        }
    }
}

// ── Metallic circular button ──────────────────────────────────────────────────
@Composable
private fun MetalButton(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(58.dp)
            .shadow(10.dp, CircleShape)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF3A3F4A), Color(0xFF22262E))
                )
            )
            .border(
                1.5.dp,
                Brush.linearGradient(
                    listOf(Color(0xFF5A606E), Color(0xFF18191F), Color(0xFF4A5060))
                ),
                CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        // Top highlight
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(22.dp)
                .align(Alignment.TopCenter)
                .clip(CircleShape)
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0x28FFFFFF), Color.Transparent)
                    )
                )
        )
        Button(
            onClick = onClick,
            modifier = Modifier.fillMaxSize(),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues(0.dp),
            elevation = null
        ) {
            Text(
                text = label,
                color = TextPrimary,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 26.sp
            )
        }
    }
}

// ── Joystick ──────────────────────────────────────────────────────────────────
@Composable
private fun JoystickPad(onMove: (Float, Float) -> Unit) {
    val padSize = 180.dp
    val knobSize = 72.dp
    val maxOffset = 54f

    var knobOffset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = Modifier
            .size(padSize)
            .shadow(14.dp, CircleShape)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    listOf(Color(0xFF282D36), Color(0xFF0F1216))
                )
            )
            .border(
                2.dp,
                Brush.sweepGradient(
                    listOf(
                        Color(0xFF4A5060), Color(0xFF22262E),
                        Color(0xFF4A5060), Color(0xFF22262E), Color(0xFF4A5060)
                    )
                ),
                CircleShape
            )
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        knobOffset = Offset.Zero
                        onMove(0f, 0f)
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val newOffset = knobOffset + dragAmount
                        val dist = sqrt(newOffset.x * newOffset.x + newOffset.y * newOffset.y)
                        knobOffset = if (dist <= maxOffset) newOffset
                                     else Offset(newOffset.x / dist * maxOffset,
                                                 newOffset.y / dist * maxOffset)
                        onMove(knobOffset.x / maxOffset, -knobOffset.y / maxOffset)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // Ring accent lines
        Box(
            modifier = Modifier
                .size(padSize - 18.dp)
                .clip(CircleShape)
                .border(0.5.dp, Color(0x20FFFFFF), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(padSize - 40.dp)
                .clip(CircleShape)
                .border(0.5.dp, Color(0x10FFFFFF), CircleShape)
        )

        // Blue ring glow at bottom
        Box(
            modifier = Modifier
                .size(padSize - 10.dp)
                .clip(CircleShape)
                .border(
                    2.dp,
                    Brush.sweepGradient(
                        listOf(
                            Color.Transparent, Color.Transparent,
                            AccentBlue.copy(alpha = 0.6f),
                            AccentBlueLit.copy(alpha = 0.8f),
                            AccentBlue.copy(alpha = 0.6f),
                            Color.Transparent
                        )
                    ),
                    CircleShape
                )
        )

        // Knob
        Box(
            modifier = Modifier
                .offset { IntOffset(knobOffset.x.roundToInt(), knobOffset.y.roundToInt()) }
                .size(knobSize)
                .shadow(12.dp, CircleShape)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(Color(0xFF4A505E), Color(0xFF22262E))
                    )
                )
                .border(
                    1.5.dp,
                    Brush.linearGradient(
                        listOf(Color(0xFF6A707E), Color(0xFF18191F))
                    ),
                    CircleShape
                )
        ) {
            // Knob top highlight
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
                    .align(Alignment.TopCenter)
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0x35FFFFFF), Color.Transparent)
                        )
                    )
            )
        }
    }
}

// ── Shared component used by SettingsScreen ───────────────────────────────────
@Composable
fun SmallButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(36.dp)
            .shadow(6.dp, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .background(
                Brush.linearGradient(listOf(Color(0xFF3A3F4A), Color(0xFF22262E)))
            )
            .border(1.dp, Color(0xFF4A5060), RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues(horizontal = 12.dp),
            elevation = null
        ) {
            Text(
                text = text,
                color = TextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Preview(
    name = "Tank Drive Screen",
    showBackground = true,
    backgroundColor = 0xFF1A1D22,
    widthDp = 800,
    heightDp = 480
)
@Composable
fun TankDriveScreenPreview() {
    TankDriveScreen()
}
