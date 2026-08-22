package com.lenshrv.app.domain.repository

import androidx.room.withTransaction
import com.lenshrv.app.domain.model.RawSample
import com.lenshrv.app.domain.model.HrvMetrics
import com.lenshrv.app.domain.model.HrvMetricsSummary
import com.lenshrv.app.local.AppDatabase
import com.lenshrv.app.local.dao.ChannelValuesDao
import com.lenshrv.app.local.dao.HrvMetricsDao
import com.lenshrv.app.local.entities.ChannelValuesEntity
import com.lenshrv.app.local.entities.HrvMetricsEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HrvMetricsRepositoryImpl @Inject constructor(
    private val db: AppDatabase,
    private val hrvMetricsDao: HrvMetricsDao,
    private val channelValuesDao: ChannelValuesDao,
) : HrvMetricsRepository {

    override suspend fun getAllMetrics(): List<HrvMetricsEntity> {
        return hrvMetricsDao.getAllMetrics()
    }

    override suspend fun getAllChannelValues(): List<ChannelValuesEntity> {
        return channelValuesDao.getAllChannelValues()
    }

    override suspend fun save(metrics: HrvMetrics, rawData: List<RawSample>) {
        db.withTransaction {
            hrvMetricsDao.insert(metrics.toEntity())
            val rawEntities = rawData.map { sample ->
                ChannelValuesEntity(
                    sessionId = metrics.id,
                    timestamp = sample.timestamp,
                    value = sample.value,
                )
            }
            channelValuesDao.insertAll(rawEntities)
        }
    }

    override suspend fun getById(id: String): HrvMetrics? {
        return hrvMetricsDao.getById(id)?.toDomain()
    }

    override fun getRecentSummaries(limit: Int): Flow<List<HrvMetricsSummary>> {
        return hrvMetricsDao.getRecentSummaries(limit)
    }

    override suspend fun delete(id: String) {
        hrvMetricsDao.deleteById(id)
    }

    override suspend fun getMetricsInRange(start: Long, end: Long): List<HrvMetrics> {
        val entities = hrvMetricsDao.getMedianInSevenDays(start, end)
        return entities.map { it.toDomain() }
    }

    private fun HrvMetrics.toEntity(): HrvMetricsEntity {
        return HrvMetricsEntity(
            id = this.id,
            timestamp = this.timestamp,
            durationSeconds = this.durationSeconds,
            bpm = this.bpm,
            rmssd = this.rmssd,
            sdnn = this.sdnn,
            stressIndex = this.stressIndex,
            lf = this.lf,
            hf = this.hf,
            lfHfRatio = this.lfHfRatio,
            coherenceScore = this.coherenceScore,
            artifactPercent = this.artifactPercent,
            algorithmVersion = this.algorithmVersion,
            coveragePercent = this.coveragePercent,
            respirationRate = this.respirationRate,
            coherencePeakHz = this.coherencePeakHz,
            cameraMetadata = this.cameraMetadata,
        )
    }

    private fun HrvMetricsEntity.toDomain(): HrvMetrics {
        return HrvMetrics(
            id = this.id,
            timestamp = this.timestamp,
            durationSeconds = this.durationSeconds,
            bpm = this.bpm,
            rmssd = this.rmssd,
            sdnn = this.sdnn,
            stressIndex = this.stressIndex,
            lf = this.lf,
            hf = this.hf,
            lfHfRatio = this.lfHfRatio,
            coherenceScore = this.coherenceScore,
            artifactPercent = this.artifactPercent,
            algorithmVersion = this.algorithmVersion,
            coveragePercent = this.coveragePercent,
            respirationRate = this.respirationRate,
            coherencePeakHz = this.coherencePeakHz,
            cameraMetadata = this.cameraMetadata,
        )
    }
}
