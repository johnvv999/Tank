package com.rcdriving.tankrtk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    private val tankWifiClient by lazy {
        TankWifiClient("192.168.4.1", 9000)
    }

    private val odometryTracker = OdometryTracker()
    private val pathReplayer by lazy { PathReplayer(tankWifiClient) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tankWifiClient.connect()

        setContent {
            val vm: TankViewModel = viewModel()
            var turboEnabled by remember { mutableStateOf(true) }
            var activeTab by remember { mutableStateOf(AppTab.MAIN) }

            val leftSpeed by vm.leftMotor.collectAsState()
            val rightSpeed by vm.rightMotor.collectAsState()
            val minSpeedPercent by vm.minSpeedPercent.collectAsState()
            val maxSpeedPercent by vm.maxSpeedPercent.collectAsState()
            val speedPercent = (vm.currentSpeedScale() * 100).toInt()
	    val currentSpeedPercent by vm.currentSpeedPercent.collectAsState()
            val trimOffset by vm.trimOffset.collectAsState()
            val connectionStatus by tankWifiClient.status.collectAsState()
            val isRecording by vm.isRecording.collectAsState()
            val isPlaying by vm.isPlaying.collectAsState()
            val recordedPath by vm.recordedPath.collectAsState()
            val playbackPath by vm.playbackPath.collectAsState()

            LaunchedEffect(isRecording) {
                if (isRecording) {
                    odometryTracker.reset()
                    vm.clearRecordedPath()
                    vm.appendRecordedPoint(odometryTracker.points.first())
                    while (isRecording) {
                        delay(200)
                        odometryTracker.sample(leftSpeed, rightSpeed, 0.2)
                        vm.appendRecordedPoint(odometryTracker.points.last())
                    }
                }
            }

            LaunchedEffect(isPlaying) {
                if (isPlaying) {
                    vm.clearPlaybackPath()
                    val path = Path(recordedPath)
                    pathReplayer.replay(path) { count ->
                        vm.setPlaybackPath(recordedPath.take(count))
                    }
                    vm.setPlaying(false)
                }
            }

            AppTabs(
                activeTab = activeTab,
                onTabSelected = { activeTab = it },
                mainContent = {
                    TankDriveScreen(
                        leftSpeed = leftSpeed,
                        rightSpeed = rightSpeed,
                        connectionStatus = connectionStatus,
                        speedPercent = speedPercent,
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
                        onToggleTurbo = { turboEnabled = !turboEnabled },
                        onIncreaseSpeed = { vm.increaseSpeed() },
                        onDecreaseSpeed = { vm.decreaseSpeed() }
                    )
                },
                recordContent = {
                    RecordScreen(
                        recordedPath = recordedPath,
                        playbackPath = playbackPath,
                        isRecording = isRecording,
                        isPlaying = isPlaying,
                        onToggleRecord = { vm.setRecording(!isRecording) },
                        onTogglePlayback = { vm.setPlaying(!isPlaying) },
                        onClearPath = {
                            vm.clearRecordedPath()
                            vm.clearPlaybackPath()
                        }
                    )
                },
                settingsContent = {
                    SettingsScreen(
                        minSpeedPercent = minSpeedPercent,
                        maxSpeedPercent = maxSpeedPercent,
                        trimOffset = trimOffset,
                        connectionStatus = connectionStatus,
                        onMinSpeedChange = { vm.setMinSpeed(it) },
                        onMaxSpeedChange = { vm.setMaxSpeed(it) },
                        onTrimLeft = { vm.adjustTrim(-5) },
                        onTrimRight = { vm.adjustTrim(5) }
                    )
                }
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        tankWifiClient.disconnect()
    }
}
