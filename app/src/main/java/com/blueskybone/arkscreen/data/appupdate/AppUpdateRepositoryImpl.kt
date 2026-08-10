package com.blueskybone.arkscreen.data.appupdate

import com.blueskybone.arkscreen.data.repository.utils.safeResultSync
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

    override suspend fun checkAppUpdate(): Result<AppUpdateInfo> = safeResultSync {
        remoteDataSource.fetchAppUpdateInfo()
    }

    override fun downloadApk(
        url: String,
        fileName: String,
    ): Flow<DownloadStatus> {
        return apkDownloader.download(
            url = url,
            fileName = fileName,
        )
    }
}