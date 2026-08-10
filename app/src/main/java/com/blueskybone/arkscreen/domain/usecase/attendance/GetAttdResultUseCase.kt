package com.blueskybone.arkscreen.domain.usecase.attendance

import com.blueskybone.arkscreen.domain.AppError
import com.blueskybone.arkscreen.domain.model.account.Account
import com.blueskybone.arkscreen.domain.model.account.AccountEf
import com.blueskybone.arkscreen.domain.model.account.AccountSk
import com.blueskybone.arkscreen.domain.repository.SklandRepository
import timber.log.Timber

/**
 * Created by blueskybone
 * Date: 2026/3/10
 */

class GetAttdResultUseCase(private val repo: SklandRepository) {
    suspend operator fun invoke(account: Account): Result<String> {
        val result = when (account) {
            is AccountSk -> repo.fetchAkCheckResult(account)
            is AccountEf -> repo.fetchEfCheckResult(account)
            else -> throw AppError.Business(null, "不支持的账号类型")
        }
        Timber.i("账号${account.nickName} 签到成功：${result}")
        return result
    }
}