package com.lenshrv.app.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lenshrv.app.data.repository.AppPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val repository: AppPreferencesRepository
): ViewModel() {
    fun acceptTerms(onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.saveTermsAcceptance(true)
            onSuccess()
        }
    }
}
