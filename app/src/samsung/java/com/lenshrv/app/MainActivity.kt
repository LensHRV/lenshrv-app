/*
 * LensHRV
 * Copyright (C) 2026 D_V
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.lenshrv.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.lenshrv.app.data.billing.SamsungIapManager
import com.lenshrv.app.data.repository.AppPreferencesRepository
import com.lenshrv.app.data.worker.TelemetryScheduler
import com.lenshrv.app.ui.components.StartupTipThanksHost
import com.lenshrv.app.ui.navigation.AppNavigation
import com.lenshrv.app.ui.screens.splash.NavigationStates
import com.lenshrv.app.ui.screens.splash.SplashViewModel
import com.lenshrv.app.ui.theme.HrvTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: SplashViewModel by viewModels()

    @Inject
    lateinit var appPreferencesRepository: AppPreferencesRepository

    @Inject
    lateinit var telemetryScheduler: TelemetryScheduler

    @Inject
    lateinit var samsungIapManager: SamsungIapManager

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        samsungIapManager.initialize()
        lifecycleScope.launch {
            if (appPreferencesRepository.isTelemetryEnabled()) {
                telemetryScheduler.schedule()
            }
        }
        splashScreen.setKeepOnScreenCondition {
            viewModel.uiState.value is NavigationStates.Loading
        }

        setContent {
            HrvTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        AppNavigation()
                        StartupTipThanksHost(iapManager = samsungIapManager)
                    }
                }
            }
        }
    }
}
