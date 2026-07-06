package com.rcdriving.tankrtk

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

class TankViewModel {

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
    // RECORDING STATE
    // ------------------------------------------------------------
    var isRecording by mutableStateOf(false)

    fun startRecording() {
        isRecording = true
    }

    fun stopRecording() {
        isRecording = false
    }

    // ------------------------------------------------------------
    // TURBO BOOST
    // ------------------------------------------------------------
    var turboActive by mutableStateOf(false)

    fun activateTurbo() {
        turboActive = true
        speedCurrent = (speedCurrent + 20).coerceIn(speedMin, speedMax)
        computeMotorOutputs()
        turboActive = false
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
    // MOTOR MIXING (forward + turn)
    // ------------------------------------------------------------
    private fun computeMotorOutputs() {
        val dirX = cos(angle)
        val dirY = sin(angle)

        val forward = dirY * speedCurrent
        val turn = dirX * speedCurrent

        val left = (forward + turn).toInt()
        val right = (forward - turn).toInt()

        sendToTank(left, right)
    }

    // ------------------------------------------------------------
    // SEND TO TANK (replace with your WiFi/UDP logic)
    // ------------------------------------------------------------
    private fun sendToTank(left: Int, right: Int) {
        // TODO: Insert your socket/UDP/WiFi send logic here
        // Example:
        // tankSocket.send("$left,$right")
    }
}
