package com.lenshrv.app.data.camera

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy

class CameraAnalyzer : ImageAnalysis.Analyzer {
    var onFrameProcessed: ((Double, Long, Boolean) -> Unit)? = null
    var onFingerAlert: ((Boolean) -> Unit)? = null
    private var yRoi = ByteArray(ROI * ROI)
    private var uRoi = ByteArray(0)
    private var vRoi = ByteArray(0)
    private var fingerAlert = false
    private var badCount = 0
    private var goodCount = 0

    override fun analyze(image: ImageProxy) {
        val currentTime = image.imageInfo.timestamp / 1_000_000L
        val yPlane = image.planes[0]
        val uPlane = image.planes[1]
        val vPlane = image.planes[2]
        val yBuf = yPlane.buffer.duplicate()
        val uBuf = uPlane.buffer.duplicate()
        val vBuf = vPlane.buffer.duplicate()
        val yPos = yPlane.buffer.position()
        val uPos = uPlane.buffer.position()
        val vPos = vPlane.buffer.position()
        val yStride = yPlane.rowStride
        val uStride = uPlane.rowStride
        val vStride = vPlane.rowStride
        val uPixelStride = uPlane.pixelStride
        val vPixelStride = vPlane.pixelStride
        val left = (image.width - ROI) / 2
        val top = (image.height - ROI) / 2
        val uvLeft = left shr 1
        val uvTop = top shr 1
        val uvW = ((left + ROI - 1) shr 1) - uvLeft + 1
        val uvH = ((top + ROI - 1) shr 1) - uvTop + 1
        val uRowBytes = (uvW - 1) * uPixelStride + 1
        val vRowBytes = (uvW - 1) * vPixelStride + 1
        val uSize = uvH * uRowBytes
        val vSize = uvH * vRowBytes
        if (uRoi.size != uSize) uRoi = ByteArray(uSize)
        if (vRoi.size != vSize) vRoi = ByteArray(vSize)
        for (yy in 0 until ROI) {
            yBuf.position(yPos + (top + yy) * yStride + left)
            yBuf.get(yRoi, yy * ROI, ROI)
        }
        for (cy in 0 until uvH) {
            uBuf.position(uPos + (uvTop + cy) * uStride + uvLeft * uPixelStride)
            uBuf.get(uRoi, cy * uRowBytes, uRowBytes)
            vBuf.position(vPos + (uvTop + cy) * vStride + uvLeft * vPixelStride)
            vBuf.get(vRoi, cy * vRowBytes, vRowBytes)
        }

        var sumG = 0L
        var sumR = 0L
        for (yy in 0 until ROI) {
            val yOff = yy * ROI
            val uvOff = (((top + yy) shr 1) - uvTop) * uRowBytes
            val vvOff = (((top + yy) shr 1) - uvTop) * vRowBytes
            for (x in 0 until ROI) {
                val yVal = yRoi[yOff + x].toInt() and 0xFF
                val uvX = ((left + x) shr 1) - uvLeft
                val uVal = (uRoi[uvOff + uvX * uPixelStride].toInt() and 0xFF) - 128
                val vVal = (vRoi[vvOff + uvX * vPixelStride].toInt() and 0xFF) - 128
                sumG += (yVal - ((88 * uVal + 183 * vVal) shr 8)).toLong()
                sumR += (yVal + ((359 * vVal) shr 8)).toLong()
            }
        }

        val pixels = ROI * ROI
        val meanG = sumG.toDouble() / pixels
        val meanR = sumR.toDouble() / pixels
        val rawOk = meanG > 0.0 && meanR / meanG >= RATIO_OK
        if (!rawOk) {
            goodCount = 0
            badCount++
            if (badCount >= BAD_FRAMES && !fingerAlert) {
                fingerAlert = true
                onFingerAlert?.invoke(true)
            }
        } else {
            badCount = 0
            goodCount++
            if (goodCount >= GOOD_FRAMES && fingerAlert) {
                fingerAlert = false
                onFingerAlert?.invoke(false)
            }
        }
        onFrameProcessed?.invoke(meanG, currentTime, rawOk)
        image.close()
    }

    private companion object {
        const val ROI = 125
        const val RATIO_OK = 3.5
        const val BAD_FRAMES = 5
        const val GOOD_FRAMES = 25
    }
}
