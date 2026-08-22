package com.lenshrv.app.domain.algorithm

import com.lenshrv.app.domain.model.Beat
import kotlin.math.roundToInt
import kotlin.math.roundToLong

data class HermiteResult(
    val beats: List<Beat>,
    val syntheticTimestamps: Set<Long>,
    val artifactPercent: Double,
    val endKeptIndex: Int,
)

object HermiteCubicInterpolator {

    private const val ANALYSIS_WINDOW_MS = 120_000L

    operator fun invoke(beats: List<Beat>): HermiteResult {
        if (beats.isEmpty()) {
            return HermiteResult(emptyList(), emptySet(), 100.0, endKeptIndex = -1)
        }

        val rr = beats.map { it.rrInterval }
        val out = mutableListOf(beats.first())
        val synth = mutableSetOf<Long>()

        for (i in 1 until beats.size) {
            val prev = out.last()
            val curr = beats[i]
            val dt = curr.timestamp - prev.timestamp

            if (dt > 0L) {
                val med = median11(rr, i - 1)
                if (med > 0L) {
                    val n = (dt.toDouble() / med).roundToInt() - 1

                    if (n > 0) {
                        val p0 = rr[i - 1].toDouble()
                        val p1 = rr[i].toDouble()
                        val m0 = slope(rr, i - 1)
                        val m1 = slope(rr, i)
                        val inserted = LongArray(n) { k ->
                            hermite((k + 1).toDouble() / (n + 1), p0, m0, p1, m1).roundToLong()
                        }

                        var t = prev.timestamp
                        for (synthRr in inserted) {
                            t += synthRr
                            out.add(Beat(timestamp = t, rrInterval = synthRr))
                            synth.add(t)
                        }
                    }
                }
            }
            out.add(curr)
        }

        val t0 = out.first().timestamp
        val analysisEnd = t0 + ANALYSIS_WINDOW_MS
        val synthIn120s = synth.count { it <= analysisEnd }
        val realIn120s = out.count { it.timestamp <= analysisEnd && it.timestamp !in synth }
        val totalIn120s = realIn120s + synthIn120s
        val artifactPercent = if (totalIn120s > 0) {
            synthIn120s.toDouble() / totalIn120s * 100.0
        } else {
            100.0
        }

        val endIdx = out.indexOfFirst { it.timestamp - t0 >= ANALYSIS_WINDOW_MS }
        val beats120s = if (endIdx >= 0) out.subList(0, endIdx + 1) else out
        val windowEnd = beats120s.last().timestamp
        val endKeptIndex = beats.indexOfLast { it.timestamp <= windowEnd }

        return HermiteResult(
            beats = beats120s,
            syntheticTimestamps = synth,
            artifactPercent = artifactPercent,
            endKeptIndex = endKeptIndex,
        )
    }

    private fun median11(rr: List<Long>, i: Int): Long {
        val n = rr.size
        var left = minOf(5, i)
        var right = minOf(5, n - 1 - i)
        val need = 10 - left - right
        if (need > 0) {
            val extraRight = minOf(need, n - 1 - i - right)
            right += extraRight
            val still = need - extraRight
            if (still > 0) left += minOf(still, i - left)
        }
        val w = rr.subList(i - left, i + right + 1).sorted()
        return w[w.size / 2]
    }

    private fun slope(rr: List<Long>, j: Int): Double {
        val n = rr.size
        return when {
            n < 2 -> 0.0
            j == 0 -> rr[1].toDouble() - rr[0].toDouble()
            j == n - 1 -> rr[n - 1].toDouble() - rr[n - 2].toDouble()
            else -> (rr[j + 1].toDouble() - rr[j - 1].toDouble()) / 2.0
        }
    }

    private fun hermite(t: Double, p0: Double, m0: Double, p1: Double, m1: Double): Double {
        val t2 = t * t
        val t3 = t2 * t
        val h00 = 2 * t3 - 3 * t2 + 1
        val h10 = t3 - 2 * t2 + t
        val h01 = -2 * t3 + 3 * t2
        val h11 = t3 - t2
        return h00 * p0 + h10 * m0 + h01 * p1 + h11 * m1
    }
}
