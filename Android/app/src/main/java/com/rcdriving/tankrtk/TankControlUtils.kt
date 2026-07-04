package com.rcdriving.tankrtk

fun mixTankDrive(x: Float, y: Float): Pair<Int, Int> {
    var left = y + x
    var right = y - x

    val maxVal = maxOf(kotlin.math.abs(left), kotlin.math.abs(right))
    if (maxVal > 1f) {
        left /= maxVal
        right /= maxVal
    }

    val leftPWM = (left * 100).toInt()
    val rightPWM = (right * 100).toInt()

    return leftPWM to rightPWM
}

fun formatTankCommand(left: Int, right: Int): String =
    "L$left R$right\n"
