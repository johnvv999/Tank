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

    // Current drive speed as a direct percentage, stepped by 5% per +/- tap,
    // clamped to whatever Min/Max range is set on the Settings screen.
    private val _currentSpeedPercent = MutableStateFlow(0)
    val currentSpeedPercent: StateFlow<Int> = _currentSpeedPercent

    fun setMinSpeed(percent: Int) {
        _minSpeedPercent.value = percent.coerceIn(0, 100)
        _currentSpeedPercent.value = _currentSpeedPercent.value.coerceIn(_minSpeedPercent.value, _maxSpeedPercent.value)
    }

    fun setMaxSpeed(percent: Int) {
        _maxSpeedPercent.value = percent.coerceIn(0, 100)
        _currentSpeedPercent.value = _currentSpeedPercent.value.coerceIn(_minSpeedPercent.value, _maxSpeedPercent.value)
    }
    
	fun currentSpeedScale(): Float = _currentSpeedPercent.value / 100f

    	// Normalizes the raw motor percentage into a 0-100 display value relative
    	// to the Min/Max range — e.g. Min=30, Max=100: raw 30% displays as 0%,
    	// raw 100% displays as 100%. Does not affect actual motor output.
    	fun displaySpeedPercent(): Int {
    	    val range = _maxSpeedPercent.value - _minSpeedPercent.value
            if (range <= 0) return 0
            return (((_currentSpeedPercent.value - _minSpeedPercent.value) * 100) / range).coerceIn(0, 100)
    }


fun increaseSpeed() {
        _currentSpeedPercent.value = (_currentSpeedPercent.value + 5).coerceAtMost(_maxSpeedPercent.value)
    }

    fun decreaseSpeed() {
        _currentSpeedPercent.value = (_currentSpeedPercent.value - 5).coerceAtLeast(_minSpeedPercent.value)
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