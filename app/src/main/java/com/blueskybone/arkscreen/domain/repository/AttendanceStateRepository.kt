package com.blueskybone.arkscreen.domain.repository

import com.blueskybone.arkscreen.domain.model.account.AccountType
import com.blueskybone.arkscreen.domain.model.attendance.AccountAttendanceState
import com.blueskybone.arkscreen.domain.model.attendance.AttendanceAccountResult

interface AttendanceStateRepository {
    suspend fun get(accountType: AccountType, accountUid: String): AccountAttendanceState?
    suspend fun record(result: AttendanceAccountResult, attemptedAt: Long)
}
