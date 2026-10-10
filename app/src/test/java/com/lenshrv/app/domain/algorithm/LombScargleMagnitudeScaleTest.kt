package com.lenshrv.app.domain.algorithm

import com.lenshrv.app.domain.model.Beat
import com.lenshrv.app.fixtures.generateSineSignal
import junit.framework.TestCase.assertEquals
import org.junit.Test

/**
 * Validates Parseval's energy conservation theorem: the total integrated power spectral
 * density (PSD) in the frequency domain must strictly equal the signal variance (SDNN^2)
 * in the time domain, calibrated in physical units of ms^2.
 */

class LombScargleMagnitudeScaleTest {
    companion object {
        private const val DELTA_SIX_DECIMALS = 1e-6
    }

    @Test
    fun `magnitude scale normalizes psd integral to match signal variance`() {
        val beats = generateSineSignal(
            frequencyHz = 0.15,
            amplitude = 20.0,
            durationSeconds = 60.0,
            samplingRateHz = 1.0,
        ).map {
            Beat(timestamp = it.timestamp, rrInterval = it.value.toLong())
        }
        val rawSpectrum = LombScargle(beats)
        val rrMs = beats.map { it.rrInterval }
        val meanRr = rrMs.map { it.toDouble() }.average()

        val expectedEnergyTimeDomain =
            rrMs.sumOf { (it.toDouble() - meanRr).let { diff -> diff * diff } } / rrMs.size

        val scaledSpectrum = LombScargleMagnitudeScale(rawSpectrum, rrMs)
        val integratedEnergyFreqDomain = scaledSpectrum.psd.sum() * scaledSpectrum.deltaF

        assertEquals(
            "Total spectral energy (integral of PSD * deltaF) must match time-domain variance (SDNN^2)",
            expectedEnergyTimeDomain,
            integratedEnergyFreqDomain,
            DELTA_SIX_DECIMALS
        )

        val rawMaxIndex = rawSpectrum.psd.indices.maxByOrNull { rawSpectrum.psd[it] } ?: 0
        val scaledMaxIndex = scaledSpectrum.psd.indices.maxByOrNull { scaledSpectrum.psd[it] } ?: 0

        assertEquals(
            "Scaling must preserve the exact peak frequency bin index (expected raw: $rawMaxIndex, got scaled: $scaledMaxIndex)",
            rawMaxIndex,
            scaledMaxIndex
        )

        assertEquals(
            "Frequency resolution (deltaF) must remain unchanged (expected raw: ${rawSpectrum.deltaF}, got scaled: ${scaledSpectrum.deltaF})",
            rawSpectrum.deltaF,
            scaledSpectrum.deltaF,
            0.0
        )
    }
}