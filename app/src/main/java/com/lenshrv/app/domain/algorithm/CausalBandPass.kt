package com.lenshrv.app.domain.algorithm

import javax.inject.Inject

class CausalBandPass @Inject constructor() {
    private var lpx1 = 0.0
    private var lpx2 = 0.0
    private var lpy1 = 0.0
    private var lpy2 = 0.0
    private var hpx1 = 0.0
    private var hpx2 = 0.0
    private var hpy1 = 0.0
    private var hpy2 = 0.0
    private var primed = false

    fun step(x: Double): Double {
        if (!primed) {
            lpx1 = x
            lpx2 = x
            lpy1 = x
            lpy2 = x
        }
        val lp = (LP_B0 * x) + (LP_B1 * lpx1) + (LP_B2 * lpx2) - (LP_A1 * lpy1) - (LP_A2 * lpy2)
        lpx2 = lpx1
        lpx1 = x
        lpy2 = lpy1
        lpy1 = lp
        if (!primed) {
            hpx1 = lp
            hpx2 = lp
            primed = true
        }
        val hp = (HP_B0 * lp) + (HP_B1 * hpx1) + (HP_B2 * hpx2) - (HP_A1 * hpy1) - (HP_A2 * hpy2)
        hpx2 = hpx1
        hpx1 = lp
        hpy2 = hpy1
        hpy1 = hp
        return hp
    }

    fun reset() {
        lpx1 = 0.0
        lpx2 = 0.0
        lpy1 = 0.0
        lpy2 = 0.0
        hpx1 = 0.0
        hpx2 = 0.0
        hpy1 = 0.0
        hpy2 = 0.0
        primed = false
    }

    private companion object {
        const val LP_B0 = 0.3242446420921887
        const val LP_B1 = 0.6484892841843773
        const val LP_B2 = 0.3242446420921887
        const val LP_A1 = 0.1227412250125192
        const val LP_A2 = 0.17423734335623545
        const val HP_B0 = 0.9286237778565042
        const val HP_B1 = -1.8572475557130084
        const val HP_B2 = 0.9286237778565042
        const val HP_A1 = -1.8521464853959357
        const val HP_A2 = 0.862348626030081
    }
}
