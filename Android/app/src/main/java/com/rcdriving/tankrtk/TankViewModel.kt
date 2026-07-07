package com.rcdriving.tankrtk

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

class TankViewModel {

    // ------------------------------------------------------------
    // WIFI / TCP CLIENT (firmware: TankAP @ 192.168.4.1:9000)
    // ------------------------------------------------------------
    val wifi = TankWifiClient(Config.TANK_HOST, Config.TANK_PORT)

    fun connect() = wifi.connect()
    fun disconnect() {
        stopMotors()
        wifi.disconnect()
    }

    // ------------------------------------------------------------
    // SPEED RANGE (0–100%)
    // ------------------------------------------------------------
    var speedMin by mutableStateOf(0)
    var speedMax by mutableStateOf(100)
    var speedCurrent by mutableStateOf(0)

    fun setSpeedRange(min: Int, max: Int) {
        speedMin = min.coerceIn(0, 100)
        speedMax = max.coerceIn(0, 100)
        speedCurrent = speedCurrent.coerceIn(speedMin, speedMax)
    }

    fun increaseSpeed() {
        speedCurrent = (speedCurrent + 5).coerceIn(speedMin, speedMax)
        computeMotorOutputs()
    }

    fun decreaseSpeed() {
        speedCurrent = (speedCurrent - 5).coerceIn(speedMin, speedMax)
        computeMotorOutputs()
    }

    // ------------------------------------------------------------
    // SIGNAL STRENGTH (0–4 bars)
    // ------------------------------------------------------------
    var signalStrength by mutableStateOf(3)

    fun updateSignal(level: Int) {
        signalStrength = level.coerceIn(0, 4)
    }

    // ------------------------------------------------------------
    // RECORDING (odometry-based dead reckoning)
    // ------------------------------------------------------------
    private val odometry = OdometryTracker()
    private var lastSampleMs = 0L

    var isRecording by mutableStateOf(false)
    var recordedPath by mutableStateOf<Path?>(null)
        private set

    fun startRecording() {
        odometry.reset()
        lastSampleMs = System.currentTimeMillis()
        isRecording = true
    }

    fun stopRecording() {
        isRecording = false
        recordedPath = Path(odometry.points)
    }

    // ------------------------------------------------------------
    // PLAYBACK
    // ------------------------------------------------------------
    private val replayer = PathReplayer(wifi)

    var isReplaying by mutableStateOf(false)
        private set
    var replayProgress by mutableStateOf(0)
        private set

    // Call from a coroutine, e.g. scope.launch { viewModel.replayRecorded() }
    suspend fun replayRecorded() {
        val path = recordedPath ?: return
        if (isReplaying || path.size < 2) return
        isReplaying = true
        try {
            replayer.replay(path) { replayProgress = it }
        } finally {
            isReplaying = false
        }
    }

    // ------------------------------------------------------------
    // TURBO BOOST (momentary: press = boost, release = restore)
    // ------------------------------------------------------------
    var turboActive by mutableStateOf(false)
    private var preTurboSpeed = 0

    fun turboPressed() {
        if (turboActive) return
        turboActive = true
        preTurboSpeed = speedCurrent
        speedCurrent = (speedCurrent + 20).coerceIn(speedMin, speedMax)
        computeMotorOutputs()
    }

    // Backward-compatible tap version used by TankDriveScreen's TurboButton:
    // gives a 20% boost for the next command, then restores speed.
    fun activateTurbo() {
        turboPressed()
        turboReleased()
    }

    fun turboReleased() {
        if (!turboActive) return
        turboActive = false
        speedCurrent = preTurboSpeed
        computeMotorOutputs()
    }

    // ------------------------------------------------------------
    // JOYSTICK DIRECTION (angle only)
    // ------------------------------------------------------------
    var angle by mutableStateOf(0f)

    fun updateDirection(newAngle: Float) {
        angle = newAngle
        computeMotorOutputs()
    }

    fun updateDirectionFromTouch(x: Float, y: Float, centerX: Float, centerY: Float) {
        val dx = x - centerX
        val dy = y - centerY
        angle = atan2(dy, dx)
        computeMotorOutputs()
    }

    // ------------------------------------------------------------
    // STOP
    // ------------------------------------------------------------
    fun stopMotors() {
        speedCurrent = speedMin
        sendToTank(0, 0)
    }

    // ------------------------------------------------------------
    // MOTOR MIXING (forward + turn)
    // ------------------------------------------------------------
    private fun computeMotorOutputs() {
        val dirX = cos(angle)
        val dirY = sin(angle)

        val forward = dirY * speedCurrent
        val turn = dirX * speedCurrent

        val left = (forward + turn).toInt().coerceIn(-100, 100)
        val right = (forward - turn).toInt().coerceIn(-100, 100)

        sendToTank(left, right)
    }

    // ------------------------------------------------------------
    // SEND TO TANK — firmware parses "L<val> R<val>\n" on port 9000
    // ------------------------------------------------------------
    private fun sendToTank(left: Int, right: Int) {
        wifi.send(formatTankCommand(left, right))

        if (isRecording) {
            val now = System.currentTimeMillis()
            val dt = (now - lastSampleMs) / 1000.0
            lastSampleMs = now
            if (dt > 0) odometry.sample(left, right, dt)
        }
    }
}
