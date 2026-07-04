package com.rcdriving.tankrtk

import kotlin.math.*

class EnuConverter(
    private val refLat: Double,
    private val refLon: Double,
    private val refAlt: Double
) {
    private val a = 6378137.0
    private val f = 1.0 / 298.257223563
    private val e2 = f * (2 - f)

    fun toEnu(lat: Double, lon: Double, alt: Double, timeMs: Long): EnuPoint {
        val (xRef, yRef, zRef) = toEcef(refLat, refLon, refAlt)
        val (x, y, z) = toEcef(lat, lon, alt)

        val dx = x - xRef
        val dy = y - yRef
        val dz = z - zRef

        val phi = Math.toRadians(refLat)
        val lam = Math.toRadians(refLon)

        val sinPhi = sin(phi)
        val cosPhi = cos(phi)
        val sinLam = sin(lam)
        val cosLam = cos(lam)

        val e = -sinLam * dx + cosLam * dy
        val n = -sinPhi * cosLam * dx - sinPhi * sinLam * dy + cosPhi * dz
        val u = cosPhi * cosLam * dx + cosPhi * sinLam * dy + sinPhi * dz

        return EnuPoint(e, n, u, timeMs)
    }

    private fun toEcef(lat: Double, lon: Double, alt: Double): Triple<Double, Double, Double> {
        val phi = Math.toRadians(lat)
        val lam = Math.toRadians(lon)
        val sinPhi = sin(phi)
        val cosPhi = cos(phi)
        val sinLam = sin(lam)
        val cosLam = cos(lam)

        val N = a / sqrt(1 - e2 * sinPhi * sinPhi)
        val x = (N + alt) * cosPhi * cosLam
        val y = (N + alt) * cosPhi * sinLam
        val z = (N * (1 - e2) + alt) * sinPhi
        return Triple(x, y, z)
    }
}
