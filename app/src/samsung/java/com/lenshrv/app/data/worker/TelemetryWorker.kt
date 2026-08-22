package com.lenshrv.app.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.lenshrv.app.data.remote.dto.MetricsDto
import com.lenshrv.app.data.remote.dto.SessionDataJson
import com.lenshrv.app.data.remote.dto.TelemetryInsertRow
import com.lenshrv.app.data.remote.dto.WaveformPointDto
import com.lenshrv.app.data.repository.AppPreferencesRepository
import com.lenshrv.app.domain.repository.HrvMetricsRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc

@HiltWorker
class TelemetryWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val appPreferencesRepository: AppPreferencesRepository,
    private val hrvMetricsRepository: HrvMetricsRepository,
    private val supabaseClient: SupabaseClient,
    private val telemetryScheduler: TelemetryScheduler,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {

        return try {
            val isEnabled = appPreferencesRepository.isTelemetryEnabled()
            if (!isEnabled) {
                return Result.success()
            }
            val pendingSessions = hrvMetricsRepository.getUnsyncedSessions(limit = 10)
            if (pendingSessions.isEmpty()) {
                return Result.success()
            }

            appPreferencesRepository.ensureAnonymousIdCreated()
            val userId = appPreferencesRepository.getAnonymousId()
            val deviceModel = "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}"
            val osVersion = android.os.Build.VERSION.SDK_INT.toString()
            val appVersion = applicationContext.packageManager
                .getPackageInfo(applicationContext.packageName, 0).versionName ?: "Unknown"

            val rowsToInsert = mutableListOf<TelemetryInsertRow>()

            for (session in pendingSessions) {
                val localWaveforms = hrvMetricsRepository.getWaveformForSession(session.id)
                val metricsDto = MetricsDto(
                    timestamp = session.timestamp,
                    durationSeconds = session.durationSeconds,
                    bpm = session.bpm,
                    rmssd = session.rmssd,
                    sdnn = session.sdnn,
                    stressIndex = session.stressIndex,
                    respirationRate = session.respirationRate,
                    lf = session.lf,
                    hf = session.hf,
                    lfHfRatio = session.lfHfRatio,
                    artifactPercent = session.artifactPercent,
                    coveragePercent = session.coveragePercent,
                    coherenceScore = session.coherenceScore,
                    coherencePeakHz = session.coherencePeakHz,
                    algorithmVersion = session.algorithmVersion,
                    cameraMetadata = session.cameraMetadata,
                )

                val waveformsDto = localWaveforms.map {
                    WaveformPointDto(timestamp = it.timestamp, value = it.value)
                }

                val sessionDataJson = SessionDataJson(
                    deviceModel = deviceModel,
                    osVersion = osVersion,
                    appVersion = appVersion,
                    metrics = metricsDto,
                    waveforms = waveformsDto,
                )

                rowsToInsert.add(
                    TelemetryInsertRow(
                        sessionId = session.id,
                        anonymousUserId = userId,
                        sessionData = sessionDataJson,
                    ),
                )
            }

            supabaseClient.postgrest.rpc(
                function = "insert_telemetry_batch",
                parameters = mapOf("p_sessions" to rowsToInsert),
            )


            val sessionIds = pendingSessions.map { it.id }
            hrvMetricsRepository.markAsSynced(sessionIds)
            val remainingCount = hrvMetricsRepository.getUnsyncedCount()
            if (remainingCount > 0) {
                telemetryScheduler.schedule()
            }

            Result.success()

        } catch (e: Exception) {
            if (runAttemptCount >= 3) {
                return Result.failure()
            }
            return Result.retry()
        }
    }
}
