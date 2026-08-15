package com.blueskybone.arkscreen.core.logger

import android.content.Context
import java.io.File

class LogRepository(private val context: Context) {

    enum class Type(val value: String, val displayName: String) {
        APP("app", "应用"),
        NETWORK("network", "网络");

        companion object {
            fun fromValue(value: String?): Type? = entries.find { it.value == value }
        }
    }

    data class Entry(
        val type: Type,
        val file: File,
        val size: Long = file.length(),
        val modifiedAt: Long = file.lastModified(),
    ) {
        val id: String = "${type.value}:${file.name}"
    }

    fun list(): List<Entry> = Type.entries
        .flatMap { type ->
            directory(type).listFiles { file -> file.isFile && file.extension == "log" }
                .orEmpty()
                .map { Entry(type, it) }
        }
        .sortedByDescending(Entry::modifiedAt)

    fun resolve(type: Type, fileName: String): File? {
        if (fileName != File(fileName).name || !fileName.endsWith(".log")) return null
        val directory = directory(type).canonicalFile
        val candidate = File(directory, fileName).canonicalFile
        return candidate.takeIf { it.isFile && it.parentFile == directory }
    }

    fun delete(entry: Entry): Boolean =
        resolve(entry.type, entry.file.name)?.delete() == true

    fun clear(): Int = list().count(::delete)

    fun directory(type: Type): File {
        val cacheRoot = context.externalCacheDir ?: context.cacheDir
        return File(cacheRoot, if (type == Type.APP) "logs" else "network_logs")
    }
}
