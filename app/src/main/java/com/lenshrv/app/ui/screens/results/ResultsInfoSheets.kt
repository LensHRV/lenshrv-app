package com.lenshrv.app.ui.screens.results

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lenshrv.app.util.formatInsightText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoherenceInfoSheet(onDismiss: () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
                .padding(bottom = 40.dp),
        ) {
            Text(
                text = "About Coherence",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = formatInsightText(
                    "How steady your heart rhythm was. Higher values usually appear when you breathe slowly and evenly.",
                ),
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 22.sp,
            )
            Spacer(modifier = Modifier.height(16.dp))
            InfoRangeRow("0.0 – 0.5", "Completely normal and expected in daily life.")
            InfoRangeRow("0.5 – 2.0", "Good resonance state — from guided breathing.")
            InfoRangeRow("> 2.0", "Excellent.")
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = formatInsightText(
                    "Internal tests have shown that a score of **3.6** is fully achievable.\n\n" +
                        "**LF peak** is the peak frequency between **0.04** and **0.12** Hz.",
                ),
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 22.sp,
            )
        }
    }
}

@Composable
private fun InfoRangeRow(range: String, label: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = range,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.width(88.dp),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.78f),
            lineHeight = 20.sp,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
fun FrequencyInfoSheet(onDismiss: () -> Unit) {
    InfoBottomSheet(
        title = "About Frequency",
        body = FREQUENCY_BODY,
        seeMoreUrl = FREQUENCY_SEE_MORE_URL,
        seeMoreLabel = FREQUENCY_SEE_MORE_LABEL,
        onDismiss = onDismiss,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeDomainInfoSheet(onDismiss: () -> Unit) {
    val uriHandler = LocalUriHandler.current
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
                .padding(bottom = 40.dp),
        ) {
            Text(
                text = "About Time Domain",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = formatInsightText(TIME_DOMAIN_BODY),
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 22.sp,
            )
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = formatInsightText(
                    "Quality thresholds for Time Domain metrics:"
                ),
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 22.sp,
            )
            Spacer(modifier = Modifier.height(16.dp))

            InfoRangeRow("< 5%", "Excellent grade.")
            InfoRangeRow("< 15%", "Good grade.")
            InfoRangeRow("≥ 15%", "Invalidated")
            Spacer(modifier = Modifier.height(20.dp))
            val accent = MaterialTheme.colorScheme.primary
            Row(
                modifier = Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) {
                    uriHandler.openUri(TIME_DOMAIN_SEE_MORE_URL)
                },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = "See more on web",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = accent,
                )
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Opens in browser",
                    tint = accent,
                    modifier = Modifier.size(16.dp),
                )
            }
            Text(
                text = TIME_DOMAIN_SEE_MORE_LABEL,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QualityInfoSheet(onDismiss: () -> Unit) {
    val uriHandler = LocalUriHandler.current
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
                .padding(bottom = 40.dp),
        ) {
            Text(
                text = "About Artifacts",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = formatInsightText(
                    "Quality thresholds follow **Effects of Missing Data on Heart Rate Variability Metrics** (University of Zaragoza, **Sensors**):"
                ),
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 22.sp,
            )
            Spacer(modifier = Modifier.height(16.dp))

            InfoRangeRow("< 5%", "Excellent grade.")
            InfoRangeRow("< 15%", "Good grade.")
            InfoRangeRow("≥ 15%", "Invalidated")

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = formatInsightText(
                    "**Coverage**: how much of the target **120 s** is spanned by the first and last beat-to-beat.\n\n" +
                        "**Artifacts**: within that coverage window, how many beats were lost.",
                ),
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 22.sp,
            )
            Spacer(modifier = Modifier.height(20.dp))
            val accent = MaterialTheme.colorScheme.primary
            Row(
                modifier = Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) {
                    uriHandler.openUri(ARTIFACTS_SEE_MORE_URL)
                },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = "See more on web",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = accent,
                )
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Opens in browser",
                    tint = accent,
                    modifier = Modifier.size(16.dp),
                )
            }
            Text(
                text = ARTIFACTS_SEE_MORE_LABEL,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InfoBottomSheet(
    title: String,
    body: String,
    onDismiss: () -> Unit,
    seeMoreUrl: String? = null,
    seeMoreLabel: String? = null,
) {
    val uriHandler = LocalUriHandler.current
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
                .padding(bottom = 40.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = formatInsightText(body),
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 22.sp,
            )
            if (seeMoreUrl != null && seeMoreLabel != null) {
                Spacer(modifier = Modifier.height(20.dp))
                val accent = MaterialTheme.colorScheme.primary
                Row(
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) {
                        uriHandler.openUri(seeMoreUrl)
                    },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = "See more on web",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = accent,
                    )
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Opens in browser",
                        tint = accent,
                        modifier = Modifier.size(16.dp),
                    )
                }
                Text(
                    text = seeMoreLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}

private const val FREQUENCY_BODY =
    "LensHRV uses **Lomb–Scargle** spectral analysis combined with **Hermite** gap-filling for frequency metrics.\n\n" +
            "This pipeline ensures maximum stability against PPG beat loss. Measurements with **15% or more missing data** are invalidated to maintain mathematical accuracy.\n\n" +
            "All power values are scaled to standard ms\u00B2."

private const val TIME_DOMAIN_BODY =
    "Quality thresholds follow **Effects of Missing Data on Heart Rate Variability Metrics** (University of Zaragoza, **Sensors**):"

private const val FREQUENCY_SEE_MORE_URL = "https://lenshrv.com/methods/frequency"
private const val FREQUENCY_SEE_MORE_LABEL = "lenshrv.com/methods/frequency"
private const val TIME_DOMAIN_SEE_MORE_URL = "https://lenshrv.com/methods/time-domain"
private const val TIME_DOMAIN_SEE_MORE_LABEL = "lenshrv.com/methods/time-domain"
private const val ARTIFACTS_SEE_MORE_URL = "https://lenshrv.com/methods/"
private const val ARTIFACTS_SEE_MORE_LABEL = "lenshrv.com/methods/"
