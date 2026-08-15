package com.blueskybone.arkscreen.domain.model.attendance

import com.blueskybone.arkscreen.domain.model.account.AccountType

data class AttendanceAccountResult(
    val accountType: AccountType,
    val accountUid: String,
    val accountName: String,
    val message: String?,
    val error: Throwable?,
) {
    val isSuccess: Boolean get() = error == null
}

data class AttendanceSummary(
    val results: List<AttendanceAccountResult>,
) {
    val successCount: Int get() = results.count(AttendanceAccountResult::isSuccess)
    val failureCount: Int get() = results.size - successCount
    val isSuccess: Boolean get() = failureCount == 0
}
