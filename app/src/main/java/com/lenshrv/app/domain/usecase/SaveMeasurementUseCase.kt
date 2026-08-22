package com.lenshrv.app.domain.usecase

import com.lenshrv.app.domain.model.RawSample
import com.lenshrv.app.domain.model.HrvMetrics
import com.lenshrv.app.domain.repository.HrvMetricsRepository
import javax.inject.Inject

class SaveMeasurementUseCase @Inject constructor(
    private val metricsRepository: HrvMetricsRepository
) {
    suspend operator fun invoke(metrics: HrvMetrics, rawData: List<RawSample>) {
        metricsRepository.save(metrics, rawData)
    }
}
