package com.blueskybone.arkscreen.domain.usecase.realtime


import com.blueskybone.arkscreen.data.common.toAppError
import com.blueskybone.arkscreen.domain.model.account.AccountSk
import com.blueskybone.arkscreen.domain.model.realtime.RealTimeData
import com.blueskybone.arkscreen.domain.repository.SklandRepository

/**
 * Created by blueskybone
 * Date: 2026/2/25
 */
class GetRealTimeUseCase(
    private val repo: SklandRepository
) {
    suspend operator fun invoke(account: AccountSk): Result<RealTimeData> {

        val result = repo.fetchRealTimeData(account)
        val data =
            result.getOrNull() ?: return Result.failure(result.exceptionOrNull()!!.toAppError())
        //TODO:设置缓存别放在这
        repo.setRealTimeCache(data)
        return result
    }
}