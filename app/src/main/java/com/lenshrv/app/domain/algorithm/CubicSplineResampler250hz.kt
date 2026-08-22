package com.lenshrv.app.domain.algorithm

import com.lenshrv.app.domain.model.RawSample
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.roundToLong

object CubicSplineResampler250hz {
    const val DEFAULT_TARGET_HZ = 250.0

    operator fun invoke(
        samples: List<RawSample>,
        targetHz: Double = DEFAULT_TARGET_HZ,
    ): List<RawSample> {

        if (samples.size < 2) return emptyList()

        val x = DoubleArray(samples.size) { samples[it].timestamp.toDouble() }
        val y = DoubleArray(samples.size) { samples[it].value }
        val m = CubicSplineKernel.secondDerivatives(x, y)
        val tStart = x.first()
        val tEnd = x.last()
        val durationMs = tEnd - tStart
        val dtMs = 1000.0 / targetHz
        val steps = max(1, floor(durationMs / dtMs).toInt())
        val out = ArrayList<RawSample>(steps + 2)
        var segment = 0

        for (k in 0..steps) {
            val t = if (k == steps) tEnd else (tStart + k * dtMs).coerceAtMost(tEnd)
            while (segment < samples.size - 2 && x[segment + 1] < t) {
                segment++
            }
            val value = CubicSplineKernel.evalSpline(x, y, m, segment, t)
            val ts = t.roundToLong()
            if (out.isNotEmpty() && out.last().timestamp == ts) {
                out[out.lastIndex] = RawSample(ts, value)
            } else {
                out.add(RawSample(timestamp = ts, value = value))
            }
        }

        val endTs = samples.last().timestamp
        val endVal = samples.last().value
        if (out.isEmpty() || out.last().timestamp != endTs) {
            out.add(RawSample(endTs, endVal))
        } else {
            out[out.lastIndex] = RawSample(endTs, endVal)
        }
        return out
    }
}

