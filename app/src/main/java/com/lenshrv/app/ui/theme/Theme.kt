package com.lenshrv.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val darkColorScheme = darkColorScheme(
    primary = Color(0xFF00E5FF),
    onPrimary = Color(0xFF0E1321),

    background = Color(0xFF0E1321),
    onBackground = Color(0xFFF2F4F7),

    surface = Color(0xFF121727),
    onSurface = Color(0xFFF2F4F7),

    error = Color(0xFFEF4444),
)

@Composable
fun HrvTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = darkColorScheme,
        content = content,
    )
}
