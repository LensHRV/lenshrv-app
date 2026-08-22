package com.lenshrv.app.domain.repository

import com.lenshrv.app.domain.model.RawSample
import com.lenshrv.app.domain.model.HrvMetrics
import com.lenshrv.app.domain.model.HrvMetricsSummary
import com.lenshrv.app.local.entities.ChannelValuesEntity
import com.lenshrv.app.local.entities.HrvMetricsEntity
import kotlinx.coroutines.flow.Flow

interface HrvMetricsRepository {
    suspend fun getAllMetrics(): List<HrvMetricsEntity>
    suspend fun getAllChannelValues(): List<ChannelValuesEntity>
    suspend fun save(metrics: HrvMetrics, rawData: List<RawSample>)
    suspend fun getById(id: String): HrvMetrics?
    fun getRecentSummaries(limit: Int): Flow<List<HrvMetricsSummary>>
    suspend fun delete(id: String)
    suspend fun getMetricsInRange(start: Long, end: Long): List<HrvMetrics>
    suspend fun getUnsyncedSessions(limit: Int): List<HrvMetrics>
    suspend fun getWaveformForSession(sessionId: String): List<RawSample>
    suspend fun markAsSynced(sessionIds: List<String>)
    suspend fun getUnsyncedCount(): Int
}
