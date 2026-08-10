package com.blueskybone.arkscreen.domain.usecase.attendance

import com.blueskybone.arkscreen.domain.model.account.Account
import com.blueskybone.arkscreen.domain.model.account.AccountEf
import com.blueskybone.arkscreen.domain.model.account.AccountSk
import com.blueskybone.arkscreen.domain.model.attendance.AttendanceAccountResult
import com.blueskybone.arkscreen.domain.model.attendance.AttendanceSummary
import com.blueskybone.arkscreen.domain.repository.AccountRepository
import kotlinx.coroutines.flow.first

class RunAttendanceUseCase(
    private val accountRepository: AccountRepository,
    private val getAttendanceResult: GetAttdResultUseCase,
) {
    suspend operator fun invoke(
        onProgress: (suspend (index: Int, total: Int, accountName: String) -> Unit)? = null,
    ): AttendanceSummary {
        val skAccounts = accountRepository.observeSkAcc().first()
        val efAccounts = accountRepository.observeEfAcc().first()
        val accounts: List<Account> = skAccounts + efAccounts

        val results = accounts.mapIndexed { index, account ->
            val label = when (account) {
                is AccountSk -> "[明日方舟] ${account.nickName}"
                is AccountEf -> "[终末地] ${account.nickName}"
                else -> account.nickName
            }
            onProgress?.invoke(index + 1, accounts.size, label)
            getAttendanceResult(account).fold(
                onSuccess = { message -> AttendanceAccountResult(label, message, null) },
                onFailure = { error -> AttendanceAccountResult(label, null, error) },
            )
        }
        return AttendanceSummary(results)
    }
}
