package com.blueskybone.arkscreen.data.resource

import android.content.Context
import com.blueskybone.arkscreen.data.common.HttpStatusException
import com.blueskybone.arkscreen.domain.model.ConfigType
import timber.log.Timber
import java.io.File
import java.net.HttpURLConnection
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
        } else if (type == ConfigType.I18N_DB && shouldReplaceI18nWithBundled(targetFile)) {
            copyAssetToCache(
                assetName = type.fileName,
                targetFile = targetFile,
            )
        }

        return targetFile
    }

    private fun shouldReplaceI18nWithBundled(cachedFile: File): Boolean {
        val cachedNode = runCatching { jsonReader.readNode(cachedFile) }.getOrNull()
            ?: return true
        if (!isValidI18n(cachedNode)) return true

        val bundledNode = runCatching {
            context.assets.open(ConfigType.I18N_DB.fileName).use(jsonReader::readNode)
        }.getOrNull() ?: return false
        return compareVersions(
            versionOf(bundledNode),
            versionOf(cachedNode),
        ) > 0
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
        type: ConfigType,
        link: String,
    ) {
        val fileName = type.fileName
        val targetFile = getTargetFile(fileName)
        val tempFile = getTempFile(fileName)

        val connection = (URL(link).openConnection() as HttpURLConnection).apply {
            connectTimeout = 10_000
            readTimeout = 10_000
            requestMethod = "GET"
        }
        try {
            val statusCode = connection.responseCode
            if (statusCode !in 200..299) {
                throw HttpStatusException(statusCode, "资源下载失败：HTTP $statusCode")
            }
            connection.inputStream.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        } finally {
            connection.disconnect()
        }

        if (!tempFile.exists()) {
            throw IllegalStateException("Temp file does not exist: ${tempFile.absolutePath}")
        }
        validateDownloadedResource(type, tempFile)

        if (targetFile.exists()) {
            targetFile.delete()
        }

        val renamed = tempFile.renameTo(targetFile)

        if (!renamed) {
            throw IllegalStateException("Rename temp file failed: ${tempFile.absolutePath}")
        }

        Timber.d("Downloaded and updated file: $fileName")
    }

    private fun validateDownloadedResource(type: ConfigType, file: File) {
        val root = jsonReader.readNode(file)
        if (type == ConfigType.I18N_DB) {
            require(isValidI18n(root)) { "i18n resource has an invalid schema" }
        }
    }

    private fun isValidI18n(root: com.fasterxml.jackson.databind.JsonNode): Boolean {
        val version = versionOf(root)
        val mapInfo = root["mapInfo"]
        if (version == "0" || mapInfo == null || !mapInfo.isObject || mapInfo.isEmpty) return false
        val entriesValid = mapInfo.fields().asSequence().all { (key, value) ->
            key.isNotBlank() && value.isTextual && value.asText().isNotBlank()
        }
        return entriesValid && REQUIRED_RECRUIT_KEYS.all(mapInfo::has)
    }

    private fun versionOf(root: com.fasterxml.jackson.databind.JsonNode): String =
        root["update"]?.get("version")?.asText() ?: "0"

    private fun compareVersions(left: String, right: String): Int {
        val leftParts = left.trim().split('.').map { it.toLongOrNull() ?: 0L }
        val rightParts = right.trim().split('.').map { it.toLongOrNull() ?: 0L }
        repeat(maxOf(leftParts.size, rightParts.size)) { index ->
            val result = (leftParts.getOrNull(index) ?: 0L)
                .compareTo(rightParts.getOrNull(index) ?: 0L)
            if (result != 0) return result
        }
        return 0
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

    private companion object {
        val REQUIRED_RECRUIT_KEYS = setOf(
            "medic", "supporter", "caster", "guard", "vanguard", "defender",
            "sniper", "specialist", "top-ope", "sen-ope", "starter", "melee",
            "ranged", "dps", "robot", "defense", "survival", "healing",
            "dp-recovery", "aoe", "slow", "support", "fast-redeploy", "debuff",
            "shift", "nuker", "summon", "crowed-control", "elemental",
        )
    }
}
