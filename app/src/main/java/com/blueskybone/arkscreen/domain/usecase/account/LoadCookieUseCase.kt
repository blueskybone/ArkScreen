package com.blueskybone.arkscreen.domain.usecase.account

import com.blueskybone.arkscreen.domain.model.account.AccountType
import com.blueskybone.arkscreen.domain.repository.AccountRepository

class LoadCookieUseCase(private val repo: AccountRepository) {
    suspend operator fun invoke(accountType: AccountType, cookieStr: String): Result<Int> =
        repo.loadAccountFromCookie(accountType, cookieStr)
}