package com.lenshrv.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.lenshrv.app.ui.screens.camera_permission.CameraAccessRoute
import com.lenshrv.app.ui.screens.camera_permission.CameraAccessScreen
import com.lenshrv.app.ui.screens.home.HomeRoute
import com.lenshrv.app.ui.screens.home.HomeScreen
import com.lenshrv.app.ui.screens.logs.LogsRoute
import com.lenshrv.app.ui.screens.logs.LogsScreen
import com.lenshrv.app.ui.screens.measure_tutorial.MeasurementTutorialRoute
import com.lenshrv.app.ui.screens.measure_tutorial.MeasurementTutorialScreen
import com.lenshrv.app.ui.screens.measurement.MeasurementRoute
import com.lenshrv.app.ui.screens.measurement.MeasurementScreen
import com.lenshrv.app.ui.screens.onboarding.OnboardingRoute
import com.lenshrv.app.ui.screens.onboarding.OnboardingScreen
import com.lenshrv.app.ui.screens.results.ResultsRoute
import com.lenshrv.app.ui.screens.results.ResultsScreen
import com.lenshrv.app.ui.screens.settings.SettingsRoute
import com.lenshrv.app.ui.screens.settings.SettingsScreen
import com.lenshrv.app.ui.screens.splash.NavigationStates
import com.lenshrv.app.ui.screens.splash.SplashViewModel
import com.lenshrv.app.ui.screens.telemetry.TelemetryRoute
import com.lenshrv.app.ui.screens.telemetry.TelemetryScreen


@Composable
fun AppNavigation(
    viewModel: SplashViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val navController = rememberNavController()

    if (state is NavigationStates.OnSuccess) {
        val destination = (state as NavigationStates.OnSuccess).destination

        LaunchedEffect(destination) {
            if (navController.currentBackStackEntry?.destination?.route != destination::class.qualifiedName) {
                navController.navigate(destination) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }


        NavHost(
            navController = navController,
            startDestination = destination,
        ) {
            composable<OnboardingRoute> {
                OnboardingScreen(
                    onTermsAccepted = {
                        navController.navigate(TelemetryRoute) {
                            popUpTo(OnboardingRoute) { inclusive = true }
                        }
                    },
                )
            }

            composable<TelemetryRoute> {
                TelemetryScreen(
                    onNavigateToCameraPermission = {
                        navController.navigate(CameraAccessRoute) {
                            popUpTo(TelemetryRoute) { inclusive = true }
                        }
                    },
                )
            }

            composable<CameraAccessRoute> {
                CameraAccessScreen(
                    onNavigateToHome = {
                        navController.navigate(HomeRoute) {
                            popUpTo(CameraAccessRoute) { inclusive = true }
                        }
                    },
                )
            }

            composable<HomeRoute> {
                HomeScreen(
                    onNavigateToMeasurement = {
                        navController.navigate(MeasurementRoute)
                    },
                    onNavigateToSettings = { navController.navigate(SettingsRoute) { launchSingleTop = true } },
                    onNavigateToTutorial = {
                        navController.navigate(MeasurementTutorialRoute) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToLogs = {
                        navController.navigate(LogsRoute) {
                            popUpTo(HomeRoute) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }

            composable<MeasurementTutorialRoute> {
                MeasurementTutorialScreen(
                    onNavigateToMeasurement = {
                        navController.navigate(MeasurementRoute) {
                            popUpTo(MeasurementTutorialRoute) { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() },
                )
            }

            composable<SettingsRoute> {
                SettingsScreen(
                    onNavigateBack = { navController.popBackStack() },
                )
            }

            composable<LogsRoute> {
                LogsScreen(
                    onNavigateToHome = {
                        navController.navigate(HomeRoute) {
                            popUpTo(HomeRoute) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToMeasurement = { navController.navigate(MeasurementRoute) },
                    onNavigateToTutorial = {
                        navController.navigate(MeasurementTutorialRoute) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToResults = { resultId ->
                        navController.navigate(ResultsRoute(resultId))
                    },
                    onNavigateToSettings = {
                        navController.navigate(SettingsRoute) { launchSingleTop = true }
                    },
                )
            }

            composable<MeasurementRoute> {
                MeasurementScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToResults = { resultId ->
                        navController.navigate(ResultsRoute(resultId)) {
                            popUpTo(MeasurementRoute) { inclusive = true }
                        }
                    },
                )
            }

            composable<ResultsRoute> {
                ResultsScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                )
            }
        }
    }
}
