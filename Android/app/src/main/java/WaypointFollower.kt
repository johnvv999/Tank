package com.rcdriving.tankrtk

import kotlin.math.atan2
import kotlin.math.hypot

class WaypointFollower(
    private val tankWifiClient: TankWifiClient
) {
    fun follow(current: EnuPoint, waypoints: List<Waypoint>) {
        for (wp in waypoints) {
            driveTo(current, wp)
        }
        tankWifiClient.send("L0 R0\n")
    }

    private fun driveTo(current: EnuPoint, wp: Waypoint) {
        val dx = wp.x - current.x
        val dy = wp.y - current.y
        val dist = hypot(dx, dy)
        val heading = atan2(dy, dx)

        val forward = 0.7f
        val turn = headingToTurn(heading)

        val (l, r) = mixTankDrive(turn, forward)
        val cmd = formatTankCommand(l, r)
        tankWifiClient.send(cmd)
        Thread.sleep((dist * 400).toLong().coerceAtLeast(150))
    }

    private fun headingToTurn(h: Double): Float {
        val norm = ((h + Math.PI) / (2 * Math.PI)).toFloat() * 2f - 1f
        return norm.coerceIn(-1f, 1f)
    }
}
