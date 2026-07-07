package com.rcdriving.tankrtk

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class TopTab { MAIN, RECORD, SETTINGS }

@Composable
fun TopBar(
    connected: Boolean,
    selectedTab: TopTab,
    signalStrength: Int,
    onMain: () -> Unit,
    onRecord: () -> Unit,
    onSettings: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        // LEFT SIDE: Connection + Signal Strength
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(start = 4.dp)
        ) {
            val color = if (connected) Color(0xFF00A050) else Color(0xFFCC2A2A)
            val label = if (connected) "CONNECTED" else "DISCONNECTED"

            Text(
                text = label,
                color = color,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            SignalStrengthMeter(signalStrength)
        }

        // RIGHT SIDE: Tab chips
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(end = 4.dp)
        ) {
            TabChip("MAIN", selectedTab == TopTab.MAIN, onMain)
            TabChip("RECORD", selectedTab == TopTab.RECORD, onRecord)
            TabChip("SETTINGS", selectedTab == TopTab.SETTINGS, onSettings)
        }
    }
}

@Composable
fun TabChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val bevel = Brush.linearGradient(
        colors = listOf(
            Color(0xFF44484C),
            Color(0xFF232629)
        )
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bevel)
            .border(1.dp, Color(0xFF5A5D60), RoundedCornerShape(8.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 18.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            color = if (selected) Color(0xFF4A90E2) else Color.White,
            fontSize = 15.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )

        Spacer(modifier = Modifier.height(3.dp))

        Box(
            modifier = Modifier
                .height(2.dp)
                .width(28.dp)
                .background(if (selected) Color(0xFF4A90E2) else Color.Transparent)
        )
    }
}

// Kept for backward compatibility with any existing call sites.
@Composable
fun TabButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    TabChip(text, selected, onClick)
}
