package com.blueskybone.arkscreen.data.appupdate

import android.content.Context
import com.blueskybone.arkscreen.domain.model.DownloadStatus
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File
import java.net.URL
import java.net.HttpURLConnection
import kotlin.coroutines.coroutineContext

/**
 * Created by blueskybone
 * Date: 2026/7/9
 */
class ApkDownloader(
    private val context: Context,
    private val dispatcher: CoroutineDispatcher,
) {

    fun download(
        url: String,
        fileName: String,
    ): Flow<DownloadStatus> = flow {
        emit(DownloadStatus.Started)

        val downloadDir = context.getExternalFilesDir("apk")
            ?: throw IllegalStateException("external files dir is null")

        if (!downloadDir.exists() && !downloadDir.mkdirs()) {
            throw IllegalStateException("create apk download dir failed")
        }

        val targetFile = File(downloadDir, fileName)
        val tempFile = File(downloadDir, "$fileName.tmp")

        val connection = URL(url).openConnection().apply {
            connectTimeout = CONNECT_TIMEOUT_MS
            readTimeout = READ_TIMEOUT_MS
        }
        if (connection is HttpURLConnection) {
            connection.instanceFollowRedirects = true
            val responseCode = connection.responseCode
            if (responseCode !in 200..299) {
                connection.disconnect()
                throw IllegalStateException("download apk failed with HTTP $responseCode")
            }
        }
        val totalBytes = connection.contentLengthLong

        var downloadedBytes = 0L

        try {
            connection.getInputStream().use { input ->
                tempFile.outputStream().buffered().use { output ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)

                    while (true) {
                        coroutineContext.ensureActive()
                        val read = input.read(buffer)
                        if (read == -1) break

                        output.write(buffer, 0, read)
                        downloadedBytes += read

                        emit(
                            DownloadStatus.Progress(
                                downloadedBytes = downloadedBytes,
                                totalBytes = totalBytes,
                            )
                        )
                    }
                }
            }
        } finally {
            (connection as? HttpURLConnection)?.disconnect()
        }

        if (targetFile.exists()) {
            targetFile.delete()
        }

        if (!tempFile.renameTo(targetFile)) {
            throw IllegalStateException("rename apk temp file failed")
        }

        emit(
            DownloadStatus.Success(
                filePath = targetFile.absolutePath,
            )
        )
    }.catch { e ->
        context.getExternalFilesDir("apk")
            ?.resolve("$fileName.tmp")
            ?.delete()
        if (e is CancellationException) throw e
        emit(DownloadStatus.Failed(e))
    }.flowOn(dispatcher)

    companion object {
        private const val CONNECT_TIMEOUT_MS = 15_000
        private const val READ_TIMEOUT_MS = 30_000
    }
}
