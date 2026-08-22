package com.lenshrv.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WaveformPointDto(
    @SerialName("t") val timestamp: Long,
    @SerialName("v") val value: Double
)

@Serializable
data class MetricsDto(
    @SerialName("timestamp") val timestamp: Long,
    @SerialName("duration_seconds") val durationSeconds: Int,
    @SerialName("bpm") val bpm: Int,
    @SerialName("rmssd") val rmssd: Double,
    @SerialName("sdnn") val sdnn: Double,
    @SerialName("stress_index") val stressIndex: Double,
    @SerialName("respiration_rate") val respirationRate: Double,
    @SerialName("lf") val lf: Double,
    @SerialName("hf") val hf: Double,
    @SerialName("lf_hf_ratio") val lfHfRatio: Double,
    @SerialName("artifact_percent") val artifactPercent: Double,
    @SerialName("coverage_percent") val coveragePercent: Double,
    @SerialName("coherence_score") val coherenceScore: Double,
    @SerialName("coherence_peak_hz") val coherencePeakHz: Double,
    @SerialName("algorithm_version") val algorithmVersion: Int,
    @SerialName("camera_metadata") val cameraMetadata: String,
)

@Serializable
data class SessionDataJson(
    @SerialName("device_model") val deviceModel: String,
    @SerialName("os_version") val osVersion: String,
    @SerialName("app_version") val appVersion: String,

    @SerialName("metrics") val metrics: MetricsDto,
    @SerialName("waveforms") val waveforms: List<WaveformPointDto>
)

@Serializable
data class TelemetryInsertRow(
    @SerialName("p_session_id") val sessionId: String,
    @SerialName("p_anonymous_user_id") val anonymousUserId: String,
    @SerialName("p_session_data") val sessionData: SessionDataJson
)
