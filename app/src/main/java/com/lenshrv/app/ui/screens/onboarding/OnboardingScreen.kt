package com.lenshrv.app.ui.screens.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

enum class OnboardingStep {
    Welcome,
    Terms
}

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel = hiltViewModel(),
    onTermsAccepted: () -> Unit,
) {
    var currentStep by rememberSaveable { mutableStateOf(OnboardingStep.Welcome) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            when (currentStep) {
                OnboardingStep.Welcome -> {
                    WelcomeBottomBar(
                        onNavigateToTerms = { currentStep = OnboardingStep.Terms }
                    )
                }

                OnboardingStep.Terms -> {
                    TermsBottomBar(
                        onContinue = {
                            viewModel.acceptTerms(onSuccess = onTermsAccepted)
                        },
                    )
                }
            }
        },
    ) { paddingValues ->
        AnimatedContent(
            targetState = currentStep,
            label = "onboarding_transition",
        ) { step ->
            when (step) {
                OnboardingStep.Welcome -> {
                    WelcomeContent(paddingValues)
                }

                OnboardingStep.Terms -> {
                    TermsContent(paddingValues = paddingValues)
                }
            }
        }
    }
}
