package com.blueskybone.arkscreen.data.appupdate

import android.content.Context
import com.blueskybone.arkscreen.domain.model.DownloadStatus
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File
import java.net.URL

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

        if (!downloadDir.exists()) {
            downloadDir.mkdirs()
        }

        val targetFile = File(downloadDir, fileName)
        val tempFile = File(downloadDir, "$fileName.tmp")

        val connection = URL(url).openConnection()
        val totalBytes = connection.contentLengthLong

        var downloadedBytes = 0L

        connection.getInputStream().use { input ->
            tempFile.outputStream().use { output ->
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)

                while (true) {
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
        emit(DownloadStatus.Failed(e))
    }.flowOn(dispatcher)
}