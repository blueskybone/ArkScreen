package com.blueskybone.arkscreen.domain.usecase.appupdate

import com.blueskybone.arkscreen.domain.model.AppUpdateInfo
import com.blueskybone.arkscreen.domain.model.AppVersion
import com.blueskybone.arkscreen.domain.repository.AppUpdateRepository

/**
 * Created by blueskybone
 * Date: 2026/7/9
 */

class CheckAppUpdateUseCase(
    private val appUpdateRepository: AppUpdateRepository,
    private val currentAppVersion: AppVersion,
) {

    suspend operator fun invoke(): Result<AppUpdateInfo?> {
        return appUpdateRepository.checkAppUpdate()
            .map { remote ->
                if (remote.versionCode > currentAppVersion.versionCode) {
                    remote
                } else {
                    null
                }
            }
    }
}