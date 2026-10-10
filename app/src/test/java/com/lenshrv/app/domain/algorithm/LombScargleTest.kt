package com.lenshrv.app.domain.algorithm

import com.lenshrv.app.domain.model.Beat
import com.lenshrv.app.fixtures.generateSineSignal
import junit.framework.TestCase.assertEquals
import org.junit.Test

/**
 * Benchmarked against SciPy's standard reference implementation (`scipy.signal.lombscargle`).
 * Note:
 * Truncating synthetic amplitudes to [Long] causes minor quantization that slightly
 * attenuates peak PSD magnitude without shifting the peak frequency.
 */

class LombScargleTest {
    companion object {
        // Absolute tolerance matching precision up to 6 decimal places (1e-6)
        private const val DELTA_SIX_DECIMALS = 1e-6

        // Ground-truth reference values generated via SciPy
        private const val EXPECTED_PEAK_INDEX = 156
        private const val EXPECTED_PEAK_FREQ = 0.14990215264187867
        private const val EXPECTED_MAX_PSD = 8290294.657869166
        private const val EXPECTED_PSD_BIN_3 = 50726.6034917096
        private const val EXPECTED_PSD_BIN_N_MINUS_3 = 581.5054792308158
    }

    @Test
    fun `lomb scargle on pure sine wave matches reference PSD spectrum and peak location` () {
        val beats = generateSineSignal(
            frequencyHz = 0.15,
            amplitude = 20.0,
            durationSeconds = 60.0,
            samplingRateHz = 1.0,
        ).map {
            Beat(timestamp = it.timestamp, rrInterval = it.value.toLong())
        }
        val lomb = LombScargle(beats)

        val maxIndex = lomb.psd.indices.maxByOrNull { lomb.psd[it] } ?: 0
        val idxNMinus3 = lomb.psd.size - 4

        assertEquals(
            "Peak index must correspond to bin 156",
            EXPECTED_PEAK_INDEX,
            maxIndex
        )
        assertEquals(
            "Peak frequency mismatch",
            EXPECTED_PEAK_FREQ,
            lomb.freqHz[maxIndex],
            DELTA_SIX_DECIMALS
        )
        assertEquals(
            "Peak PSD magnitude mismatch",
            EXPECTED_MAX_PSD,
            lomb.psd[maxIndex],
            DELTA_SIX_DECIMALS
        )
        assertEquals(
            "PSD mismatch at bin 3",
            EXPECTED_PSD_BIN_3,
            lomb.psd[3],
            DELTA_SIX_DECIMALS
        )
        assertEquals(
            "PSD mismatch at bin n-3 (idx $idxNMinus3)",
            EXPECTED_PSD_BIN_N_MINUS_3,
            lomb.psd[idxNMinus3],
            DELTA_SIX_DECIMALS
        )
    }
}