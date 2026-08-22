package com.lenshrv.app.domain.algorithm

import kotlin.math.sqrt

object TimeDomainMetrics {
     fun bpm(rrIntervals: List<Long>): Int {
        if (rrIntervals.isEmpty()) return 0
        val bpm = 60000.0 / rrIntervals.average()
        return (bpm + 0.5).toInt()
    }

     fun rmssd(rrIntervals: List<Long>): Double {
        if (rrIntervals.size < 3) return 0.0
        var squareSum = 0L
        for (i in 0 until rrIntervals.size - 1) {
            val sum = rrIntervals[i + 1] - rrIntervals[i]
            val square = sum * sum
            squareSum += square
        }
        return sqrt(squareSum.toDouble() / (rrIntervals.size - 1))
    }

     fun sdnn(rrIntervals: List<Long>): Double {
        if (rrIntervals.isEmpty()) return 0.0
        val average = rrIntervals.average()
        var sumOfSquares = 0.0
        for (interval in rrIntervals) {
            val difference = interval - average
            sumOfSquares += difference * difference
        }
        return sqrt(sumOfSquares / rrIntervals.size)
    }

     fun stressIndex(rrIntervals: List<Long>): Double {
        if (rrIntervals.size < 10) return 0.0

        val maxInterval = rrIntervals.maxOrNull() ?: 0L
        val minInterval = rrIntervals.minOrNull() ?: 0L

        val histogram = rrIntervals.groupingBy { (it / 50) * 50 }.eachCount()
        val modeBinStart = histogram.maxByOrNull { it.value }?.key ?: 0L
        val modeInSeconds = (modeBinStart + 25) / 1000.0
        if (modeInSeconds == 0.0) return 0.0

        val maxRepetitions = histogram.values.maxOrNull() ?: 0
        val amplitudeOfMode = (maxRepetitions.toDouble() / rrIntervals.size) * 100

        val variationScopeInSeconds = (maxInterval - minInterval) / 1000.0
        if (variationScopeInSeconds == 0.0) return 0.0

        return amplitudeOfMode / (2 * modeInSeconds * variationScopeInSeconds)
    }
}
