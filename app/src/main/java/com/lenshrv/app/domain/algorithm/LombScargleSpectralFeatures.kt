package com.lenshrv.app.domain.algorithm

data class LombScargleSpectralFeaturesResult(
    val lf: Double,
    val hf: Double,
    val lfHfRatio: Double,
    val respirationRate: Double,
    val coherenceScore: Double,
    val coherencePeakHz: Double,
)

object LombScargleSpectralFeatures {

    private const val LF_LO = 0.04
    private const val LF_HI = 0.15
    private const val HF_LO = 0.15
    private const val HF_HI = 0.40
    private const val COH_LO = 0.04
    private const val COH_HI = 0.12
    private const val COH_HALF_WIN_HZ = 0.015625

    operator fun invoke(spectrum: LombScarglePsdResult): LombScargleSpectralFeaturesResult {
        val freq = spectrum.freqHz
        val psd = spectrum.psd
        val df = spectrum.deltaF
        val n = freq.size

        var lfPower = 0.0
        var hfPower = 0.0

        var maxResp = -1.0
        var respHz = 0.0

        var maxCoh = -1.0
        var cohPeakHz = 0.0
        var cohPeakIdx = -1

        for (k in 0 until n) {
            val f = freq[k]
            val p = psd[k] * df

            if (f in LF_LO..<LF_HI) {
                lfPower += p
            }
            if (f in HF_LO..HF_HI) {
                hfPower += p
                val weighted = psd[k] * f
                if (weighted > maxResp) {
                    maxResp = weighted
                    respHz = f
                }
            }
            if (f in COH_LO..COH_HI) {
                if (psd[k] > maxCoh) {
                    maxCoh = psd[k]
                    cohPeakHz = f
                    cohPeakIdx = k
                }
            }
        }

        val total = lfPower + hfPower
        val ratio = if (hfPower > 0.0) lfPower / hfPower else 0.0
        val rpm = respHz * 60.0

        var coherenceRatio = 0.0
        if (cohPeakIdx >= 0) {
            val fPeak = freq[cohPeakIdx]
            var peakPower = 0.0
            for (k in 0 until n) {
                val f = freq[k]
                if (f >= fPeak - COH_HALF_WIN_HZ && f <= fPeak + COH_HALF_WIN_HZ && f <= HF_HI) {
                    peakPower += psd[k] * df
                }
            }
            val rest = total - peakPower
            coherenceRatio = if (rest > 0.0) peakPower / rest else 0.0
        }

        return LombScargleSpectralFeaturesResult(
            lf = lfPower,
            hf = hfPower,
            lfHfRatio = ratio,
            respirationRate = rpm,
            coherenceScore = coherenceRatio,
            coherencePeakHz = cohPeakHz,
        )
    }
}
