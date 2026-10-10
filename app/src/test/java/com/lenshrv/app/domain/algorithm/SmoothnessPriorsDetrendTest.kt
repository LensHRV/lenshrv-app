package com.lenshrv.app.domain.algorithm

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Verifies the regularized trend removal pipeline based on the Smoothness Priors method
 * (Tarvainen et al., 2002), benchmarking against analytical/numerical reference implementations
 * from Python (NumPy / SciPy).
 * An absolute tolerance (delta) of 1e-5 ms is used to accommodate minor floating-point
 * divergence across distinct numerical solver architectures (Gauss-Jordan elimination
 * in Kotlin vs Python Standard).
 */

class SmoothnessPriorsDetrendTest {

    companion object {
        private const val DELTA_FIVE_DECIMALS = 1e-5
        private const val DELTA_SIX_DECIMALS = 1e-6
    }

    @Test
    fun testSmoothnessPriorsDetrendTwelveBeats() {
        val rr = doubleArrayOf(
            910.0, 925.0, 905.0, 940.0, 960.0, 950.0,
            975.0, 965.0, 990.0, 980.0, 1005.0, 995.0
        )
        val expectedResidual = doubleArrayOf(
            -0.8325903694221779,
            5.5307377124711365,
            -23.105930875274225,
            3.2573850747521647,
            14.620759193683966,
            -4.015747916885516,
            12.347865861600212,
            -6.288381289310678,
            10.075479420466763,
            -8.560558065457485,
            7.803459894608977,
            -10.832478815409331
        )
        val actualResidual = SmoothnessPriorsDetrend(rr)

        assertEquals(
            "The output array length must match the input sequence length",
            expectedResidual.size,
            actualResidual.size
        )

        for (i in expectedResidual.indices) {
            assertEquals(
                "Residual mismatch at sample index $i",
                expectedResidual[i],
                actualResidual[i],
                DELTA_FIVE_DECIMALS
            )
        }
    }
    /**
     * Analytical Golden Test: A pure linear ramp has zero second-order curvature (D2 * rr = 0).
     * Therefore, the trend line must match the input ramp identically, and the detrended
     * residual must evaluate to 0.0 across all samples.
     */
    @Test
    fun testSmoothnessPriorsDetrendPureLinearRampEvaluatesToZero() {
        // Arrange: A perfectly straight line
        val linearRamp = DoubleArray(12) { (it + 1) * 10.0 }

        // Act: Run detrending
        val actualResidual = SmoothnessPriorsDetrend(linearRamp)

        // Assert: Every single residual point must be 0.0
        for (i in actualResidual.indices) {
            assertEquals(
                "Residual at index $i must be 0.0 for a pure straight line",
                0.0,
                actualResidual[i],
                DELTA_SIX_DECIMALS
            )
        }
    }
}