package com.lenshrv.app.domain.algorithm

import kotlin.math.abs

object SmoothnessPriorsDetrend {
    private const val DEFAULT_LAMBDA = 500.0

    operator fun invoke(rr: DoubleArray, regularization: Double = DEFAULT_LAMBDA): DoubleArray {
        val n = rr.size
        if (n < 3) return rr.copyOf()

        val lambda2 = regularization * regularization
        val a = Array(n) { DoubleArray(n) }
        for (i in 0 until n) a[i][i] = 1.0
        for (i in 0 until n - 2) {
            val cols = intArrayOf(i, i + 1, i + 2)
            val coef = doubleArrayOf(1.0, -2.0, 1.0)
            for (p in 0..2) {
                for (q in 0..2) {
                    a[cols[p]][cols[q]] += lambda2 * coef[p] * coef[q]
                }
            }
        }

        val trend = solveLinear(a, rr.copyOf())
        return DoubleArray(n) { rr[it] - trend[it] }
    }

    private fun solveLinear(a: Array<DoubleArray>, b: DoubleArray): DoubleArray {
        val n = b.size
        val m = Array(n) { i -> a[i].copyOf() }
        val x = b.copyOf()

        for (col in 0 until n) {
            var pivot = col
            var best = abs(m[col][col])
            for (r in col + 1 until n) {
                val v = abs(m[r][col])
                if (v > best) {
                    best = v
                    pivot = r
                }
            }
            if (best < 1e-18) continue
            if (pivot != col) {
                val tmp = m[col]
                m[col] = m[pivot]
                m[pivot] = tmp
                val tb = x[col]
                x[col] = x[pivot]
                x[pivot] = tb
            }
            val div = m[col][col]
            for (c in col until n) m[col][c] /= div
            x[col] /= div
            for (r in 0 until n) {
                if (r == col) continue
                val f = m[r][col]
                if (f == 0.0) continue
                for (c in col until n) m[r][c] -= f * m[col][c]
                x[r] -= f * x[col]
            }
        }
        return x
    }
}
