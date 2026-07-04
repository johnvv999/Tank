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

// Splits trimOffset between motors so overall speed is unaffected, just left/right balance.
// Positive trimOffset biases toward the right motor (compensates a tank that drifts left).
fun applyTrim(left: Int, right: Int, trimOffset: Int): Pair<Int, Int> {
    val trimmedLeft = (left - trimOffset / 2).coerceIn(-100, 100)
    val trimmedRight = (right + trimOffset / 2).coerceIn(-100, 100)
    return trimmedLeft to trimmedRight
}

fun formatTankCommand(left: Int, right: Int): String =
    "L$left R$right\n"