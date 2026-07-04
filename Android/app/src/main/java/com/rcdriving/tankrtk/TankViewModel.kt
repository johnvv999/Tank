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

    private val _minSpeedPercent = MutableStateFlow(30)
    val minSpeedPercent: StateFlow<Int> = _minSpeedPercent

    private val _maxSpeedPercent = MutableStateFlow(100)
    val maxSpeedPercent: StateFlow<Int> = _maxSpeedPercent

    // The displayed throttle value, 0-100, stepping by 5 per tap. This is
    // shown directly on screen. The actual motor scale is derived from it
    // in MainActivity as: min + (displayPercent/100) * (max - min).
    private val _speedDisplayPercent = MutableStateFlow(0)
    val speedDisplayPercent: StateFlow<Int> = _speedDisplayPercent

    fun setMinSpeed(percent: Int) { _minSpeedPercent.value = percent.coerceIn(0, 100) }
    fun setMaxSpeed(percent: Int) { _maxSpeedPercent.value = percent.coerceIn(0, 100) }

    fun increaseSpeed() {
        _speedDisplayPercent.value = (_speedDisplayPercent.value + 5).coerceAtMost(100)
    }

    fun decreaseSpeed() {
        _speedDisplayPercent.value = (_speedDisplayPercent.value - 5).coerceAtLeast(0)
    }

    // Trim: steering bias to correct drift, split between motors, doesn't change overall speed.
    private val _trimOffset = MutableStateFlow(0)
    val trimOffset: StateFlow<Int> = _trimOffset

    fun adjustTrim(delta: Int) {
        _trimOffset.value = (_trimOffset.value + delta).coerceIn(-30, 30)
    }

    // Path recording / playback state
    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _recordedPath = MutableStateFlow<List<EnuPoint>>(emptyList())
    val recordedPath: StateFlow<List<EnuPoint>> = _recordedPath

    private val _playbackPath = MutableStateFlow<List<EnuPoint>>(emptyList())
    val playbackPath: StateFlow<List<EnuPoint>> = _playbackPath

    fun setRecording(active: Boolean) { _isRecording.value = active }
    fun setPlaying(active: Boolean) { _isPlaying.value = active }
    fun appendRecordedPoint(p: EnuPoint) { _recordedPath.value = _recordedPath.value + p }
    fun clearRecordedPath() { _recordedPath.value = emptyList() }
    fun setPlaybackPath(points: List<EnuPoint>) { _playbackPath.value = points }
    fun clearPlaybackPath() { _playbackPath.value = emptyList() }

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