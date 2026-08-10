package com.blueskybone.arkscreen.data.appupdate

import com.blueskybone.arkscreen.data.common.repositoryResultOf
import com.blueskybone.arkscreen.domain.model.AppUpdateInfo
import com.blueskybone.arkscreen.domain.model.DownloadStatus
import com.blueskybone.arkscreen.domain.repository.AppUpdateRepository
import kotlinx.coroutines.flow.Flow

/**
 * Created by blueskybone
 * Date: 2026/7/9
 */
class AppUpdateRepositoryImpl(
    private val remoteDataSource: AppUpdateRemoteDataSource,
    private val apkDownloader: ApkDownloader,
) : AppUpdateRepository {

    override suspend fun checkAppUpdate(): Result<AppUpdateInfo> = repositoryResultOf {
        remoteDataSource.fetchAppUpdateInfo()
    }

    override fun downloadApk(
        url: String,
        expectedVersionCode: Long,
        fileName: String,
    ): Flow<DownloadStatus> {
        return apkDownloader.download(
            url = url,
            expectedVersionCode = expectedVersionCode,
            fileName = fileName,
        )
    }

    override fun resumeApkDownload(): Flow<DownloadStatus>? = apkDownloader.resume()
}
