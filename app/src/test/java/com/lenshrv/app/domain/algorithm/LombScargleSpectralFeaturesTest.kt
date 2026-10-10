package com.lenshrv.app.domain.algorithm

import junit.framework.TestCase.assertEquals
import org.junit.Test

class LombScargleSpectralFeaturesTest {

    companion object {
        private const val DELTA_SIX_DECIMALS = 1e-6
    }

    private val mockSpectrum = LombScarglePsdResult(
        freqHz = DoubleArray(51) { it * 0.01 },
        psd = DoubleArray(51).apply {
            this[10] = 100.0
            this[25] = 50.0
            this[11] = 50.0
            this[26] = 25.0
        },
        deltaF = 0.01
    )

    @Test
    fun `extracts exact spectral features from controlled mock spectrum`() {
        val result = LombScargleSpectralFeatures(mockSpectrum)
        assertEquals("LF power mismatch", 1.5, result.lf, DELTA_SIX_DECIMALS)
        assertEquals("HF power mismatch", 0.75, result.hf, DELTA_SIX_DECIMALS)
        assertEquals("LF/HF ratio mismatch", 2.0, result.lfHfRatio, DELTA_SIX_DECIMALS)
        assertEquals("Respiration rate mismatch", 15.0, result.respirationRate, DELTA_SIX_DECIMALS)
        assertEquals(
            "Coherence peak frequency mismatch",
            0.10,
            result.coherencePeakHz,
            DELTA_SIX_DECIMALS
        )
        assertEquals("Coherence score mismatch", 2.0, result.coherenceScore, DELTA_SIX_DECIMALS)
    }
}