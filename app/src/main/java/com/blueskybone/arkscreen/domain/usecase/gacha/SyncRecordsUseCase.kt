package com.blueskybone.arkscreen.domain.usecase.gacha

import com.blueskybone.arkscreen.domain.model.account.AccountGc
import com.blueskybone.arkscreen.domain.repository.GachaRepository

/**
 * Created by blueskybone
 * Date: 2026/3/10
 */

class SyncRecordsUseCase(private val repo: GachaRepository) {
    suspend operator fun invoke(account: AccountGc): Result<Unit> = repo.syncRecords(account)
}