package com.lenshrv.app.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "hrv_metrics")
data class HrvMetricsEntity(
    @PrimaryKey
    val id: String,
    val timestamp: Long,
    val durationSeconds: Int,
    val bpm: Int,
    val rmssd: Double,
    val sdnn: Double,
    val stressIndex: Double,
    val respirationRate: Double,
    val lf: Double,
    val hf: Double,
    val lfHfRatio: Double,
    val artifactPercent: Double,
    val coveragePercent: Double,
    val coherenceScore: Double,
    val coherencePeakHz: Double,
    val algorithmVersion: Int = 1,
    val cameraMetadata: String,
    val isSynced: Boolean = false,
)
