package com.rcdriving.tankrtk

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SettingsScreen(
    viewModel: TankViewModel,
    connected: Boolean,
    onMain: () -> Unit,
    onRecord: () -> Unit,
    onSettings: () -> Unit
) {
    var minSpeedText by remember { mutableStateOf(viewModel.speedMin.toString()) }
    var maxSpeedText by remember { mutableStateOf(viewModel.speedMax.toString()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(satinGunmetal)
    ) {

        // ------------------------------------------------------------
        // TOP BAR
        // ------------------------------------------------------------
        TopBar(
            connected = connected,
            selectedTab = TopTab.SETTINGS,
            signalStrength = viewModel.signalStrength,
            onMain = onMain,
            onRecord = onRecord,
            onSettings = onSettings
        )

        // ------------------------------------------------------------
        // SETTINGS CONTENT
        // ------------------------------------------------------------
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            Text(
                text = "Speed Range",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            // ------------------------------------------------------------
            // MIN SPEED INPUT
            // ------------------------------------------------------------
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Min:",
                    fontSize = 20.sp,
                    color = Color.White
                )

                TextField(
                    value = minSpeedText,
                    onValueChange = { minSpeedText = it },
                    modifier = Modifier
                        .width(120.dp)
                        .background(Color.DarkGray, RoundedCornerShape(8.dp)),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        color = Color.White,
                        fontSize = 20.sp
                    )
                )
            }

            // ------------------------------------------------------------
            // MAX SPEED INPUT
            // ------------------------------------------------------------
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Max:",
                    fontSize = 20.sp,
                    color = Color.White
                )

                TextField(
                    value = maxSpeedText,
                    onValueChange = { maxSpeedText = it },
                    modifier = Modifier
                        .width(120.dp)
                        .background(Color.DarkGray, RoundedCornerShape(8.dp)),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        color = Color.White,
                        fontSize = 20.sp
                    )
                )
            }

            // ------------------------------------------------------------
            // APPLY BUTTON
            // ------------------------------------------------------------
            Button(
                onClick = {
                    val minVal = minSpeedText.toIntOrNull() ?: viewModel.speedMin
                    val maxVal = maxSpeedText.toIntOrNull() ?: viewModel.speedMax
                    viewModel.setSpeedRange(minVal, maxVal)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .clip(RoundedCornerShape(12.dp)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4A90E2)
                )
            ) {
                Text(
                    text = "Apply",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
