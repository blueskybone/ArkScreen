package com.blueskybone.arkscreen.domain.usecase.appupdate

import com.blueskybone.arkscreen.domain.model.DownloadStatus
import com.blueskybone.arkscreen.domain.repository.AppUpdateRepository
import kotlinx.coroutines.flow.Flow

/**
 * Created by blueskybone
 * Date: 2026/7/9
 */
class DownloadAppUpdateUseCase(
    private val appUpdateRepository: AppUpdateRepository,
) {

    operator fun invoke(
        url: String,
        expectedVersionCode: Long,
    ): Flow<DownloadStatus> {
        return appUpdateRepository.downloadApk(
            url = url,
            expectedVersionCode = expectedVersionCode,
            fileName = "ArkScreen.apk",
        )
    }

    fun resume(): Flow<DownloadStatus>? = appUpdateRepository.resumeApkDownload()
}
