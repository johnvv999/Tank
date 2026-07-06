package com.rcdriving.tankrtk

data class EnuPoint(
    val x: Double,
    val y: Double,
    val z: Double = 0.0,
    val timeMs: Long = System.currentTimeMillis()
)
