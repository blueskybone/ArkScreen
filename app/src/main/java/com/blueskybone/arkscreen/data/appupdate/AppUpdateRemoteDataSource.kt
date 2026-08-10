package com.blueskybone.arkscreen.data.appupdate

import com.blueskybone.arkscreen.data.resource.ResourceUpdateChecker
import com.blueskybone.arkscreen.domain.model.AppUpdateInfo
import com.blueskybone.arkscreen.domain.model.ConfigType

/**
 * Created by blueskybone
 * Date: 2026/7/9
 */
class AppUpdateRemoteDataSource(
    private val resourceUpdateChecker: ResourceUpdateChecker,
) {

    suspend fun fetchAppUpdateInfo(): AppUpdateInfo {
        val remoteInfo = resourceUpdateChecker.fetchUpdateInfo(
            ConfigType.APP_INFO.xmlUrl
        )

        return AppUpdateInfo(
            version = remoteInfo.version,
            versionCode = remoteInfo.versionCode,
            date = remoteInfo.date,
            content = remoteInfo.content,
            link = remoteInfo.link,
        )
    }
}