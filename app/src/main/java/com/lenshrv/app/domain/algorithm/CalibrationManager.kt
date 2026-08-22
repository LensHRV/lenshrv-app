package com.lenshrv.app.domain.algorithm

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CalibrationManager @Inject constructor() {
    @Volatile
    var isCalibrated = false
        private set

    @Volatile
    var usedFallback = false
        private set

    @Volatile
    var aeWindowOver = false
        private set

    @Volatile
    var lastRawOk = false

    fun complete(usedFallback: Boolean) {
        this.usedFallback = usedFallback
        isCalibrated = true
    }

    fun markAeWindowOver() {
        aeWindowOver = true
    }

    fun reset() {
        isCalibrated = false
        usedFallback = false
        aeWindowOver = false
        lastRawOk = false
    }
}
