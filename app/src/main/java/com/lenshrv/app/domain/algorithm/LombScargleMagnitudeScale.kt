package com.lenshrv.app.domain.algorithm

object LombScargleMagnitudeScale {

    operator fun invoke(
        spectrumRaw: LombScarglePsdResult,
        rrMs: List<Long>,
    ): LombScarglePsdResult {
        if (rrMs.size < 2) return spectrumRaw

        var mean = 0.0
        for (v in rrMs) mean += v.toDouble()
        mean /= rrMs.size

        var ss = 0.0
        for (v in rrMs) {
            val d = v.toDouble() - mean
            ss += d * d
        }
        val varRr = ss / rrMs.size
        if (varRr <= 0.0) return spectrumRaw

        var integral = 0.0
        for (p in spectrumRaw.psd) integral += p * spectrumRaw.deltaF
        if (integral <= 0.0) return spectrumRaw

        val scale = varRr / integral
        val scaled = DoubleArray(spectrumRaw.psd.size) { spectrumRaw.psd[it] * scale }
        return LombScarglePsdResult(
            freqHz = spectrumRaw.freqHz,
            psd = scaled,
            deltaF = spectrumRaw.deltaF,
        )
    }
}
