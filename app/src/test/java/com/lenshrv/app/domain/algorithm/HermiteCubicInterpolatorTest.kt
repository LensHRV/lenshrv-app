package com.lenshrv.app.domain.algorithm

import com.lenshrv.app.domain.model.Beat
import junit.framework.TestCase.assertEquals
import org.junit.Test

/**
 * Mathematical and behavioral tests for cubic Hermite interpolation.
 * Verifies finite-difference slopes, cubic polynomial convergence,
 * and deterministic beat imputation across timeline gaps.
 */

class HermiteCubicInterpolatorTest {

    companion object {
        private const val DELTA_TWELVE_DECIMALS = 1e-12
    }

    val goldenSample = listOf<Long>(800, 820, 880, 980, 1020, 1040)

    /** Verifies forward, central, and backward finite differences across all index positions. */
    @Test
    fun `slope calculates forward, central, and backward differences correctly`() {
        assertEquals("slope[0] mismatch", 20.0, HermiteCubicInterpolator.slope(goldenSample, 0), DELTA_TWELVE_DECIMALS)
        assertEquals("slope[1] mismatch", 40.0, HermiteCubicInterpolator.slope(goldenSample, 1), DELTA_TWELVE_DECIMALS)
        assertEquals("slope[2] mismatch", 80.0, HermiteCubicInterpolator.slope(goldenSample, 2), DELTA_TWELVE_DECIMALS)
        assertEquals("slope[3] mismatch", 70.0, HermiteCubicInterpolator.slope(goldenSample, 3), DELTA_TWELVE_DECIMALS)
        assertEquals("slope[4] mismatch", 30.0, HermiteCubicInterpolator.slope(goldenSample, 4), DELTA_TWELVE_DECIMALS)
        assertEquals("slope[5] mismatch", 20.0, HermiteCubicInterpolator.slope(goldenSample, 5), DELTA_TWELVE_DECIMALS)
    }

    /** Verifies polynomial convergence at boundary anchors (t=0, t=1) and midpoint with active slopes. */
    @Test
    fun `hermite calculates boundary and midpoint interpolation correctly`() {
        val p0 = 880.0
        val p1 = 980.0
        val m0 = 80.0
        val m1 = 70.0
        assertEquals("hermite t=0 mismatch", 880.0, HermiteCubicInterpolator.hermite(0.0, p0, m0, p1, m1), DELTA_TWELVE_DECIMALS)
        assertEquals("hermite t=1 mismatch", 980.0, HermiteCubicInterpolator.hermite(1.0, p0, m0, p1, m1), DELTA_TWELVE_DECIMALS)
        assertEquals("hermite t=0.5 mismatch", 931.25, HermiteCubicInterpolator.hermite(0.5, p0, m0, p1, m1), DELTA_TWELVE_DECIMALS)
    }

    /** Verifies zero-tangent flattening and exact linear ramp reproduction. */
    @Test
    fun `hermite handles zero and equal slopes correctly`() {
        val p0 = 880.0
        val p1 = 980.0
        assertEquals("zero-slope midpoint mismatch", 930.0, HermiteCubicInterpolator.hermite(0.5, p0, 0.0, p1, 0.0), DELTA_TWELVE_DECIMALS)
        assertEquals("equal-slope quarter mismatch", 905.0, HermiteCubicInterpolator.hermite(0.25, p0, 100.0, p1, 100.0), DELTA_TWELVE_DECIMALS)
    }

    /** Verifies multi-beat imputation and sequential timestamp accumulation across an extended gap. */
    @Test
    fun `interpolator inserts exactly 2 beats on a 3x gap`() {
        val input = listOf(
            Beat(timestamp = 1000L, rrInterval = 1000L),
            Beat(timestamp = 2000L, rrInterval = 1000L),
            Beat(timestamp = 5000L, rrInterval = 1000L),
            Beat(timestamp = 6000L, rrInterval = 1000L),
        )
        val result = HermiteCubicInterpolator(input)
        assertEquals("beats size mismatch", 6, result.beats.size)
        assertEquals("synthetic set mismatch", setOf(3000L, 4000L), result.syntheticTimestamps)
        assertEquals("beat[2] timestamp mismatch", 3000L, result.beats[2].timestamp)
        assertEquals("beat[2] rr mismatch", 1000L, result.beats[2].rrInterval)
        assertEquals("beat[3] timestamp mismatch", 4000L, result.beats[3].timestamp)
        assertEquals("beat[3] rr mismatch", 1000L, result.beats[3].rrInterval)
    }

    /** Verifies gap reconstruction and synthetic timestamp registration on multi-beat loss. */
    @Test
    fun `interpolator detects gap and reconstructs multiple missing beats`() {
        val input = listOf(
            Beat(timestamp = 1000L, rrInterval = 1000L),
            Beat(timestamp = 2000L, rrInterval = 1000L),
            Beat(timestamp = 5000L, rrInterval = 1000L),
            Beat(timestamp = 6000L, rrInterval = 1000L),
        )
        val result = HermiteCubicInterpolator(input)
        assertEquals("beats size mismatch", 6, result.beats.size)
        assertEquals("synthetic set mismatch", setOf(3000L, 4000L), result.syntheticTimestamps)
        assertEquals("beat[2] timestamp mismatch", 3000L, result.beats[2].timestamp)
        assertEquals("beat[3] timestamp mismatch", 4000L, result.beats[3].timestamp)
    }
}