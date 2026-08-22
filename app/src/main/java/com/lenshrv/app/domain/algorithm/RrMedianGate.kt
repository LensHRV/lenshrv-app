package com.lenshrv.app.domain.algorithm

import com.lenshrv.app.domain.model.Beat
import com.lenshrv.app.util.calculateMedian

object RrMedianGate {
    private const val FACTOR = 1.40
    data class Result(
        val medianRr: Double,
        val kept: List<Beat>,
        val dropped: List<Beat>,
    )

    operator fun invoke(beats: List<Beat>): Result {
        if (beats.isEmpty()) {
            return Result(medianRr = 0.0, kept = emptyList(), dropped = emptyList())
        }

        val medianRr = calculateMedian(beats.map { it.rrInterval.toDouble() })
        val hi = medianRr * FACTOR
        val lo = medianRr / FACTOR

        val kept = mutableListOf<Beat>()
        val dropped = mutableListOf<Beat>()

        for (b in beats) {
            val rr = b.rrInterval.toDouble()
            if (rr !in lo..<hi) dropped.add(b) else kept.add(b)
        }

        return Result(medianRr = medianRr, kept = kept, dropped = dropped)
    }
}
