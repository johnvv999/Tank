package com.rcdriving.tankrtk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {

    private val tankWifiClient by lazy {
        TankWifiClient("192.168.4.1", 9000)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tankWifiClient.connect()

        setContent {
            val vm: TankViewModel = viewModel()
            var turboEnabled by remember { mutableStateOf(true) }
            var showSettings by remember { mutableStateOf(false) }

            val leftSpeed by vm.leftMotor.collectAsState()
            val rightSpeed by vm.rightMotor.collectAsState()
            val speedLevel by vm.speedLevel.collectAsState()
            val trimOffset by vm.trimOffset.collectAsState()
            val connectionStatus by tankWifiClient.status.collectAsState()

            if (showSettings) {
                SettingsScreen(
                    speedLevel = speedLevel,
                    trimOffset = trimOffset,
                    connectionStatus = connectionStatus,
                    onTrimLeft = { vm.adjustTrim(-5) },
                    onTrimRight = { vm.adjustTrim(5) },
                    onSpeed = { vm.cycleSpeed() },
                    onBack = { showSettings = false }
                )
            } else {
                TankDriveScreen(
                    leftSpeed = leftSpeed,
                    rightSpeed = rightSpeed,
                    connectionStatus = connectionStatus,
                    onJoystickMove = { x, y ->
                        val turboScale = if (turboEnabled) 1f else 0.5f
                        val speedScale = vm.currentSpeedScale()
                        val scale = turboScale * speedScale

                        val (rawLeft, rawRight) = mixTankDrive(x * scale, y * scale)
                        val (leftPWM, rightPWM) = applyTrim(rawLeft, rawRight, trimOffset)

                        tankWifiClient.send(formatTankCommand(leftPWM, rightPWM))
                        vm.setMotors(leftPWM, rightPWM)
                    },
                    onStop = {
                        tankWifiClient.send(formatTankCommand(0, 0))
                        vm.setMotors(0, 0)
                    },
                    turboEnabled = turboEnabled,
                    onTurboToggle = { turboEnabled = it },
                    onOpenSettings = { showSettings = true }
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        tankWifiClient.disconnect()
    }
}