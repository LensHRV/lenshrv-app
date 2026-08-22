package com.lenshrv.app.domain.model

data class HrvMetrics(
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
) {
    val totalPower: Double get() = lf + hf
}
