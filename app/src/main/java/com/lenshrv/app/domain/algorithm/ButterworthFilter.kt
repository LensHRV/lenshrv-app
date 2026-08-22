package com.lenshrv.app.domain.algorithm

import com.lenshrv.app.domain.model.RawSample
import kotlin.math.sqrt
import kotlin.math.tan
import kotlin.math.PI

object ButterworthFilter {
    private fun butterworthLowPassFiltFilt(list: List<RawSample>, fc: Double): List<RawSample> {
        if (list.size < 3) return list

        val durationSeconds = (list.last().timestamp - list.first().timestamp) / 1000.0
        if (durationSeconds <= 0.0) return list

        val fs = (list.size - 1) / durationSeconds
        val nyquistFs = fs / 2.0

        if (fc >= nyquistFs) return list

        val ratio = fc / nyquistFs
        val omega = tan(PI * ratio / 2.0)

        val c = 1.0 / (1.0 + sqrt(2.0) * omega + omega * omega)
        val b0 = omega * omega * c
        val b1 = 2.0 * b0
        val b2 = b0
        val a1 = 2.0 * (omega * omega - 1.0) * c
        val a2 = (1.0 - sqrt(2.0) * omega + omega * omega) * c

        val rawValues = list.map { it.value }


        fun applyFilter(list: List<Double>): List<Double> {
            val output = ArrayList<Double>(list.size)
            var x1 = list.first()
            var x2 = list.first()
            var y1 = list.first()
            var y2 = list.first()

            for (i in list.indices) {
                val x0 = list[i]
                val y0 = (b0 * x0) + (b1 * x1) + (b2 * x2) - (a1 * y1) - (a2 * y2)
                output.add(y0)

                x2 = x1
                x1 = x0
                y2 = y1
                y1 = y0
            }
            return output
        }
        val forwardPass = applyFilter(rawValues)
        val reversedList = forwardPass.reversed()
        val backwardPass = applyFilter(reversedList)
        val filtFiltResult = backwardPass.reversed()
        val filteredList = ArrayList<RawSample>(list.size)
        for (i in list.indices) {
            filteredList.add(RawSample(timestamp = list[i].timestamp, value = filtFiltResult[i]))
        }

        return filteredList
    }

    private fun butterworthHighPassFiltFilt(data: List<RawSample>, fc: Double): List<RawSample> {
        if (data.size < 3) return data

        val durationSeconds = (data.last().timestamp - data.first().timestamp) / 1000.0
        if (durationSeconds <= 0.0) return data
        val fs = (data.size - 1) / durationSeconds
        val nyquistFs = fs / 2.0
        if (fc >= nyquistFs) return data

        val ratio = fc / nyquistFs
        val omega = tan(PI * ratio / 2.0)

        val c = 1.0 / (1.0 + sqrt(2.0) * omega + omega * omega)
        val b0 = c
        val b1 = -2.0 * b0
        val b2 = b0

        val a1 = 2.0 * (omega * omega - 1.0) * c
        val a2 = (1.0 - sqrt(2.0) * omega + omega * omega) * c

        val rawValues = data.map { it.value }

        fun processHighPass(list: List<Double>): List<Double> {
            val output = ArrayList<Double>(list.size)
            var x1 = list.first()
            var x2 = list.first()
            var y1 = 0.0
            var y2 = 0.0

            for (i in list.indices) {
                val x0 = list[i]
                val y0 = (b0 * x0) + (b1 * x1) + (b2 * x2) - (a1 * y1) - (a2 * y2)
                output.add(y0)

                x2 = x1
                x1 = x0
                y2 = y1
                y1 = y0
            }
            return output
        }

        val forwardPass = processHighPass(rawValues)
        val reversedList = forwardPass.reversed()
        val backwardPass = processHighPass(reversedList)
        val filtFiltResult = backwardPass.reversed()
        val filteredList = ArrayList<RawSample>(data.size)
        for (i in data.indices) {
            filteredList.add(RawSample(timestamp = data[i].timestamp, value = filtFiltResult[i]))
        }

        return filteredList
    }
    operator fun invoke (rawData: List<RawSample>): List<RawSample> {
        val bandPass=butterworthLowPassFiltFilt(rawData,8.0)
        return butterworthHighPassFiltFilt(bandPass,0.5)
    }
}
