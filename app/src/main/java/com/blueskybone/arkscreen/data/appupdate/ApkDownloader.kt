package com.blueskybone.arkscreen.data.appupdate

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.domain.model.DownloadStatus
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File

class ApkDownloader(
    private val context: Context,
    private val dispatcher: CoroutineDispatcher,
    private val validator: ApkValidator,
) {
    private val downloadManager =
        context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    private val state = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun download(
        url: String,
        expectedVersionCode: Long,
        fileName: String,
    ): Flow<DownloadStatus> {
        val pendingId = state.getLong(KEY_DOWNLOAD_ID, NO_DOWNLOAD)
        if (pendingId != NO_DOWNLOAD) return observe(pendingId)

        return flow {
            val uri = Uri.parse(url)
            require(uri.scheme == "https") { "更新地址必须使用 HTTPS" }

            val targetFile = targetFile(fileName)
            targetFile.delete()
            val request = DownloadManager.Request(uri)
                .setTitle(context.getString(R.string.app_update_download_title))
                .setDescription(context.getString(R.string.app_update_download_description))
                .setMimeType(APK_MIME_TYPE)
                .setNotificationVisibility(
                    DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
                )
                .setDestinationInExternalFilesDir(context, APK_DIRECTORY, fileName)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(false)

            val id = downloadManager.enqueue(request)
            state.edit()
                .putLong(KEY_DOWNLOAD_ID, id)
                .putLong(KEY_EXPECTED_VERSION, expectedVersionCode)
                .putString(KEY_FILE_NAME, fileName)
                .apply()
            emitAllDownload(id)
        }.catch { error -> emitFailure(error) }.flowOn(dispatcher)
    }

    fun resume(): Flow<DownloadStatus>? {
        val id = state.getLong(KEY_DOWNLOAD_ID, NO_DOWNLOAD)
        return id.takeIf { it != NO_DOWNLOAD }?.let(::observe)
    }

    private fun observe(id: Long): Flow<DownloadStatus> =
        flow { emitAllDownload(id) }
            .catch { error -> emitFailure(error) }
            .flowOn(dispatcher)

    private suspend fun kotlinx.coroutines.flow.FlowCollector<DownloadStatus>.emitAllDownload(
        id: Long,
    ) {
        emit(DownloadStatus.Started)
        var lastPercent = -1
        while (true) {
            val snapshot = query(id) ?: error("系统下载任务不存在")
            when (snapshot.status) {
                DownloadManager.STATUS_PENDING,
                DownloadManager.STATUS_PAUSED,
                DownloadManager.STATUS_RUNNING -> {
                    val percent = if (snapshot.totalBytes > 0L) {
                        ((snapshot.downloadedBytes * 100L) / snapshot.totalBytes).toInt()
                    } else {
                        0
                    }
                    if (percent != lastPercent) {
                        lastPercent = percent
                        emit(
                            DownloadStatus.Progress(
                                downloadedBytes = snapshot.downloadedBytes,
                                totalBytes = snapshot.totalBytes,
                            )
                        )
                    }
                    delay(QUERY_INTERVAL_MS)
                }

                DownloadManager.STATUS_SUCCESSFUL -> {
                    val file = targetFile(state.getString(KEY_FILE_NAME, DEFAULT_FILE_NAME)!!)
                    validator.validate(
                        file = file,
                        expectedVersionCode = state.getLong(KEY_EXPECTED_VERSION, Long.MAX_VALUE),
                    )
                    clearState()
                    emit(DownloadStatus.Success(file.absolutePath))
                    return
                }

                DownloadManager.STATUS_FAILED ->
                    error("下载失败（系统错误码 ${snapshot.reason}）")

                else -> error("未知下载状态 ${snapshot.status}")
            }
        }
    }

    private fun query(id: Long): Snapshot? {
        val cursor = downloadManager.query(DownloadManager.Query().setFilterById(id))
        cursor.use {
            if (!it.moveToFirst()) return null
            return Snapshot(
                status = it.getInt(it.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS)),
                reason = it.getInt(it.getColumnIndexOrThrow(DownloadManager.COLUMN_REASON)),
                downloadedBytes = it.getLong(
                    it.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                ),
                totalBytes = it.getLong(
                    it.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                ),
            )
        }
    }

    private suspend fun kotlinx.coroutines.flow.FlowCollector<DownloadStatus>.emitFailure(
        error: Throwable,
    ) {
        if (error is CancellationException) throw error
        clearState()
        emit(DownloadStatus.Failed(error))
    }

    private fun targetFile(fileName: String): File {
        val directory = context.getExternalFilesDir(APK_DIRECTORY)
            ?: error("无法访问安装包目录")
        check(directory.exists() || directory.mkdirs()) { "无法创建安装包目录" }
        return File(directory, fileName)
    }

    private fun clearState() {
        state.edit().clear().apply()
    }

    private data class Snapshot(
        val status: Int,
        val reason: Int,
        val downloadedBytes: Long,
        val totalBytes: Long,
    )

    private companion object {
        const val PREFS_NAME = "app_update_download"
        const val KEY_DOWNLOAD_ID = "download_id"
        const val KEY_EXPECTED_VERSION = "expected_version"
        const val KEY_FILE_NAME = "file_name"
        const val NO_DOWNLOAD = -1L
        const val QUERY_INTERVAL_MS = 500L
        const val APK_DIRECTORY = "apk"
        const val DEFAULT_FILE_NAME = "ArkScreen.apk"
        const val APK_MIME_TYPE = "application/vnd.android.package-archive"
    }
}
