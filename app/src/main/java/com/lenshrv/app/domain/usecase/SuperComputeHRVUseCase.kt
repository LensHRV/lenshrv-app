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
import javax.inject.Inject
import kotlin.math.round

class SuperComputeHRVUseCase @Inject constructor(
) {
    operator fun invoke(
        rawSamples: List<RawSample>,
        sessionId: String,
        timestamp: Long,
        durationSeconds: Int,
        cameraMetadata: String,
    ): HrvMetrics? {
        val filtered = ButterworthFilter(rawSamples)
        val at250 = CubicSplineResampler250hz(filtered, targetHz = 250.0)
        val peaks = ElgendiPeaks(at250, fs = 250)
        val gate = RrMedianGate(peaks)
        val kept = gate.kept
        val hermite = HermiteCubicInterpolator(kept)
        val beats = hermite.beats
        if (beats.size < 4) return null
        val rr = kept.subList(0, hermite.endKeptIndex + 1).map { it.rrInterval }
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
        val coveragePercent =
            if (coveredMs >= 120_000L) 100.0
            else (coveredMs.toDouble() / 120_000.0) * 100.0

        return HrvMetrics(
            id = sessionId,
            timestamp = timestamp,
            durationSeconds = durationSeconds,
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
            algorithmVersion = 1,
            cameraMetadata = cameraMetadata,
        )
    }
}
