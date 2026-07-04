package com.rcdriving.tankrtk

import kotlin.math.atan2
import kotlin.math.hypot

class PathReplayer(
    private val tankWifiClient: TankWifiClient
) {
    fun replay(path: Path) {
        if (path.points.size < 2) return
        for (i in 0 until path.points.size - 1) {
            val p = path.points[i]
            val q = path.points[i + 1]
            driveSegment(p, q)
        }
        tankWifiClient.send("L0 R0\n")
    }

    private fun driveSegment(p: EnuPoint, q: EnuPoint) {
        val dx = q.x - p.x
        val dy = q.y - p.y
        val dist = hypot(dx, dy)
        val heading = atan2(dy, dx)

        val forward = 0.6f
        val turn = headingToTurn(heading)

        val (l, r) = mixTankDrive(turn, forward)
        val cmd = formatTankCommand(l, r)
        tankWifiClient.send(cmd)
        Thread.sleep((dist * 500).toLong().coerceAtLeast(100))
    }

    private fun headingToTurn(h: Double): Float {
        val norm = ((h + Math.PI) / (2 * Math.PI)).toFloat() * 2f - 1f
        return norm.coerceIn(-1f, 1f)
    }
}
