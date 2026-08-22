package com.lenshrv.app.local.exporter

import com.lenshrv.app.local.entities.ChannelValuesEntity
import com.lenshrv.app.local.entities.HrvMetricsEntity
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object DataExporter {
    fun hrvMetricsToCsv(metrics: List<HrvMetricsEntity>): String {
        val sb = StringBuilder()
        sb.append("id,timestamp,durationSeconds,bpm,rmssd,sdnn,stressIndex,respirationRate,lf,hf,lfHfRatio,artifactPercent,coveragePercent,coherenceScore,coherencePeakHz,algorithmVersion,cameraMetadata,isSynced\n")
        for (m in metrics) {
            sb.append("${m.id},")
                .append("${m.timestamp},")
                .append("${m.durationSeconds},")
                .append("${m.bpm},")
                .append("${m.rmssd},")
                .append("${m.sdnn},")
                .append("${m.stressIndex},")
                .append("${m.respirationRate},")
                .append("${m.lf},")
                .append("${m.hf},")
                .append("${m.lfHfRatio},")
                .append("${m.artifactPercent},")
                .append("${m.coveragePercent},")
                .append("${m.coherenceScore},")
                .append("${m.coherencePeakHz},")
                .append("${m.algorithmVersion},")
                .append("\"${m.cameraMetadata}\",")
                .append("${m.isSynced}\n")
        }
        return sb.toString()
    }

    fun channelValuesToCsv(channels: List<ChannelValuesEntity>): String {
        val sb = StringBuilder()
        sb.append("id,sessionId,timestamp,value\n")
        for (c in channels) {
            sb.append("${c.id},")
                .append("${c.sessionId},")
                .append("${c.timestamp},")
                .append("${c.value}\n")
        }
        return sb.toString()
    }

    fun exportToZipStream(
        metrics: List<HrvMetricsEntity>,
        channels: List<ChannelValuesEntity>,
        outputStream: OutputStream
    ) {
        val metricsCsv = hrvMetricsToCsv(metrics)
        val channelsCsv = channelValuesToCsv(channels)

        ZipOutputStream(outputStream).use { zipStream ->
            zipStream.putNextEntry(ZipEntry("hrv_metrics.csv"))
            zipStream.write(metricsCsv.toByteArray(Charsets.UTF_8))
            zipStream.closeEntry()
            zipStream.putNextEntry(ZipEntry("channel_values.csv"))
            zipStream.write(channelsCsv.toByteArray(Charsets.UTF_8))
            zipStream.closeEntry()
        }
    }
}
