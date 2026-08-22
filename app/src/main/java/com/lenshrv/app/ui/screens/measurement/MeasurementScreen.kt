package com.lenshrv.app.ui.screens.measurement

import androidx.annotation.OptIn
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalView
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalCamera2Interop::class)
@Composable
fun MeasurementScreen(
    viewModel: MeasurementViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
    onNavigateToResults: (String) -> Unit = {},
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    val view = LocalView.current

    DisposableEffect(Unit) {
        view.keepScreenOn = true

        onDispose {
            view.keepScreenOn = false
        }
    }
    LaunchedEffect(state.isFlashEnabled) {
        viewModel.setFlash(state.isFlashEnabled)
    }

    LaunchedEffect(state.phase) {
        val phase = state.phase
        if (phase is MeasurementPhase.Finished) {
            onNavigateToResults(phase.measurementId)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.prepareCamera(lifecycleOwner)
    }

    MeasurementContent(
        state = state,
        onNavigateBack = onNavigateBack,
    )
}



