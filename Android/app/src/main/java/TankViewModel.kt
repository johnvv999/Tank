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
