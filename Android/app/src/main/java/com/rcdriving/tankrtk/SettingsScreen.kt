package com.rcdriving.tankrtk

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SettingsScreen(
    viewModel: TankViewModel,
    connected: Boolean,
    status: ConnectionStatus,
    onMain: () -> Unit,
    onRecord: () -> Unit,
    onSettings: () -> Unit,
    onConnectTankAp: () -> Unit,
    onDisconnectTankAp: () -> Unit
) {
    var minSpeedText by remember { mutableStateOf(viewModel.speedMin.toString()) }
    var maxSpeedText by remember { mutableStateOf(viewModel.speedMax.toString()) }

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    // Shared by the Apply button and the keyboard's Enter/Done key, so
    // hitting Enter behaves exactly like tapping Apply — no need to hit
    // the phone's back arrow to get the value to take effect.
    fun applyAndDismiss() {
        val minVal = minSpeedText.toIntOrNull() ?: viewModel.speedMin
        val maxVal = maxSpeedText.toIntOrNull() ?: viewModel.speedMax
        viewModel.setSpeedRange(minVal, maxVal)

        // setSpeedRange clamps to 0-255 (Min/Max are raw PWM values sent
        // straight to the motor drivers — 255 is the hardware's absolute
        // ceiling, so there's no such thing as "300"). Without this, the
        // text field kept showing whatever was typed until the screen
        // was left and re-entered, which made the clamp look like a
        // delayed, unexplained reset. Syncing here shows the real,
        // clamped value immediately instead.
        minSpeedText = viewModel.speedMin.toString()
        maxSpeedText = viewModel.speedMax.toString()

        keyboardController?.hide()
        focusManager.clearFocus()
    }

    // Black container for both fields, in every focus state — the default
    // Material3 TextField background otherwise overrides a plain
    // Modifier.background() with its own light container color.
    val blackFieldColors = TextFieldDefaults.colors(
        focusedContainerColor = Color.Black,
        unfocusedContainerColor = Color.Black,
        disabledContainerColor = Color.Black,
        cursorColor = Color.White,
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        focusedIndicatorColor = Color.White,
        unfocusedIndicatorColor = Color.Gray
    )

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
        // SPEED RANGE (left) + TRIM (right) — side by side in the
        // remaining landscape width instead of stacked vertically, so
        // neither section competes for the screen's limited height.
        // ------------------------------------------------------------
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {

            // -- SPEED RANGE --------------------------------------------
            Column(
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                    text = "Speed Range",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Min:",
                        fontSize = 20.sp,
                        color = Color(0xFF1A1A1A)
                    )

                    TextField(
                        value = minSpeedText,
                        onValueChange = { minSpeedText = it },
                        modifier = Modifier
                            .width(120.dp)
                            .background(Color.Black, RoundedCornerShape(8.dp))
                            // Tapping away (e.g. to hit Connect/Disconnect)
                            // used to leave whatever was mid-typed —
                            // including blank, if the field had been
                            // cleared — sitting there un-applied. Losing
                            // focus now applies/resyncs the same as Done.
                            .onFocusChanged { if (!it.isFocused) applyAndDismiss() },
                        textStyle = androidx.compose.ui.text.TextStyle(
                            color = Color.White,
                            fontSize = 20.sp
                        ),
                        colors = blackFieldColors,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { applyAndDismiss() }
                        )
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Max:",
                        fontSize = 20.sp,
                        color = Color(0xFF1A1A1A)
                    )

                    TextField(
                        value = maxSpeedText,
                        onValueChange = { maxSpeedText = it },
                        modifier = Modifier
                            .width(120.dp)
                            .background(Color.Black, RoundedCornerShape(8.dp))
                            .onFocusChanged { if (!it.isFocused) applyAndDismiss() },
                        textStyle = androidx.compose.ui.text.TextStyle(
                            color = Color.White,
                            fontSize = 20.sp
                        ),
                        colors = blackFieldColors,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { applyAndDismiss() }
                        )
                    )
                }
            }

            // -- TRIM -----------------------------------------------------
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Trim",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    MetalCircleButton3D(
                        text = "-",
                        onClick = { viewModel.decreaseTrim() },
                        modifier = Modifier.size(56.dp)
                    )

                    Box(
                        modifier = Modifier
                            .width(90.dp)
                            .height(56.dp)
                            .background(Color.Black, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (viewModel.trim > 0) "+${viewModel.trim}" else "${viewModel.trim}",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    MetalCircleButton3D(
                        text = "+",
                        onClick = { viewModel.increaseTrim() },
                        modifier = Modifier.size(56.dp)
                    )
                }

                // -- WIFI CONNECT / DISCONNECT TOGGLE --------------------
                // Single button, centered under Trim (Column above is
                // already horizontalAlignment = CenterHorizontally). Its
                // color/label reflect current state instead of showing
                // two separate buttons.
                WifiToggleButton(
                    status = status,
                    onClick = {
                        // Only a fully-idle DISCONNECTED state attempts a
                        // new connection. CONNECTED, CONNECTING, and FAILED
                        // are all "currently tied up with TankAP in some
                        // way" — tapping in any of those releases the
                        // TankAP binding and lets the phone fall back to
                        // its normal WiFi, rather than retrying a TCP
                        // connect that already failed once.
                        if (status == ConnectionStatus.DISCONNECTED) onConnectTankAp() else onDisconnectTankAp()
                    },
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

// ------------------------------------------------------------
// WIFI CONNECT / DISCONNECT TOGGLE — reflects the actual connection
// status, not just a plain connected/disconnected boolean. A tap
// triggers connect or disconnect immediately, but the real TCP/WiFi
// handshake can take a moment (up to a few seconds) to actually land —
// showing a distinct amber "Connecting…" state right away means the
// button visibly reacts to the tap instead of appearing to do nothing
// until the connection either succeeds or fails.
// ------------------------------------------------------------
@Composable
private fun WifiToggleButton(
    status: ConnectionStatus,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when (status) {
        ConnectionStatus.CONNECTED -> Color(0xFF2E7D32)
        ConnectionStatus.CONNECTING -> Color(0xFFB8860B)
        ConnectionStatus.FAILED, ConnectionStatus.DISCONNECTED -> Color(0xFFB71C1C)
    }
    val label = when (status) {
        ConnectionStatus.CONNECTED -> "Connected"
        ConnectionStatus.CONNECTING -> "Connecting…"
        ConnectionStatus.FAILED -> "Failed — Tap to Disconnect"
        ConnectionStatus.DISCONNECTED -> "Disconnected"
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(backgroundColor)
            .border(2.dp, Color.Black.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 28.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
