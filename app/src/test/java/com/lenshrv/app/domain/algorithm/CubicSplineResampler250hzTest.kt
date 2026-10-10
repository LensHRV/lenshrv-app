package com.lenshrv.app.domain.algorithm


import com.lenshrv.app.fixtures.generateSineSignal
import org.junit.Test
import org.junit.Assert.assertEquals

/**
 * Validates the mathematical precision of the cubic spline resampler against a pure sine wave.
 *
 * Test design rationale:
 * - Input rate (25 Hz): Chosen because 1000 ms is evenly divisible by 25.
 *   This avoids integer millisecond truncation artifacts and aligns cleanly with the default
 *   250 Hz target grid (4 ms steps), allowing exact node placement.
 * - Duration (1s): Provides sufficient support points (20 nodes) to stabilize Thomas's
 *   tridiagonal solver and prevent boundary reflections from the natural spline condition.
 * - Scope (indices 0..10): Evaluates the entire first 40 ms interval
 *   without requiring additional steps along the sine wave,
 *   guaranteeing mathematical accuracy up to three decimal places (0.001 tolerance).
 */

class CubicSplineResampler250hzTest {

    companion object {
        private const val DELTA_THREE_DECIMALS = 0.001
    }

    @Test
    fun `resample 25Hz to 250Hz preserves signal shape with minimal polynomial error`() {
        val input25Hz = generateSineSignal(1.5, samplingRateHz = 25.0, durationSeconds = 1.0)
        val input250Hz = generateSineSignal(1.5, samplingRateHz = 250.0, durationSeconds = 0.05)
        val splineOutput250Hz = CubicSplineResampler250hz(input25Hz)
        for (i in 0..10) {
            assertEquals(
                "Timestamp misaligned at sample index $i",
                input250Hz[i].value,
                splineOutput250Hz[i].value,
                DELTA_THREE_DECIMALS
            )
            assertEquals(
                "Spline amplitude error exceeds tolerance at sample index $i",
                (i * 4).toLong(),
                splineOutput250Hz[i].timestamp
            )
        }
    }
}