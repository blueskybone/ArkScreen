package com.blueskybone.arkscreen.domain.model.attendance

import com.blueskybone.arkscreen.domain.model.account.AccountType

data class AccountAttendanceState(
    val accountType: AccountType,
    val accountUid: String,
    val lastAttemptTs: Long,
    val lastSuccessTs: Long,
    val lastError: String?,
)
