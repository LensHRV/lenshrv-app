package com.lenshrv.app.domain.algorithm

import junit.framework.TestCase.assertEquals
import org.junit.Test

class TimeDomainMetricsTest {
    private val rrIntervals = listOf(
        990L, 1010L, 990L, 1010L, 990L, 1010L,
        990L, 1010L, 990L, 1010L, 990L, 1010L
    )

    companion object {
        // Absolute tolerance matching precision up to 6 decimal places (1e-6)
        private const val DELTA_SIX_DECIMALS = 1e-6
    }

    @Test
    fun `calculates exact time domain HRV metrics from synthetic heartbeats`() {
        assertEquals("BPM mismatch", 60, TimeDomainMetrics.bpm(rrIntervals))
        assertEquals("SDNN mismatch", 10.0, TimeDomainMetrics.sdnn(rrIntervals), DELTA_SIX_DECIMALS)
        assertEquals(
            "RMSSD mismatch",
            20.0,
            TimeDomainMetrics.rmssd(rrIntervals),
            DELTA_SIX_DECIMALS
        )
        assertEquals(
            "Stress Index mismatch",
            1282.051282051282,
            TimeDomainMetrics.stressIndex(rrIntervals),
            DELTA_SIX_DECIMALS
        )
    }
}