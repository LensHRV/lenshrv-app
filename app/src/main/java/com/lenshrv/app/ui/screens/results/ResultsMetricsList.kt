package com.lenshrv.app.ui.screens.results

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lenshrv.app.domain.model.HrvMetrics
import com.lenshrv.app.ui.screens.results.MetricUiMapper.formatTwoDecimals
import kotlin.math.roundToInt

private data class MetricLine(
    val label: String,
    val value: String,
    val unit: String,
)

private data class MetricGroupData(
    val title: String,
    val lines: List<MetricLine>,
)

@Composable
fun ResultsMetricsList(
    metrics: HrvMetrics,
    onCoherenceInfoClick: (() -> Unit)? = null,
    onFrequencyInfoClick: (() -> Unit)? = null,
    onSignalInfoClick: (() -> Unit)? = null,
    onTimeDomainInfoClick: (() -> Unit)? = null,
) {
    val accent = MaterialTheme.colorScheme.primary

    val artifactPct = metrics.artifactPercent.roundToInt()
    val coveragePct =
        if (metrics.coveragePercent >= 100) 100 else metrics.coveragePercent.roundToInt()

    val groups = listOf(
        MetricGroupData(
            title = "Time domain",
            lines = listOf(
                MetricLine("Heart rate", "${metrics.bpm}", "bpm"),
                MetricLine("RMSSD", "${metrics.rmssd.roundToInt()}", "ms"),
                MetricLine("SDNN", "${metrics.sdnn.roundToInt()}", "ms"),
                MetricLine("Stress", "${metrics.stressIndex.roundToInt()}", "pts"),
            ),
        ),
        MetricGroupData(
            title = "Frequency",
            lines = listOf(
                MetricLine("Total power", metrics.totalPower.roundToInt().toString(), "ms²"),
                MetricLine("LF", metrics.lf.roundToInt().toString(), "ms²"),
                MetricLine("HF", metrics.hf.roundToInt().toString(), "ms²"),
                MetricLine("LF/HF", metrics.lfHfRatio.formatTwoDecimals().toString(), "ratio"),
                MetricLine("Respiration", "${metrics.respirationRate.roundToInt()}", "rpm"),
            ),
        ),
        MetricGroupData(
            title = "Signal quality",
            lines = listOf(
                MetricLine("Coverage", "$coveragePct", "%"),
                MetricLine("Artifacts", "$artifactPct", "%"),
            ),
        ),

        MetricGroupData(
            title = "Coherence",
            lines = listOf(
                MetricLine(
                    "Score",
                    metrics.coherenceScore.formatTwoDecimals().toString(),
                    "pts",
                ),
                MetricLine(
                    "LF peak",
                    metrics.coherencePeakHz.formatTwoDecimals().toString(),
                    "hz",
                ),
            ),
        ),
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(40.dp),
    ) {
        groups.forEach { group ->
            val onInfoClick = when (group.title) {
                "Coherence" -> onCoherenceInfoClick
                "Frequency" -> onFrequencyInfoClick
                "Signal quality" -> onSignalInfoClick
                "Time domain" -> onTimeDomainInfoClick
                else -> null
            }
            MetricGroupCard(
                group = group,
                accent = accent,
                onInfoClick = onInfoClick,
            )
        }
    }
}

@Composable
private fun MetricGroupCard(
    group: MetricGroupData,
    accent: Color,
    onInfoClick: (() -> Unit)? = null,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 2.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = group.title.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.6.sp,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (onInfoClick != null) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "About ${group.title}",
                    tint = accent.copy(alpha = 0.85f),
                    modifier = Modifier
                        .size(24.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onInfoClick,
                        ),
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface),
        ) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight()
                    .background(accent),
            )
            Column(modifier = Modifier.weight(1f)) {
                group.lines.forEach { line ->
                    MetricRow(line = line, accent = accent)
                }
            }
        }
    }
}

@Composable
private fun MetricRow(
    line: MetricLine,
    accent: Color,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 13.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = line.label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f),
            letterSpacing = 0.15.sp,
        )
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Text(
                text = line.value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontFeatureSettings = "tnum",
                    letterSpacing = (-0.2).sp,
                ),
                color = accent,
            )
            Text(
                text = line.unit,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f),
                modifier = Modifier.padding(bottom = 2.dp),
            )
        }
    }
}
