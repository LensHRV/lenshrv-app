package com.lenshrv.app.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class HrvMetricsTest {
    @Test
    fun `measurement tag is trimmed and limited to 140 characters`() {
        val tag = "  ${"a".repeat(MAX_MEASUREMENT_TAG_LENGTH + 20)}  "

        val normalized = normalizeMeasurementTag(tag)

        assertEquals(MAX_MEASUREMENT_TAG_LENGTH, normalized.length)
        assertEquals("a".repeat(MAX_MEASUREMENT_TAG_LENGTH), normalized)
    }
}
