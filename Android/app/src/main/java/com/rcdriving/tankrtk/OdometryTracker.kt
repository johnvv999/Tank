package com.rcdriving.tankrtk

import kotlin.math.cos
import kotlin.math.sin

// Approximates position via differential-drive odometry, used until real RTK/GPS fixes
// are wired into this app (see PathRecorder/RtkFix for that future path). Produces
// EnuPoints in a local frame starting at (0,0), heading 0 = +X axis.
// This drifts over time and is NOT a substitute for real GPS positioning.
class OdometryTracker(
    private val trackWidthMeters: Double = 0.3,
    private val maxSpeedMps: Double = 1.0
) {
    private var x = 0.0
    private var y = 0.0
    private var heading = 0.0
    private var startTimeMs = 0L

    private val _points = mutableListOf<EnuPoint>()
    val points: List<EnuPoint> get() = _points.toList()

    fun reset() {
        x = 0.0; y = 0.0; heading = 0.0
        _points.clear()
        startTimeMs = System.currentTimeMillis()
        _points += EnuPoint(x, y, 0.0, 0L)
    }

    fun sample(leftPwm: Int, rightPwm: Int, dtSeconds: Double) {
        val vLeft = (leftPwm / 100.0) * maxSpeedMps
        val vRight = (rightPwm / 100.0) * maxSpeedMps
        val v = (vLeft + vRight) / 2.0
        val omega = (vRight - vLeft) / trackWidthMeters

        heading += omega * dtSeconds
        x += v * cos(heading) * dtSeconds
        y += v * sin(heading) * dtSeconds

        val elapsed = System.currentTimeMillis() - startTimeMs
        _points += EnuPoint(x, y, 0.0, elapsed)
    }
}
