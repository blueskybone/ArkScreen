package com.blueskybone.arkscreen.data.resource

import android.content.Context
import com.blueskybone.arkscreen.domain.model.ConfigType
import timber.log.Timber
import java.io.File
import java.net.URL

/**
 * Created by blueskybone
 * Date: 2026/7/9
 */


class ResourceFileStore(
    private val context: Context,
    private val jsonReader: ResourceJsonReader,
) {

    fun getValidFile(type: ConfigType): File {
        val targetFile = getTargetFile(type.fileName)

        if (!targetFile.exists()) {
            copyAssetToCache(
                assetName = type.fileName,
                targetFile = targetFile,
            )
        }

        return targetFile
    }

    fun getLocalVersion(type: ConfigType): String {
        return runCatching {
            val file = getValidFile(type)
            val node = jsonReader.readNode(file)

            node["update"]?.get("version")?.asText() ?: "0"
        }.getOrElse { throwable ->
            Timber.e(throwable, "Read local version failed: ${type.fileName}")
            "0"
        }
    }

    fun getResourceDate(type: ConfigType): String {
        return runCatching {
            val file = getValidFile(type)
            val node = jsonReader.readNode(file)

            node["update"]?.get("date")?.asText() ?: "0"
        }.getOrElse { throwable ->
            Timber.e(throwable, "Read local date failed: ${type.fileName}")
            "0"
        }
    }

    fun downloadConfig(
        fileName: String,
        link: String,
    ) {
        val targetFile = getTargetFile(fileName)
        val tempFile = getTempFile(fileName)

        URL(link).openStream().use { input ->
            tempFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        if (!tempFile.exists()) {
            throw IllegalStateException("Temp file does not exist: ${tempFile.absolutePath}")
        }

        if (targetFile.exists()) {
            targetFile.delete()
        }

        val renamed = tempFile.renameTo(targetFile)

        if (!renamed) {
            throw IllegalStateException("Rename temp file failed: ${tempFile.absolutePath}")
        }

        Timber.d("Downloaded and updated file: $fileName")
    }

    private fun getTargetFile(fileName: String): File {
        val cacheDir = context.externalCacheDir
            ?: throw IllegalStateException("externalCacheDir is null")

        return File(cacheDir, fileName)
    }

    private fun getTempFile(fileName: String): File {
        val cacheDir = context.externalCacheDir
            ?: throw IllegalStateException("externalCacheDir is null")

        return File(cacheDir, "$fileName.tmp")
    }

    private fun copyAssetToCache(
        assetName: String,
        targetFile: File,
    ) {
        context.assets.open(assetName).use { input ->
            targetFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        Timber.d("Copied asset $assetName to cache.")
    }
}