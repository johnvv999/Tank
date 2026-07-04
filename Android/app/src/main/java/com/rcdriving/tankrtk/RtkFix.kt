package com.rcdriving.tankrtk

data class RtkFix(
    val lat: Double,
    val lon: Double,
    val alt: Double,
    val timeMs: Long
)
