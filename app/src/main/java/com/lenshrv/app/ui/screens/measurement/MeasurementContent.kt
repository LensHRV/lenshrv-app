package com.lenshrv.app.ui.screens.measurement

import androidx.camera.view.PreviewView
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun MeasurementContent(
    state: MeasurementUiState,
    onNavigateBack: () -> Unit,
) {
    val phase = state.phase
    val isCalibrating = phase is MeasurementPhase.Calibrating
    val isMeasuring = phase is MeasurementPhase.Measuring
    val measuringId = (phase as? MeasurementPhase.Measuring)?.measurementId
    var isSurfaceProviderSet by remember(state.previewUseCase) { mutableStateOf(false) }
    val timeLeft = if (phase is MeasurementPhase.Measuring) phase.timeLeft else state.durationSeconds
    val colorSurface = MaterialTheme.colorScheme.surface
    val colorWhite = MaterialTheme.colorScheme.onSurface
    val infiniteTransition = rememberInfiniteTransition(label = "spin")
    val animatedProgress = remember { Animatable(0f) }
    val fallbackAlpha by animateFloatAsState(
        targetValue = if (state.showFallbackHint) 1f else 0f,
        animationSpec = tween(if (state.showFallbackHint) 300 else 400),
        label = "fallbackHint",
    )

    val spinAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "spinAngle",
    )

    LaunchedEffect(measuringId) {
        if (measuringId == null) {
            animatedProgress.snapTo(0f)
            return@LaunchedEffect
        }
        animatedProgress.snapTo(0f)
        delay(300.milliseconds)
        animatedProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = state.durationSeconds * 1000,
                easing = LinearEasing,
            ),
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(bottom = 4.dp)
                    .height(80.dp),
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .height(70.dp)
                        .padding(top = 24.dp, bottom = 24.dp, start = 16.dp, end = 16.dp),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Menu",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .size(26.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                            ) {
                                onNavigateBack()
                            },
                    )
                    Text(
                        text = if (isCalibrating) "CALIBRATING" else "MEASURING",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.align(Alignment.Center),
                        letterSpacing = 2.sp,
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .aspectRatio(1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize(0.98f)
                                .clip(CircleShape)
                                .background(Color.Black),
                        ) {
                            if (state.previewUseCase != null && (isCalibrating || isMeasuring)) {
                                AndroidView(
                                    factory = { ctx ->
                                        PreviewView(ctx).apply {
                                            implementationMode = PreviewView.ImplementationMode.PERFORMANCE
                                            scaleType = PreviewView.ScaleType.FILL_CENTER
                                        }
                                    },
                                    modifier = Modifier.fillMaxSize(),
                                    update = { previewView ->
                                        if (!isSurfaceProviderSet) {
                                            state.previewUseCase.surfaceProvider = previewView.surfaceProvider
                                            isSurfaceProviderSet = true
                                        }
                                    },
                                )
                            }
                        }
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val strokeWidth = 4.dp.toPx()
                            drawCircle(
                                color = colorSurface,
                                style = Stroke(width = strokeWidth),
                                radius = size.minDimension / 2,
                            )
                            if (isCalibrating) {
                                drawArc(
                                    color = colorWhite,
                                    startAngle = spinAngle - 90f,
                                    sweepAngle = 120f,
                                    useCenter = false,
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                                )
                            } else if (isMeasuring) {
                                drawArc(
                                    color = colorWhite,
                                    startAngle = -90f,
                                    sweepAngle = 360f * animatedProgress.value,
                                    useCenter = false,
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                                )
                            }
                        }
                        if (isMeasuring && (state.liveBpm != null || state.liveRrMs != null)) {
                            val numberShadow = TextStyle(
                                shadow = Shadow(
                                    color=colorSurface,
                                    offset = Offset(0f, 1f),
                                    blurRadius = 8f,
                                ),
                            )
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.align(Alignment.Center),
                            ) {
                                val liveBpm = state.liveBpm
                                if (liveBpm != null) {
                                    Text(
                                        text = "$liveBpm",
                                        color = colorWhite,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 64.sp,
                                        textAlign = TextAlign.Center,
                                        style = numberShadow,
                                    )
                                    Text(
                                        text = "bpm",
                                        color = colorWhite.copy(alpha = 0.7f),
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 14.sp,
                                        letterSpacing = 1.sp,
                                    )
                                }
                                val liveRr = state.liveRrMs
                                if (liveRr != null) {
                                    Text(
                                        text = "$liveRr ms",
                                        color = colorWhite.copy(alpha = 0.5f),
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 16.sp,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(top = 8.dp),
                                    )
                                }
                            }
                        }
                    }
                    if (isMeasuring) {
                        Text(
                            text = "$timeLeft s",
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .offset(y = 20.dp),
                        )
                    }
                }
            }

            Text(
                text = "FALLBACK",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                fontWeight = FontWeight.Medium,
                fontSize = 10.sp,
                letterSpacing = 1.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 62.dp)
                    .fillMaxWidth()
                    .graphicsLayer { alpha = fallbackAlpha },
            )
            if (state.fingerAlert) {
                Text(
                    text = "CHECK FINGER",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    letterSpacing = 2.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(bottom = 100.dp)
                        .background(Color.Red)
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                )
            }
        }
    }
}

