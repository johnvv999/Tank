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
    // SPEED RANGE — Min/Max are raw PWM values sent straight to the
    // motor drivers (0-255 hardware ceiling), not a percentage; the
    // firmware no longer rescales incoming L/R values, it just clamps
    // them to ±255 (see TankArduino.ino's setLeftMotor/setRightMotor).
    // Defaults: 0 to 250 (just under the hardware's absolute 255 max).
    // ------------------------------------------------------------
    var speedMin by mutableStateOf(0)
    var speedMax by mutableStateOf(250)

    // The throttle "dial" is a step index (0..SPEED_STEPS), not a raw
    // value — every +/- press moves exactly one step, i.e. exactly 10%
    // of (speedMax - speedMin), and the main-screen display always
    // reads a clean multiple of 10%. Deriving both the actual PWM-scale
    // value AND the displayed percent from this same integer index
    // guarantees they can never drift out of sync with each other via
    // rounding, the way separately-tracked values could.
    private val SPEED_STEPS = 10
    var speedStepIndex by mutableStateOf(0)
        private set

    // The actual value fed into motor mixing, in speedMin..speedMax.
    val speedCurrent: Int
        get() = speedMin + ((speedMax - speedMin) * speedStepIndex) / SPEED_STEPS

    fun setSpeedRange(min: Int, max: Int) {
        speedMin = min.coerceIn(0, 255)
        speedMax = max.coerceIn(0, 255)
        speedStepIndex = speedStepIndex.coerceIn(0, SPEED_STEPS)
    }

    // Bumped on every +/- press — TankDriveScreen watches this to flash
    // the new setting on the main-screen display for 1 second, even if
    // the joystick isn't currently being driven.
    var speedChangeTrigger by mutableStateOf(0)
        private set

    fun increaseSpeed() {
        speedStepIndex = (speedStepIndex + 1).coerceIn(0, SPEED_STEPS)
        speedChangeTrigger++
        computeMotorOutputs()
    }

    fun decreaseSpeed() {
        speedStepIndex = (speedStepIndex - 1).coerceIn(0, SPEED_STEPS)
        speedChangeTrigger++
        computeMotorOutputs()
    }

    // Set by TankDriveScreen for 1 second after a +/- press, so the
    // speed setting is visible even while the joystick is centered.
    var showSpeedSetting by mutableStateOf(false)

    // speedStepIndex is the throttle "dial" (persists whether or not the
    // joystick is touched, since it's set separately via +/-). Normally
    // the main screen shows 0 whenever the joystick is centered (nothing
    // being commanded right now) and otherwise the dial's percent — but
    // right after a +/- press, showSpeedSetting overrides that so the
    // new setting is visible for a moment regardless of joystick state.
    val speedPercentDisplay: Int
        get() {
            val touching = joystickX != 0f || joystickY != 0f
            if (!touching && !showSpeedSetting) return 0
            return speedStepIndex * 10
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
        speedStepIndex = 0
        joystickX = 0f
        joystickY = 0f
        sendToTank(0, 0)
    }

    // ------------------------------------------------------------
    // SPIN-IN-PLACE — driven only by the dedicated spin-arrow buttons,
    // completely separate from joystick state/computeMotorOutputs. This
    // is the ONLY way to make both treads spin in opposite directions;
    // the joystick itself can never do this (see computeMotorOutputs'
    // no-spin cap below).
    // ------------------------------------------------------------
    fun startSpin(clockwise: Boolean) {
        val magnitude = speedCurrent
        val left  = if (clockwise) magnitude else -magnitude
        val right = if (clockwise) -magnitude else magnitude
        sendToTank(left.coerceIn(-255, 255), right.coerceIn(-255, 255))
    }

    fun stopSpin() {
        sendToTank(0, 0)
    }

    // ------------------------------------------------------------
    // MOTOR MIXING (arcade drive: throttle ± steering ± trim)
    // Clamped to ±255 now that speedCurrent is a raw PWM-scale value
    // (was ±100 back when it was a percent).
    //
    // Steering can never create a spin on its own: steer is capped to
    // the current throttle's magnitude, so left/right always share
    // throttle's sign (or are both zero) — pushing the stick straight
    // sideways with no forward/back component produces no movement at
    // all, rather than a pivot. That's intentional: spinning in place
    // is only available via startSpin()/the dedicated arrow buttons.
    //
    // Only once the stick is more than 10% off straight forward/back
    // (|joystickX| > 0.1 — a small dead band around dead-straight) does
    // the minimum-gap rule below kick in. That, plus enough throttle to
    // allow it without breaking the no-spin cap, pushes left and right
    // to differ by at least 10 (diff = 2*steer, so steer's magnitude
    // needs to be at least 5) so a deliberate turn always produces a
    // perceptible difference between the two treads rather than two
    // nearly-identical values. Within that 10% band, or dead straight,
    // left and right are free to be equal.
    // ------------------------------------------------------------
    private fun computeMotorOutputs() {
        val throttle = joystickY * speedCurrent
        val throttleMag = kotlin.math.abs(throttle)

        var steer = (joystickX * speedCurrent).coerceIn(-throttleMag, throttleMag)

        val steerDeadBand = 0.1f
        val minSteerMagnitude = 5f
        if (kotlin.math.abs(joystickX) > steerDeadBand && throttleMag >= minSteerMagnitude) {
            steer = if (steer >= 0f) steer.coerceAtLeast(minSteerMagnitude) else steer.coerceAtMost(-minSteerMagnitude)
        }

        val left  = (throttle + steer + trim).toInt().coerceIn(-255, 255)
        val right = (throttle - steer - trim).toInt().coerceIn(-255, 255)

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
