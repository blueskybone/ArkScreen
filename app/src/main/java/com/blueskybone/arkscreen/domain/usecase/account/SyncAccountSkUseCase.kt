package com.blueskybone.arkscreen.domain.usecase.account

import com.blueskybone.arkscreen.domain.model.account.AccountType
import com.blueskybone.arkscreen.domain.repository.AccountRepository
import kotlinx.coroutines.flow.first

/**
 * Created by blueskybone
 * Date: 2026/2/26
 */
class SyncAccountSkUseCase(private val repo: AccountRepository) {

    sealed class LoginWay {
        data class PhoneAndPassword(val phone: String, val password: String) : LoginWay()
        data class Token(val token: String, val dId: String? = null) : LoginWay()
        data class Cookie(val cookieStr: String) : LoginWay()
    }

    suspend operator fun invoke(way: LoginWay): Result<Int> {
        val loginResult = when (way) {
            is LoginWay.PhoneAndPassword -> repo.loginByPhonePassword(way.phone, way.password)
            is LoginWay.Token -> repo.loginByToken(way.token, way.dId)
            is LoginWay.Cookie -> repo.loadAccountFromCookie(AccountType.SK, way.cookieStr)
        }

        if (loginResult.isFailure) return loginResult

        // A first import must also establish the current account. Otherwise consumers that
        // observe only the selected account continue to behave as logged out.
        if (repo.observeCurrentSkAcc().first() == null) {
            val firstAccount = repo.observeSkAcc().first().firstOrNull()
            if (firstAccount != null) {
                repo.setCurrentAccountSk(firstAccount)
                    .onFailure { return Result.failure(it) }
            }
        }

        return loginResult
    }

}
