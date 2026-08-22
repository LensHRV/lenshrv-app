package com.lenshrv.app.ui.screens.results


import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lenshrv.app.data.camera.MeasurementCache
import com.lenshrv.app.domain.model.BaselineResult
import com.lenshrv.app.domain.model.HrvMetrics
import com.lenshrv.app.domain.repository.HrvMetricsRepository
import com.lenshrv.app.domain.usecase.GetBaselineUseCase
import com.lenshrv.app.domain.usecase.SaveMeasurementUseCase
import com.lenshrv.app.domain.usecase.SuperComputeHRVUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

sealed interface ResultsUiState {
    data object Loading : ResultsUiState
    data class Success(
        val metrics: HrvMetrics,
        val isSaved: Boolean,
        val baseline: BaselineResult,
        val hero: List<HeroItem>,
    ) : ResultsUiState

    data class Error(val message: String) : ResultsUiState
    data object Deleted : ResultsUiState
}

@HiltViewModel
class ResultsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: HrvMetricsRepository,
    private val measurementCache: MeasurementCache,
    private val superComputeHRVUseCase: SuperComputeHRVUseCase,
    private val saveMeasurementUseCase: SaveMeasurementUseCase,
    private val getBaselineUseCase: GetBaselineUseCase,
) : ViewModel() {
    private val resultId: String = checkNotNull(savedStateHandle["resultId"])
    private val _uiState = MutableStateFlow<ResultsUiState>(ResultsUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                var hrvMetrics = repository.getById(resultId)
                var isSaved = hrvMetrics != null
                if (hrvMetrics == null) {
                    val resultFromCalculation = withContext(Dispatchers.IO) {
                        val rawData = measurementCache.getAll()
                        val computed = superComputeHRVUseCase(
                            rawSamples = rawData,
                            sessionId = resultId,
                            timestamp = System.currentTimeMillis(),
                            durationSeconds = 120,
                            cameraMetadata = measurementCache.cameraMetadata,
                        )
                        if (computed != null) {
                            saveMeasurementUseCase(computed, rawData)
                            measurementCache.reset()
                            isSaved = true
                        }
                        computed
                    }
                    hrvMetrics = resultFromCalculation
                    if (hrvMetrics == null) {
                        _uiState.value =
                            ResultsUiState.Error("Oops! Something went wrong. Please try again.")
                        return@launch
                    }
                }

                val baseline = getBaselineUseCase(current = hrvMetrics, daysToLookBack = 7)
                val hero = ResultsHeroMapper.generateHeroList(hrvMetrics, baseline)

                _uiState.value = ResultsUiState.Success(
                    metrics = hrvMetrics,
                    isSaved = isSaved,
                    baseline = baseline,
                    hero = hero,
                )
            } catch (e: Exception) {
                _uiState.value = ResultsUiState.Error("Something went wrong. Please try again.")
            }
        }
    }

    fun deleteResult() {
        viewModelScope.launch {
            repository.delete(resultId)
            _uiState.value = ResultsUiState.Deleted
        }
    }
}
