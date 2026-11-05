package com.blueskybone.arkscreen.domain.repository

import com.blueskybone.arkscreen.domain.model.account.AccountType
import com.blueskybone.arkscreen.domain.model.attendance.AccountAttendanceState
import com.blueskybone.arkscreen.domain.model.attendance.AttendanceAccountResult
import kotlinx.coroutines.flow.Flow

interface AttendanceStateRepository {
    suspend fun get(accountType: AccountType, accountUid: String): AccountAttendanceState?
    fun observe(accountType: AccountType, accountUid: String): Flow<AccountAttendanceState?>
    suspend fun record(result: AttendanceAccountResult, attemptedAt: Long)
}
