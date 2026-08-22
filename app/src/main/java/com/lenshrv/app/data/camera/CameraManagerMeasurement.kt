package com.lenshrv.app.data.camera

import android.content.Context
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.CaptureResult
import android.hardware.camera2.TotalCaptureResult
import android.util.Range
import android.util.Size
import androidx.camera.camera2.interop.Camera2CameraControl
import androidx.camera.camera2.interop.Camera2Interop
import androidx.camera.camera2.interop.CaptureRequestOptions
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
import androidx.camera.core.CameraControl
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import com.lenshrv.app.domain.algorithm.CalibrationManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.guava.await
import java.util.concurrent.Executors
import javax.inject.Inject


@ExperimentalCamera2Interop
class CameraManagerMeasurement @Inject constructor(
    private val calibrationManager: CalibrationManager,
    private val measurementCache: MeasurementCache,
    @param:ApplicationContext private val context: Context,
) {

    private var cameraProvider: ProcessCameraProvider? = null
    private var imageAnalysis: ImageAnalysis? = null
    private var cameraControl: CameraControl? = null
    private var interopControl: Camera2CameraControl? = null
    private val cameraExecutor = Executors.newSingleThreadExecutor()

    suspend fun initializeCamera(
        lifecycleOwner: androidx.lifecycle.LifecycleOwner,
        analyzer: ImageAnalysis.Analyzer,
        onPreviewReady: (Preview) -> Unit
    ) {
        try {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            val provider = cameraProviderFuture.await()
            cameraProvider = provider

            val resolutionSelector = ResolutionSelector.Builder()
                .setResolutionStrategy(
                    ResolutionStrategy(
                        Size(640, 480),
                        ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER,
                    ),
                )
                .setAspectRatioStrategy(AspectRatioStrategy.RATIO_4_3_FALLBACK_AUTO_STRATEGY)
                .build()


            val previewBuilder = Preview.Builder()
                .setResolutionSelector(resolutionSelector)


            Camera2Interop.Extender(previewBuilder).apply {
                setCaptureRequestOption(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, Range(30, 30))

            }
            val imageAnalysisBuilder = ImageAnalysis.Builder()
                .setResolutionSelector(resolutionSelector)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)

            Camera2Interop.Extender(imageAnalysisBuilder).apply {
                setCaptureRequestOption(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, Range(30, 30))
                setSessionCaptureCallback(
                    object : CameraCaptureSession.CaptureCallback() {
                        var convergenceFrameCount = 0
                        var frameCount = 0
                        var fingerStreak = 0
                        override fun onCaptureCompleted(
                            session: CameraCaptureSession,
                            request: CaptureRequest,
                            result: TotalCaptureResult,
                        ) {
                            super.onCaptureCompleted(session, request, result)
                            if (calibrationManager.isCalibrated) return
                            frameCount++

                            val currentAeState = result.get(CaptureResult.CONTROL_AE_STATE) ?: -1
                            if (currentAeState == 2 && calibrationManager.lastRawOk) {
                                convergenceFrameCount++
                            } else {
                                convergenceFrameCount = 0
                            }
                            val aeReady = convergenceFrameCount >= 100
                            val windowOver = frameCount >= 210

                            if (aeReady && !calibrationManager.aeWindowOver) {
                                lockExposure()
                                saveFinalMetadata(result, isFallback = false)
                                calibrationManager.complete(usedFallback = false)
                                return
                            }

                            if (windowOver && !calibrationManager.aeWindowOver) {
                                calibrationManager.markAeWindowOver()
                            }

                            if (calibrationManager.aeWindowOver) {
                                if (calibrationManager.lastRawOk) fingerStreak++ else fingerStreak =
                                    0

                                if (fingerStreak >= 60) {
                                    lockExposure()
                                    saveFinalMetadata(result, isFallback = true)
                                    calibrationManager.complete(usedFallback = true)
                                    return
                                }
                            }
                        }
                    },
                )
            }

            val preview = previewBuilder.build()
            onPreviewReady(preview)
            val analysis = imageAnalysisBuilder.build()
            this.imageAnalysis = analysis
            analysis.setAnalyzer(cameraExecutor, analyzer)
            provider.unbindAll()

            val camera = provider.bindToLifecycle(
                lifecycleOwner,
                androidx.camera.core.CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                analysis,
            )
            cameraControl = camera.cameraControl
            interopControl = Camera2CameraControl.from(camera.cameraControl)
            cameraControl?.enableTorch(true)

        } catch (e: Exception) {

        }
    }

    fun setFlash(enabled: Boolean) {
        cameraControl?.enableTorch(enabled)
    }

    private fun saveFinalMetadata(result: TotalCaptureResult, isFallback: Boolean) {
        val resolution = this@CameraManagerMeasurement.imageAnalysis?.resolutionInfo?.resolution
        val resStr = resolution?.let { "${it.width}x${it.height}" } ?: "unknown"
        val iso = result.get(CaptureResult.SENSOR_SENSITIVITY)
        val exposureTimeNs = result.get(CaptureResult.SENSOR_EXPOSURE_TIME)
        val exposureTimeMs = exposureTimeNs?.let { it / 1_000_000 } ?: 0
        val aeState = result.get(CaptureResult.CONTROL_AE_STATE)
        val dgain = result.get(CaptureResult.CONTROL_POST_RAW_SENSITIVITY_BOOST)
        val awbState = result.get(CaptureResult.CONTROL_AWB_STATE)
        val awbGains = result.get(CaptureResult.COLOR_CORRECTION_GAINS)
        val awbGainsStr =
            awbGains?.let { "[${it.red},${it.greenEven},${it.greenOdd},${it.blue}]" } ?: "null"
        val afState = result.get(CaptureResult.CONTROL_AF_STATE)
        val fpsRange = result.get(CaptureResult.CONTROL_AE_TARGET_FPS_RANGE)
        val metadata =
            "res:$resStr|iso:$iso|exp:${exposureTimeMs}|range:$fpsRange|ae:$aeState|awb:$awbState|awb_gains:$awbGainsStr|af:$afState|dgain:$dgain|fallback:${isFallback}"
        measurementCache.cameraMetadata = metadata
    }

    private fun lockExposure() {
        val lockOptions = CaptureRequestOptions.Builder()
            .setCaptureRequestOption(CaptureRequest.CONTROL_AE_LOCK, true)
            .setCaptureRequestOption(CaptureRequest.CONTROL_AWB_LOCK, true)
            .build()
        interopControl?.setCaptureRequestOptions(lockOptions)
    }

    fun shutdown() = cameraExecutor.shutdown()
}
