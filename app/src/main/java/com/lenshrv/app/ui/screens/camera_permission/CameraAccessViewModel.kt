package com.lenshrv.app.ui.screens.camera_permission

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.lenshrv.app.data.repository.CameraPermissionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CameraAccessViewModel @Inject constructor(
    private val repository: CameraPermissionManager
): ViewModel() {

    var isPermanentlyDenied by mutableStateOf(false)
        private set

    fun checkPermission(): Boolean {
        return repository.hasCameraPermission()
    }

    fun markAsPermanentlyDenied() {
        isPermanentlyDenied = true
    }
}
