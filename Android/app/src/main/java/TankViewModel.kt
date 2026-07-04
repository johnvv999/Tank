package com.rcdriving.tankrtk

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class TankViewModel : ViewModel() {

    private val _leftMotor = MutableStateFlow(0)
    val leftMotor: StateFlow<Int> = _leftMotor

    private val _rightMotor = MutableStateFlow(0)
    val rightMotor: StateFlow<Int> = _rightMotor

    private val _rtkFix = MutableStateFlow<RtkFix?>(null)
    val rtkFix: StateFlow<RtkFix?> = _rtkFix

    private val _enuPosition = MutableStateFlow<EnuPoint?>(null)
    val enuPosition: StateFlow<EnuPoint?> = _enuPosition

    private val _waypoints = MutableStateFlow<List<Waypoint>>(emptyList())
    val waypoints: StateFlow<List<Waypoint>> = _waypoints

    // Speed level: 1 = precision, 4 = max. Scales joystick output before mixing.
    private val speedScales = listOf(0.3f, 0.55f, 0.8f, 1.0f)
    private val _speedLevel = MutableStateFlow(2)
    val speedLevel: StateFlow<Int> = _speedLevel

    fun currentSpeedScale(): Float = speedScales[_speedLevel.value - 1]

    fun cycleSpeed() {
        _speedLevel.value = if (_speedLevel.value >= 4) 1 else _speedLevel.value + 1
    }

    // Trim: steering bias to correct drift, split between motors, doesn't change overall speed.
    private val _trimOffset = MutableStateFlow(0)
    val trimOffset: StateFlow<Int> = _trimOffset

    fun adjustTrim(delta: Int) {
        _trimOffset.value = (_trimOffset.value + delta).coerceIn(-30, 30)
    }

    fun setMotors(left: Int, right: Int) {
        _leftMotor.value = left
        _rightMotor.value = right
    }

    fun updateFix(fix: RtkFix, enuConverter: EnuConverter) {
        _rtkFix.value = fix
        _enuPosition.value = enuConverter.toEnu(fix.lat, fix.lon, fix.alt, fix.timeMs)
    }

    fun setWaypoints(list: List<Waypoint>) {
        _waypoints.value = list
    }
}