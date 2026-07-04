package com.rcdriving.tankrtk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material3.*
import androidx.compose.runtime.*

class MainActivity : ComponentActivity() {

    private val tankWifiClient by lazy {
        TankWifiClient("192.168.4.1", 9000)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val vm: TankViewModel = viewModel()

            TankScreen(
                viewModel = vm,
                onJoystickMove = { x, y ->
                    val (leftPWM, rightPWM) = mixTankDrive(x, y)
                    val cmd = formatTankCommand(leftPWM, rightPWM)
                    tankWifiClient.send(cmd)
                    vm.setMotors(leftPWM, rightPWM)
                },
                onConnect = { tankWifiClient.connect() },
                onDisconnect = { tankWifiClient.disconnect() }
            )
        }
    }
}
