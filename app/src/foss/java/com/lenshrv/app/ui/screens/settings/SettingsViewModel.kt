package com.lenshrv.app.ui.screens.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lenshrv.app.data.repository.AppPreferencesRepository
import com.lenshrv.app.domain.repository.HrvMetricsRepository
import com.lenshrv.app.local.exporter.DataExporter
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class SettingsUiState(
    val isPrepEnabled: Boolean,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val appPreferencesRepository: AppPreferencesRepository,
    private val hrvMetricsRepository: HrvMetricsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<SettingsUiState?>(null)
    val uiState: StateFlow<SettingsUiState?> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            appPreferencesRepository.measurementPrepEnabled.collect { prep ->
                _uiState.value = SettingsUiState(isPrepEnabled = prep)
            }
        }
    }

    fun exportBackup(uri: Uri, onSuccess: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val metrics = hrvMetricsRepository.getAllMetrics()
                val channels = hrvMetricsRepository.getAllChannelValues()
                context.contentResolver.openOutputStream(uri)?.use { stream ->
                    DataExporter.exportToZipStream(metrics, channels, stream)
                    withContext(Dispatchers.Main) { onSuccess() }
                }
            } catch (e: Exception) {
            }
        }
    }

    fun toggleMeasurementPrep(enabled: Boolean) {
        _uiState.update { it?.copy(isPrepEnabled = enabled) }
        viewModelScope.launch {
            appPreferencesRepository.saveMeasurementPrepEnabled(enabled)
        }
    }
}
