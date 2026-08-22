package com.lenshrv.app.domain.algorithm

import kotlin.math.abs

object CubicSplineKernel {

    internal fun secondDerivatives(x: DoubleArray, y: DoubleArray): DoubleArray {

        val n = x.size
        val m = DoubleArray(n)
        val h = DoubleArray(n - 1)

        for (i in h.indices) {
            h[i] = x[i + 1] - x[i]
        }

        val unknownsM = n - 2

        val lowerWeight = DoubleArray(unknownsM)
        val diagWeight = DoubleArray(unknownsM)
        val upperWeight = DoubleArray(unknownsM)
        val deltaVelocity = DoubleArray(unknownsM)

        for (k in lowerWeight.indices) {
            val i = k + 1
            lowerWeight[k] = h[i - 1]
            diagWeight[k] = (h[i - 1] + h[i]) * 2
            upperWeight[k] = h[i]
            val prevVelocity = (y[i] - y[i - 1]) / h[i - 1]
            val nextVelocity = (y[i + 1] - y[i]) / h[i]
            deltaVelocity[k] = 6.0 * (nextVelocity - prevVelocity)
        }

        val solved = thomas(lowerWeight, diagWeight, upperWeight, deltaVelocity)

        for (k in 0 until unknownsM) {
            m[k + 1] = solved[k]
        }
        return m
    }

    internal fun thomas(
        lower: DoubleArray,
        diag: DoubleArray,
        upper: DoubleArray,
        rhs: DoubleArray,
    ): DoubleArray {

        val n = diag.size
        val cPrime = DoubleArray(n)
        val dPrime = DoubleArray(n)
        val x = DoubleArray(n)

        cPrime[0] = upper[0] / diag[0]
        dPrime[0] = rhs[0] / diag[0]

        for (i in 1 until n) {
            val denom = diag[i] - lower[i] * cPrime[i - 1]
            cPrime[i] = if (i < n - 1) upper[i] / denom else 0.0
            dPrime[i] = (rhs[i] - lower[i] * dPrime[i - 1]) / denom
        }

        x[n - 1] = dPrime[n - 1]
        for (i in n - 2 downTo 0) {
            x[i] = dPrime[i] - cPrime[i] * x[i + 1]
        }
        return x
    }

    internal fun evalSpline(
        x: DoubleArray,
        y: DoubleArray,
        m: DoubleArray,
        segment: Int,
        t: Double,
    ): Double {
        var i = segment
        if (i < 0) i = 0
        if (i > x.size - 2) i = x.size - 2

        if (abs(t - x[i]) < 1e-12) return y[i]
        if (abs(t - x[i + 1]) < 1e-12) return y[i + 1]

        val h = x[i + 1] - x[i]
        val a = (x[i + 1] - t) / h
        val b = (t - x[i]) / h
        return a * y[i] + b * y[i + 1] +
                ((a * a * a - a) * m[i] + (b * b * b - b) * m[i + 1]) * (h * h) / 6.0
    }
}
