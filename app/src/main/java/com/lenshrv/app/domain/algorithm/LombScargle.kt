package com.lenshrv.app.domain.algorithm

import com.lenshrv.app.domain.model.Beat
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
data class LombScarglePsdResult(
    val freqHz: DoubleArray,
    val psd: DoubleArray,
    val deltaF: Double,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as LombScarglePsdResult
        if (deltaF != other.deltaF) return false
        if (!freqHz.contentEquals(other.freqHz)) return false
        if (!psd.contentEquals(other.psd)) return false
        return true
    }

    override fun hashCode(): Int {
        var result = deltaF.hashCode()
        result = 31 * result + freqHz.contentHashCode()
        result = 31 * result + psd.contentHashCode()
        return result
    }
}

object LombScargle {
    private const val DEFAULT_N_FREQ = 512
    private const val DEFAULT_F_MIN = 0.04
    private const val DEFAULT_F_MAX = 0.40

    operator fun invoke(
        beats: List<Beat>,
        nFreq: Int = DEFAULT_N_FREQ,
        fMin: Double = DEFAULT_F_MIN,
        fMax: Double = DEFAULT_F_MAX,
    ): LombScarglePsdResult {
        val deltaF = (fMax - fMin) / (nFreq - 1)
        val freqHz = DoubleArray(nFreq) { k -> fMin + k * deltaF }

        val t0 = beats.first().timestamp
        val t = DoubleArray(beats.size) { (beats[it].timestamp - t0) / 1000.0 }
        val y = DoubleArray(beats.size) { beats[it].rrInterval.toDouble() }

        val power = periodogramPower(t, y, freqHz)
        val psd = DoubleArray(nFreq) { k -> power[k] / deltaF }

        return LombScarglePsdResult(
            freqHz = freqHz,
            psd = psd,
            deltaF = deltaF,
        )
    }

    fun periodogramPower(t: DoubleArray, y: DoubleArray, freqHz: DoubleArray): DoubleArray {
        val n = t.size
        var mean = 0.0
        for (v in y) mean += v
        mean /= n

        val yy = DoubleArray(n) { y[it] - mean }
        val out = DoubleArray(freqHz.size)

        for (i in freqHz.indices) {
            val f = freqHz[i]
            val w = 2.0 * PI * f

            var s2 = 0.0
            var c2 = 0.0
            for (j in 0 until n) {
                val a = 2.0 * w * t[j]
                s2 += sin(a)
                c2 += cos(a)
            }
            val tau = atan2(s2, c2) / (2.0 * w)

            var yc = 0.0
            var ys = 0.0
            var cc = 0.0
            var ss = 0.0
            for (j in 0 until n) {
                val wt = w * (t[j] - tau)
                val c = cos(wt)
                val s = sin(wt)
                yc += yy[j] * c
                ys += yy[j] * s
                cc += c * c
                ss += s * s
            }

            val termC = if (cc > 0.0) (yc * yc) / cc else 0.0
            val termS = if (ss > 0.0) (ys * ys) / ss else 0.0
            out[i] = 0.5 * (termC + termS)
        }
        return out
    }
}
