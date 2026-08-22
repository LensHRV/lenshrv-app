package com.lenshrv.app.ui.screens.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lenshrv.app.data.repository.AppPreferencesRepository
import com.lenshrv.app.data.repository.CameraPermissionManager
import com.lenshrv.app.ui.screens.camera_permission.CameraAccessRoute
import com.lenshrv.app.ui.screens.home.HomeRoute
import com.lenshrv.app.ui.screens.onboarding.OnboardingRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


sealed interface NavigationStates {
    data object Loading : NavigationStates
    data class OnSuccess(val destination: Any) : NavigationStates
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val appPreferencesRepository: AppPreferencesRepository,
    private val permissionManager: CameraPermissionManager,
) : ViewModel() {
    private val _uiState = MutableStateFlow<NavigationStates>(NavigationStates.Loading)
    val uiState: StateFlow<NavigationStates> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            if (!appPreferencesRepository.hasAcceptedTerms()) {
                _uiState.value = NavigationStates.OnSuccess(OnboardingRoute)
            } else if (!permissionManager.hasCameraPermission()) {
                _uiState.value = NavigationStates.OnSuccess(CameraAccessRoute)
            } else {
                _uiState.value = NavigationStates.OnSuccess(HomeRoute)
            }
        }
    }
}
