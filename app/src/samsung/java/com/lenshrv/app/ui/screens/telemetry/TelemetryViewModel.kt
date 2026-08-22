package com.lenshrv.app.ui.screens.telemetry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lenshrv.app.data.repository.AppPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TelemetryViewModel @Inject constructor(
    private val repository: AppPreferencesRepository
): ViewModel() {
    fun setTelemetryEnabled(enabled: Boolean, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.saveTelemetryEnabled(enabled)
            if (enabled) {
                repository.ensureAnonymousIdCreated()
            }
            onComplete()
        }
    }
}
