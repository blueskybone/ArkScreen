package com.blueskybone.arkscreen.domain.repository

import com.blueskybone.arkscreen.domain.model.DownloadStatus
import com.blueskybone.arkscreen.domain.model.AppUpdateInfo
import kotlinx.coroutines.flow.Flow

/**
 * Created by blueskybone
 * Date: 2026/7/9
 */
interface AppUpdateRepository {

    suspend fun checkAppUpdate(): Result<AppUpdateInfo>

    fun downloadApk(
        url: String,
        expectedVersionCode: Long,
        fileName: String = "ArkScreen.apk",
    ): Flow<DownloadStatus>

    fun resumeApkDownload(): Flow<DownloadStatus>?
}
