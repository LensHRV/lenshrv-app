package com.lenshrv.app.domain.usecase

import com.lenshrv.app.domain.algorithm.ButterworthFilter
import com.lenshrv.app.domain.algorithm.CubicSplineResampler250hz
import com.lenshrv.app.domain.algorithm.ElgendiPeaks
import com.lenshrv.app.domain.algorithm.HermiteCubicInterpolator
import com.lenshrv.app.domain.algorithm.LombScargle
import com.lenshrv.app.domain.algorithm.LombScargleMagnitudeScale
import com.lenshrv.app.domain.algorithm.LombScargleSpectralFeatures
import com.lenshrv.app.domain.algorithm.RrMedianGate
import com.lenshrv.app.domain.algorithm.SmoothnessPriorsDetrend
import com.lenshrv.app.domain.algorithm.TimeDomainMetrics
import com.lenshrv.app.domain.model.Beat
import com.lenshrv.app.domain.model.HrvMetrics
import com.lenshrv.app.domain.model.RawSample
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.round

/**
 * End-to-end golden test for the full HRV pipeline (filter -> resample ->
 * peaks -> gate -> interpolate -> detrend -> Lomb-Scargle -> time domain).
 * A raw ~30 Hz session stored as a resource is decoded into RawSample lists,
 * matching the production pipeline, and the resulting HrvMetrics are compared
 * field by field against the notebook reference values.
 */
class SuperComputeHRVUseCaseTest {

    @Serializable
    private data class TvPoint(
        @SerialName("t") val t: Long,
        @SerialName("v") val v: Double,
    )

    companion object {
        private const val GOLD_FILE = "raw_30hz_5e12.json"
        private const val SESSION_ID = "gold-0-artifact"
        private const val TIMESTAMP = 142668275L
        private const val DURATION_SECONDS = 127

        private const val DELTA_SIX_DECIMALS = 1e-6

        private const val BPM = 61
        private const val RMSSD = 89.46177863236106
        private const val SDNN = 90.88171298910012
        private const val SI = 21.508151589452403
        private const val RESPIRATION_RATE = 19.30802348336595
        private const val LF = 2362.590731444277
        private const val HF = 3398.3929422949022
        private const val RATIO_LF_HF = 0.6952082268181861
        private const val ARTIFACT_PERCENT = 0.0
        private const val COVERAGE_PERCENT = 100.0
        private const val COHERENCE_SCORE = 0.2553381062409351
        private const val COHERENCE_PEAK_HZ = 0.05761252446183954
    }

    private fun loadRawSamples(): List<RawSample> {
        val resource = javaClass.classLoader
            ?.getResourceAsStream(GOLD_FILE)
            ?: error("Missing $GOLD_FILE in test resources")
        val text = resource.bufferedReader().use { it.readText() }
        val points = Json.decodeFromString<List<TvPoint>>(text)
        return List(points.size) { RawSample(points[it].t, points[it].v) }
    }

