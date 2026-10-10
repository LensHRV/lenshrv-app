package com.lenshrv.app.fixtures

import com.lenshrv.app.domain.model.RawSample
import kotlin.math.PI
import kotlin.math.roundToLong
import kotlin.math.sin

fun generateSineSignal(
    frequencyHz: Double,
    amplitude: Double = 10.0,
    durationSeconds: Double = 20.0,
    samplingRateHz: Double = 30.0,
): List<RawSample> {
    val totalSamples = (durationSeconds * samplingRateHz).toInt()
    return List(totalSamples) { i ->
        val t = i.toDouble() / samplingRateHz
        val timestamp = (t * 1000.0).roundToLong()
        val value = amplitude * sin(2.0 * PI * frequencyHz * t)
        RawSample(timestamp = timestamp, value = value)
    }
}
