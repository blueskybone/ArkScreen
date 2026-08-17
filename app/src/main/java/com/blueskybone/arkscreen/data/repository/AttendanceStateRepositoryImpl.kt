package com.blueskybone.arkscreen.data.repository

import com.blueskybone.arkscreen.data.local.room.AttendanceRecord
import com.blueskybone.arkscreen.data.local.room.dao.AttendanceRecordDao
import com.blueskybone.arkscreen.domain.model.account.AccountType
import com.blueskybone.arkscreen.domain.model.attendance.AccountAttendanceState
import com.blueskybone.arkscreen.domain.model.attendance.AttendanceAccountResult
import com.blueskybone.arkscreen.domain.repository.AttendanceStateRepository

class AttendanceStateRepositoryImpl(
    private val dao: AttendanceRecordDao,
) : AttendanceStateRepository {
    override suspend fun get(
        accountType: AccountType,
        accountUid: String,
    ): AccountAttendanceState? =
        dao.get(accountType.name, accountUid)?.toDomain()

    override suspend fun record(result: AttendanceAccountResult, attemptedAt: Long) {
        val previous = dao.get(result.accountType.name, result.accountUid)
        dao.upsert(
            AttendanceRecord(
                accountType = result.accountType.name,
                accountUid = result.accountUid,
                lastAttemptTs = attemptedAt,
                lastSuccessTs =
                    if (result.isSuccess) attemptedAt else previous?.lastSuccessTs ?: 0L,
                lastError = result.error?.message.orEmpty(),
            )
        )
    }

    private fun AttendanceRecord.toDomain() = AccountAttendanceState(
        accountType = AccountType.valueOf(accountType),
        accountUid = accountUid,
        lastAttemptTs = lastAttemptTs,
        lastSuccessTs = lastSuccessTs,
        lastError = lastError.takeIf(String::isNotBlank),
    )
}
