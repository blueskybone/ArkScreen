package com.blueskybone.arkscreen.domain.model

/**
 * Created by blueskybone
 * Date: 2026/3/11
 */
sealed interface DownloadStatus {

    data object Started : DownloadStatus

    data class Progress(
        val downloadedBytes: Long,
        val totalBytes: Long,
    ) : DownloadStatus {
        val percent: Int
            get() = if (totalBytes <= 0L) {
                0
            } else {
                ((downloadedBytes * 100) / totalBytes).toInt()
            }
    }

    data class Success(
        val filePath: String,
    ) : DownloadStatus

    data class Failed(
        val throwable: Throwable,
    ) : DownloadStatus
}