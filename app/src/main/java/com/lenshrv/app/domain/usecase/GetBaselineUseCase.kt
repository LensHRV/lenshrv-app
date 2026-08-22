package com.lenshrv.app.domain.usecase

import com.lenshrv.app.domain.model.BaselineResult
import com.lenshrv.app.domain.model.HrvMetrics
import com.lenshrv.app.domain.repository.HrvMetricsRepository
import com.lenshrv.app.util.calculateMedian
import java.util.Calendar
import javax.inject.Inject

class GetBaselineUseCase @Inject constructor(
    private val metricsRepository: HrvMetricsRepository,
) {

    suspend operator fun invoke(
        current: HrvMetrics,
        daysToLookBack: Int = 7,
    ): BaselineResult {
        if (current.artifactPercent >= 15.0) {
            return baselineFrom(listOf(current))
        }

        val calendar = Calendar.getInstance().apply {
            timeInMillis = current.timestamp
            add(Calendar.DAY_OF_YEAR, -(daysToLookBack - 1))
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val pool = metricsRepository.getMetricsInRange(calendar.timeInMillis, current.timestamp)
        return baselineFrom(pool.ifEmpty { listOf(current) })
    }

    private fun baselineFrom(pool: List<HrvMetrics>): BaselineResult {
        return BaselineResult(
            bpm = calculateMedian(pool.map { it.bpm.toDouble() }),
            rmssd = calculateMedian(pool.map { it.rmssd }),
            sdnn = calculateMedian(pool.map { it.sdnn }),
            stressIndex = calculateMedian(pool.map { it.stressIndex }),
            lf = calculateMedian(pool.map { it.lf }),
            hf = calculateMedian(pool.map { it.hf }),
            lfHfRatio = calculateMedian(pool.map { it.lfHfRatio }),
            totalPower = calculateMedian(pool.map { it.totalPower }),
        )
    }
}
