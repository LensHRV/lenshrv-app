package com.lenshrv.app.ui.screens.results

import com.lenshrv.app.domain.model.BaselineResult
import com.lenshrv.app.domain.model.HrvMetrics
import kotlin.math.abs
import kotlin.math.roundToInt

data class HeroItem(
    val label: String,
    val value: String,
    val diffPercentage: String,
    val isIncrease: Boolean,
    val baselineValue: String,
)

object ResultsHeroMapper {
    fun generateHeroList(metrics: HrvMetrics, baseline: BaselineResult): List<HeroItem> {
        val itemBpm = pctItem(
            label = "HEART RATE",
            actual = metrics.bpm.toDouble(),
            base = baseline.bpm,
        )
        val itemRmssd = pctItem(
            label = "RMSSD",
            actual = metrics.rmssd,
            base = baseline.rmssd,
        )
        val itemStress = pctItem(
            label = "STRESS INDEX",
            actual = metrics.stressIndex,
            base = baseline.stressIndex,
        )
        val itemSdnn = pctItem(
            label = "SDNN",
            actual = metrics.sdnn,
            base = baseline.sdnn,
        )
        val itemTotalPower = pctItem(
            label = "TOTAL POWER",
            actual = metrics.lf + metrics.hf,
            base = baseline.totalPower,
        )
        val itemLf = pctItem(
            label = "LF POWER",
            actual = metrics.lf,
            base = baseline.lf,
        )
        val itemHf = pctItem(
            label = "HF POWER",
            actual = metrics.hf,
            base = baseline.hf,
        )
        val itemRatio = pctItem(
            label = "LF/HF",
            actual = metrics.lfHfRatio,
            base = baseline.lfHfRatio,
            decimals = 2,
        )
        return listOf(
            itemBpm,
            itemRmssd,
            itemStress,
            itemSdnn,
            itemTotalPower,
            itemLf,
            itemHf,
            itemRatio,
        )
    }

    private fun pctItem(
        label: String,
        actual: Double,
        base: Double,
        decimals: Int = 0,
    ): HeroItem {
        val actualR = if (decimals > 0) {
            (actual * 100.0).roundToInt() / 100.0
        } else {
            actual.roundToInt().toDouble()
        }
        val baseR = if (decimals > 0) {
            (base * 100.0).roundToInt() / 100.0
        } else {
            base.roundToInt().toDouble()
        }
        val actualShown = if (decimals > 0) {
            String.format("%.2f", actualR)
        } else {
            actualR.roundToInt().toString()
        }
        val baseShown = if (decimals > 0) {
            String.format("%.2f", baseR)
        } else {
            baseR.roundToInt().toString()
        }
        val pct = if (baseR > 0.0) {
            ((actualR - baseR) / baseR * 100.0).roundToInt()
        } else {
            0
        }
        return HeroItem(
            label = label,
            value = actualShown,
            diffPercentage = "${abs(pct)}%",
            baselineValue = baseShown,
            isIncrease = pct >= 0,
        )
    }
}
