package com.lenshrv.app.domain.algorithm

import com.lenshrv.app.domain.model.RawSample
import com.lenshrv.app.fixtures.generateSineSignal
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Validates the frequency response of [ButterworthFilter] against theoretical values
 * computed from SciPy (`scipy.signal.freqz`).
 *
 * Test Design Rationale:
 * - Forward-Backward Filtering (Zero-Phase): The filter operates bidirectionally (filtfilt),
 *   squaring the magnitude response (|H(f)|^2) and eliminating phase distortion.
 * - Transient Warmup: The first and last 120 samples (~4s at 30 Hz) are excluded to discard
 *   IIR startup transients and measure purely in steady-state oscillation.
 * - Nyquist Limit Consideration: Frequencies near the Nyquist fold (e.g., 8 Hz or 12 Hz at Fs = 30 Hz,
 *   where Nyquist is 15 Hz) suffer from discretization straddling, where discrete samples miss
 *   continuous signal peaks and degrade peak-to-peak amplitude measurement into coarse approximations.
 *   Validation is strictly focused on well-sampled physiological bands (<= 1.5 Hz).
 */

class ButterworthFilterTest {
    companion object {
        private const val DELTA_THREE_DECIMALS = 0.001

        // Theoretical output amplitudes from SciPy reference (scipy.signal.freqz | A_in = 10.0, Fs = 30Hz)
        // Gain formula: |H_HP(f)|^2 * |H_LP(f)|^2
        private const val EXPECTED_1_5HZ = 9.877458308464
        private const val EXPECTED_0_5HZ = 4.999975208624
    }


    private fun measureSteadyStateAmplitude(
        samples: List<RawSample>,
        warmupSamples: Int = 120
    ): Double {
        val steadyRegion = samples.subList(warmupSamples, samples.size - warmupSamples)
        val max = steadyRegion.maxOf { it.value }
        val min = steadyRegion.minOf { it.value }

        return (max - min) / 2.0
    }

    private fun filterAndMeasure(frequencyHz: Double): Double {
        val input = generateSineSignal(frequencyHz)
        val output = ButterworthFilter(input)
        return measureSteadyStateAmplitude(output)
    }

    @Test
    fun passband_heartRateFrequency_passesWithFullAmplitude() {
        val amplitude = filterAndMeasure(frequencyHz = 1.5)
        println("Measured amplitude at 1.5 Hz: $amplitude")
        assertEquals(
            "Heart rate frequencies in the passband (1.5 Hz) must retain full amplitude",
            EXPECTED_1_5HZ,
            amplitude,
            DELTA_THREE_DECIMALS
        )
    }

    @Test
    fun cutoffFrequency_lowCutoff_halfHz_attenuatesToFiftyPercent() {
        val amplitude = filterAndMeasure(frequencyHz = 0.5)
        assertEquals(
            "Signal at lower cutoff frequency (0.5 Hz) must be attenuated to 50% power",
            EXPECTED_0_5HZ,
            amplitude,
            DELTA_THREE_DECIMALS
        )
    }
}
