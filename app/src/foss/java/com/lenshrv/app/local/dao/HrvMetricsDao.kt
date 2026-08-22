package com.lenshrv.app.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.lenshrv.app.domain.model.HrvMetricsSummary
import com.lenshrv.app.local.entities.HrvMetricsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HrvMetricsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(metrics: HrvMetricsEntity)

    @Query("SELECT * FROM hrv_metrics WHERE id = :id")
    suspend fun getById(id: String): HrvMetricsEntity?

    @Query("SELECT id, timestamp, bpm FROM hrv_metrics ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentSummaries(limit: Int): Flow<List<HrvMetricsSummary>>

    @Query("DELETE FROM hrv_metrics WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("""
    SELECT * FROM hrv_metrics
    WHERE timestamp BETWEEN :start AND :end
    AND artifactPercent < 15.0
    ORDER BY timestamp DESC
""")
    suspend fun getMedianInSevenDays(start: Long, end: Long): List<HrvMetricsEntity>


    @Query("SELECT * FROM hrv_metrics ORDER BY timestamp ASC")
    suspend fun getAllMetrics(): List<HrvMetricsEntity>
}
