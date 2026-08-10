package com.blueskybone.arkscreen.core.logger

import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class RotatingLogWriter(
    private val directory: File,
    private val filePrefix: String,
    private val maxFileBytes: Long = 1024L * 1024L,
    private val maxFiles: Int = 5,
) {
    private val executor: ExecutorService = Executors.newSingleThreadExecutor { task ->
        Thread(task, "ArkScreen-$filePrefix-logger").apply { isDaemon = true }
    }
    private val timestampFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    init {
        directory.mkdirs()
        executor.execute(::cleanup)
    }

    fun write(message: String) {
        executor.execute {
            runCatching {
                directory.mkdirs()
                val now = LocalDateTime.now()
                val file = File(directory, "${filePrefix}_${dateFormatter.format(now)}.log")
                val line = "[${timestampFormatter.format(now)}] ${LogSanitizer.sanitize(message)}\n"
                if (file.exists() && file.length() + line.toByteArray().size > maxFileBytes) {
                    rotate(file)
                }
                file.appendText(line, Charsets.UTF_8)
                cleanup()
            }
        }
    }

    private fun rotate(current: File) {
        File(directory, "${current.nameWithoutExtension}_${maxFiles - 1}.log").delete()
        for (index in maxFiles - 2 downTo 1) {
            val source = File(directory, "${current.nameWithoutExtension}_$index.log")
            if (source.exists()) {
                source.renameTo(File(directory, "${current.nameWithoutExtension}_${index + 1}.log"))
            }
        }
        current.renameTo(File(directory, "${current.nameWithoutExtension}_1.log"))
    }

    private fun cleanup() {
        val files = directory.listFiles { file ->
            file.isFile && file.name.startsWith("${filePrefix}_") && file.extension == "log"
        }?.sortedByDescending(File::lastModified).orEmpty()
        files.drop(maxFiles).forEach(File::delete)
    }
}
