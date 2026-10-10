package com.lenshrv.app.domain.algorithm

import com.lenshrv.app.domain.model.RawSample
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class RrMedianGateTest {

    @Serializable
    private data class TvPoint(
        @SerialName("t") val t: Long,
        @SerialName("v") val v: Double,
    )

    companion object {
        // RR kept dcea4ade
        private val KEPT_RR = longArrayOf(
            776L, 792L, 800L, 936L, 852L, 896L, 796L, 820L, 816L, 700L,
            732L, 732L, 1096L, 940L, 864L, 828L, 800L, 820L, 820L, 920L,
            976L, 1064L, 900L, 900L, 868L, 960L, 1156L, 864L, 868L, 816L,
            848L, 904L, 812L, 812L, 928L, 892L, 832L, 852L, 860L, 836L,
            836L, 880L, 908L, 852L, 836L, 808L, 852L, 808L, 668L, 804L,
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
    fun `median gate filters outliers`() {
        val resource = javaClass.classLoader
            ?.getResourceAsStream("gold_250hz_drop.json")
            ?: error("Missing gold_250hz_drop.json in test resources")
        val text = resource.bufferedReader().use { it.readText() }
        val points = Json.decodeFromString<List<TvPoint>>(text)
        val samples = List(points.size) { RawSample(points[it].t, points[it].v) }
        val beats = ElgendiPeaks(samples, fs = 250)
        val gate = RrMedianGate(beats)
        assertEquals("kept count mismatch", KEPT_RR.size, gate.kept.size)
        assertEquals("dropped count mismatch", beats.size - KEPT_RR.size, gate.dropped.size)
        for (i in gate.kept.indices) {
            assertEquals("kept_rr[$i] mismatch", KEPT_RR[i], gate.kept[i].rrInterval)
        }
    }
}
