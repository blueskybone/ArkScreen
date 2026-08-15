package com.blueskybone.arkscreen.domain.usecase.realtime


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
        val data = result.getOrNull() ?: return Result.failure(
            result.exceptionOrNull() ?: IllegalStateException("实时数据为空")
        )
        // 缓存必须和本次刷新一并更新，确保首页与桌面组件读取的是同一份快照。
        repo.setRealTimeCache(account, data)
        return result
    }
}
