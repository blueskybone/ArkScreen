package com.blueskybone.arkscreen.domain.repository

import com.blueskybone.arkscreen.domain.model.account.Account
import com.blueskybone.arkscreen.domain.model.account.AccountGc
import com.blueskybone.arkscreen.domain.model.gacha.Record
import kotlinx.coroutines.flow.Flow


//根据前端需求重新设计接口
interface GachaRepository {

    fun observeRecords(uid: String): Flow<List<Record>>

    suspend fun importRecords(account: AccountGc, records: List<Record>): Result<Unit>

    suspend fun deleteRecords(account: AccountGc): Result<Unit>

    suspend fun deleteRecordsByUid(uid: String): Result<Unit>

    suspend fun fixGachaCate(): Result<Unit>

    suspend fun syncRecords(account: AccountGc): Result<Unit>

    suspend fun correctUnCateRecord(account: Account): Result<Int>

}
