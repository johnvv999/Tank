package com.rcdriving.tankrtk

import com.rcdriving.tankrtk.EnuPoint
import com.rcdriving.tankrtk.Path
import com.rcdriving.tankrtk.Waypoint


class PathRecorder(
    private val enuConverter: EnuConverter
) {
    private val _points = mutableListOf<EnuPoint>()
    val path: Path get() = Path(_points.toList())

    fun recordFix(fix: RtkFix) {
        _points += enuConverter.toEnu(fix.lat, fix.lon, fix.alt, fix.timeMs)
    }

    fun clear() {
        _points.clear()
    }
}
