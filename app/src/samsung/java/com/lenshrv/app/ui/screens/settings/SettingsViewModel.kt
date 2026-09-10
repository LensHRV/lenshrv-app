package com.lenshrv.app.ui.screens.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lenshrv.app.data.billing.SamsungIapManager
import com.lenshrv.app.data.repository.AppPreferencesRepository
import com.lenshrv.app.domain.repository.HrvMetricsRepository
import com.lenshrv.app.local.exporter.DataExporter
import com.samsung.android.sdk.iap.lib.vo.ProductVo
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class SettingsUiState(
    val isTelemetryEnabled: Boolean,
    val isPrepEnabled: Boolean,
)

data class TipJarUiState(
    val products: List<ProductVo> = emptyList(),
    val isProcessing: Boolean = false,
    val showTipThanks: Boolean = false,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val appPreferencesRepository: AppPreferencesRepository,
    private val hrvMetricsRepository: HrvMetricsRepository,
    private val samsungIapManager: SamsungIapManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow<SettingsUiState?>(null)
    val uiState: StateFlow<SettingsUiState?> = _uiState.asStateFlow()

    private val _tipJarState = MutableStateFlow(TipJarUiState())
    val tipJarState = _tipJarState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                appPreferencesRepository.telemetryEnabled,
                appPreferencesRepository.measurementPrepEnabled,
            ) { telemetry, prep ->
                SettingsUiState(
                    isTelemetryEnabled = telemetry,
                    isPrepEnabled = prep,
                )
            }.collect { _uiState.value = it }
        }
        loadTips()
    }

    private fun loadTips() {
        samsungIapManager.getListItems { products ->
            _tipJarState.update { it.copy(products = products) }
        }
    }

    fun purchaseTip(itemId: String) {
        _tipJarState.update { it.copy(isProcessing = true) }
        samsungIapManager.startPayment(itemId) { success, errorCode ->
            val paid = success && errorCode == 0
            _tipJarState.update {
                it.copy(isProcessing = false, showTipThanks = paid)
            }
            if (paid) {
                viewModelScope.launch {
                    appPreferencesRepository.setNextPromptCount(
                        appPreferencesRepository.nextPromptCount.first() + 30,
                    )
                }
            }
        }
    }

    fun dismissTipThanks() {
        _tipJarState.update { it.copy(showTipThanks = false) }
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
            } catch (_: Exception) {
            }
        }
    }

    fun toggleMeasurementPrep(enabled: Boolean) {
        _uiState.update { it?.copy(isPrepEnabled = enabled) }
        viewModelScope.launch {
            appPreferencesRepository.saveMeasurementPrepEnabled(enabled)
        }
    }

    fun toggleTelemetry(enabled: Boolean) {
        _uiState.update { it?.copy(isTelemetryEnabled = enabled) }
        viewModelScope.launch {
            appPreferencesRepository.saveTelemetryEnabled(enabled)
            if (enabled) {
                appPreferencesRepository.ensureAnonymousIdCreated()
            }
        }
    }
}
