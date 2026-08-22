package com.lenshrv.app.ui.screens.logs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lenshrv.app.data.repository.AppPreferencesRepository
import com.lenshrv.app.domain.repository.HrvMetricsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

data class MetricUiModel(
    val id: String,
    val bpm: Int,
    val formattedTime: String,
)

sealed interface LogsUiState {
    data object Loading : LogsUiState
    data class Success(val groupedItems: Map<String, List<MetricUiModel>>) : LogsUiState
    data class Error(val message: String) : LogsUiState
}

@HiltViewModel
class LogsViewModel @Inject constructor(
    private val repository: HrvMetricsRepository,
    appPreferencesRepository: AppPreferencesRepository,
) : ViewModel() {
    private val zone: ZoneId = ZoneId.systemDefault()

    private val dateFormatter: DateTimeFormatter = DateTimeFormatter
        .ofPattern("MMM dd, yyyy", Locale.US)
        .withZone(zone)

    private val weekdayFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("EEEE", Locale.ENGLISH)
    private val hourAmPmFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH)

    val isPrepEnabled = appPreferencesRepository.measurementPrepEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val uiState: StateFlow<LogsUiState> = repository.getRecentSummaries(500)
        .map { items ->
            val grouped = items.groupBy { metric ->
                val instant = Instant.ofEpochMilli(metric.timestamp)
                dateFormatter.format(instant).uppercase()
            }.mapValues { (_, metricsList) ->
                metricsList.map { metric ->
                    MetricUiModel(
                        id = metric.id,
                        bpm = metric.bpm,
                        formattedTime = formatCardTime(metric.timestamp),
                    )
                }
            }
            LogsUiState.Success(grouped) as LogsUiState
        }
        .flowOn(Dispatchers.Default)
        .catch { e ->
            emit(LogsUiState.Error(e.message ?: "Unknown error occurred"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = LogsUiState.Loading,
        )

    private fun formatCardTime(epochMs: Long): String {
        val zoned = Instant.ofEpochMilli(epochMs).atZone(zone)
        val day = weekdayFormatter.format(zoned)
        val hour = hourAmPmFormatter.format(zoned).lowercase(Locale.ENGLISH)
        return "$day $hour"
    }
}

