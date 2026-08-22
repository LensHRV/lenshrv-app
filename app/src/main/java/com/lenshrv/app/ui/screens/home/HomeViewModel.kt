package com.lenshrv.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lenshrv.app.data.repository.AppPreferencesRepository
import com.lenshrv.app.domain.model.BaselineResult
import com.lenshrv.app.domain.model.HrvMetrics
import com.lenshrv.app.domain.repository.HrvMetricsRepository
import com.lenshrv.app.util.calculateMedian
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject
import kotlin.math.roundToInt

data class HomeMetricPage(
    val label: String,
    val value: String,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    appPreferencesRepository: AppPreferencesRepository,
    metricsRepository: HrvMetricsRepository,
) : ViewModel() {

    val isPrepEnabled = appPreferencesRepository.measurementPrepEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    @OptIn(ExperimentalCoroutinesApi::class)
    val metrics: StateFlow<List<HomeMetricPage>> = metricsRepository
        .getRecentSummaries(limit = 1)
        .mapLatest {
            val end = System.currentTimeMillis()
            val start = Calendar.getInstance().apply {
                timeInMillis = end
                add(Calendar.DAY_OF_YEAR, -(DAYS_LOOKBACK - 1))
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            val pool = metricsRepository.getMetricsInRange(start, end)
            val result = if (pool.isEmpty()) null else baselineFrom(pool)
            result?.toPages() ?: zeroPages()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = zeroPages(),
        )

    private fun baselineFrom(pool: List<HrvMetrics>): BaselineResult {
        return BaselineResult(
            bpm = calculateMedian(pool.map { it.bpm.toDouble() }),
            rmssd = calculateMedian(pool.map { it.rmssd }),
            sdnn = calculateMedian(pool.map { it.sdnn }),
            stressIndex = calculateMedian(pool.map { it.stressIndex }),
            lf = calculateMedian(pool.map { it.lf }),
            hf = calculateMedian(pool.map { it.hf }),
            lfHfRatio = calculateMedian(pool.map { it.lfHfRatio }),
            totalPower = calculateMedian(pool.map { it.totalPower }),
        )
    }

    private fun BaselineResult.toPages(): List<HomeMetricPage> {
        return listOf(
            HomeMetricPage("HEART RATE", asInt(bpm)),
            HomeMetricPage("RMSSD", asInt(rmssd)),
            HomeMetricPage("STRESS", asInt(stressIndex)),
            HomeMetricPage("SDNN", asInt(sdnn)),
            HomeMetricPage("TOTAL POWER", "${asInt(totalPower)} ms²"),
            HomeMetricPage("LF", "${asInt(lf)} ms²"),
            HomeMetricPage("HF", "${asInt(hf)} ms²"),
            HomeMetricPage("LF/HF", asRatio(lfHfRatio)),
        )
    }
    private fun asInt(value: Double): String = value.roundToInt().toString()
    private fun asRatio(value: Double): String =
        String.format(Locale.US, "%.2f", value)
    private companion object {
        const val DAYS_LOOKBACK = 7
        fun zeroPages(): List<HomeMetricPage> = listOf(
            HomeMetricPage("HEART RATE", "0"),
            HomeMetricPage("RMSSD", "0"),
            HomeMetricPage("STRESS", "0"),
            HomeMetricPage("SDNN", "0"),
            HomeMetricPage("TOTAL POWER", "0 ms²"),
            HomeMetricPage("LF", "0 ms²"),
            HomeMetricPage("HF", "0 ms²"),
            HomeMetricPage("LF/HF", "0.00"),
        )
    }
}

