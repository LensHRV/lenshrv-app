package com.lenshrv.app.domain.algorithm

import com.lenshrv.app.domain.model.RawSample
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Golden tests for Elgendi peak detection, the median gate and the
 * sliding-window moving average.
 * 250Hz signals stored as resources are decoded into RawSample lists,
 * matching the production pipeline, and outputs are compared RR by RR
 * against references. Window bounds (start, full, end) are
 * checked individually on synthetic input.
 */

class ElgendiPeaksTest {
    @Serializable
    private data class TvPoint(
        @SerialName("t") val t: Long,
        @SerialName("v") val v: Double,
    )

    companion object {

        private const val DELTA_SIX_DECIMALS = 1e-6
        private val REF_RR = longArrayOf(
            952L, 1028L, 996L, 872L, 888L, 976L, 1160L, 948L, 1028L, 1084L,
            1028L, 984L, 1020L, 1036L, 940L, 1020L, 992L, 860L, 896L, 1008L,
            956L, 932L, 1048L, 1016L, 940L, 984L, 968L, 880L, 976L, 968L,
            868L, 872L, 936L, 904L, 884L, 940L, 992L, 860L, 880L, 916L,
            924L, 836L, 928L, 928L, 852L, 868L, 964L, 916L, 848L, 924L,
            932L, 884L, 960L, 964L, 864L, 988L, 1044L, 920L, 784L, 776L,
            748L, 920L, 1180L, 1252L, 860L, 1104L, 1100L, 1092L, 1092L, 984L,
            984L, 1020L, 972L, 856L, 936L, 932L, 860L, 920L, 972L, 1024L,
            940L, 996L, 1020L, 996L, 1020L, 1080L, 992L, 1052L, 1044L, 928L,
            1000L, 1052L, 916L, 1052L, 1064L, 972L, 1068L, 1152L, 1048L, 1096L,
            1024L, 988L, 1012L, 1028L, 1004L, 1100L, 1056L, 964L, 1088L, 1068L,
            968L, 1080L, 1164L, 1072L, 1136L, 1092L, 1100L, 1176L, 1116L, 1032L,
            1116L, 1096L, 1024L, 1132L, 1020L, 924L, 1044L
        )

        // RR  (art 9.2%) dcea4ade
        private val REF_RR_DROP = longArrayOf(
            776L, 792L, 800L, 936L, 852L, 472L, 444L, 896L, 796L, 820L,
            816L, 700L, 732L, 732L, 520L, 1096L, 400L, 1380L, 940L, 864L,
            828L, 800L, 820L, 820L, 1492L, 920L, 976L, 1064L, 900L, 900L,
            868L, 960L, 1156L, 1340L, 864L, 868L, 1720L, 816L, 848L, 904L,
            812L, 812L, 928L, 892L, 832L, 852L, 860L, 836L, 836L, 880L,
            908L, 852L, 836L, 808L, 852L, 808L, 668L, 1676L, 1660L, 804L,
            844L, 896L, 928L, 896L, 876L, 836L, 812L, 808L, 824L, 888L,
            896L, 852L, 868L, 892L, 832L, 796L, 792L, 912L, 840L, 896L,
            876L, 872L, 832L, 852L, 860L, 832L, 836L, 840L, 872L, 824L,
            860L, 892L, 884L, 832L, 844L, 816L, 832L, 788L, 820L, 856L,
            824L, 844L, 864L, 868L, 804L, 836L, 844L, 852L, 800L, 784L,
            812L, 808L, 828L, 880L, 900L, 856L, 864L, 844L, 824L, 792L,
            808L, 892L, 900L, 896L, 888L, 900L, 800L, 832L, 816L, 808L,
            800L, 824L, 860L, 832L, 812L, 820L, 840L, 804L, 808L, 820L,
            816L, 772L, 824L, 880L, 840L
        )
    }

    @Test
    fun `should match reference RR intervals at 250Hz`() {
        val resource = javaClass.classLoader
            ?.getResourceAsStream("gold_250hz.json")
            ?: error("Missing gold_250hz.json in test resources")

        val text = resource.bufferedReader().use { it.readText() }
        val points = Json.decodeFromString<List<TvPoint>>(text)
        val samples = List(points.size) { RawSample(points[it].t, points[it].v) }

        val beats = ElgendiPeaks(samples, fs = 250)
        assertEquals("n beats", REF_RR.size, beats.size)
        for (i in beats.indices) {
            assertEquals("rr[$i]", REF_RR[i], beats[i].rrInterval)
        }
    }

    @Test
    fun `should match notebook baseline and filter outliers with median gate`() {
        val resource = javaClass.classLoader
            ?.getResourceAsStream("gold_250hz_drop.json")
            ?: error("Missing gold_250hz_drop.json in test resources")

        val text = resource.bufferedReader().use { it.readText() }
        val points = Json.decodeFromString<List<TvPoint>>(text)
        val samples = List(points.size) { RawSample(points[it].t, points[it].v) }

        val beats = ElgendiPeaks(samples, fs = 250)
        assertEquals("n beats drop", REF_RR_DROP.size, beats.size)
        for (i in beats.indices) {
            assertEquals("rr[$i]", REF_RR_DROP[i], beats[i].rrInterval)
        }
    }

    @Test
    fun `moving average respects window bounds at edges and center`() {
        val s = DoubleArray(168) { (it + 1).toDouble() }

        // Peak window (small: 28 samples, half: 14)
        val peak = movingAverage(s, 28)
        assertEquals("peak start mismatch", 8.0, peak[0], DELTA_SIX_DECIMALS)
        assertEquals("peak center mismatch", 51.0, peak[50], DELTA_SIX_DECIMALS)
        assertEquals("peak mid mismatch", 101.0, peak[100], DELTA_SIX_DECIMALS)
        assertEquals("peak end mismatch", 161.0, peak[167], DELTA_SIX_DECIMALS)

        // Beat window (large: 167 samples, half: 83)
        val beat = movingAverage(s, 167)
        assertEquals("beat start mismatch", 42.5, beat[0], DELTA_SIX_DECIMALS)
        assertEquals("beat center mismatch", 84.0, beat[83], DELTA_SIX_DECIMALS)
        assertEquals("beat next mismatch", 85.0, beat[84], DELTA_SIX_DECIMALS)
        assertEquals("beat end mismatch", 126.5, beat[167], DELTA_SIX_DECIMALS)
    }
}