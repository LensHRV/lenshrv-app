package com.lenshrv.app.util

fun calculateMedian(list: List<Double>): Double {
    if (list.isEmpty()) return 0.0
    val sorted = list.sorted()
    val size = sorted.size
    return if (size % 2 == 0) {
        (sorted[size / 2 - 1] + sorted[size / 2]) / 2.0
    } else {
        sorted[size / 2]
    }
}
