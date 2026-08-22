package com.lenshrv.app.ui.screens.results

import kotlin.math.roundToLong

object MetricUiMapper {
    fun Double.formatTwoDecimals(): Double {
        return (this * 100.0).roundToLong() / 100.0
    }
}
