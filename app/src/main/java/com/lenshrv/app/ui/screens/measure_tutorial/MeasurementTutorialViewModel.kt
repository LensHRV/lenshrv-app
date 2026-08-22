package com.lenshrv.app.ui.screens.measure_tutorial

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lenshrv.app.data.repository.AppPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class MeasurementTutorialViewModel @Inject constructor(
    private val appPreferencesRepository: AppPreferencesRepository,
): ViewModel() {

    fun toggleMeasurementPrep(enabled: Boolean) {
        viewModelScope.launch {
            appPreferencesRepository.saveMeasurementPrepEnabled(enabled)
        }
    }
}

