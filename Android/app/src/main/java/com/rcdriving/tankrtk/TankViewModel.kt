package com.rcdriving.tankrtk

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

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

    // speedCurrent is the throttle "dial" (persists whether or not the
    // joystick is touched, since it's set separately via +/-). What the
    // main screen should actually show is: 0 whenever the joystick is
    // centered (nothing being commanded right now), and otherwise
    // speedCurrent expressed as a percentage of the configured min-max
    // range — i.e. speedMin reads as 0%, speedMax reads as 100%, not
    // speedCurrent's raw absolute value.
    val speedPercentDisplay: Int
        get() {
            val touching = joystickX != 0f || joystickY != 0f
            if (!touching) return 0
            val range = (speedMax - speedMin).coerceAtLeast(1)
            return (((speedCurrent - speedMin) * 100) / range).coerceIn(0, 100)
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
    // TRIM — persistent left/right bias to correct for the tank
    // pulling to one side when driving "straight" (steer = 0).
    // Added to the left tread and subtracted from the right, so a
    // positive trim shifts power toward the left side.
    // ------------------------------------------------------------
    var trim by mutableStateOf(0)
        private set

    private val trimRange = -30..30

    fun increaseTrim() {
        trim = (trim + 1).coerceIn(trimRange.first, trimRange.last)
        computeMotorOutputs()
    }

    fun decreaseTrim() {
        trim = (trim - 1).coerceIn(trimRange.first, trimRange.last)
        computeMotorOutputs()
    }

    // ------------------------------------------------------------
    // JOYSTICK POSITION — normalized -1..1 on each axis.
    //   joystickY: throttle  (+1 = full forward, -1 = full reverse)
    //   joystickX: steering  (+1 = full right,   -1 = full left)
    // Using the actual stick displacement (not just its angle) matters:
    // a small nudge should give a small, proportional command. The old
    // angle-only version always used the full unit-circle decomposition
    // regardless of how far the stick was pushed, so any direction more
    // "sideways" than "forward" (e.g. a gentle forward-right nudge) could
    // immediately drive one tread backward instead of just slowing it —
    // that's what caused the right tread reversing on a forward-right turn.
    // ------------------------------------------------------------
    var joystickX by mutableStateOf(0f)
    var joystickY by mutableStateOf(0f)

    fun updateJoystick(normX: Float, normY: Float) {
        joystickX = normX.coerceIn(-1f, 1f)
        joystickY = normY.coerceIn(-1f, 1f)
        computeMotorOutputs()
    }

    // ------------------------------------------------------------
    // STOP
    // ------------------------------------------------------------
    fun stopMotors() {
        speedCurrent = speedMin
        joystickX = 0f
        joystickY = 0f
        sendToTank(0, 0)
    }

    // ------------------------------------------------------------
    // MOTOR MIXING (arcade drive: throttle ± steering ± trim)
    // ------------------------------------------------------------
    private fun computeMotorOutputs() {
        val throttle = joystickY * speedCurrent
        val steer    = joystickX * speedCurrent

        val left  = (throttle + steer + trim).toInt().coerceIn(-100, 100)
        val right = (throttle - steer - trim).toInt().coerceIn(-100, 100)

        sendToTank(left, right)
    }

    // ------------------------------------------------------------
    // LAST COMMAND — shown in the UI in place of the old turbo button
    // ------------------------------------------------------------
    var lastCommand by mutableStateOf("L0 R0")
        private set

    // ------------------------------------------------------------
    // SEND TO TANK — firmware parses "L<val> R<val>\n" on port 9000
    // ------------------------------------------------------------
    private fun sendToTank(left: Int, right: Int) {
        val cmd = formatTankCommand(left, right)
        lastCommand = cmd.trim()
        wifi.send(cmd)

        if (isRecording) {
            val now = System.currentTimeMillis()
            val dt = (now - lastSampleMs) / 1000.0
            lastSampleMs = now
            if (dt > 0) odometry.sample(left, right, dt)
        }
    }
}
