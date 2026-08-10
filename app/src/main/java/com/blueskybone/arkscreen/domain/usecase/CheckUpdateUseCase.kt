package com.blueskybone.arkscreen.domain.usecase

import com.blueskybone.arkscreen.domain.AppError
import com.blueskybone.arkscreen.domain.model.AppUpdateInfo
import com.blueskybone.arkscreen.domain.model.AppVersion
import com.blueskybone.arkscreen.domain.repository.ResourceRepository

/**
 * Created by blueskybone
 * Date: 2026/3/10
 */

class CheckUpdateUseCase(
    private val appVersion: AppVersion,
    private val repo: ResourceRepository,
) {
    sealed class CheckResult {
        data class Update(val info: AppUpdateInfo) : CheckResult()
        data class NoUpdate(val message: String) : CheckResult()
    }

    suspend operator fun invoke(): Result<CheckResult> {
        val result = repo.checkAppUpdate()
        val code = result.getOrNull()?.versionCode
            ?: return Result.failure(AppError.Business(null,"code is null"))
        return if(code <= appVersion.versionCode) {
            Result.success(CheckResult.NoUpdate("Already update."))
        }else {
            Result.success(CheckResult.Update(result.getOrNull()!!))
        }
    }
}
