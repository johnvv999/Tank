package com.rcdriving.tankrtk

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.Color

@Composable
fun RtkOverlay(viewModel: TankViewModel) {
    val fix = viewModel.rtkFix.collectAsState().value
    val pos = viewModel.enuPosition.collectAsState().value

    Column {
        Text(
            text = if (fix != null) "RTK: FIX" else "RTK: NO FIX",
            color = Color.White
        )
        if (pos != null) {
            Text("ENU: x=${pos.x.toInt()} y=${pos.y.toInt()} z=${pos.z.toInt()}", color = Color.White)
        }
    }
}
