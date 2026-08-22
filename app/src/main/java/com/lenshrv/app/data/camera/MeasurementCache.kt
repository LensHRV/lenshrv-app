package com.lenshrv.app.data.camera

import com.lenshrv.app.domain.model.RawSample
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MeasurementCache @Inject constructor() {

    private val maxSize = 10_000
    private val timestamps = LongArray(maxSize)
    private val greenValues = DoubleArray(maxSize)
    private var size = 0
    var cameraMetadata: String = "none"

    fun push(green: Double, time: Long) {
        timestamps[size] = time
        greenValues[size] = green
        size++
    }

    fun getAll(): List<RawSample> {
        val result = ArrayList<RawSample>(size)
        for (i in 0 until size) {
            result.add(RawSample(timestamps[i], greenValues[i]))
        }
        return result
    }
    fun reset() {
        size = 0
    }

    fun resetCameraMetadata() {
        cameraMetadata = "none"
    }
}
