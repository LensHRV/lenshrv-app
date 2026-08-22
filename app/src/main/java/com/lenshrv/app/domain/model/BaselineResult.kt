package com.lenshrv.app.domain.model

data class BaselineResult(
    val bpm: Double,
    val rmssd: Double,
    val sdnn: Double,
    val stressIndex: Double,
    val lf: Double,
    val hf: Double,
    val lfHfRatio: Double,
    val totalPower: Double,
)
