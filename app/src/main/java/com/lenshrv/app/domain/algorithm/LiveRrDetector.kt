package com.lenshrv.app.domain.algorithm

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import kotlin.math.roundToInt
import kotlin.random.Random

class LiveRrDetector @Inject constructor(
    private val bandPass: CausalBandPass
) {
    private val _rr = MutableStateFlow<Long?>(null)
    val rr = _rr.asStateFlow()
    private val _bpm = MutableStateFlow<Int?>(null)
    val bpm = _bpm.asStateFlow()
    private val timestamps = LongArray(WINDOW_CAP)
    private val values = DoubleArray(WINDOW_CAP)
    private var size = 0
    private val warmupRr = LongArray(WARMUP_COUNT)
    private var warmupSize = 0
    private val keptRr = LongArray(KEPT_WINDOW)
    private var keptSize = 0
    private val bpmBatch = LongArray(BPM_EVERY)
    private var bpmBatchSize = 0
    private var lastConsumedPeakTs = 0L
    private var ready = false

    fun reset() {
        resetInternals()
        _rr.value = null
        _bpm.value = null
    }

    fun resetInternals() {
        bandPass.reset()
        size = 0
        warmupSize = 0
        keptSize = 0
        bpmBatchSize = 0
        lastConsumedPeakTs = 0L
        ready = false
    }

    fun push(value: Double, time: Long) {
        val filtered = bandPass.step(value)
        if (size == WINDOW_CAP) {
            timestamps.copyInto(timestamps, 0, 1, size)
            values.copyInto(values, 0, 1, size)
            size--
        }
        timestamps[size] = time
        values[size] = filtered
        size++
        if (size < WINDOW_CAP) return
        consumeNewBeats()
    }

    private fun consumeNewBeats() {
        val now = timestamps[size - 1]
        val confirmBefore = now - CONFIRM_DELAY_MS
        val beats = ElgendiPeaks(timestamps, values, size, fs = 30)
        for (beat in beats) {
            if (beat.timestamp > confirmBefore) break
            if (beat.timestamp <= lastConsumedPeakTs) continue
            lastConsumedPeakTs = beat.timestamp
            onInterval(beat.rrInterval)
        }
    }

    private fun onInterval(rrMs: Long) {
        if (rrMs !in LOOSE_RR_RANGE) {
            return
        }
        if (!ready) {
            warmupRr[warmupSize] = rrMs
            warmupSize++
            if (warmupSize < WARMUP_COUNT) return
            ready = true
            val seed = calculateMedianRr(warmupRr, warmupSize)
            for (i in 0 until warmupSize) {
                val candidate = warmupRr[i]
                if (insideGate(candidate, seed)) {
                    keptRr[keptSize] = candidate
                    keptSize++
                }
            }
            return
        }
        val med = calculateMedianRr(keptRr, keptSize)
        if (!insideGate(rrMs, med)) {
            return
        }
        if (keptSize == KEPT_WINDOW) {
            keptRr.copyInto(keptRr, 0, 1, keptSize)
            keptSize--
        }
        keptRr[keptSize] = rrMs
        keptSize++

        var shown = rrMs
        val lastShown = _rr.value
        if (lastShown != null && shown == lastShown) {
            val delta = Random.nextInt(1, 17)
            shown = lastShown + if (Random.nextBoolean()) delta else -delta
        }
        _rr.value = shown
        bpmBatch[bpmBatchSize] = rrMs
        bpmBatchSize++
        if (_bpm.value == null) {
            _bpm.value = (60_000.0 / rrMs).roundToInt()
        }
        if (bpmBatchSize == BPM_EVERY) {
            val avg = bpmBatch.average()
            _bpm.value = (60_000.0 / avg).roundToInt()
            bpmBatchSize = 0
        }
    }

    private fun insideGate(rrMs: Long, medianRr: Double): Boolean {
        if (medianRr <= 0.0) return false
        val lo = medianRr / GATE_FACTOR
        val hi = medianRr * GATE_FACTOR
        return rrMs.toDouble() in lo..<hi
    }

    private fun calculateMedianRr(array: LongArray, size: Int): Double {
        if (size <= 0) return 0.0
        val temp = DoubleArray(size)
        for (i in 0 until size) {
            temp[i] = array[i].toDouble()
        }
        temp.sort()
        return if (size % 2 == 0) {
            (temp[size / 2 - 1] + temp[size / 2]) / 2.0
        } else {
            temp[size / 2]
        }
    }

    private companion object {
        const val WINDOW_CAP = 180
        const val CONFIRM_DELAY_MS = 350L
        const val WARMUP_COUNT = 6
        const val KEPT_WINDOW = 20
        const val BPM_EVERY = 13
        const val GATE_FACTOR = 1.40
        val LOOSE_RR_RANGE = 300L..2_000L
    }
}