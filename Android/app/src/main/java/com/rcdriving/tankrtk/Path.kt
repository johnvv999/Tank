package com.rcdriving.tankrtk

// A recorded path: an ordered list of local-frame points.
// Produced by OdometryTracker (dead reckoning) today, and by
// PathRecorder (RTK GPS via EnuConverter) in the future.
data class Path(
    val points: List<EnuPoint>
) {
    val isEmpty: Boolean get() = points.isEmpty()
    val size: Int get() = points.size
}
