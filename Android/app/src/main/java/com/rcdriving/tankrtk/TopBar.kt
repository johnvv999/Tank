package com.rcdriving.tankrtk

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
            .height(60.dp)
            .background(satinGunmetal),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        // LEFT SIDE: Connection + Signal Strength
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(start = 20.dp)
        ) {
            val color = if (connected) Color(0xFF00FF6A) else Color(0xFFFF4A4A)
            val label = if (connected) "CONNECTED" else "DISCONNECTED"

            Text(
                text = label,
                color = color,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            // ⭐ NEW: Signal Strength Meter
            SignalStrengthMeter(signalStrength)
        }

        // RIGHT SIDE: Tabs
        Row(
            horizontalArrangement = Arrangement.spacedBy(30.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(end = 20.dp)
        ) {
            TabButton("MAIN", selectedTab == TopTab.MAIN, onMain)
            TabButton("RECORD", selectedTab == TopTab.RECORD, onRecord)
            TabButton("SETTINGS", selectedTab == TopTab.SETTINGS, onSettings)
        }
    }
}

@Composable
fun TabButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Text(
        text = text,
        color = if (selected) Color(0xFF4A90E2) else Color.White,
        fontSize = 18.sp,
        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
        modifier = Modifier.clickable { onClick() }
    )
}
