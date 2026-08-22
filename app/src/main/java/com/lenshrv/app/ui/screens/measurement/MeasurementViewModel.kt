package com.lenshrv.app.ui.screens.measurement

import androidx.annotation.OptIn
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
import androidx.camera.core.Preview
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lenshrv.app.data.camera.CameraAnalyzer
import com.lenshrv.app.data.camera.CameraManagerMeasurement
import com.lenshrv.app.data.camera.MeasurementCache
import com.lenshrv.app.domain.algorithm.CalibrationManager
import com.lenshrv.app.domain.algorithm.LiveRrDetector
import com.lenshrv.app.domain.algorithm.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds


sealed interface MeasurementPhase {
    object Calibrating : MeasurementPhase

    data class Measuring(
        val timeLeft: Int = 127,
        val measurementId: String = "",
    ) : MeasurementPhase

    data class Finished(
        val measurementId: String,
    ) : MeasurementPhase
}

data class MeasurementUiState(
    val previewUseCase: Preview? = null,
    val isFlashEnabled: Boolean = true,
    val durationSeconds: Int = 127,
    val phase: MeasurementPhase = MeasurementPhase.Calibrating,
    val showFallbackHint: Boolean = false,
    val liveRrMs: Long? = null,
    val liveBpm: Int? = null,
    val fingerAlert: Boolean = false,
)


@OptIn(ExperimentalCamera2Interop::class)
@HiltViewModel
class MeasurementViewModel @Inject constructor(
    private val calibrationManager: CalibrationManager,
    private val sessionManager: SessionManager,
    private val measurementCache: MeasurementCache,
    private val cameraManager: CameraManagerMeasurement,
    private val liveRrDetector: LiveRrDetector,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MeasurementUiState())
    val uiState = _uiState.asStateFlow()

    private val cameraAnalyzer = CameraAnalyzer()
    var currentMeasurementId = ""
    private var signalOk = true

    init {

        calibrationManager.reset()
        measurementCache.resetCameraMetadata()

        cameraAnalyzer.onFrameProcessed = { green, timestamp, rawOk ->
            calibrationManager.lastRawOk = rawOk
            if (calibrationManager.isCalibrated) {
                if (_uiState.value.phase is MeasurementPhase.Calibrating) {
                    startTracking()
                }
                measurementCache.push(green, timestamp)
                if (rawOk) {
                    signalOk = true
                    liveRrDetector.push(green, timestamp)
                } else if (signalOk) {
                    signalOk = false
                    liveRrDetector.resetInternals()
                }
            }
        }

        cameraAnalyzer.onFingerAlert = { alert ->
            _uiState.update { it.copy(fingerAlert = alert) }
        }

        viewModelScope.launch {
            liveRrDetector.rr.collect { ms ->
                _uiState.update { it.copy(liveRrMs = ms) }
            }
        }

        viewModelScope.launch {
            liveRrDetector.bpm.collect { bpm ->
                _uiState.update { it.copy(liveBpm = bpm) }
            }
        }

        viewModelScope.launch {
            sessionManager.remainingTime.collect { time ->
                val currentPhase = _uiState.value.phase
                if (currentPhase is MeasurementPhase.Measuring) {
                    _uiState.update {
                        it.copy(
                            phase = currentPhase.copy(timeLeft = time)
                        )
                    }
                }
            }
        }


        viewModelScope.launch {
            sessionManager.isFinished.collect { finished ->
                val currentPhase = _uiState.value.phase
                if (finished && currentPhase is MeasurementPhase.Measuring) {
                    finalizeMeasurement()
                }
            }
        }
    }


    fun prepareCamera(lifecycleOwner: LifecycleOwner) {
        viewModelScope.launch {
            cameraManager.initializeCamera(
                lifecycleOwner, cameraAnalyzer,
                onPreviewReady = { preview ->
                    _uiState.update { it.copy(previewUseCase = preview) }

                },
            )
        }
    }

    fun setFlash(enabled: Boolean) {
        cameraManager.setFlash(enabled)
    }


    private fun startTracking() {
        val newId = UUID.randomUUID().toString()
        currentMeasurementId = newId
        measurementCache.reset()
        liveRrDetector.reset()
        signalOk = true
        val usedFallback = calibrationManager.usedFallback
        _uiState.update { currentState ->
            currentState.copy(
                phase = MeasurementPhase.Measuring(
                    timeLeft = currentState.durationSeconds,
                    measurementId = currentMeasurementId,
                ),
                showFallbackHint = usedFallback,
            )
        }
        sessionManager.startSession(_uiState.value.durationSeconds)
        if (usedFallback) {
            viewModelScope.launch {
                delay(2000.milliseconds)
                _uiState.update { it.copy(showFallbackHint = false) }
            }
        }
    }


    private fun finalizeMeasurement() {
        calibrationManager.reset()
        _uiState.update {
            it.copy(
                isFlashEnabled = false,
                phase = MeasurementPhase.Finished(currentMeasurementId)
            )
        }

    }

    override fun onCleared() {
        super.onCleared()
        sessionManager.stopSession()
        cameraManager.shutdown()
        liveRrDetector.reset()
    }
}
