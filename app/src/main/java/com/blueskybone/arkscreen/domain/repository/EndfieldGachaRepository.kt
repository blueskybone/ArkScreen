package com.blueskybone.arkscreen.domain.repository

import com.blueskybone.arkscreen.domain.model.account.AccountEf
import com.blueskybone.arkscreen.domain.model.gacha.EndfieldGachaRecord
import com.blueskybone.arkscreen.domain.model.gacha.EndfieldGachaPool
import kotlinx.coroutines.flow.Flow

interface EndfieldGachaRepository {
    fun observe(roleId: String): Flow<List<EndfieldGachaRecord>>
    fun observePools(roleId: String): Flow<List<EndfieldGachaPool>>
    suspend fun sync(account: AccountEf, progress: (String) -> Unit): Int
}
