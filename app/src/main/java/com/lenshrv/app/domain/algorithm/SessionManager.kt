package com.lenshrv.app.domain.algorithm

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

class SessionManager @Inject constructor() {
    private val _remainingTime = MutableStateFlow(0)
    val remainingTime = _remainingTime.asStateFlow()

    private val _isFinished = MutableStateFlow(false)
    val isFinished = _isFinished.asStateFlow()

    private val managerScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var timerJob: Job? = null


    fun startSession(durationSeconds: Int) {
        _remainingTime.value =durationSeconds
        _isFinished.value = false

        timerJob?.cancel()
        timerJob = managerScope.launch {
            val targetEndTime = System.currentTimeMillis() + (durationSeconds * 1000L)

            while (isActive) {
                val currentTime = System.currentTimeMillis()
                val diffMillis = targetEndTime - currentTime

                if (diffMillis <= 0) {
                    _remainingTime.value = 0
                    _isFinished.value = true
                    break
                }
                val diffSeconds=kotlin.math.ceil(diffMillis / 1000.0).toInt()
                if (_remainingTime.value != diffSeconds) {
                    _remainingTime.value = diffSeconds
                }


                delay(100.milliseconds)
            }
        }
    }

    fun stopSession() {
        timerJob?.cancel()
        _remainingTime.value = 0
    }
    fun clear() {
        managerScope.cancel()
    }
}
