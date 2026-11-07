package com.lenshrv.app.local.exporter

import com.lenshrv.app.local.entities.HrvMetricsEntity
import org.junit.Assert.assertTrue
import org.junit.Test

class DataExporterTest {
    @Test
    fun `metrics export includes and escapes measurement tag`() {
        val metrics = HrvMetricsEntity(
            id = "session-1",
            timestamp = 1L,
            durationSeconds = 120,
            bpm = 60,
            rmssd = 1.0,
            sdnn = 2.0,
            stressIndex = 3.0,
            respirationRate = 4.0,
            lf = 5.0,
            hf = 6.0,
            lfHfRatio = 7.0,
            artifactPercent = 8.0,
            coveragePercent = 100.0,
            coherenceScore = 9.0,
            coherencePeakHz = 0.1,
            cameraMetadata = "camera",
            tag = "After \"run\", calm",
        )

        val csv = DataExporter.hrvMetricsToCsv(listOf(metrics))

        assertTrue(csv.lineSequence().first().split(',').contains("tag"))
        assertTrue(csv.contains("\"After \"\"run\"\", calm\""))
    }
}
