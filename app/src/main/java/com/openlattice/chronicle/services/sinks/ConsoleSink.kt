package com.openlattice.chronicle.services.sinks

import android.os.Environment
import android.util.Log
import com.openlattice.chronicle.android.ChronicleSample
import com.openlattice.chronicle.android.ChronicleUsageEvent
import com.openlattice.chronicle.models.FullUsageEvent
import com.openlattice.chronicle.util.RetrofitBuilders
import java.io.File
import java.io.FileWriter
import java.io.IOException

/*
 * This class is mainly for testing.
 */
class ConsoleSink : DataSink {
    override fun submit(data: List<ChronicleSample>): Map<String, Boolean> {
        Log.d(javaClass.name, RetrofitBuilders.mapper.writeValueAsString(data))
        exportDataToCSV(data)
        return mapOf(ConsoleSink::class.java.name to true )
    }


    private fun exportDataToCSV(data: List<ChronicleSample>) {
        // 获取外部存储目录
        val storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        // 定义文件名
        val fileName = "chronicle_data.csv"
        // 创建文件对象
        val file = File(storageDir, fileName)

        var fileWriter: FileWriter? = null
        try {
            fileWriter = FileWriter(file)
            // 写入CSV头
//            fileWriter.append("studyId,participantId,appPackageName,interactionType,timestamp,timezone,user,applicationLabel\n")
            fileWriter.append("studyId,participantId,appPackageName,className,interactionType,timestamp,timezone,user,applicationLabel\n")

            // 写入数据
            for (sample in data) {
//                if (sample is ChronicleUsageEvent) {
//                    fileWriter.append("${sample.studyId},${sample.participantId},${sample.appPackageName},${sample.interactionType},${sample.timestamp},${sample.timezone},${sample.user},${sample.applicationLabel}\n")
//                }
                when (sample) {
                    is ChronicleUsageEvent -> {
                        fileWriter.append("${sample.studyId},${sample.participantId},${sample.appPackageName},,${sample.interactionType},${sample.timestamp},${sample.timezone},${sample.user},${sample.applicationLabel}\n")
                    }
                    is FullUsageEvent -> {
                        fileWriter.append("${sample.studyId},${sample.participantId},${sample.appPackageName},${sample.className ?: ""},${sample.interactionType},${sample.timestamp},${sample.timezone},${sample.user},${sample.applicationLabel}\n")
                    }
                }
            }
            Log.d(javaClass.name, "CSV文件已成功保存至: ${file.absolutePath}")
        } catch (e: IOException) {
            Log.e(javaClass.name, "写入CSV文件时发生错误", e)
        } finally {
            try {
                fileWriter?.flush()
                fileWriter?.close()
            } catch (e: IOException) {
                Log.e(javaClass.name, "关闭FileWriter时发生错误", e)
            }
        }
    }
}