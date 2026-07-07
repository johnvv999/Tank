package com.rcdriving.tankrtk

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun SignalStrengthMeter(level: Int) {
    val barColor = when (level) {
        0 -> Color(0xFFFF4A4A)
        1 -> Color(0xFFFFA64A)
        2 -> Color(0xFFFFE14A)
        3 -> Color(0xFF7CFF4A)
        else -> Color(0xFF00FF6A)
    }

    Row(
        modifier = Modifier
            .width(70.dp)
            .height(30.dp)
            .background(
                metalFrame,
                RoundedCornerShape(6.dp)
            )
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        for (i in 0..3) {
            Box(
                modifier = Modifier
                    .width(12.dp)
                    .height((10 + i * 5).dp)
                    .background(
                        if (i < level) barColor else Color(0xFF3A3E42),
                        RoundedCornerShape(3.dp)
                    )
            )
        }
    }
}
