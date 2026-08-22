package com.lenshrv.app.domain.algorithm

import com.lenshrv.app.domain.model.Beat
import com.lenshrv.app.domain.model.RawSample
import kotlin.math.roundToInt

object ElgendiPeaks {

    operator fun invoke(
        data: List<RawSample>, fs: Int = 250,
        peakWindowSec: Double = 0.111,
        beatWindowSec: Double = 0.667,
        beatOffset: Double = 0.02,
        minDelaySec: Double = 0.3,
    ): List<Beat> {
        val n = data.size
        val timestamps = LongArray(n) { data[it].timestamp }
        val values = DoubleArray(n) { data[it].value }
        return invoke(timestamps, values, n, fs, peakWindowSec, beatWindowSec, beatOffset, minDelaySec)
    }

    operator fun invoke(
        timestamps: LongArray,
        values: DoubleArray,
        size: Int,
        fs: Int = 250,
        peakWindowSec: Double = 0.111,
        beatWindowSec: Double = 0.667,
        beatOffset: Double = 0.02,
        minDelaySec: Double = 0.3,
    ): List<Beat> {
        val signal = DoubleArray(size) { i ->
            val v = values[i]
            if (v > 0.0) v * v else 0.0
        }
        val peakWindowSamples = (peakWindowSec * fs).roundToInt()
        val beatWindowSamples = (beatWindowSec * fs).roundToInt()
        val minDelaySamples = (minDelaySec * fs).roundToInt()
        val maPeak = movingAverage(signal, peakWindowSamples)
        val maBeat = movingAverage(signal, beatWindowSamples)
        val sBar = signal.average()
        val offset = beatOffset * sBar
        val peakIndices = mutableListOf<Int>()
        var inBlock = false
        var blockStart = 0

        for (i in 0 until size) {
            val threshold = maBeat[i] + offset
            val isAbove = maPeak[i] > threshold

            if (isAbove && !inBlock) {
                inBlock = true
                blockStart = i
            } else if (inBlock && (!isAbove || i == size - 1)) {
                inBlock = false
                val blockEnd = if (isAbove) i else i - 1
                if ((blockEnd - blockStart) >= peakWindowSamples) {
                    var maxIndex = blockStart
                    var maxValue = signal[blockStart]

                    for (j in blockStart..blockEnd) {
                        if (signal[j] > maxValue) {
                            maxValue = signal[j]
                            maxIndex = j
                        }
                    }
                    if (peakIndices.isEmpty() || (maxIndex - peakIndices.last()) >= minDelaySamples) {
                        peakIndices.add(maxIndex)
                    }
                }
            }
        }
        if (peakIndices.isNotEmpty()) {
            peakIndices.removeAt(0)
        }
        val beats = mutableListOf<Beat>()

        for (i in 1 until peakIndices.size) {
            val currentTimestamp = timestamps[peakIndices[i]]
            val previousTimestamp = timestamps[peakIndices[i - 1]]

            beats.add(
                Beat(
                    timestamp = currentTimestamp,
                    rrInterval = currentTimestamp - previousTimestamp
                )
            )
        }

        return beats
    }
}


fun movingAverage(signal: DoubleArray, windowSize: Int): DoubleArray {
    val n = signal.size
    if (n == 0) return DoubleArray(0)

    val result = DoubleArray(n)
    val halfWindow = windowSize / 2
    val initialRightEnd = minOf(n - 1, halfWindow)
    var currentSum = 0.0
    var count = 0

    for (j in 0..initialRightEnd) {
        currentSum += signal[j]
        count++
    }
    for (i in 0 until n) {
        result[i] = currentSum / count
        val addIndex = i + 1 + halfWindow
        if (addIndex < n) {
            currentSum += signal[addIndex]
            count++
        }
        val removeIndex = i - halfWindow
        if (i + 1 - halfWindow > 0 && removeIndex >= 0) {
            currentSum -= signal[removeIndex]
            count--
        }
    }
    return result
}