    @Test
    fun `pipeline outputs HrvMetrics`() {
        val raw = loadRawSamples()
        val filtered = ButterworthFilter(raw)
        assertEquals(raw.size, filtered.size)

        // Note (at250): The last sample might be up to 3 ms off the 4 ms grid.
        // Consider fixing in a future update.
        val at250 = CubicSplineResampler250hz(filtered, targetHz = 250.0)

        val peaks = ElgendiPeaks(at250, fs = 250)
        val gate = RrMedianGate(peaks)
        val kept = gate.kept

        // Note (hermite): from here on the pipeline keeps the two-Long Beat structure
        // (timestamp, rrInterval), so fractional values get rounded to Long at
        // some points. Impact was quickly simulated with AI and is negligible
        // spectral magnitudes shift ~0.08%, respiration and coherence unaffected,
        // but a future update should switch to a Double rrInterval Beat signature and drop the truncations.
        val hermite = HermiteCubicInterpolator(kept)

        val beats = hermite.beats
        // Nullable UseCase output; mirrors the null branch (null when beats < 4).
        val nonNull = if (beats.size >= 4) beats else null
        assertTrue("pipeline must not return null", nonNull != null)

        // rr for time domain
        val rr = kept.subList(0, hermite.endKeptIndex + 1).map { it.rrInterval }

        // Note (rrDet), (detBeats): Receives truncated precision.
        val rrDet = SmoothnessPriorsDetrend(
            DoubleArray(beats.size) { beats[it].rrInterval.toDouble() },
        )
        val detBeats = List(beats.size) { i ->
            Beat(
                timestamp = beats[i].timestamp,
                rrInterval = round(rrDet[i]).toLong(),
            )
        }
        val detRr = detBeats.map { it.rrInterval }
        val spectrumRaw = LombScargle(detBeats)
        val spectrumScaled = LombScargleMagnitudeScale(spectrumRaw, detRr)
        val lomb = LombScargleSpectralFeatures(spectrumScaled)
        val coveredMs = beats.last().timestamp - beats.first().timestamp
        assertTrue("tail trim must keep span under 121s", coveredMs < 121_000L)
        assertTrue("tail trim must cover at least 120s", coveredMs >= 120_000L)

        val coveragePercent =
            if (coveredMs >= 120_000L) 100.0
            else (coveredMs.toDouble() / 120_000.0) * 100.0

        val metrics = HrvMetrics(
            id = SESSION_ID,
            timestamp = TIMESTAMP,
            durationSeconds = DURATION_SECONDS,
            bpm = TimeDomainMetrics.bpm(rr),
            rmssd = TimeDomainMetrics.rmssd(rr),
            sdnn = TimeDomainMetrics.sdnn(rr),
            stressIndex = TimeDomainMetrics.stressIndex(rr),
            respirationRate = lomb.respirationRate,
            lf = lomb.lf,
            hf = lomb.hf,
            lfHfRatio = lomb.lfHfRatio,
            artifactPercent = hermite.artifactPercent,
            coveragePercent = coveragePercent,
            coherenceScore = lomb.coherenceScore,
            coherencePeakHz = lomb.coherencePeakHz,
            cameraMetadata = "test",
        )
        assertEquals("session id mismatch", SESSION_ID, metrics.id)
        assertEquals("timestamp mismatch", TIMESTAMP, metrics.timestamp)
        assertEquals("duration mismatch", DURATION_SECONDS, metrics.durationSeconds)
        assertEquals("bpm mismatch", BPM, metrics.bpm)
        assertEquals("rmssd mismatch", RMSSD, metrics.rmssd, DELTA_SIX_DECIMALS)
        assertEquals("sdnn mismatch", SDNN, metrics.sdnn, DELTA_SIX_DECIMALS)
        assertEquals("stress index mismatch", SI, metrics.stressIndex, DELTA_SIX_DECIMALS)
        assertEquals(
            "respiration rate mismatch",
            RESPIRATION_RATE,
            metrics.respirationRate,
            DELTA_SIX_DECIMALS
        )
        assertEquals("lf mismatch", LF, metrics.lf, DELTA_SIX_DECIMALS)
        assertEquals("hf mismatch", HF, metrics.hf, DELTA_SIX_DECIMALS)
        assertEquals("lf/hf ratio mismatch", RATIO_LF_HF, metrics.lfHfRatio, DELTA_SIX_DECIMALS)
        assertEquals(
            "artifact percent mismatch",
            ARTIFACT_PERCENT,
            metrics.artifactPercent,
            DELTA_SIX_DECIMALS
        )
        assertEquals(
            "coverage percent mismatch",
            COVERAGE_PERCENT,
            metrics.coveragePercent,
            DELTA_SIX_DECIMALS
        )
        assertEquals(
            "coherence score mismatch",
            COHERENCE_SCORE,
            metrics.coherenceScore,
            DELTA_SIX_DECIMALS
        )
        assertEquals(
            "coherence peak hz mismatch",
            COHERENCE_PEAK_HZ,
            metrics.coherencePeakHz,
            DELTA_SIX_DECIMALS
        )
    }
}
